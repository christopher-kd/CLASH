package net.infinitygrid.clash.player.ui.menu

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class CreateMatchMenu private constructor() : InventoryHolder {

    private lateinit var inventory: Inventory
    private lateinit var loading: InventoryLoadingAnimation
    private val slotArenas = mutableMapOf<Int, String>()

    val isLoading: Boolean
        get() = loading.isRunning

    override fun getInventory(): Inventory = inventory

    fun arenaKeyAt(slot: Int): String? = slotArenas[slot]

    fun startLoadingAnimation() = loading.start()

    fun stopLoadingAnimation() = loading.stop()

    companion object {
        private const val DESCRIPTION_LINE_LENGTH = 40

        private fun wrapText(text: String, maxLineLength: Int): List<String> {
            val lines = mutableListOf<String>()
            val currentLine = StringBuilder()

            text.split(" ").forEach { word ->
                if (currentLine.isNotEmpty() && currentLine.length + 1 + word.length > maxLineLength) {
                    lines.add(currentLine.toString())
                    currentLine.clear()
                }
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine.toString())

            return lines
        }

        fun open(clashPlayer: CLASHPlayer) {
            val arenas = CLASH.INSTANCE.arenaRegistry.entries().entries.toList()
            val size = (((arenas.size - 1) / 9) + 1).coerceIn(1, 6) * 9

            val menu = CreateMatchMenu()
            val title = text("➡ Arena Selection")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
            val inv = Bukkit.createInventory(menu, size, title)

            arenas.forEachIndexed { index, (key, arena) ->
                if (index >= size) return@forEachIndexed
                val material = Material.matchMaterial(arena.mapIcon) ?: Material.PAPER
                inv.setItem(index, ItemStack(material).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(
                            text(arena.mapName)
                                .color(NamedTextColor.GOLD)
                                .decoration(TextDecoration.BOLD, true)
                                .decoration(TextDecoration.ITALIC, false)
                        )
                        lore(
                            wrapText(arena.description, DESCRIPTION_LINE_LENGTH).map { line ->
                                text(line)
                                    .color(NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false)
                            }
                        )
                    }
                })
                menu.slotArenas[index] = key
            }

            menu.inventory = inv
            menu.loading = InventoryLoadingAnimation(inv)

            val rawPlayer = Bukkit.getPlayer(clashPlayer.uniqueId) ?: return
            rawPlayer.openInventory(inv)
        }
    }
}
