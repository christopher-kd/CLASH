package net.infinitygrid.clash.player.ui

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.util.CubicBezierCurve
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

object GradientTitleAnimation {

    private val EASE_OUT = CubicBezierCurve(0.33, 1.0, 0.68, 1.0)

    fun show(
        player: Player,
        content: String,
        subtitle: Component,
        startColor: TextColor = TextColor.fromHexString("#310842")!!,
        endColor: TextColor = TextColor.fromHexString("#8d13c2")!!, // 8d13c2
        durationMillis: Long = 4000L,
        endColorBias: Double = 3.2,
        maxBandWidth: Double = 0.58
    ) {
        val totalTicks = (durationMillis / 50L).toInt().coerceAtLeast(1)
        var tick = 0
        lateinit var task: BukkitTask

        task = Bukkit.getScheduler().runTaskTimer(CLASH.INSTANCE, Runnable {
            val fraction = (tick.toDouble() / totalTicks).coerceIn(0.0, 1.0)
            val eased = EASE_OUT.solve(fraction)
            val isFinalFrame = tick >= totalTicks
            // Linear 0 -> maxBandWidth -> 0 triangle across the animation, so the band
            // starts and ends as a thin sliver and is widest at the midpoint.
            val bandWidth = (maxBandWidth * (1.0 - abs(2.0 * fraction - 1.0))).coerceAtLeast(0.001)

            val times = if (isFinalFrame)
                Title.Times.times(Duration.ZERO, Duration.ofMillis(3000), Duration.ofMillis(500))
            else
                Title.Times.times(Duration.ZERO, Duration.ofMillis(150), Duration.ZERO)

            player.showTitle(
                Title.title(gradientText(content, startColor, endColor, eased, endColorBias, bandWidth), subtitle, times)
            )

            if (isFinalFrame) task.cancel()
            tick++
        }, 0L, 1L)
    }

    private fun gradientText(
        content: String,
        startColor: TextColor,
        endColor: TextColor,
        progress: Double,
        endColorBias: Double,
        bandWidth: Double
    ): Component {
        val length = content.length
        val builder = text()
        // The bright band's center travels from the right edge (progress 0) to the
        // left edge (progress 1), so it visibly sweeps through the middle on the way.
        val peakPos = 1.0 - progress
        // Baseline crossfade from startColor to endColor across the whole animation,
        // so by progress 1 every character is pure endColor regardless of band width -
        // the traveling band only ever boosts a character further toward endColor, on
        // top of this baseline, and contributes nothing once the baseline reaches 1.
        val baseBlend = progress.coerceIn(0.0, 1.0)

        content.forEachIndexed { index, char ->
            val positionNorm = if (length <= 1) 0.5 else index.toDouble() / (length - 1)
            val distance = abs(positionNorm - peakPos)
            val raw = (1.0 - distance / bandWidth).coerceIn(0.0, 1.0)
            val bandBoost = raw.pow(endColorBias)
            val blend = (baseBlend + (1.0 - baseBlend) * bandBoost).coerceIn(0.0, 1.0)
            builder.append(
                text(char.toString(), lerpColor(startColor, endColor, blend))
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false)
            )
        }

        return builder.build()
    }

    private fun lerpColor(a: TextColor, b: TextColor, t: Double): TextColor {
        val r = (a.red() + (b.red() - a.red()) * t).roundToInt()
        val g = (a.green() + (b.green() - a.green()) * t).roundToInt()
        val bl = (a.blue() + (b.blue() - a.blue()) * t).roundToInt()
        return TextColor.color(r, g, bl)
    }
}
