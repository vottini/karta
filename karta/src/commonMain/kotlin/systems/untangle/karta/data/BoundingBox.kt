package systems.untangle.karta.data

/**
 * Defines a rectangle corresponding to a
 * region in degrees coordinates. The
 * rectangle is defined by its top left degree coordinates and
 * its bottom right degrees coordinates.
 */

data class BoundingBox(
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    val deltaLatitude: Double by lazy { topLeft.latitude - bottomRight.latitude }
    val deltaLongitude: Double by lazy { topLeft.longitude - bottomRight.longitude }
}
