package systems.untangle.karta.network

import java.sql.DriverManager

internal actual class MBTileReader actual constructor(path: String) {
    private val connection = DriverManager.getConnection("jdbc:sqlite:$path")

    actual fun getTile(zoom: Int, x: Int, y: Int): ByteArray? {
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

    actual fun getMetadataValue(name: String): String? {
        val stmt = connection.prepareStatement("SELECT value FROM metadata WHERE name=?")
        return stmt.use { ps ->
            ps.setString(1, name)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    actual fun close() = connection.close()
}
