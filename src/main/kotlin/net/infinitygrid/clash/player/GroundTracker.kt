package net.infinitygrid.clash.player

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.movement.MovementAction
import net.infinitygrid.clash.player.movement.MovementController
import org.bukkit.Sound

class GroundTracker(private val clashPlayer: CLASHPlayer) {

    private var groundScheduler: ScheduledTask? = null

    fun isOnGround(): Boolean {
        val box = clashPlayer.boundingBox
        val checkBox = box.clone().expand(.0, .1, .0).shift(.0, -.01, .0)
        return clashPlayer.world.hasCollisionsIn(checkBox)
    }

    fun runGroundScheduler() {
        if (groundScheduler != null) return
        groundScheduler = clashPlayer.scheduler.runAtFixedRate(CLASH.INSTANCE, {
            val player = PlayerRegistry.instance.getPlayer(clashPlayer)
            if (player == null) {
                stopGroundScheduler()
                return@runAtFixedRate
            }

            if (clashPlayer.isOnGround) {
                val movementController = clashPlayer.movementController
                if (movementController.currentMovementAction == MovementAction.SMASH) {
                    clashPlayer.soundPlayer.playSmashHitGround()
                    movementController.startSmashCooldown()
                } else {
                    clashPlayer.hudManager.displayDoubleJumpReady()
                    movementController.allowDoubleJump(true)
                }
                movementController.currentMovementAction = MovementAction.IDLE
                stopGroundScheduler()
            }

        }, null, 1L, 1L)
    }

    fun stopGroundScheduler() {
        groundScheduler?.cancel()
        groundScheduler = null
    }
}
