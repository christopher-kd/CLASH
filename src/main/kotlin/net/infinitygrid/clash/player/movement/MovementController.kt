package net.infinitygrid.clash.player.movement

import net.infinitygrid.clash.player.CLASHPlayer

class MovementController(private val clashPlayer: CLASHPlayer) {
    var currentMovementAction: MovementAction = MovementAction.IDLE
    val abilities = MovementAbilities(clashPlayer)
    val cooldown = MovementCooldown(clashPlayer) {
        allowDoubleJump(true)
    }

    fun allowDoubleJump(boolean: Boolean) {
        clashPlayer.allowFlight = boolean
    }

    fun executeMovementAction() {
        if (!cooldown.isReady()) return
        clashPlayer.isFlying = false
        if (currentMovementAction == MovementAction.IDLE) {
            currentMovementAction = MovementAction.DOUBLE_JUMP
            abilities.doubleJump()
        } else if (currentMovementAction == MovementAction.DOUBLE_JUMP) {
            currentMovementAction = MovementAction.DASH
            abilities.dash()
        }
        clashPlayer.hudManager.displayMovementAction(currentMovementAction)
    }

    fun smash() {
        if (currentMovementAction == MovementAction.SMASH) return
        if (!cooldown.isReady()) return
        currentMovementAction = MovementAction.SMASH
        abilities.smashStart()
        clashPlayer.hudManager.displayMovementAction(currentMovementAction)
    }

    fun startSmashCooldown() {
        cooldown.startCooldown()
        allowDoubleJump(false)
    }

    fun stopCooldown() {
        cooldown.stopCooldown()
    }

    fun reset() {
        clashPlayer.groundTracker.stopGroundScheduler()
        currentMovementAction = MovementAction.IDLE
        cooldown.reset()
        allowDoubleJump(true)
    }

}
