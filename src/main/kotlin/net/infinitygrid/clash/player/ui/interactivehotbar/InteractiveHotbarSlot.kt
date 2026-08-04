package net.infinitygrid.clash.player.ui.interactivehotbar

import org.bukkit.inventory.ItemStack

class InteractiveHotbarSlot(
    val item: ItemStack,
    val slot: Int,
    val onRightClick: () -> Unit,
    val onLeftClick: () -> Unit
)