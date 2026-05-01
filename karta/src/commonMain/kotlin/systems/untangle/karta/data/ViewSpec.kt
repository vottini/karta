package systems.untangle.karta.data

/**
 * Describes a desired map view for programmatic pan/zoom via [systems.untangle.karta.Karta]'s
 * `viewFlow` parameter.
 *
 * Both fields are nullable so you can change only one at a time — emit
 * `ViewSpec(zoom = 15)` to zoom without moving the center, or
 * `ViewSpec(centerCoordinates = paris)` to pan without changing the zoom.
 *
 * @property centerCoordinates Target map center, or `null` to keep the current center.
 * @property zoom Target zoom level, or `null` to keep the current zoom.
 */
data class ViewSpec(
    val centerCoordinates: Coordinates? = null,
    val zoom: Int? = null
)
