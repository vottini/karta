package systems.untangle.karta.composables

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import systems.untangle.karta.LocalConverter
import systems.untangle.karta.LocalPointerEvents
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.data.PxSize

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
            (2f * radiusInPixels).toInt(),
            (2f * radiusInPixels).toInt()
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

