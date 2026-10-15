package systems.untangle.karta.network

import java.sql.DriverManager

private class DesktopMBTileReader(path: String) : MBTileReader {
    private val connection = DriverManager.getConnection("jdbc:sqlite:$path")

    override fun getTile(zoom: Int, x: Int, y: Int): ByteArray? {
        val stmt = connection.prepareStatement(
            "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?"
        )
        return stmt.use { ps ->
            ps.setInt(1, zoom)
            ps.setInt(2, x)
            ps.setInt(3, y)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getBytes(1) else null }
        }
    }

    override fun getMetadataValue(name: String): String? {
        val stmt = connection.prepareStatement("SELECT value FROM metadata WHERE name=?")
        return stmt.use { ps ->
            ps.setString(1, name)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    override fun close() = connection.close()
}

internal actual fun createMBTileReader(path: String): MBTileReader = DesktopMBTileReader(path)
