package systems.untangle.karta.data

import androidx.compose.ui.unit.IntOffset

/**
 * Stores offsets of the x and y directions.
 * Comes with operations to scale, invert and add offsets.
 */

data class DoubleOffset(
    val x: Double,
    val y: Double
) {
    operator fun unaryMinus() = DoubleOffset(
        -this.x,
        -this.y
    )

    fun scale(k: Double) = DoubleOffset(x * k, y * k)
    fun add(o: DoubleOffset) = DoubleOffset(this.x + o.x, this.y + o.y)
    fun minus(o: DoubleOffset) = this.add(o.unaryMinus())
}

fun DoubleOffset.toIntOffset() = IntOffset(
    this.x.toInt(),
    this.y.toInt()
)
