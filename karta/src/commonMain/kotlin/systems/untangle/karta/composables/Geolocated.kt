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
    val coordsOffsets = remember(coordinates, converter, offset) {
        val offsets = mutableListOf<IntOffset>()
        val finalOffset = offset ?: IntOffset(0, 0)
        val turns = listOf(0, -360, 360)

        turns.forEach { turn ->
            val turnCoords = coordinates.copy(longitude = coordinates.longitude - turn)
            val offset = converter.convertToOffset(turnCoords).minus(finalOffset)
            if (converter.insideView(coordinates, extension)) {
                offsets.add(offset)
            }
        }

        offsets
    }

    coordsOffsets.forEach { coords ->
        content(coords.correctedPx())
    }
}

