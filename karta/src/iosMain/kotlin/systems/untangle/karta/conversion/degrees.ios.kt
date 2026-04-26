package systems.untangle.karta.conversion



actual fun latitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "N" else "S"
    val dms = value.toDMS()

    return "${dms.degrees}°${dms.minutes}'${dms.seconds.toInt()}''${hemisphere}"
}

actual fun longitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "E" else "W"
    val dms = value.toDMS()

    return "${dms.degrees}°${dms.minutes}'${dms.seconds.toInt()}''${hemisphere}"
}
