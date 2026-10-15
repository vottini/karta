package systems.untangle.karta.network

import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.Coordinates

/**
 * Geographic and zoom coverage metadata read from an MBTiles file.
 *
 * @property bounds Geographic extent of the tileset (WGS84).
 * @property minZoom Minimum zoom level stored in the file.
 * @property maxZoom Maximum zoom level stored in the file.
 * @property center Suggested default view, if present in the file.
 */
data class MBTilesMetadata(
    val bounds: BoundingBox,
    val minZoom: Int,
    val maxZoom: Int,
    val center: Coordinates? = null
)

/** Reads coverage metadata from an MBTiles file. Returns null if required fields are absent. */
suspend fun readMBTilesMetadata(path: String): MBTilesMetadata? = MBTileStore.getMetadata(path)

internal fun parseMBTilesMetadata(reader: MBTileReader): MBTilesMetadata? {
    val boundsRaw = reader.getMetadataValue("bounds") ?: return null
    val parts = boundsRaw.split(",").map { it.trim().toDoubleOrNull() ?: return null }
    if (parts.size < 4) return null
    val west = parts[0]; val south = parts[1]; val east = parts[2]; val north = parts[3]

    val minZoom = reader.getMetadataValue("minzoom")?.toIntOrNull() ?: return null
    val maxZoom = reader.getMetadataValue("maxzoom")?.toIntOrNull() ?: return null

    val center = reader.getMetadataValue("center")?.let { raw ->
        val c = raw.split(",").map { it.trim().toDoubleOrNull() }
        if (c.size >= 2 && c[0] != null && c[1] != null)
            Coordinates(latitude = c[1]!!, longitude = c[0]!!)
        else null
    }

    return MBTilesMetadata(
        bounds = BoundingBox(
            topLeft = Coordinates(latitude = north, longitude = west),
            bottomRight = Coordinates(latitude = south, longitude = east)
        ),
        minZoom = minZoom,
        maxZoom = maxZoom,
        center = center
    )
}
