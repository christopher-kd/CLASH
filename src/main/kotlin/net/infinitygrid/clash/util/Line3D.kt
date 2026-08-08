package net.infinitygrid.clash.util

import org.bukkit.util.Vector

class Line3D(private val start: Vector, private val end: Vector) : SpacedPoints {

    fun length(): Double = start.distance(end)

    fun pointAt(t: Double): Vector {
        val clamped = t.coerceIn(0.0, 1.0)
        return start.clone().add(end.clone().subtract(start).multiply(clamped))
    }

    override fun points(count: Int): List<Vector> {
        require(count >= 2) { "count must be at least 2" }
        return (0 until count).map { i -> pointAt(i.toDouble() / (count - 1)) }
    }

    override fun estimateCount(spacing: Double): Int = (length() / spacing).toInt() + 1
}
