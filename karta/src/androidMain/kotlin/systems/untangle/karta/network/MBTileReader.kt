package systems.untangle.karta.network

import android.database.sqlite.SQLiteDatabase

internal actual class MBTileReader actual constructor(path: String) {
    private val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)

    actual fun getTile(zoom: Int, x: Int, y: Int): ByteArray? {
        val cursor = db.rawQuery(
            "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?",
            arrayOf(zoom.toString(), x.toString(), y.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.getBlob(0) else null }
    }

    actual fun getMetadataValue(name: String): String? {
        val cursor = db.rawQuery(
            "SELECT value FROM metadata WHERE name=?",
            arrayOf(name)
        )
        return cursor.use { if (it.moveToFirst()) it.getString(0) else null }
    }

    actual fun close() = db.close()
}
