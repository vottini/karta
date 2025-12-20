package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.base.LocalConverter
import systems.untangle.karta.conversion.correctedPx
import systems.untangle.karta.conversion.longitudeDMS
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize

@Composable
fun Geolocated(
    coordinates: Coordinates,
    offset: IntOffset? = null,
    extension: PxSize? = null,
    content: @Composable (coordsOffset: IntOffset) -> Unit
) {
    val converter = LocalConverter.current
    converter.tileRegion.topLeft.component2()
    val coordsOffset = remember(coordinates, converter, offset) {
        val finalOffset = offset ?: IntOffset(0, 0)
        converter.convertToOffset(coordinates)
            .minus(finalOffset)
    }

    if (converter.insideView(coordinates, extension)) {
        content(coordsOffset.correctedPx())
    }

    val leftCoordinates = coordinates.copy(longitude = coordinates.longitude - 360.0)
    val leftCoordsOffset = remember(leftCoordinates, converter, offset) {
        val finalOffset = offset ?: IntOffset(0, 0)
        converter.convertToOffset(leftCoordinates)
            .minus(finalOffset)
    }

    if (converter.insideView(leftCoordinates, extension)) {
        content(leftCoordsOffset.correctedPx())
    }
}

