package systems.untangle.karta.network

/**
 * A local tile source whose tiles are laid out as `<path>/<zoom>/<x>/<y>.<extension>`,
 * matching the XYZ output of tools like gdal2tiles (`--xyz` flag).
 *
 * @property path Absolute path to the root directory containing the zoom-level subdirectories.
 * @property extension File extension of tile images, without the leading dot (default `"png"`).
 */
data class LocalTileDirectory(
    val path: String,
    val extension: String = "png"
) : TileSource
