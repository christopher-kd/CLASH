package net.infinitygrid.clash.player.movement

import net.infinitygrid.clash.player.CLASHPlayer

class MovementAbilities(private val clashPlayer: CLASHPlayer) {

    fun dash() {
        clashPlayer.allowFlight = false
        val multiplier = 1.2
        val direction = clashPlayer.location.direction
        val velocity = direction.multiply(multiplier).setY(0)
        clashPlayer.velocity = velocity
        clashPlayer.soundPlayer.playMovementActionSound(MovementAction.DASH)
    }

    fun doubleJump() {
        val multiplier = .8
        val upwardForce = .8
        val minUpwardForce = .6
        val direction = clashPlayer.location.direction
        val velocity = clashPlayer.velocity.add(direction.multiply(multiplier).setY(upwardForce))
        if (velocity.y < minUpwardForce) {
            velocity.y = minUpwardForce
        }
        clashPlayer.velocity = velocity
        clashPlayer.soundPlayer.playMovementActionSound(MovementAction.DOUBLE_JUMP)
        clashPlayer.hudManager.clearActionBar()
        clashPlayer.groundTracker.runGroundScheduler()
    }

    fun smashStart() {
        clashPlayer.allowFlight = false
        clashPlayer.velocity = clashPlayer.velocity.setY(-1)
        clashPlayer.soundPlayer.playMovementActionSound(MovementAction.SMASH)
        clashPlayer.groundTracker.runGroundScheduler()
        clashPlayer.hudManager.startSmashAnimation()
    }

}
