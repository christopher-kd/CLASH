package net.infinitygrid.clash.arena

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.infinitygrid.clash.player.ui.GradientTitleAnimation
import net.infinitygrid.clash.player.ui.interactivehotbar.InteractiveHotbarTemplates
import net.infinitygrid.clash.world.TemporaryWorld
import net.infinitygrid.clash.world.TemporaryWorldState
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import java.util.UUID
import java.util.concurrent.CompletableFuture

class Arena private constructor(val data: ArenaData, val world: TemporaryWorld) {

    private val players = mutableListOf<CLASHPlayer>()
    private val readyFuture: CompletableFuture<Unit>

    init {
        world.state = TemporaryWorldState.OCCUPIED
        readyFuture = world.fromSchematicAsync(data.schematic, showTitle = false)
    }

    fun join(clashPlayer: CLASHPlayer, onReady: (() -> Unit)? = null) {
        players.add(clashPlayer)
        clashPlayer.setArena(this)
        clashPlayer.soundPlayer.playArenaJoin()
        readyFuture.thenAccept {
            val spawn = data.playerSpawns.randomOrNull() ?: data.spectatorSpawn
            val location = Location(world.bukkitWorld, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch)
            clashPlayer.teleportAsync(location).thenAccept {
                clashPlayer.gameMode = GameMode.SURVIVAL
                clashPlayer.movementController.reset()
                InteractiveHotbarTemplates.applyMatchMode(clashPlayer)
                showJoinTitle(clashPlayer)
                updateScoreboard()
                clashPlayer.soundPlayer.playArenaJoined()
                onReady?.invoke()
            }
        }
    }

    private fun updateScoreboard() {
        val title = text("CLASH > ${data.mapName}")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)

        players.forEach { player ->
            player.scoreboardManager.setTitle(title)
            player.scoreboardManager.setText(listOf(
                "",
                "Waiting for more",
                "players...",
                " "
            ))
            player.scoreboardManager.hide(false)
        }
    }

    private fun showJoinTitle(clashPlayer: CLASHPlayer) {
        val rawPlayer = Bukkit.getPlayer(clashPlayer.uniqueId) ?: return
        GradientTitleAnimation.show(
            rawPlayer,
            data.mapName,
            text("by ${creatorNames()}")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
        )
    }

    private fun creatorNames(): String {
        return data.mapCreators.joinToString(", ") { raw ->
            val uuid = runCatching { UUID.fromString(raw) }.getOrNull()
            uuid?.let { CLASH.INSTANCE.creatorNameCache.get(it) ?: Bukkit.getOfflinePlayer(it).name } ?: raw
        }
    }

    fun playerCount(): Int = players.size

    fun leave(clashPlayer: CLASHPlayer) {
        if (!players.remove(clashPlayer)) return
        clashPlayer.setArena(null)
        clashPlayer.scoreboardManager.reset()
        clashPlayer.teleportAsync(Bukkit.getWorlds()[0].spawnLocation).thenAccept {
            clashPlayer.applyDefaultValues()
        }
        if (players.isEmpty()) {
            CLASH.INSTANCE.arenaManager.onEmpty(this)
        } else {
            updateScoreboard()
        }
    }

    companion object {
        fun create(data: ArenaData): Arena? {
            val world = CLASH.INSTANCE.temporaryWorldManager.getAnyFreeWorld() ?: return null
            return Arena(data, world)
        }
    }
}
