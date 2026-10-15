package systems.untangle.karta.network

internal interface MBTileReader {
    fun getTile(zoom: Int, x: Int, y: Int): ByteArray?
    fun getMetadataValue(name: String): String?
    fun close()
}

internal expect fun createMBTileReader(path: String): MBTileReader
