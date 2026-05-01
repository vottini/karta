package systems.untangle.karta.network

/**
 * Configuration for an XYZ tile server.
 *
 * The URL template must contain the `{zoom}`, `{x}`, and `{y}` placeholders that Karta
 * replaces per tile, e.g. `"https://tile.openstreetmap.org/{zoom}/{x}/{y}.png"`.
 *
 * @property tileUrl URL template with `{zoom}`, `{x}`, and `{y}` placeholders.
 * @property requestHeaders Optional HTTP headers sent with every tile request — useful for
 *   `Authorization` tokens on private tile servers.
 */
data class TileServer(
    val tileUrl: String,
    val requestHeaders: List <Header> = listOf()
)
