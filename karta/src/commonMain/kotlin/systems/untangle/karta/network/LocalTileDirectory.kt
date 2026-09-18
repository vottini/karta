package systems.untangle.karta.network

/**
 * A local tile source whose tiles are laid out as `<path>/<zoom>/<x>/<y>.<extension>`.
 *
 * @property path Absolute path to the root directory containing the zoom-level subdirectories.
 * @property extension File extension of tile images, without the leading dot (default `"png"`).
 * @property tms When true, flips the Y axis to convert TMS coordinates (Y=0 at south, default
 *   for gdal2tiles without `--xyz`) to XYZ coordinates (Y=0 at north). Set this if your tiles
 *   were generated without the `--xyz` flag.
 */
data class LocalTileDirectory(
    val path: String,
    val extension: String = "png",
    val tms: Boolean = false
) : TileSource
