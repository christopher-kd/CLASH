package net.infinitygrid.clash.player.setup

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.arena.ArenaData
import net.infinitygrid.clash.arena.ItemSpawnPoint
import net.infinitygrid.clash.arena.SpawnPoint
import net.infinitygrid.clash.player.CLASHPlayer
import net.infinitygrid.clash.world.TemporaryWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import kotlin.properties.Delegates

class MapSetup(val clashPlayer: CLASHPlayer, val schematicName: String) {

    companion object {
        private const val MIN_PLAYER_SPAWNS = 2
        private const val MIN_ITEM_SPAWNS = 1
        private val httpClient: HttpClient = HttpClient.newHttpClient()
    }

    class Requirements(private val onUpdate: () -> Unit) {
        private fun <T> obs(initial: T) = Delegates.observable(initial) { _, _, _ -> onUpdate() }

        var mapName by obs(false)
        var mapIcon by obs(false)
        var mapCreators by obs(false)
        var description by obs(false)
        var spectatorSpawn by obs(false)

        var playerSpawns by obs(0)
        var itemSpawns by obs(0)
    }

    private val requirements = Requirements { updateScoreboard() }
    private var world: TemporaryWorld? = null

    private val playerSpawnLocations = mutableListOf<Location>()
    private val playerSpawnMarkers = mutableListOf<ArmorStand>()
    private val itemSpawnLocations = mutableListOf<Location>()
    private val itemSpawnMarkers = mutableListOf<Item>()
    private var spectatorSpawnLocation: Location? = null
    private var spectatorSpawnMarker: ArmorStand? = null

    private sealed class SpawnAction {
        data class PlayerSpawn(val marker: ArmorStand) : SpawnAction()
        data class ItemSpawn(val marker: Item) : SpawnAction()
    }

    private val spawnActionHistory = mutableListOf<SpawnAction>()

    private var mapNameValue: String = ""
    private var mapIconValue: String = ""
    private var mapCreatorsValue: String = ""
    private var mapCreatorUuids: List<UUID> = emptyList()
    private var descriptionValue: String = ""

    init {
        clashPlayer.setupMap(this)
    }

    fun openDetailsDialog() {
        // Deferred by a tick since showing UI synchronously from within the triggering
        // PlayerInteractEvent handler risks racing the client's own interaction resolution.
        clashPlayer.scheduler.runDelayed(CLASH.INSTANCE, { showDetailsDialog() }, null, 1L)
    }

    private fun showDetailsDialog() {
        // CLASHPlayer's `Player by player` delegation does not correctly forward showDialog
        // (calls silently no-op client-side) - go through the raw Bukkit Player instead.
        val rawPlayer = Bukkit.getPlayer(clashPlayer.uniqueId) ?: return
        try {
            val nameInput = DialogInput.text("map_name", text("Map Name"))
                .initial(mapNameValue)
                .maxLength(64)
                .build()

            val iconInput = DialogInput.text("map_icon", text("Map Icon (e.g. minecraft:diamond_sword)"))
                .initial(mapIconValue)
                .maxLength(64)
                .build()

            val creatorsInput = DialogInput.text("map_creators", text("Map Creators (UUIDs, comma separated)"))
                .initial(mapCreatorsValue)
                .maxLength(512)
                .build()

            val descriptionInput = DialogInput.text("description", text("Description"))
                .initial(descriptionValue)
                .maxLength(512)
                .multiline(TextDialogInput.MultilineOptions.create(5, null))
                .build()

            val dialog = Dialog.create { factory ->
                factory.empty()
                    .base(
                        DialogBase.builder(text("Map Details"))
                            .pause(false)
                            .body(listOf(DialogBody.plainMessage(text("Fill in the map name, icon, creators and description."))))
                            .inputs(listOf(nameInput, iconInput, creatorsInput, descriptionInput))
                            .build()
                    )
                    .type(
                        DialogType.notice(
                            ActionButton.create(
                                text("Save"),
                                null,
                                150,
                                DialogAction.customClick(
                                    { response, _ ->
                                        applyDetails(
                                            response.getText("map_name"),
                                            response.getText("map_icon"),
                                            response.getText("map_creators"),
                                            response.getText("description")
                                        )
                                    },
                                    ClickCallback.Options.builder().build()
                                )
                            )
                        )
                    )
            }

            rawPlayer.showDialog(dialog)
        } catch (e: Throwable) {
            clashPlayer.sendMessage(text("Failed to open Map Details dialog: ${e}").color(NamedTextColor.RED))
            CLASH.INSTANCE.logger.log(Level.SEVERE, "Failed to open Map Details dialog for ${clashPlayer.name}", e)
        }
    }

    private fun applyDetails(name: String?, icon: String?, creators: String?, description: String?) {
        mapNameValue = name?.trim().orEmpty()
        mapIconValue = icon?.trim().orEmpty()
        mapCreatorsValue = creators?.trim().orEmpty()
        descriptionValue = description?.trim().orEmpty()

        requirements.mapName = mapNameValue.isNotBlank()
        requirements.mapIcon = isValidItemId(mapIconValue)
        requirements.description = descriptionValue.isNotBlank()

        if (mapIconValue.isNotBlank() && !requirements.mapIcon) {
            clashPlayer.sendMessage(text("'$mapIconValue' is not a valid item id, ignoring Map Icon.").color(NamedTextColor.RED))
        }

        validateMapCreators(mapCreatorsValue)

        clashPlayer.sendMessage(text("Map details updated.").color(NamedTextColor.GREEN))
    }

    private fun validateMapCreators(raw: String) {
        requirements.mapCreators = false
        mapCreatorUuids = emptyList()

        val tokens = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return

        val uuids = mutableListOf<UUID>()
        for (token in tokens) {
            val uuid = try {
                UUID.fromString(token)
            } catch (e: IllegalArgumentException) {
                clashPlayer.sendMessage(text("'$token' is not a valid UUID.").color(NamedTextColor.RED))
                return
            }
            uuids.add(uuid)
        }

        // Checked against NameMC (404 = no such account, 200 = exists) rather than Mojang's own
        // profile API, which rate-limits aggressively and fails silently. Must run off the main
        // thread and apply the result back via the scheduler once resolved.
        val lookups = uuids.map { uuid -> checkUuidExists(uuid) }

        CompletableFuture.allOf(*lookups.toTypedArray()).whenComplete { _, _ ->
            val problems = uuids.indices.mapNotNull { i -> lookups[i].getNow(null)?.let { reason -> uuids[i] to reason } }
            clashPlayer.scheduler.run(CLASH.INSTANCE, {
                if (problems.isEmpty()) {
                    mapCreatorUuids = uuids
                    requirements.mapCreators = true
                    clashPlayer.sendMessage(text("Map creators verified.").color(NamedTextColor.GREEN))
                } else {
                    problems.forEach { (uuid, reason) ->
                        clashPlayer.sendMessage(text("Could not verify '$uuid': $reason").color(NamedTextColor.RED))
                    }
                }
            }, null)
        }
    }

    private fun checkUuidExists(uuid: UUID): CompletableFuture<String?> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://de.namemc.com/profile/$uuid"))
            .header("User-Agent", "Mozilla/5.0 (compatible; CLASH-plugin)")
            .GET()
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .handle { response, throwable ->
                when {
                    throwable != null -> "lookup failed (${throwable.message})"
                    response.statusCode() == 404 -> "no account found for this UUID"
                    response.statusCode() != 200 -> "unexpected response (HTTP ${response.statusCode()})"
                    else -> null
                }
            }
    }

    private fun isValidItemId(id: String): Boolean {
        val key = NamespacedKey.fromString(id.lowercase()) ?: return false
        return Registry.ITEM.get(key) != null
    }

    fun addPlayerSpawn(location: Location) {
        val snapped = snapToHalfBlock(location)
        playerSpawnLocations.add(snapped)
        requirements.playerSpawns = playerSpawnLocations.size
        spawnPlayerSpawnMarker(snapped, playerSpawnLocations.size)
        clashPlayer.sendMessage(
            text("Player spawn #${playerSpawnLocations.size} set at (${formatCoordinate(snapped.x)}, ${formatCoordinate(snapped.y)}, ${formatCoordinate(snapped.z)}).")
                .color(NamedTextColor.GREEN)
        )
    }

    private fun snapToHalfBlock(location: Location): Location {
        return Location(
            location.world,
            roundToHalf(location.x),
            roundToHalf(location.y),
            roundToHalf(location.z),
            location.yaw,
            location.pitch
        )
    }

    private fun roundToHalf(value: Double): Double = Math.round(value * 2.0) / 2.0

    private fun formatCoordinate(value: Double): String = "%.1f".format(value)

    private fun spawnPlayerSpawnMarker(location: Location, index: Int) {
        val marker = spawnHeadMarker(location, Material.PLAYER_HEAD, text("Player Spawn #$index").color(NamedTextColor.AQUA))
        playerSpawnMarkers.add(marker)
        spawnActionHistory.add(SpawnAction.PlayerSpawn(marker))
    }

    private fun spawnHeadMarker(location: Location, headMaterial: Material, name: Component): ArmorStand {
        return location.world!!.spawn(location, ArmorStand::class.java) { armorStand ->
            armorStand.isMarker = true
            armorStand.setGravity(false)
            armorStand.isInvulnerable = true
            armorStand.isPersistent = false
            armorStand.isCustomNameVisible = true
            armorStand.customName(name)
            armorStand.equipment?.setHelmet(ItemStack(headMaterial))
        }
    }

    fun addItemSpawn(location: Location) {
        val snapped = snapToHalfBlock(location)
        itemSpawnLocations.add(snapped)
        requirements.itemSpawns = itemSpawnLocations.size
        spawnItemSpawnMarker(snapped, itemSpawnLocations.size)
        clashPlayer.sendMessage(
            text("Item spawn #${itemSpawnLocations.size} set at (${formatCoordinate(snapped.x)}, ${formatCoordinate(snapped.y)}, ${formatCoordinate(snapped.z)}).")
                .color(NamedTextColor.GREEN)
        )
    }

    private fun spawnItemSpawnMarker(location: Location, index: Int) {
        val markerItem = ItemStack(Material.NETHER_STAR).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Item Spawn #$index").color(NamedTextColor.AQUA))
            }
        }

        val marker = location.world!!.dropItem(location, markerItem) { item ->
            item.setCanPlayerPickup(false)
            item.setCanMobPickup(false)
            item.setUnlimitedLifetime(true)
            item.isInvulnerable = true
            item.isPersistent = false
            item.setGravity(false)
            item.velocity = Vector(0.0, 0.0, 0.0)
        }
        itemSpawnMarkers.add(marker)
        spawnActionHistory.add(SpawnAction.ItemSpawn(marker))
    }

    fun undoLastSpawn() {
        val last = spawnActionHistory.removeLastOrNull()
        if (last == null) {
            clashPlayer.sendMessage(text("Nothing to undo.").color(NamedTextColor.RED))
            return
        }

        when (last) {
            is SpawnAction.PlayerSpawn -> {
                last.marker.remove()
                playerSpawnMarkers.remove(last.marker)
                playerSpawnLocations.removeAt(playerSpawnLocations.lastIndex)
                requirements.playerSpawns = playerSpawnLocations.size
                clashPlayer.sendMessage(text("Undid last player spawn.").color(NamedTextColor.YELLOW))
            }
            is SpawnAction.ItemSpawn -> {
                last.marker.remove()
                itemSpawnMarkers.remove(last.marker)
                itemSpawnLocations.removeAt(itemSpawnLocations.lastIndex)
                requirements.itemSpawns = itemSpawnLocations.size
                clashPlayer.sendMessage(text("Undid last item spawn.").color(NamedTextColor.YELLOW))
            }
        }
    }

    fun setSpectatorSpawn(location: Location) {
        spectatorSpawnLocation = location
        requirements.spectatorSpawn = true
        spectatorSpawnMarker?.remove()
        spectatorSpawnMarker = spawnHeadMarker(location, Material.ZOMBIE_HEAD, text("Spectator Spawn").color(NamedTextColor.AQUA))
        clashPlayer.sendMessage(
            text("Spectator spawn set at (${location.blockX}, ${location.blockY}, ${location.blockZ}).")
                .color(NamedTextColor.GREEN)
        )
    }

    fun isSetupComplete(): Boolean {
        return requirements.mapName &&
            requirements.mapIcon &&
            requirements.mapCreators &&
            requirements.description &&
            requirements.spectatorSpawn &&
            requirements.playerSpawns >= MIN_PLAYER_SPAWNS &&
            requirements.itemSpawns >= MIN_ITEM_SPAWNS
    }

    fun save() {
        if (!isSetupComplete()) {
            clashPlayer.sendMessage(text("Cannot save: not all requirements are fulfilled yet.").color(NamedTextColor.RED))
            return
        }

        val data = ArenaData(
            schematic = schematicName,
            mapName = mapNameValue,
            mapIcon = mapIconValue,
            mapCreators = mapCreatorUuids.map { it.toString() },
            description = descriptionValue,
            spectatorSpawn = toSpawnPoint(spectatorSpawnLocation!!),
            playerSpawns = playerSpawnLocations.map { toSpawnPoint(it) },
            itemSpawns = itemSpawnLocations.map { toItemSpawnPoint(it) }
        )

        val fileName = "${sanitizeFileName(mapNameValue)}.json"
        val file = File(CLASH.INSTANCE.fileManager.arenasFolder, fileName)

        try {
            FileWriter(file).use { writer -> CLASH.INSTANCE.fileManager.gson.toJson(data, writer) }
            clashPlayer.sendMessage(text("Map saved to arenas/$fileName").color(NamedTextColor.GREEN))
            clashPlayer.cancelMapSetup()
            clashPlayer.interactiveHotbar.clear()
        } catch (e: IOException) {
            clashPlayer.sendMessage(text("Failed to save map: ${e.message}").color(NamedTextColor.RED))
            CLASH.INSTANCE.logger.log(Level.SEVERE, "Failed to save arena to $fileName", e)
        }
    }

    private fun toSpawnPoint(location: Location) = SpawnPoint(location.x, location.y, location.z, location.yaw, location.pitch)

    private fun toItemSpawnPoint(location: Location) = ItemSpawnPoint(location.x, location.y, location.z)

    private fun sanitizeFileName(name: String): String {
        return name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { "arena" }
    }

    fun cancel() {
        clashPlayer.scoreboardManager.reset()
        playerSpawnMarkers.forEach { it.remove() }
        playerSpawnMarkers.clear()
        itemSpawnMarkers.forEach { it.remove() }
        itemSpawnMarkers.clear()
        spectatorSpawnMarker?.remove()
        spectatorSpawnMarker = null
        spawnActionHistory.clear()
        world?.resetAndFreeAsync()
        world = null
    }

    fun prepareWorld() {
        val world = CLASH.INSTANCE.temporaryWorldManager.getAnyFreeWorld() ?: error("No free worlds!")
        this.world = world
        world.fromSchematicAsync(schematicName).thenAccept {
            clashPlayer.teleportAsync(Location(world.bukkitWorld, 0.0, 150.0, 0.0))
            clashPlayer.scoreboardManager.setTitle(
                    text(
                        "Setup Mode // $schematicName",
                        TextColor.color(0xFFAA00)
                    ).decoration(TextDecoration.BOLD, true)
            )
            updateScoreboard()
            clashPlayer.scoreboardManager.hide(false)
        }
    }

    private fun updateScoreboard() {
        clashPlayer.scoreboardManager.setText(
            listOf(
                " ",
                "${if (requirements.mapName) "§a✔" else "§c✖"} Map Name",
                "${if (requirements.mapIcon) "§a✔" else "§c✖"} Map Icon",
                "${if (requirements.mapCreators) "§a✔" else "§c✖"} Map Creators",
                "${if (requirements.description) "§a✔" else "§c✖"} Description",
                "${if (requirements.spectatorSpawn) "§a✔" else "§c✖"} Spectator Spawn",
                "${if (requirements.playerSpawns >= MIN_PLAYER_SPAWNS) "§a✔" else "§c✖"} ${requirements.playerSpawns} / $MIN_PLAYER_SPAWNS Player Spawns",
                "${if (requirements.itemSpawns >= MIN_ITEM_SPAWNS) "§a✔" else "§c✖"} ${requirements.itemSpawns} / $MIN_ITEM_SPAWNS Item Spawns",
                "  "
            ))
    }

}