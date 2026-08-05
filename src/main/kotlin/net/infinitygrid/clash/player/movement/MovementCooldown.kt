package net.infinitygrid.clash.player.movement

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import org.bukkit.Bukkit
import org.bukkit.Sound

class MovementCooldown(private val clashPlayer: CLASHPlayer, private val onReady: () -> Unit) {

    var cooldownProgress: Int = clashPlayer.hudManager.maxCooldownTicks
    private var cooldownTask: ScheduledTask? = null

    fun isReady(): Boolean = cooldownProgress >= clashPlayer.hudManager.maxCooldownTicks

    fun startCooldown() {
        clashPlayer.hudManager.cancelSmashAnimation()
        cooldownProgress = 0
        clashPlayer.hudManager.updateCooldown(0)
        cooldownTask?.cancel()
        cooldownTask = clashPlayer.scheduler.runAtFixedRate(CLASH.INSTANCE, { task ->
            if (!clashPlayer.isOnline) {
                task.cancel()
                cooldownTask = null
                return@runAtFixedRate
            }
            cooldownProgress++
            clashPlayer.hudManager.updateCooldown(cooldownProgress)
            if (cooldownProgress >= clashPlayer.hudManager.maxCooldownTicks) {
                cooldownProgress = clashPlayer.hudManager.maxCooldownTicks
                clashPlayer.hudManager.displayDoubleJumpReady()

                // play sound
                clashPlayer.soundPlayer.playEnergyLoadedSound()

                onReady()
                task.cancel()
                cooldownTask = null
            }
        }, null, 1L, 1L)
    }

    fun stopCooldown() {
        cooldownTask?.cancel()
        cooldownTask = null
        clashPlayer.hudManager.cancelSmashAnimation()
    }

    fun reset() {
        stopCooldown()
        cooldownProgress = clashPlayer.hudManager.maxCooldownTicks
        clashPlayer.hudManager.updateCooldown(cooldownProgress)
    }

}
