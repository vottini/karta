package systems.untangle.karta.conversion

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.DoubleOffset
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.ln
import kotlin.math.atan
import kotlin.math.sinh

fun convertToLatLong(zoom: Int, tileOffset: DoubleOffset) : Coordinates {
    val numberOfTiles = 2.0.pow(zoom)
    val longitude = (tileOffset.x / numberOfTiles) * 360.0 - 180.0
    val latRadians = atan(sinh(PI * (1.0 - (2.0 * tileOffset.y / numberOfTiles))))
    val latitude = (latRadians * 180.0) / PI
    return Coordinates(latitude, longitude)
}

fun convertToTileCoordinates(zoom: Int, coordinates: Coordinates) : DoubleOffset {
    val latitudeRads = (coordinates.latitude / 180.0) * PI
    val secLatitude = 1.0 / cos(latitudeRads)
    val tanLatitude = tan(latitudeRads)

    val numberOfTiles = 2.0.pow(zoom)
    val xOffset = numberOfTiles * ((coordinates.longitude + 180.0) / 360.0)
    val yOffset = numberOfTiles * (1.0 - (ln(tanLatitude + secLatitude) / PI)) / 2.0

    return DoubleOffset(
        xOffset,
        yOffset
    )
}
