package systems.untangle.karta.conversion

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign

data class DMS(
    val signal: Int,
    val degrees: Int,
    val minutes: Int,
    val seconds: Double
)

fun Double.toDMS(): DMS {
    val absDegrees = abs(this)
    val degrees = floor(absDegrees)
    val decimalMinutes = (absDegrees - degrees) * 60.0
    val minutes = floor(decimalMinutes)
    val seconds = (decimalMinutes - minutes) * 60.0
    val signal = sign(this)

    return DMS(
        signal.toInt(),
        degrees.toInt(),
        minutes.toInt(),
        seconds
    )
}

fun latitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "N" else "S"
    val dms = value.toDMS()

    return "%02d°%02d'%02d''%s".format(
        dms.degrees, dms.minutes,
        dms.seconds.toInt(),
        hemisphere)
}

fun longitudeDMS(value: Double): String {
    val hemisphere = if (value > 0) "E" else "W"
    val dms = value.toDMS()

    return "%03d°%02d'%02d''%s".format(
        dms.degrees, dms.minutes,
        dms.seconds.toInt(),
        hemisphere)
}
