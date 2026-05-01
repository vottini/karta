package systems.untangle.karta.composables

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import systems.untangle.karta.base.LocalConverter
import systems.untangle.karta.base.LocalPointerEvents
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.data.PxSize
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
 * @param borderWidth Outline stroke width in pixels. Set to `0` to suppress the border.
 * @param borderColor Color of the circle outline.
 * @param fillColor Interior fill color. Pass `null` for a hollow circle.
 */
@Composable
fun Circle(
    coords: Coordinates,
    radius: Float,
    radiusUnit: DistanceUnit = DistanceUnit.PIXELS,
    borderWidth: Float = 0f,
    borderColor: Color = Color.Black,
    fillColor: Color? = Color.Black
) {
    val converter = LocalConverter.current
    val pointerEvents = LocalPointerEvents.current

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
            if (null != fillColor) {
                drawCircle(
                    color = fillColor,
                    radius = radiusInPixels
                )
            }

            if (borderWidth > 0) {
                drawCircle(
                    color = borderColor,
                    style = Stroke(borderWidth),
                    radius = radiusInPixels
                )
            }
        }
    }
}

