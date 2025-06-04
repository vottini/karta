package systems.untangle.karta.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Immutable
data class Px(
    val value: Float
)

val Float.px get() = Px(this)
val Int.px get() = Px(this.toFloat())
val Double.px get() = Px(this.toFloat())

fun Px.toDP(density: Density) = Dp(this.value / density.density)
