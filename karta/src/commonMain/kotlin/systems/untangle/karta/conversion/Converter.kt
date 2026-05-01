package systems.untangle.karta.conversion

import kotlin.math.PI
import kotlin.math.abs
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.intersects
import systems.untangle.karta.data.toIntOffset
import systems.untangle.karta.kartaTileSize

const val earthRadiusMeters = 6378137.0
const val radiansToDegrees = 180.0 / PI

/**
 * Converts between geographic [Coordinates], tile offsets, and screen-pixel [IntOffset]s at a
 * fixed zoom level and viewport.
 *
 * A new instance is created whenever the viewport size, center, or zoom changes. The current
 * instance is available inside a [systems.untangle.karta.Karta] `content` lambda via
 * [systems.untangle.karta.base.LocalConverter].
 */
class Converter(
    private val viewingBoundingBox: BoundingBox,
    private val viewPxSize: PxSize,
    private val center: DoubleOffset,
    private val zoomLevel: Int
) {
    private val horizontalPixels = viewPxSize.width.value / viewingBoundingBox.deltaLongitude

    val tileRegion by lazy {
        TileRegion(
            convertToOffset(viewingBoundingBox.topLeft),
            convertToOffset(viewingBoundingBox.bottomRight)
        )
    }

    /**
     * Converts geographic [coordinates] to a screen-pixel [IntOffset] relative to the top-left
     * corner of the map viewport at the current zoom and center.
     */
    fun convertToOffset(coordinates: Coordinates) : IntOffset {
        val tileCoords = convertToTileCoordinates(zoomLevel, coordinates)
        val offsetFromCenter = tileCoords.minus(center)
            .scale(kartaTileSize.value.toDouble())

        val centerOffset = DoubleOffset(
            viewPxSize.halfWidth.value.toDouble(),
            viewPxSize.halfHeight.value.toDouble()
        )

        return IntOffset(
            (offsetFromCenter.x + centerOffset.x).toInt(),
            (offsetFromCenter.y + centerOffset.y).toInt()
        )
    }

    /**
     * Returns `true` when [coords] is visible in the current viewport.
     *
     * When [extension] is provided, the test uses a box of that size centered on the projected
     * point instead of the exact coordinate, so elements that overlap the edge are still included.
     */
    fun insideView(coords: Coordinates, extension: PxSize?) : Boolean {
        if (null != extension) {
            val apothems = extension.div(2).toIntOffset()
            val offset = convertToOffset(coords)

            return tileRegion.intersects(
                TileRegion(
                    offset - apothems,
                    offset + apothems
                )
            )
        }

        val (topLeft, bottomRight) = viewingBoundingBox

        return (
            (coords.latitude in bottomRight.latitude..topLeft.latitude) &&
            (coords.longitude in topLeft.longitude..bottomRight.longitude)
        )
    }

    /**
     * Converts a real-world distance in meters to screen pixels at the current zoom and latitude,
     * using the equirectangular approximation.
     *
     * @param distanceInMeters Distance in meters to convert.
     * @return Equivalent distance in screen pixels.
     */
    fun metersToPixels(distanceInMeters: Float) : Float {
        val angle = distanceInMeters / earthRadiusMeters
        return abs(angle * horizontalPixels * radiansToDegrees).toFloat()
    }
}
