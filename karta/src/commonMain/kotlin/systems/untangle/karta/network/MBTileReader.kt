package systems.untangle.karta.network

internal expect class MBTileReader(path: String) {
    fun getTile(zoom: Int, x: Int, y: Int): ByteArray?
    fun getMetadataValue(name: String): String?
    fun close()
}
