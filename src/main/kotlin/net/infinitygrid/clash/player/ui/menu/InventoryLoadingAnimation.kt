package net.infinitygrid.clash.player.ui.menu

import net.infinitygrid.clash.CLASH
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import kotlin.math.roundToInt

class InventoryLoadingAnimation(private val inventory: Inventory) {

    private var task: BukkitTask? = null

    var isRunning = false
        private set

    fun start() {
        if (isRunning) return
        isRunning = true

        var frame = 0
        task = Bukkit.getScheduler().runTaskTimer(CLASH.INSTANCE, Runnable {
            val fraction = (frame % SWEEP_FRAMES).toDouble() / SWEEP_FRAMES
            val center = SWEEP_MIN + fraction * (SWEEP_MAX - SWEEP_MIN)

            for (slot in 0 until inventory.size) {
                val column = slot % 9
                val offset = (column - center).roundToInt()
                val pane = paneForOffset(offset)
                inventory.setItem(slot, ItemStack(pane).apply {
                    itemMeta = itemMeta?.apply { displayName(text(" ")) }
                })
            }
            frame++
        }, 0L, 2L)
    }

    fun stop() {
        task?.cancel()
        task = null
        isRunning = false
    }

    companion object {
        private const val SWEEP_MIN = -3.0
        private const val SWEEP_MAX = 11.0
        private const val SWEEP_FRAMES = 10

        private fun paneForOffset(offset: Int): Material {
            return when (offset) {
                -2, 2 -> Material.ORANGE_STAINED_GLASS_PANE
                -1, 0, 1 -> Material.YELLOW_STAINED_GLASS_PANE
                else -> Material.RED_STAINED_GLASS_PANE
            }
        }
    }
}
