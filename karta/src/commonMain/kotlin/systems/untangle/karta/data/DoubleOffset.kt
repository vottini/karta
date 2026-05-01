package systems.untangle.karta.data

import androidx.compose.ui.unit.IntOffset

/**
 * A two-dimensional floating-point offset used for tile and anchor coordinates.
 *
 * Used as the anchor parameter in [systems.untangle.karta.composables.Marker] (normalized `[0,1]`
 * range) and internally for tile-space arithmetic.
 *
 * @property x Horizontal component.
 * @property y Vertical component.
 */
data class DoubleOffset(
    val x: Double,
    val y: Double
) {
    /** Returns the negation of this offset. */
    operator fun unaryMinus() = DoubleOffset(-this.x, -this.y)

    /** Returns this offset scaled by [k]. */
    fun scale(k: Double) = DoubleOffset(x * k, y * k)
    /** Returns the element-wise sum of this offset and [o]. */
    fun add(o: DoubleOffset) = DoubleOffset(this.x + o.x, this.y + o.y)
    /** Returns `this − o`. */
    fun minus(o: DoubleOffset) = this.add(o.unaryMinus())
}

fun DoubleOffset.toIntOffset() = IntOffset(
    this.x.toInt(),
    this.y.toInt()
)
