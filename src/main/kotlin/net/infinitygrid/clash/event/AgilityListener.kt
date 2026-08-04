package net.infinitygrid.clash.event

import net.infinitygrid.clash.player.PlayerRegistry
import net.infinitygrid.clash.player.movement.MovementController
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class AgilityListener : Listener {

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        if (event.player.gameMode != GameMode.SURVIVAL) return
        if (event.isFlying) {
            event.isCancelled = true
            val player = PlayerRegistry.instance.getPlayer(event.player) ?: return
            player.movementController.executeMovementAction()
        }
    }

    @EventHandler
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        if (event.player.gameMode != GameMode.SURVIVAL) return
        if (event.isSneaking) {
            val player = PlayerRegistry.instance.getPlayer(event.player) ?: return
            if (!player.isOnGround) player.movementController.smash()
        }
    }

}