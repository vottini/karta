package systems.untangle.karta.network

import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.Coordinates

/** Tile format declared in the MBTiles metadata table. */
enum class MBTilesFormat {
    PNG, JPEG, WEBP,
    /** Vector tiles — not renderable by Karta. */
    PBF,
    UNKNOWN;

    val isRaster: Boolean get() = this == PNG || this == JPEG || this == WEBP
}

/**
 * Metadata read from an MBTiles file's `metadata` table.
 *
 * @property name Tileset name.
 * @property description Human-readable description, if present.
 * @property format Tile encoding format.
 * @property bounds Geographic extent of the tileset (WGS84).
 * @property minZoom Minimum zoom level stored in the file.
 * @property maxZoom Maximum zoom level stored in the file.
 * @property center Suggested default view center, if present.
 * @property type `"baselayer"` or `"overlay"`, if declared.
 * @property generator Tool that created the file, if declared.
 */
data class MBTilesMetadata(
    val name: String?,
    val description: String?,
    val format: MBTilesFormat,
    val bounds: BoundingBox,
    val minZoom: Int,
    val maxZoom: Int,
    val center: Coordinates? = null,
    val type: String? = null,
    val generator: String? = null
)

/** Reads metadata from an MBTiles file. Returns null if required fields (`bounds`, `minzoom`, `maxzoom`) are absent. */
suspend fun readMBTilesMetadata(path: String): MBTilesMetadata? = MBTileStore.getMetadata(path)

internal fun parseMBTilesMetadata(reader: MBTileReader): MBTilesMetadata? {
    val boundsRaw = reader.getMetadataValue("bounds") ?: return null
    val parts = boundsRaw.split(",").map { it.trim().toDoubleOrNull() ?: return null }
    if (parts.size < 4) return null
    val west = parts[0]; val south = parts[1]; val east = parts[2]; val north = parts[3]

    val minZoom = reader.getMetadataValue("minzoom")?.toIntOrNull() ?: return null
    val maxZoom = reader.getMetadataValue("maxzoom")?.toIntOrNull() ?: return null

    val format = when (reader.getMetadataValue("format")?.lowercase()) {
        "png"  -> MBTilesFormat.PNG
        "jpg", "jpeg" -> MBTilesFormat.JPEG
        "webp" -> MBTilesFormat.WEBP
        "pbf"  -> MBTilesFormat.PBF
        else   -> MBTilesFormat.UNKNOWN
    }

    val center = reader.getMetadataValue("center")?.let { raw ->
        val c = raw.split(",").map { it.trim().toDoubleOrNull() }
        if (c.size >= 2 && c[0] != null && c[1] != null)
            Coordinates(latitude = c[1]!!, longitude = c[0]!!)
        else null
    }

    return MBTilesMetadata(
        name = reader.getMetadataValue("name"),
        description = reader.getMetadataValue("description"),
        format = format,
        bounds = BoundingBox(
            topLeft = Coordinates(latitude = north, longitude = west),
            bottomRight = Coordinates(latitude = south, longitude = east)
        ),
        minZoom = minZoom,
        maxZoom = maxZoom,
        center = center,
        type = reader.getMetadataValue("type"),
        generator = reader.getMetadataValue("generator")
    )
}
