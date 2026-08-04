package net.infinitygrid.clash.player.ui.interactivehotbar

import net.infinitygrid.clash.player.CLASHPlayer
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class InteractiveHotbar(val clashPlayer: CLASHPlayer) {

    var active = false
    private val slots = mutableListOf<InteractiveHotbarSlot>()

    fun onRightClickEvent(event: PlayerInteractEvent) {
        if (!active) return
        val currentSlot = event.player.inventory.heldItemSlot + 1
        slots.find { it.slot == currentSlot }?.onRightClick?.invoke()
    }

    fun onLeftClickEvent(event: PlayerInteractEvent) {
        if (!active) return
        val currentSlot = event.player.inventory.heldItemSlot + 1
        slots.find { it.slot == currentSlot }?.onLeftClick?.invoke()
    }

    fun setSlot(slot: InteractiveHotbarSlot) {
        slots.removeIf { it.slot == slot.slot }
        slots.add(slot)
        clashPlayer.inventory.setItem(slot.slot - 1, slot.item)
        active = true
    }

    fun setSlots(vararg newSlots: InteractiveHotbarSlot) {
        newSlots.forEach { setSlot(it) }
    }

    fun setSlots(newSlots: Collection<InteractiveHotbarSlot>) {
        newSlots.forEach { setSlot(it) }
    }

    fun load(applyFn: InteractiveHotbar.() -> Unit) {
        applyFn()
    }

    fun getSlots() = slots.toList()

    fun clear() {
        clashPlayer.inventory.clear()
        slots.clear()
        active = false
    }

}