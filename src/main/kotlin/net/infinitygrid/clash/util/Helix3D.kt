package net.infinitygrid.clash.util

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class Helix3D(
    private val center: Vector,
    private val radius: Double,
    private val height: Double,
    private val turns: Double
) : SpacedPoints {

    fun pointAt(t: Double): Vector {
        val clamped = t.coerceIn(0.0, 1.0)
        val angle = clamped * turns * 2 * Math.PI
        val x = center.x + radius * cos(angle)
        val y = center.y + clamped * height
        val z = center.z + radius * sin(angle)
        return Vector(x, y, z)
    }

    fun length(): Double = hypot(2 * Math.PI * radius * turns, height)

    override fun points(count: Int): List<Vector> {
        require(count >= 2) { "count must be at least 2" }
        return (0 until count).map { i -> pointAt(i.toDouble() / (count - 1)) }
    }

    override fun estimateCount(spacing: Double): Int = (length() / spacing).toInt() + 1
}
