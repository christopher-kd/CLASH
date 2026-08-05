package net.infinitygrid.clash.player.ui

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.CLASHPlayer
import net.infinitygrid.clash.player.movement.MovementAction
import net.infinitygrid.clash.util.CubicBezierCurve
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class HUDManager(private val player: CLASHPlayer, val maxCooldownTicks: Int) {
    var p0x = 0.0f
    var p0y = .5f
    var p1x = 1f
    var p1y = .5f

    var maxAnimationTicks = 10L // Total ticks for depletion animation

    private val smashEase = CubicBezierCurve(0.0, 0.0, 0.58, 1.0)

    private var smashAnimationTask: ScheduledTask? = null

    fun startSmashAnimation() {
        smashAnimationTask?.cancel()
        var ticks = 0
        smashAnimationTask = player.scheduler.runAtFixedRate(CLASH.INSTANCE, { task ->
            if (!player.isOnline) {
                task.cancel()
                smashAnimationTask = null
                return@runAtFixedRate
            }
            ticks++
            // Time progress from 0 to 1
            val t = ticks.toFloat() / maxAnimationTicks.toFloat()
            applySmashAnimation(t)
            if (ticks >= maxAnimationTicks) {
                task.cancel()
                smashAnimationTask = null
            }
        }, null, 1L, 1L)
    }

    fun cancelSmashAnimation() {
        smashAnimationTask?.cancel()
        smashAnimationTask = null
    }

    fun displayMovementAction(action: MovementAction) {
        player.sendActionBar(
            LegacyComponentSerializer.legacySection().serialize(
                Component.text(action.actionDisplay, NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
            )
        )
    }

    fun displayDoubleJumpReady() {
        // player.scheduler.run {
        //     player.sendActionBar(Component.text("Double Jump Ready!", NamedTextColor.GREEN))
        // }
        player.sendActionBar(
            LegacyComponentSerializer.legacySection().serialize(
                Component.text(" ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
            )
        )
    }

    fun clearActionBar() {
        player.sendActionBar(" ")
    }

    fun applySmashAnimation(t: Float) {
        val bezierVal = smashEase.solve(t.coerceIn(0.0f, 1.0f).toDouble())
        player.exp = (1.0f - bezierVal.toFloat()).coerceIn(0.0f, 1.0f)
    }

    fun updateCooldown(current: Int) {
        val x = if (maxCooldownTicks == 0) 1.0f else current.toFloat() / maxCooldownTicks.toFloat()
        val progress = CubicBezierCurve(p0x.toDouble(), p0y.toDouble(), p1x.toDouble(), p1y.toDouble()).solve(x.toDouble())
        player.exp = progress.toFloat().coerceIn(0.0f, 1.0f)
    }

}