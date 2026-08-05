package net.infinitygrid.clash.event

import net.infinitygrid.clash.player.PlayerRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class InventoryInteractEvent : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = PlayerRegistry.instance.getPlayer(event.player) ?: return

        if (player.interactiveHotbar.active) {
            event.isCancelled = true
        }

        if (event.action in setOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) {
            player.interactiveHotbar.onRightClickEvent(event)
        } else if (event.action in setOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK)) {
            player.interactiveHotbar.onLeftClickEvent(event)
        }

    }

}