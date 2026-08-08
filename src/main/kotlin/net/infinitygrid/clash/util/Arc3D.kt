package net.infinitygrid.clash.util

import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Arc3D(
    private val center: Vector,
    private val radius: Double,
    private val startAngle: Double,
    private val endAngle: Double
) : SpacedPoints {

    fun sweep(): Double = endAngle - startAngle

    fun pointAt(angleRadians: Double): Vector {
        val x = center.x + radius * cos(angleRadians)
        val z = center.z + radius * sin(angleRadians)
        return Vector(x, center.y, z)
    }

    override fun points(count: Int): List<Vector> {
        require(count >= 2) { "count must be at least 2" }
        val step = sweep() / (count - 1)
        return (0 until count).map { i -> pointAt(startAngle + i * step) }
    }

    override fun estimateCount(spacing: Double): Int {
        val arcLength = abs(sweep()) * radius
        return (arcLength / spacing).toInt() + 1
    }
}
