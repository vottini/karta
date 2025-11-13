package systems.untangle.karta.network

data class TileServer(
    val tileUrl: String,
    val requestHeaders: List <Header> = listOf()
)
