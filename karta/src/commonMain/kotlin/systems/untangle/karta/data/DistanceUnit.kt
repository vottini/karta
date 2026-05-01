package systems.untangle.karta.data

/**
 * Unit of distance used by overlay composables such as [systems.untangle.karta.composables.Circle].
 */
enum class DistanceUnit {
    /** Real-world meters. The pixel equivalent is recomputed whenever the zoom level changes. */
    METERS,
    /** Fixed screen pixels, unaffected by zoom. */
    PIXELS
}
