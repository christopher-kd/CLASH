
fun main() {
    val p0 = 0.0f
    val p1 = 1.0f
    val p2 = 1.0f
    val p3 = 1.0f
    val maxCooldownTicks = 100
    for (current in 0..100 step 10) {
        val t = current.toFloat() / maxCooldownTicks.toFloat()
        val it = 1.0f - t
        val progress = (it * it * it * p0) + (3 * it * it * t * p1) + (3 * it * t * t * p2) + (t * t * t * p3)
        println("current: $current, t: $t, progress: $progress")
    }
}
main()
