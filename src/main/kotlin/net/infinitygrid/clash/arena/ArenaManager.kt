package net.infinitygrid.clash.arena

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

class ArenaManager {

    private val activeArenas = mutableMapOf<String, Arena>()

    fun joinOrCreate(clashPlayer: CLASHPlayer, key: String, onReady: (() -> Unit)? = null) {
        activeArenas[key]?.let {
            it.join(clashPlayer, onReady)
            return
        }

        val data = CLASH.INSTANCE.arenaRegistry.get(key) ?: run {
            clashPlayer.sendMessage(text("Arena not found.").color(NamedTextColor.RED))
            onReady?.invoke()
            return
        }
        val arena = Arena.create(data) ?: run {
            clashPlayer.sendMessage(text("No free worlds available right now.").color(NamedTextColor.RED))
            onReady?.invoke()
            return
        }
        activeArenas[key] = arena
        arena.join(clashPlayer, onReady)
    }

    fun onEmpty(arena: Arena) {
        activeArenas.entries.removeIf { it.value === arena }
        arena.world.resetAndFreeAsync()
    }

    fun getActive(): Map<String, Arena> = activeArenas.toMap()
}
