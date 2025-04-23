package systems.untangle.karta.conversion

import kotlin.math.PI
import kotlin.math.abs
import androidx.compose.ui.unit.IntOffset
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Region
import systems.untangle.karta.data.Size
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.intersects

const val earthRadiusMeters = 6378137.0
const val radiansToDegrees = 180.0 / PI

class Converter(val viewingRegion: Region, viewSize: Size) {
    val horizontalPixelDensity = viewSize.width.toDouble() / viewingRegion.deltaLongitude
    val verticalPixelDensity = viewSize.height.toDouble() / viewingRegion.deltaLatitude

    val tileRegion by lazy {
        TileRegion(
            convertToOffset(viewingRegion.topLeft),
            convertToOffset(viewingRegion.bottomRight)
        )
    }

    fun convertToOffset(coordinates: Coordinates) : IntOffset {
        val origin = viewingRegion.topLeft
        val diff = coordinates.minus(origin)

        return IntOffset(
            (diff.longitude * horizontalPixelDensity).toInt(),
            (diff.latitude * verticalPixelDensity).toInt()
        )
    }

    fun insideView(coords: Coordinates, extension: Size?) : Boolean {
        if (null != extension) {
            val apothems = extension.div(2)
            val offset = convertToOffset(coords)

            return tileRegion.intersects(
                TileRegion(
                    IntOffset(offset.x - apothems.width, offset.y - apothems.height),
                    IntOffset(offset.x + apothems.width, offset.y + apothems.height)
                )
            )
        }

        val (topLeft, bottomRight) = viewingRegion

        return (
            (coords.latitude in bottomRight.latitude..topLeft.latitude) &&
            (coords.longitude in topLeft.longitude..bottomRight.longitude)
        )
    }

    fun metersToPixels(distanceInMeters: Float) : Float {
        val angle = distanceInMeters / earthRadiusMeters
        return abs(angle * horizontalPixelDensity * radiansToDegrees).toFloat()
    }
}
