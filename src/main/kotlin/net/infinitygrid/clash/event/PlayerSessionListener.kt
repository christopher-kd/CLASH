package net.infinitygrid.clash.event

import net.infinitygrid.clash.player.PlayerRegistry
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerSessionListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text(">> ${event.player.name}"))
        PlayerRegistry.instance.registerPlayer(event.player)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        event.quitMessage(Component.text("<< ${event.player.name}"))
        PlayerRegistry.instance.unregisterPlayer(event.player)
    }

}