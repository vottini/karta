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
import systems.untangle.karta.data.Size
import androidx.compose.ui.unit.dp
import systems.untangle.karta.conversion.dpToPx

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

    val correctedRadius = radiusInPixels.dp.dpToPx()

    Geolocated(
        coordinates = coords,
        extension = Size(
            (2f * correctedRadius).toInt(),
            (2f * correctedRadius).toInt()
        )
    ) { coordsOffset ->
        Canvas(modifier = Modifier.offset { coordsOffset }) {
            if (null != fillColor) {
                drawCircle(
                    color = fillColor,
                    radius = correctedRadius.toFloat()
                )
            }

            if (borderWidth > 0) {
                drawCircle(
                    color = borderColor,
                    style = Stroke(borderWidth),
                    radius = correctedRadius.toFloat()
                )
            }
        }
    }
}

