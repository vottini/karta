package systems.untangle.karta.data

/**
 * A geographic coordinate expressed in decimal degrees.
 *
 * @property latitude Latitude in decimal degrees, in the range `[-90, 90]`.
 * @property longitude Longitude in decimal degrees, in the range `[-180, 180]`.
 *   Values outside this range are valid internally but should be normalized with
 *   [systems.untangle.karta.conversion.wrapLongitude] before display.
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Returns the signed difference `other − this` as a [Coordinates].
     * Useful for computing the offset between two geographic positions.
     */
    fun minus(other: Coordinates) = Coordinates(
        other.latitude - latitude,
        other.longitude - longitude
    )

    /**
     * Returns the element-wise sum of this and [other] as a [Coordinates].
     * Useful for applying a coordinate offset computed with [minus].
     */
    fun plus(other: Coordinates) = Coordinates(
        other.latitude + latitude,
        other.longitude + longitude
    )
}
