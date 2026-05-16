package systems.untangle.karta.network

/**
 * A tile source paired with an opacity value for layer compositing.
 *
 * @property source The tile source — either a [TileServer] or a [LocalTileDirectory].
 * @property alpha Opacity of this layer in the range `[0f, 1f]` (default `1f` — fully opaque).
 */
data class TileLayer(
    val source: TileSource,
    val alpha: Float = 1f
)
