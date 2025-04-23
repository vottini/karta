package systems.untangle.karta.data

data class Region(
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    val deltaLatitude: Double by lazy { topLeft.latitude - bottomRight.latitude }
    val deltaLongitude: Double by lazy { topLeft.longitude - bottomRight.longitude }
}
