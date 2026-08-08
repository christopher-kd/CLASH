package net.infinitygrid.clash.util

import org.bukkit.util.Vector

interface SpacedPoints {

    val minPoints: Int
        get() = 2

    fun points(count: Int): List<Vector>

    fun estimateCount(spacing: Double): Int

    fun pointsBySpacing(spacing: Double): List<Vector> {
        require(spacing > 0.0) { "spacing must be positive" }
        return points(estimateCount(spacing).coerceAtLeast(minPoints))
    }
}
