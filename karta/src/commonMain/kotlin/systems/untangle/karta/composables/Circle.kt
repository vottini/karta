package systems.untangle.karta.composables

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.base.LocalConverter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.data.FillPattern
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.px

/**
 * Draws a circle centered on a geographic coordinate.
 *
 * The radius can be expressed in screen pixels or in real-world meters; the latter is converted
 * at the current zoom level using the equirectangular approximation.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coords Geographic center of the circle.
 * @param radius Circle radius in the units specified by [radiusUnit].
 * @param radiusUnit [DistanceUnit.PIXELS] for a fixed screen size,
 *   [DistanceUnit.METERS] to scale with zoom.
 * @param strokeWidth Outline stroke width in pixels. Set to `0` to suppress the stroke.
 * @param strokeColor Color of the circle outline.
 * @param fillPattern Optional [FillPattern] for the interior — [FillPattern.Solid],
 *   [FillPattern.Hatched], [FillPattern.Crossed], or [FillPattern.Dotted]. Pass `null`
 *   (default) for no fill.
 * @param pathEffect Optional [PathEffect] applied to the stroke, e.g.
 *   `PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)` for a dashed outline.
 *   Pass `null` (default) for a solid stroke.
 */
@Composable
fun Circle(
    coords: Coordinates,
    radius: Float,
    radiusUnit: DistanceUnit = DistanceUnit.PIXELS,
    strokeWidth: Float = 0f,
    strokeColor: Color = Color.Black,
    fillPattern: FillPattern? = null,
    pathEffect: PathEffect? = null
) {
    val converter = LocalConverter.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val radiusInPixels = remember(radius, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> converter.metersToPixels(radius)
            DistanceUnit.PIXELS -> radius
        }
    }

    // Pre-render non-Solid patterns to a bitmap keyed on the pixel radius so panning reuses it.
    val patternBitmap = remember(radiusInPixels.toInt(), fillPattern) {
        if (fillPattern == null || fillPattern is FillPattern.Solid) {
            null
        } else {
            val diameter = (2f * radiusInPixels).toInt().coerceAtLeast(1)
            val bitmap = ImageBitmap(diameter, diameter)
            val localBounds = TileRegion(IntOffset(0, 0), IntOffset(diameter, diameter))
            CanvasDrawScope().draw(density, layoutDirection, GraphicsCanvas(bitmap), Size(diameter.toFloat(), diameter.toFloat())) {
                when (fillPattern) {
                    is FillPattern.Hatched -> drawHatchLines(localBounds, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                    is FillPattern.Crossed -> {
                        drawHatchLines(localBounds, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                        drawHatchLines(localBounds, fillPattern.color, fillPattern.spacing, fillPattern.angle + 90f, fillPattern.strokeWidth)
                    }
                    is FillPattern.Dotted -> drawDottedPattern(localBounds, fillPattern.color, fillPattern.spacing, fillPattern.radius)
                    is FillPattern.Solid -> {}
                }
            }
            bitmap
        }
    }

    Geolocated(
        coordinates = coords,
        extension = PxSize(
            (2f * radiusInPixels).toInt().px,
            (2f * radiusInPixels).toInt().px
        )
    ) { coordsOffset ->
        Canvas(modifier = Modifier.offset { coordsOffset }) {
            if (fillPattern != null) {
                val circlePath = Path().apply {
                    addOval(Rect(center, radiusInPixels))
                }
                when (fillPattern) {
                    is FillPattern.Solid -> drawCircle(
                        color = fillPattern.color,
                        alpha = fillPattern.alpha,
                        radius = radiusInPixels
                    )
                    else -> patternBitmap?.let { bitmap ->
                        clipPath(circlePath) {
                            drawImage(bitmap, topLeft = Offset.Zero)
                        }
                    }
                }
            }

            if (strokeWidth > 0) {
                drawCircle(
                    color = strokeColor,
                    style = Stroke(width = strokeWidth, pathEffect = pathEffect),
                    radius = radiusInPixels
                )
            }
        }
    }
}
