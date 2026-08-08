package net.infinitygrid.clash.player.ui.menu

import org.bukkit.inventory.InventoryHolder

interface ArenaSelectionMenu : InventoryHolder {
    val isLoading: Boolean
    fun arenaKeyAt(slot: Int): String?
    fun startLoadingAnimation()
    fun stopLoadingAnimation()
}
