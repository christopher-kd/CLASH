package net.infinitygrid.clash.player.ui.menu

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class JoinMatchMenu private constructor() : ArenaSelectionMenu {

    private lateinit var inventory: Inventory
    private lateinit var loading: InventoryLoadingAnimation
    private val slotArenas = mutableMapOf<Int, String>()

    override val isLoading: Boolean
        get() = loading.isRunning

    override fun getInventory(): Inventory = inventory

    override fun arenaKeyAt(slot: Int): String? = slotArenas[slot]

    override fun startLoadingAnimation() = loading.start()

    override fun stopLoadingAnimation() = loading.stop()

    companion object {
        fun open(clashPlayer: CLASHPlayer) {
            val running = CLASH.INSTANCE.arenaManager.getActive().entries.toList()
            val size = (((running.size - 1) / 9) + 1).coerceIn(1, 6) * 9

            val menu = JoinMatchMenu()
            val title = text("➡ Running Matches")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
            val inv = Bukkit.createInventory(menu, size, title)

            if (running.isEmpty()) {
                inv.setItem(size / 2, ItemStack(Material.BARRIER).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(
                            text("No running matches")
                                .color(NamedTextColor.RED)
                                .decoration(TextDecoration.BOLD, true)
                                .decoration(TextDecoration.ITALIC, false)
                        )
                    }
                })
            }

            running.forEachIndexed { index, (key, arena) ->
                if (index >= size) return@forEachIndexed
                val material = Material.matchMaterial(arena.data.mapIcon) ?: Material.PAPER
                inv.setItem(index, ItemStack(material).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(
                            text(arena.data.mapName)
                                .color(NamedTextColor.GOLD)
                                .decoration(TextDecoration.BOLD, true)
                                .decoration(TextDecoration.ITALIC, false)
                        )
                        lore(
                            listOf(
                                text("${arena.playerCount()} player(s) in match")
                                    .color(NamedTextColor.WHITE)
                                    .decoration(TextDecoration.ITALIC, false)
                            )
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
