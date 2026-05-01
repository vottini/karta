package systems.untangle.karta.input

import androidx.compose.ui.geometry.Offset
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.TileRegion

/**
 * The geographic and screen-pixel position of a pointer event.
 *
 * @property coordinates Geographic location (latitude/longitude) under the pointer.
 * @property offset Screen-space pixel position relative to the top-left corner of the map.
 */
data class PointerPosition(
    val coordinates: Coordinates,
    val offset: Offset
)

val outsideScreen = PointerPosition(
    Coordinates(Double.NaN, Double.NaN),
    Offset(Float.NaN, Float.NaN)
)

fun PointerPosition.isOutsideScreen() = this === outsideScreen
fun PointerPosition.isInside(tileRegion: TileRegion) : Boolean {
    val (x, y) = this.offset

    return (
        x.toInt() in tileRegion.topLeft.x..tileRegion.bottomRight.x &&
        y.toInt() in tileRegion.topLeft.y..tileRegion.bottomRight.y
    )
}
