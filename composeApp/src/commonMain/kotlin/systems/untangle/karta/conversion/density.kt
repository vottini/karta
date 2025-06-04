package systems.untangle.karta.conversion

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import systems.untangle.karta.data.DpSize
import systems.untangle.karta.data.PxSize

// see https://stackoverflow.com/a/65921800

@Composable fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx().toInt() }
@Composable fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Int.toDp(density: Float): Int = (this / density).toInt()
fun Float.toDp(density: Float): Float = this / density
fun Double.toDp(density: Float): Double = this / density
fun Dp.toPixels(density: Float): Int = (this.value * density).toInt()

@Composable
fun IntOffset.correctedPx() = IntOffset(
    this@correctedPx.x.dp.dpToPx(),
    this@correctedPx.y.dp.dpToPx()
)

fun PxSize.toDp(density: Density) : DpSize = DpSize(
    this.width.toDp(density.density),
    this.height.toDp(density.density)
)

fun DpSize.toPx(density: Density) : PxSize = PxSize(
    this.width.dp.toPixels(density.density),
    this.height.dp.toPixels(density.density)
)
