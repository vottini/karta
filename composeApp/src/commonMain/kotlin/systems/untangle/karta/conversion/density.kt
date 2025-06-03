package systems.untangle.karta.conversion

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import systems.untangle.karta.data.Size

// see https://stackoverflow.com/a/65921800

@Composable fun Dp.dpToFPx() = with(LocalDensity.current) { this@dpToFPx.toPx().toDouble() }
@Composable fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx().toInt() }
@Composable fun Double.pxToDp() = with(LocalDensity.current) { this@pxToDp.toFloat().toDp() }

fun Int.toDp(density: Float): Int = (this / density).toInt()
fun Float.toDp(density: Float): Float = this / density
fun Double.toDp(density: Float): Double = this / density
fun Dp.toPixels(density: Float): Int = (this.value * density).toInt()

@Composable
fun IntOffset.correctedPx() = IntOffset(
    this@correctedPx.x.dp.dpToPx(),
    this@correctedPx.y.dp.dpToPx()
)

@Composable
fun Size.correctedPx() = Size(
    this@correctedPx.width.dp.dpToPx(),
    this@correctedPx.height.dp.dpToPx()
)
