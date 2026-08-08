package net.infinitygrid.clash.util

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class Circle3D(private val center: Vector, private val radius: Double) : SpacedPoints {

    override val minPoints = 3

    fun pointAt(angleRadians: Double): Vector {
        val x = center.x + radius * cos(angleRadians)
        val z = center.z + radius * sin(angleRadians)
        return Vector(x, center.y, z)
    }

    override fun points(count: Int): List<Vector> {
        require(count >= 3) { "count must be at least 3" }
        val step = (2 * Math.PI) / count
        return (0 until count).map { i -> pointAt(i * step) }
    }

    override fun estimateCount(spacing: Double): Int {
        val circumference = 2 * Math.PI * radius
        return (circumference / spacing).toInt()
    }
}
