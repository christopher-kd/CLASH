package net.infinitygrid.clash.event

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.PlayerRegistry
import net.infinitygrid.clash.player.ui.menu.CreateMatchMenu
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class CreateMatchMenuListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.view.topInventory.holder as? CreateMatchMenu ?: return
        event.isCancelled = true
        if (event.clickedInventory !== event.view.topInventory) return
        if (holder.isLoading) return

        val key = holder.arenaKeyAt(event.slot) ?: return
        val player = event.whoClicked as? Player ?: return
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return

        holder.startLoadingAnimation()
        CLASH.INSTANCE.arenaManager.joinOrCreate(clashPlayer, key) {
            holder.stopLoadingAnimation()
            player.closeInventory()
        }
    }

}
