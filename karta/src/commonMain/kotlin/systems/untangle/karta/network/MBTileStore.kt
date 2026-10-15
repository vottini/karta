package systems.untangle.karta.network

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import systems.untangle.karta.base.toImageBitmap
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sinh

internal object MBTileStore {
    private const val CACHE_SIZE = 200

    private data class TileKey(val path: String, val zoom: Int, val x: Int, val y: Int)

    private val mutex = Mutex()
    private val readers = mutableMapOf<String, MBTileReader>()
    private val cache = LruCache<TileKey, ImageBitmap>(CACHE_SIZE)
    private val metadataCache = mutableMapOf<String, MBTilesMetadata?>()

    suspend fun getMetadata(path: String): MBTilesMetadata? {
        mutex.withLock { if (metadataCache.containsKey(path)) return metadataCache[path] }
        val metadata = mutex.withLock {
            val reader = readers.getOrPut(path) { MBTileReader(path) }
            parseMBTilesMetadata(reader).also { metadataCache[path] = it }
        }
        return metadata
    }

    suspend fun getTile(path: String, zoom: Int, x: Int, tmsY: Int): ImageBitmap? {
        val metadata = getMetadata(path)
        if (metadata != null && !tileInCoverage(zoom, x, tmsY, metadata)) return null

        val key = TileKey(path, zoom, x, tmsY)
        mutex.withLock { cache[key] }?.let { return it }

        val bytes = mutex.withLock {
            readers.getOrPut(path) { MBTileReader(path) }.getTile(zoom, x, tmsY)
        } ?: return null

        val bitmap = bytes.toImageBitmap()
        mutex.withLock { cache[key] = bitmap }
        return bitmap
    }

    private fun tileInCoverage(zoom: Int, x: Int, tmsY: Int, metadata: MBTilesMetadata): Boolean {
        if (zoom < metadata.minZoom || zoom > metadata.maxZoom) return false

        val n = 1 shl zoom
        val xyzY = n - 1 - tmsY

        val tileWest = x.toDouble() / n * 360.0 - 180.0
        val tileEast = (x + 1).toDouble() / n * 360.0 - 180.0
        val tileNorth = atan(sinh(PI * (1.0 - 2.0 * xyzY / n))) * (180.0 / PI)
        val tileSouth = atan(sinh(PI * (1.0 - 2.0 * (xyzY + 1) / n))) * (180.0 / PI)

        val boundsNorth = metadata.bounds.topLeft.latitude
        val boundsSouth = metadata.bounds.bottomRight.latitude
        val boundsWest = metadata.bounds.topLeft.longitude
        val boundsEast = metadata.bounds.bottomRight.longitude

        return tileNorth > boundsSouth &&
               tileSouth < boundsNorth &&
               tileEast > boundsWest &&
               tileWest < boundsEast
    }
}
