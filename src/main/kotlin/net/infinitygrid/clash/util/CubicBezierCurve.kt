package net.infinitygrid.clash.util

class CubicBezierCurve(
    private val x1: Double,
    private val y1: Double,
    private val x2: Double,
    private val y2: Double
) {

    fun solve(x: Double, iterations: Int = 12): Double {
        val target = x.coerceIn(0.0, 1.0)
        var low = 0.0
        var high = 1.0
        var u = target

        repeat(iterations) {
            u = (low + high) / 2
            val xu = 3 * (1 - u) * (1 - u) * u * x1 + 3 * (1 - u) * u * u * x2 + u * u * u
            if (xu < target) low = u else high = u
        }

        return 3 * (1 - u) * (1 - u) * u * y1 + 3 * (1 - u) * u * u * y2 + u * u * u
    }
}
