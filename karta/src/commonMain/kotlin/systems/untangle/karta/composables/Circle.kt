package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.data.FillPattern

/**
 * Draws a circle, sector, annulus, or annular sector centered on a geographic coordinate.
 *
 * Convenience wrapper around [Ellipse] with equal horizontal and vertical radii.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coords Geographic center of the shape.
 * @param radius Outer radius in the units specified by [radiusUnit].
 * @param radiusUnit [DistanceUnit.PIXELS] for a fixed screen size,
 *   [DistanceUnit.METERS] to scale with zoom.
 * @param startAngle Starting angle in degrees. 0° points right (east), clockwise positive.
 * @param sweepAngle Angular extent in degrees. 360 (default) draws a full circle.
 * @param innerRadius Inner radius in the same units as [radius]. 0 (default) = solid fill.
 * @param strokeWidth Outline stroke width in pixels. Set to `0` to suppress the stroke.
 * @param strokeColor Color of the outline.
 * @param fillPattern Optional [FillPattern] for the interior. Pass `null` (default) for no fill.
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
    Ellipse(
        coords = coords,
        radiusX = radius,
        radiusY = radius,
        radiusUnit = radiusUnit,
        inclination = 0f,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        innerRadiusX = innerRadius,
        innerRadiusY = innerRadius,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        fillPattern = fillPattern,
        pathEffect = pathEffect
    )
}
