package systems.untangle.karta.data

data class DoubleOffset(
    val x: Double,
    val y: Double
) {
    fun scale(k: Double) = DoubleOffset(x * k, y * k)
    fun add(o: DoubleOffset) = DoubleOffset(this.x + o.x, this.y + o.y)
    operator fun unaryMinus() = DoubleOffset(-this.x, -this.y)
}
