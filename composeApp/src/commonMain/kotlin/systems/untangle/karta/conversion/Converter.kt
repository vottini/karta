package systems.untangle.karta.conversion

import kotlin.math.PI
import kotlin.math.abs
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Region
import systems.untangle.karta.data.Size
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.intersects

const val earthRadiusMeters = 6378137.0
const val radiansToDegrees = 180.0 / PI

class Converter(
    private val viewingRegion: Region,
    private val viewSize: Size,
    private val pixelDensity: Float
) {
    private val horizontalPxDelta = (viewSize.width / pixelDensity) / viewingRegion.deltaLongitude
    private val verticalPxDelta = (viewSize.height / pixelDensity) / viewingRegion.deltaLatitude

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
            (diff.longitude * horizontalPxDelta).toInt(),
            (diff.latitude * verticalPxDelta).toInt()
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
        val totalPixels = horizontalPxDelta * pixelDensity
        return abs(angle * totalPixels * radiansToDegrees).toFloat()
    }
}
