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

    fun metersToPixels(distanceInMeters: Float) : Float {
        val angle = distanceInMeters / earthRadiusMeters
        return abs(angle * horizontalPixels * radiansToDegrees).toFloat()
    }
}
