package systems.untangle.karta.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Px(
    val value: Float
)

val Float.px get() = Px(this)
val Int.px get() = Px(this.toFloat())
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
