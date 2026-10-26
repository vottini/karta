package systems.untangle.karta.network

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import systems.untangle.karta.base.readFileBytes
import systems.untangle.karta.base.toImageBitmap

internal object LocalTileStore {
    private const val CACHE_SIZE = 200

    private val mutex = Mutex()
    private val cache = LruCache<String, ImageBitmap>(CACHE_SIZE)
    private val states = HashMap<String, MutableState<ImageBitmap?>>()
    private val checkedStates = HashMap<String, MutableState<Boolean>>()

    @Synchronized
    fun getTileState(filePath: String): MutableState<ImageBitmap?> =
        states.getOrPut(filePath) { mutableStateOf(cache.peek(filePath)) }

    @Synchronized
    fun getTileCheckedState(filePath: String): MutableState<Boolean> =
        checkedStates.getOrPut(filePath) { mutableStateOf(cache.peek(filePath) != null) }

    @Synchronized
    private fun updateStates(filePath: String, bitmap: ImageBitmap?) {
        if (bitmap != null) states[filePath]?.value = bitmap
        checkedStates[filePath]?.value = true
    }

    suspend fun getTile(filePath: String): ImageBitmap? {
        mutex.withLock { cache[filePath] }?.let { return it }

        val bytes = readFileBytes(filePath)?.takeIf { it.isNotEmpty() } ?: run {
            updateStates(filePath, null)
            return null
        }

        val bitmap = try {
            bytes.toImageBitmap()
        } catch (_: Exception) {
            return null
        }

        mutex.withLock { cache[filePath] = bitmap }
        updateStates(filePath, bitmap)
        return bitmap
    }
}
