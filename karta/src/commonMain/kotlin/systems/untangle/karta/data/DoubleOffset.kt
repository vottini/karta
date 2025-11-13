package systems.untangle.karta.data

data class DoubleOffset(
    val x: Double,
    val y: Double
) {
    fun times(k: Double) = DoubleOffset(x * k, y * k)
    fun div(k: Double) = DoubleOffset(x / k, y / k)
}
