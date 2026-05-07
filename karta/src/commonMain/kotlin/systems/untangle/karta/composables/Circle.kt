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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
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
import kotlin.math.absoluteValue

/**
 * Builds a sector or annular-sector path in the given draw-scope coordinate system.
 *
 * - Full circle / annulus when |sweepAngle| >= 360.
 * - Pie sector when innerRadius <= 0 and |sweepAngle| < 360.
 * - Annular sector otherwise.
 *
 * Angles follow Compose's convention: 0° = right (east), clockwise positive.
 */
private fun buildSectorPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    startAngle: Float,
    sweepAngle: Float
): Path {
    val path = Path()
    val outerRect = Rect(center, outerRadius)
    val isFullCircle = sweepAngle.absoluteValue >= 360f

    if (isFullCircle) {
        path.addOval(outerRect)
        if (innerRadius > 0f) {
            path.fillType = PathFillType.EvenOdd
            path.addOval(Rect(center, innerRadius))
        }
    } else if (innerRadius <= 0f) {
        path.moveTo(center.x, center.y)
        path.arcTo(outerRect, startAngle, sweepAngle, false)
        path.close()
    } else {
        path.arcTo(outerRect, startAngle, sweepAngle, false)
        path.arcTo(Rect(center, innerRadius), startAngle + sweepAngle, -sweepAngle, false)
        path.close()
    }

    return path
}

/**
 * Draws a circle, sector, annulus, or annular sector centered on a geographic coordinate.
 *
 * The radius can be expressed in screen pixels or in real-world meters; the latter is converted
 * at the current zoom level using the equirectangular approximation.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coords Geographic center of the shape.
 * @param radius Outer radius in the units specified by [radiusUnit].
 * @param radiusUnit [DistanceUnit.PIXELS] for a fixed screen size,
 *   [DistanceUnit.METERS] to scale with zoom.
 * @param startAngle Starting angle in degrees. 0° points right (east), clockwise positive.
 * @param sweepAngle Angular extent in degrees. 360 (default) draws a full circle.
 * @param innerRadius Inner radius in the same units as [radius]. 0 (default) fills the sector
 *   solid to the center; a positive value creates a donut / annular sector.
 * @param strokeWidth Outline stroke width in pixels. Set to `0` to suppress the stroke.
 * @param strokeColor Color of the outline.
 * @param fillPattern Optional [FillPattern] for the interior — [FillPattern.Solid],
 *   [FillPattern.Hatched], [FillPattern.Crossed], or [FillPattern.Dotted]. Pass `null`
 *   (default) for no fill.
 * @param pathEffect Optional [PathEffect] applied to the stroke.
 */
@Composable
fun Circle(
    coords: Coordinates,
    radius: Float,
    radiusUnit: DistanceUnit = DistanceUnit.PIXELS,
    startAngle: Float = 0f,
    sweepAngle: Float = 360f,
    innerRadius: Float = 0f,
    strokeWidth: Float = 0f,
    strokeColor: Color = Color.Black,
    fillPattern: FillPattern? = null,
    pathEffect: PathEffect? = null
) {
    val converter = LocalConverter.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val outerRadiusInPixels = remember(radius, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> converter.metersToPixels(radius)
            DistanceUnit.PIXELS -> radius
        }
    }

    val innerRadiusInPixels = remember(innerRadius, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> if (innerRadius > 0f) converter.metersToPixels(innerRadius) else 0f
            DistanceUnit.PIXELS -> innerRadius
        }
    }

    // Pre-render non-Solid patterns to a bitmap keyed on the pixel radius so panning reuses it.
    // The bitmap covers the full bounding box; clipping to the sector shape happens at draw time.
    val patternBitmap = remember(outerRadiusInPixels.toInt(), fillPattern) {
        if (fillPattern == null || fillPattern is FillPattern.Solid) {
            null
        } else {
            val diameter = (2f * outerRadiusInPixels).toInt().coerceAtLeast(1)
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
            (2f * outerRadiusInPixels).toInt().px,
            (2f * outerRadiusInPixels).toInt().px
        )
    ) { coordsOffset ->
        Canvas(modifier = Modifier.offset { coordsOffset }) {
            val sectorPath = buildSectorPath(center, outerRadiusInPixels, innerRadiusInPixels, startAngle, sweepAngle)

            if (fillPattern != null) {
                when (fillPattern) {
                    is FillPattern.Solid -> drawPath(sectorPath, color = fillPattern.color, alpha = fillPattern.alpha, style = Fill)
                    else -> patternBitmap?.let { bitmap ->
                        clipPath(sectorPath) {
                            drawImage(bitmap, topLeft = Offset(-outerRadiusInPixels, -outerRadiusInPixels))
                        }
                    }
                }
            }

            if (strokeWidth > 0) {
                drawPath(
                    path = sectorPath,
                    color = strokeColor,
                    style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                )
            }
        }
    }
}
