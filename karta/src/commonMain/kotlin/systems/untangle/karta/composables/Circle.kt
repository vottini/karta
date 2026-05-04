package systems.untangle.karta.composables

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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

    val radiusInPixels = remember(radius, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> converter.metersToPixels(radius)
            DistanceUnit.PIXELS -> radius
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
                val bounds = TileRegion(
                    IntOffset((center.x - radiusInPixels).toInt(), (center.y - radiusInPixels).toInt()),
                    IntOffset((center.x + radiusInPixels).toInt(), (center.y + radiusInPixels).toInt())
                )
                when (fillPattern) {
                    is FillPattern.Solid -> drawCircle(
                        color = fillPattern.color,
                        alpha = fillPattern.alpha,
                        radius = radiusInPixels
                    )
                    is FillPattern.Hatched -> clipPath(circlePath) {
                        drawHatchLines(bounds, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                    }
                    is FillPattern.Crossed -> clipPath(circlePath) {
                        drawHatchLines(bounds, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                        drawHatchLines(bounds, fillPattern.color, fillPattern.spacing, fillPattern.angle + 90f, fillPattern.strokeWidth)
                    }
                    is FillPattern.Dotted -> clipPath(circlePath) {
                        drawDottedPattern(bounds, fillPattern.color, fillPattern.spacing, fillPattern.radius)
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
