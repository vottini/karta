package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.LocalConverter
import systems.untangle.karta.LocalZoom
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Size

@Composable
fun Geolocated(
    coordinates: Coordinates,
    offset: IntOffset? = null,
    minZoom: Int = 13,
    extension: Size? = null,
    content: @Composable (coordsOffset: IntOffset) -> Unit
) {
    val zoom = LocalZoom.current
    val converter = LocalConverter.current

    val coordsOffset = remember(coordinates, converter, offset) {
        val finalOffset = offset ?: IntOffset(0, 0)
        converter.convertToOffset(coordinates)
            .minus(finalOffset)
    }

    if (zoom >= minZoom) {
        if (converter.insideView(coordinates, extension)) {
            content(coordsOffset)
        }
    }
}

