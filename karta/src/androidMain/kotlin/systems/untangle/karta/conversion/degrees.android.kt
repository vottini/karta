package systems.untangle.karta.conversion




actual fun latitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "N" else "S"
    val dms = value.toDMS()

    return "%02d°%02d'%02d''%s".format(
        dms.degrees, dms.minutes,
        dms.seconds.toInt(),
        hemisphere)
}

actual fun longitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "E" else "W"
    val dms = value.toDMS()

    return "%03d°%02d'%02d''%s".format(
        dms.degrees, dms.minutes,
        dms.seconds.toInt(),
        hemisphere)
}
