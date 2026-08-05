package net.infinitygrid.clash.player.ui.interactivehotbar

import net.infinitygrid.clash.player.CLASHPlayer
import net.infinitygrid.clash.player.ui.menu.CreateMatchMenu
import net.infinitygrid.clash.player.ui.menu.JoinMatchMenu
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object InteractiveHotbarTemplates {

    fun applyDefaultMode(player: CLASHPlayer, defaultSlot: Int? = 4) {
        val hotbar = player.interactiveHotbar
        hotbar.clear()

        val createMatchItem = ItemStack(Material.NETHER_STAR).apply {
            itemMeta = itemMeta?.apply {
                displayName(
                    text("Create Match")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false)
                )
            }
        }

        val joinMatchItem = ItemStack(Material.ENDER_EYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(
                    text("Join Match")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false)
                )
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(createMatchItem, 4, {
                CreateMatchMenu.open(player)
            }, {}),
            InteractiveHotbarSlot(joinMatchItem, 6, {
                JoinMatchMenu.open(player)
            }, {})
        )

        defaultSlot?.let { player.inventory.heldItemSlot = it - 1 }
    }

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

        val mapDetailsItem = ItemStack(Material.BOOK).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Set Map Details").color(NamedTextColor.YELLOW))
            }
        }

        val undoItem = ItemStack(Material.RED_CANDLE).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Undo Last Action").color(NamedTextColor.RED))
            }
        }

        val playerSpawnItem = ItemStack(Material.PLAYER_HEAD).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Set Player Spawn").color(NamedTextColor.AQUA))
            }
        }

        val itemSpawnItem = ItemStack(Material.ITEM_FRAME).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Set Item Spawn").color(NamedTextColor.AQUA))
            }
        }

        val spectatorSpawnItem = ItemStack(Material.FEATHER).apply {
            itemMeta = itemMeta?.apply {
                displayName(text("Set Spectator Spawn").color(NamedTextColor.AQUA))
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(saveItem, 1, {
                val mapSetup = player.mapSetup
                if (mapSetup == null) {
                    player.sendMessage(text("No active map setup.").color(NamedTextColor.RED))
                } else {
                    mapSetup.save()
                }
            }, {}),
            InteractiveHotbarSlot(mapDetailsItem, 3, {
                val mapSetup = player.mapSetup
                if (mapSetup == null) {
                    player.sendMessage(text("No active map setup.").color(NamedTextColor.RED))
                } else {
                    mapSetup.openDetailsDialog()
                }
            }, {}),
            InteractiveHotbarSlot(undoItem, 4, {
                player.mapSetup?.undoLastSpawn()
            }, {}),
            InteractiveHotbarSlot(playerSpawnItem, 5, {
                player.mapSetup?.addPlayerSpawn(player.location.clone())
            }, {}),
            InteractiveHotbarSlot(itemSpawnItem, 6, {
                player.mapSetup?.addItemSpawn(player.location.clone())
            }, {}),
            InteractiveHotbarSlot(spectatorSpawnItem, 7, {
                player.mapSetup?.setSpectatorSpawn(player.location.clone())
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

        val rulebookItem = ItemStack(Material.WRITTEN_BOOK).apply {
            itemMeta = itemMeta?.apply {
                displayName(
                    text("Rulebook / Match Modifiers")
                        .color(NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
                )
            }
        }

        val pauseCountdownItem = ItemStack(Material.CLOCK).apply {
            itemMeta = itemMeta?.apply {
                displayName(
                    text("Pause countdown")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
                )
            }
        }

        val leaveArenaItem = ItemStack(Material.RED_DYE).apply {
            itemMeta = itemMeta?.apply {
                displayName(
                    text("Leave arena")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
                )
            }
        }

        hotbar.setSlots(
            InteractiveHotbarSlot(rulebookItem, 1, {
                player.sendMessage(text("okay here we have rules"))
            }, {}),
            InteractiveHotbarSlot(pauseCountdownItem, 7, {
                player.sendMessage(text("okay, this pauses countdown"))
            }, {}),
            InteractiveHotbarSlot(leaveArenaItem, 9, {
                player.arena?.leave(player)
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
