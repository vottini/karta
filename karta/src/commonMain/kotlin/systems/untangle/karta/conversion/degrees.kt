package systems.untangle.karta.conversion

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign

/**
 * Degrees-minutes-seconds decomposition of a decimal-degree angle.
 *
 * @property signal Sign of the original value: `1` for positive, `-1` for negative.
 * @property degrees Whole-degree component (unsigned).
 * @property minutes Whole-minute component (0–59).
 * @property seconds Fractional seconds component (0–60).
 */
data class DMS(
    val signal: Int,
    val degrees: Int,
    val minutes: Int,
    val seconds: Double
)

/**
 * Converts this decimal-degree value to a [DMS] (degrees-minutes-seconds) decomposition.
 */
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

/**
 * Formats a decimal-degree latitude as a DMS string with N/S hemisphere indicator,
 * e.g. `48°51'23"N` or `48°51'23.4"N` when [decimalSeconds] is `true`.
 *
 * @param value Latitude in decimal degrees.
 * @param decimalSeconds When `true`, one decimal digit of seconds is included (e.g. `23.4"`).
 *   Defaults to `false` for whole seconds.
 */
fun latitudeDMS(value: Double, decimalSeconds: Boolean = false): String {
    val hemisphere = if (value > 0) "N" else "S"
    val dms = value.toDMS()
    val secondsStr = if (decimalSeconds) "%04.1f".format(dms.seconds)
                     else "%02d".format(dms.seconds.toInt())

    return "%02d°%02d'%s\"%s".format(dms.degrees, dms.minutes, secondsStr, hemisphere)
}

/**
 * Formats a decimal-degree longitude as a DMS string with E/W hemisphere indicator,
 * e.g. `002°21'07"E` or `002°21'07.3"E` when [decimalSeconds] is `true`.
 *
 * @param value Longitude in decimal degrees.
 * @param decimalSeconds When `true`, one decimal digit of seconds is included (e.g. `07.3"`).
 *   Defaults to `false` for whole seconds.
 */
fun longitudeDMS(value: Double, decimalSeconds: Boolean = false): String {
    val hemisphere = if (value > 0) "E" else "W"
    val dms = value.toDMS()
    val secondsStr = if (decimalSeconds) "%04.1f".format(dms.seconds)
                     else "%02d".format(dms.seconds.toInt())

    return "%03d°%02d'%s\"%s".format(dms.degrees, dms.minutes, secondsStr, hemisphere)
}
