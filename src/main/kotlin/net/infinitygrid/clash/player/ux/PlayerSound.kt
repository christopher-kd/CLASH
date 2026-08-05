package net.infinitygrid.clash.player.ux

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.infinitygrid.clash.player.movement.MovementAction
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitTask

class PlayerSound(val clashPlayer: CLASHPlayer) {

    fun playMovementActionSound(movementAction: MovementAction) {
        val location = clashPlayer.location
        when (movementAction) {
            MovementAction.DOUBLE_JUMP -> {
                clashPlayer.playSound(location, Sound.ENTITY_GOAT_LONG_JUMP, 1f, .6f)
                clashPlayer.playSound(location, Sound.ENTITY_DONKEY_JUMP, 1f, .6f)
            }
            MovementAction.DASH -> clashPlayer.playSound(location, Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.2f)
            MovementAction.SMASH -> clashPlayer.playSound(location, Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1f, .7f)
            else -> {}
        }
    }

    fun playSmashHitGround() {
        clashPlayer.playSound(clashPlayer.location, Sound.ITEM_MACE_SMASH_GROUND, 1f, 1.4f)
    }

    fun playSetupMarkerSound() {
        clashPlayer.playSound(clashPlayer.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }

    fun playUndoSound() {
        clashPlayer.playSound(clashPlayer.location, Sound.BLOCK_AZALEA_LEAVES_BREAK, 1f, 1.5f)
    }

    fun playErrorSound() {
        lateinit var task: BukkitTask
        var playCount = 0
        task = Bukkit.getScheduler().runTaskTimer(CLASH.INSTANCE, Runnable {
            clashPlayer.playSound(clashPlayer.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            playCount++
            if (playCount >= 3) {
                task.cancel()
            }
        }, 0L, 2L)
    }

    fun playEnergyLoadedSound() {
        var soundCount = 0
        Bukkit.getScheduler().runTaskTimer(CLASH.INSTANCE, Runnable {
            if (soundCount >= 4) {
                return@Runnable
            }
            clashPlayer.playSound(clashPlayer.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1f, 2f)
            soundCount++
        }, 0L, 2L)
    }

}