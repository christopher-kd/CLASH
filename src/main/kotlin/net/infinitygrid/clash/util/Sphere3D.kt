package net.infinitygrid.clash.util

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Sphere3D(private val center: Vector, private val radius: Double) : SpacedPoints {

    fun pointAt(direction: Vector): Vector {
        val normalized = direction.clone().normalize()
        return center.clone().add(normalized.multiply(radius))
    }

    override fun points(count: Int): List<Vector> {
        require(count >= 2) { "count must be at least 2" }
        val goldenAngle = Math.PI * (3.0 - sqrt(5.0))
        return (0 until count).map { i ->
            val y = 1.0 - (i.toDouble() / (count - 1)) * 2.0
            val radiusAtY = sqrt(1.0 - y * y)
            val theta = goldenAngle * i
            val x = cos(theta) * radiusAtY
            val z = sin(theta) * radiusAtY
            pointAt(Vector(x, y, z))
        }
    }

    override fun estimateCount(spacing: Double): Int {
        val surfaceArea = 4 * Math.PI * radius * radius
        return (surfaceArea / (spacing * spacing)).toInt()
    }
}
