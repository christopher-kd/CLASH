package net.infinitygrid.clash.player.ui.interactivehotbar

import net.infinitygrid.clash.player.CLASHPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object InteractiveHotbarTemplates {

    fun applySetupMode(player: CLASHPlayer) {
        val hotbar = player.interactiveHotbar
        hotbar.clear()
        
        val saveItem = ItemStack(Material.LIME_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Save Map").color(NamedTextColor.GREEN))
            }
        }
        
        val cancelItem = ItemStack(Material.RED_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Cancel Setup").color(NamedTextColor.RED))
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(saveItem, 1, {
                player.sendMessage(text("Map saved!").color(NamedTextColor.GREEN))
            }, {}),
            InteractiveHotbarSlot(cancelItem, 9, {
                player.sendMessage(text("Setup cancelled.").color(NamedTextColor.RED))
                player.cancelMapSetup()
                player.interactiveHotbar.clear()
            }, {})
        )
    }
    
    fun applyMatchMode(player: CLASHPlayer) {
        val hotbar = player.interactiveHotbar
        hotbar.clear()

        val saveItem = ItemStack(Material.LIME_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Save Map").color(NamedTextColor.GREEN))
            }
        }

        val otherItem = ItemStack(Material.RED_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Other").color(NamedTextColor.RED))
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(saveItem, 1, {
                player.sendMessage(text("yay"))
            }, {}),
            InteractiveHotbarSlot(otherItem, 9, {
                player.sendMessage(text("okay"))
            }, {})
        )
    }

    fun applyPartyMode(player: CLASHPlayer) {
        val hotbar = player.interactiveHotbar
        hotbar.clear()

        val leaveItem = ItemStack(Material.RED_BED).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Leave Party").color(NamedTextColor.RED))
            }
        }

        val inviteItem = ItemStack(Material.PAPER).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Invite to Party").color(NamedTextColor.YELLOW))
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(inviteItem, 1, {
                player.sendMessage(text("Party invite menu...").color(NamedTextColor.YELLOW))
                // Here should be party invite logic
            }, {}),
            InteractiveHotbarSlot(leaveItem, 9, {
                player.sendMessage(text("Leaving party...").color(NamedTextColor.RED))
                // Here should be party leaving logic
            }, {})
        )
    }

}
