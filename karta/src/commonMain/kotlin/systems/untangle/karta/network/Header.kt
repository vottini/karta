package systems.untangle.karta.network

/**
 * A single HTTP request header attached to every tile request made by a [TileServer].
 *
 * @property key Header name, e.g. `"Authorization"`.
 * @property value Header value, e.g. `"Bearer <token>"`.
 */
data class Header(
    val key: String,
    val value: String
)
