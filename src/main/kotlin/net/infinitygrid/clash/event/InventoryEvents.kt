package net.infinitygrid.clash.event

import net.infinitygrid.clash.player.PlayerRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class InventoryEvents : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return
        event.isCancelled = clashPlayer.interactiveHotbar.active
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return
        event.isCancelled = clashPlayer.interactiveHotbar.active
    }

    @EventHandler
    fun onInventoryInteract(event: PlayerDropItemEvent) {
        val player = PlayerRegistry.instance.getPlayer(event.player) ?: return
        event.isCancelled = player.interactiveHotbar.active
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return
        event.isCancelled = clashPlayer.interactiveHotbar.active
    }

    @EventHandler
    fun onHandSwap(event: PlayerSwapHandItemsEvent) {
        val player = PlayerRegistry.instance.getPlayer(event.player) ?: return
        event.isCancelled = player.interactiveHotbar.active
    }

    @EventHandler
    fun onInventoryCreative(event: InventoryCreativeEvent) {
        val player = event.whoClicked as? Player ?: return
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return
        event.isCancelled = clashPlayer.interactiveHotbar.active
    }


}