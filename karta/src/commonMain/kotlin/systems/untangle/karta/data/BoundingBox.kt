package systems.untangle.karta.data

/**
 * A geographic bounding box defined by its northwest and southeast corners.
 *
 * The top-left corner has the larger latitude (north) and smaller longitude (west),
 * while the bottom-right corner has the smaller latitude (south) and larger longitude (east).
 *
 * Available via [systems.untangle.karta.base.LocalViewingBoundingBox] inside a
 * [systems.untangle.karta.Karta] `content` lambda to inspect the currently visible area.
 *
 * @property topLeft Northwest corner of the bounding box.
 * @property bottomRight Southeast corner of the bounding box.
 */
data class BoundingBox(
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    /** Absolute latitude span of the bounding box in decimal degrees. */
    val deltaLatitude: Double by lazy { topLeft.latitude - bottomRight.latitude }
    /** Absolute longitude span of the bounding box in decimal degrees. */
    val deltaLongitude: Double by lazy { topLeft.longitude - bottomRight.longitude }
}
