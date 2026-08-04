package net.infinitygrid.clash.player.setup

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import kotlin.properties.Delegates

class MapSetup(val clashPlayer: CLASHPlayer, val schematicName: String) {

    class Requirements(private val onUpdate: () -> Unit) {
        private fun <T> obs(initial: T) = Delegates.observable(initial) { _, _, _ -> onUpdate() }

        var mapName by obs(false)
        var mapIcon by obs(false)
        var mapCreators by obs(false)
        var description by obs(true)
        var spectatorSpawn by obs(false)

        var playerSpawns by obs(0)
        var itemSpawns by obs(0)
    }

    private val requirements = Requirements { updateScoreboard() }

    init {
        clashPlayer.setupMap(this)
    }

    fun prepareWorld() {
        val world = CLASH.INSTANCE.temporaryWorldManager.getAnyFreeWorld() ?: error("No free worlds!")
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
                "${if (requirements.playerSpawns >= 2) "§a✔" else "§c✖"} ${requirements.playerSpawns} Player Spawns",
                "${if (requirements.itemSpawns > 0) "§a✔" else "§c✖"} ${requirements.itemSpawns} Item Spawns",
                "  "
            ))
    }

}