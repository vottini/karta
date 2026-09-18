package systems.untangle.karta.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.drawscope.withTransform
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
import kotlin.math.max

/**
 * Builds a sector or annular-sector path for an ellipse.
 *
 * - Full ellipse / annular ellipse when |sweepAngle| >= 360.
 * - Pie sector when innerRadiusX <= 0 or innerRadiusY <= 0 and |sweepAngle| < 360.
 * - Annular sector otherwise.
 *
 * Angles follow Compose's convention: 0° = right (east), clockwise positive.
 * Inclination is applied externally via canvas transform so the center stays fixed.
 */
private fun buildEllipseSectorPath(
    center: Offset,
    outerRadiusX: Float,
    outerRadiusY: Float,
    innerRadiusX: Float,
    innerRadiusY: Float,
    startAngle: Float,
    sweepAngle: Float
): Path {
    val path = Path()
    val outerRect = Rect(center.x - outerRadiusX, center.y - outerRadiusY, center.x + outerRadiusX, center.y + outerRadiusY)
    val isFullEllipse = sweepAngle.absoluteValue >= 360f

    if (isFullEllipse) {
        path.addOval(outerRect)
        if (innerRadiusX > 0f && innerRadiusY > 0f) {
            path.fillType = PathFillType.EvenOdd
            path.addOval(Rect(center.x - innerRadiusX, center.y - innerRadiusY, center.x + innerRadiusX, center.y + innerRadiusY))
        }
    } else if (innerRadiusX <= 0f || innerRadiusY <= 0f) {
        path.moveTo(center.x, center.y)
        path.arcTo(outerRect, startAngle, sweepAngle, false)
        path.close()
    } else {
        path.arcTo(outerRect, startAngle, sweepAngle, false)
        path.arcTo(
            Rect(center.x - innerRadiusX, center.y - innerRadiusY, center.x + innerRadiusX, center.y + innerRadiusY),
            startAngle + sweepAngle, -sweepAngle, false
        )
        path.close()
    }

    return path
}

/**
 * Draws an ellipse, elliptic sector, annular ellipse, or annular elliptic sector centered on
 * a geographic coordinate.
 *
 * [inclination] rotates the shape around [coords] so the geographic center stays fixed.
 *
 * The radii can be expressed in screen pixels or real-world meters; the latter is converted at
 * the current zoom level using the equirectangular approximation.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coords Geographic center of the shape.
 * @param radiusX Outer horizontal semi-axis in the units specified by [radiusUnit].
 * @param radiusY Outer vertical semi-axis in the units specified by [radiusUnit].
 * @param radiusUnit [DistanceUnit.PIXELS] for a fixed screen size,
 *   [DistanceUnit.METERS] to scale with zoom.
 * @param inclination Rotation angle in degrees around [coords]. 0° = no rotation; positive = clockwise.
 * @param startAngle Starting angle of the sector in degrees. 0° points right (east), clockwise positive.
 * @param sweepAngle Angular extent in degrees. 360 (default) draws the full ellipse.
 * @param innerRadiusX Inner horizontal semi-axis, same units as [radiusX]. 0 (default) = solid fill.
 * @param innerRadiusY Inner vertical semi-axis, same units as [radiusY]. 0 (default) = solid fill.
 * @param strokeWidth Outline stroke width in pixels. Set to `0` to suppress the stroke.
 * @param strokeColor Color of the outline.
 * @param fillPattern Optional [FillPattern] for the interior. Pass `null` (default) for no fill.
 * @param pathEffect Optional [PathEffect] applied to the stroke.
 */
@Composable
fun Ellipse(
    coords: Coordinates,
    radiusX: Float,
    radiusY: Float,
    radiusUnit: DistanceUnit = DistanceUnit.PIXELS,
    inclination: Float = 0f,
    startAngle: Float = 0f,
    sweepAngle: Float = 360f,
    innerRadiusX: Float = 0f,
    innerRadiusY: Float = 0f,
    strokeWidth: Float = 0f,
    strokeColor: Color = Color.Black,
    fillPattern: FillPattern? = null,
    pathEffect: PathEffect? = null
) {
    val converter = LocalConverter.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val outerRadiusXPx = remember(radiusX, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> converter.metersToPixels(radiusX)
            DistanceUnit.PIXELS -> radiusX
        }
    }

    val outerRadiusYPx = remember(radiusY, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> converter.metersToPixels(radiusY)
            DistanceUnit.PIXELS -> radiusY
        }
    }

    val innerRadiusXPx = remember(innerRadiusX, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> if (innerRadiusX > 0f) converter.metersToPixels(innerRadiusX) else 0f
            DistanceUnit.PIXELS -> innerRadiusX
        }
    }

    val innerRadiusYPx = remember(innerRadiusY, radiusUnit, converter) {
        when (radiusUnit) {
            DistanceUnit.METERS -> if (innerRadiusY > 0f) converter.metersToPixels(innerRadiusY) else 0f
            DistanceUnit.PIXELS -> innerRadiusY
        }
    }

    // For viewport culling, use the larger semi-axis as a conservative bounding radius.
    val outerMaxRadiusPx = max(outerRadiusXPx, outerRadiusYPx)

    // Pre-render non-Solid patterns to a bitmap sized to the ellipse bounding box.
    // Inclination is applied via canvas transform at draw time; the pattern rotates with the shape.
    val patternBitmap = remember(outerRadiusXPx.toInt(), outerRadiusYPx.toInt(), fillPattern) {
        if (fillPattern == null || fillPattern is FillPattern.Solid) {
            null
        } else {
            val bitmapW = (2f * outerRadiusXPx).toInt().coerceAtLeast(1)
            val bitmapH = (2f * outerRadiusYPx).toInt().coerceAtLeast(1)
            val bitmap = ImageBitmap(bitmapW, bitmapH)
            val localBounds = TileRegion(IntOffset(0, 0), IntOffset(bitmapW, bitmapH))
            CanvasDrawScope().draw(density, layoutDirection, GraphicsCanvas(bitmap), Size(bitmapW.toFloat(), bitmapH.toFloat())) {
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
            (2f * outerMaxRadiusPx).toInt().px,
            (2f * outerMaxRadiusPx).toInt().px
        )
    ) { coordsOffset ->
        Canvas(modifier = Modifier.offset { coordsOffset }) {
            // Rotate around the canvas center, which corresponds to the geographic coordinate,
            // so the ellipse center stays fixed regardless of inclination.
            withTransform({ rotate(inclination, center) }) {
                val ellipsePath = buildEllipseSectorPath(
                    center,
                    outerRadiusXPx, outerRadiusYPx,
                    innerRadiusXPx, innerRadiusYPx,
                    startAngle, sweepAngle
                )

                if (fillPattern != null) {
                    when (fillPattern) {
                        is FillPattern.Solid -> drawPath(ellipsePath, color = fillPattern.color, alpha = fillPattern.alpha, style = Fill)
                        else -> patternBitmap?.let { bitmap ->
                            clipPath(ellipsePath) {
                                drawImage(bitmap, topLeft = Offset(center.x - outerRadiusXPx, center.y - outerRadiusYPx))
                            }
                        }
                    }
                }

                if (strokeWidth > 0f) {
                    drawPath(
                        path = ellipsePath,
                        color = strokeColor,
                        style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                    )
                }
            }
        }
    }
}
