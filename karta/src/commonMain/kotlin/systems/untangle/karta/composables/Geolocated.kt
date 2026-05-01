package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.base.LocalConverter
import systems.untangle.karta.conversion.correctedPx
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize

/*
 *    window origin
 *        (0,0)
 *          *
 *          |
 *          |        offset
 *   /      '----------*-------------,
 *   |                 |             |
 *   |  extension      |             |
 *   |  height         |             |
 *   |                 |             |
 *   |                 |             |
 *   /                 '-------------'
 *
 *                     /-------------/
 *                        extension
 *                         width
 *
 */

/**
 * Converts geographic [coordinates] to screen-space pixel offsets and invokes [content] for
 * each visible instance, including world-wrap duplicates when [wrapLongitude] is `true`.
 *
 * This is a low-level primitive used internally by [Marker] and [Circle]. Prefer those
 * composables for common use-cases; use [Geolocated] only when you need raw pixel offsets
 * to position a fully custom overlay.
 *
 * [content] may be called zero times (coordinate outside viewport) or up to three times (center,
 * −360°, +360° copies) depending on [wrapLongitude] and the current viewport bounds.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coordinates Geographic position to project to screen coordinates.
 * @param offset Optional pixel offset subtracted from the computed screen position, useful for
 *   nudging the content relative to the projected point.
 * @param extension Optional size used for viewport culling. When provided, the content is
 *   included even if [coordinates] itself is slightly outside the viewport, as long as a box
 *   of this size centered on the projected point still intersects the viewport.
 * @param wrapLongitude When `true`, [content] is also invoked for the −360° and +360° longitude
 *   copies of [coordinates] so that overlays remain visible during continuous horizontal panning.
 * @param content Called with the screen-space [IntOffset] for each visible instance.
 */
@Composable
fun Geolocated(
    coordinates: Coordinates,
    offset: IntOffset? = null,
    extension: PxSize? = null,
    wrapLongitude: Boolean = true,
    content: @Composable (coordsOffset: IntOffset) -> Unit
) {
    val converter = LocalConverter.current
    val coordsOffsets = remember(coordinates, converter, offset) {
        val finalOffset = offset ?: IntOffset(0, 0)

        val offsets = mutableListOf<IntOffset>()
        val turns = if (wrapLongitude) listOf(0, -360, 360)
            else listOf(0)

        turns.forEach { turn ->
            val turnCoords = coordinates.copy(longitude = coordinates.longitude + turn)
            val offset = converter.convertToOffset(turnCoords).minus(finalOffset)
            if (converter.insideView(turnCoords, extension)) {
                offsets.add(offset)
            }
        }

        offsets
    }

    coordsOffsets.forEach { coords ->
        content(coords.correctedPx())
    }
}

