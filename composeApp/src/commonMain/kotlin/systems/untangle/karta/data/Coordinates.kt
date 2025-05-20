package systems.untangle.karta.data

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun minus(other: Coordinates) = Coordinates(
        other.latitude - latitude,
        other.longitude - longitude
    )

    fun plus(other: Coordinates) = Coordinates(
        other.latitude + latitude,
        other.longitude + longitude
    )
}
