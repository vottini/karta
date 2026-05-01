package systems.untangle.karta.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Absolute pixel size unit, independent of screen density.
 *
 * Convert to density-independent [androidx.compose.ui.unit.Dp] with
 * [systems.untangle.karta.conversion.toDp].
 *
 * @property value Raw pixel value.
 */
@Immutable
data class Px(
    val value: Float
)

/** Creates a [Px] from a [Float] value: `32f.px`. */
val Float.px get() = Px(this)
/** Creates a [Px] from an [Int] value: `32.px`. */
val Int.px get() = Px(this.toFloat())
/** Creates a [Px] from a [Double] value: `32.0.px`. */
val Double.px get() = Px(this.toFloat())

@Stable operator fun Px.plus(other: Px) = Px(value + other.value)
@Stable operator fun Px.minus(other: Px) = Px(value - other.value)
@Stable operator fun Px.times(other: Px) = Px(value * other.value)
@Stable operator fun Px.div(other: Px) = Px(value / other.value)

@Stable operator fun Px.plus(other: Float) = Px(value + other)
@Stable operator fun Px.minus(other: Float) = Px(value - other)
@Stable operator fun Px.times(other: Float) = Px(value * other)
@Stable operator fun Px.div(other: Float) = Px(value / other)

@Stable operator fun Px.plus(other: Int) = Px(value + other)
@Stable operator fun Px.minus(other: Int) = Px(value - other)
@Stable operator fun Px.times(other: Int) = Px(value * other)
@Stable operator fun Px.div(other: Int) = Px(value / other)
