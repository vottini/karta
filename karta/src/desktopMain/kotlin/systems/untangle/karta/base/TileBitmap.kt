package systems.untangle.karta.base

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

internal actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bytes = maybeGunzip()
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

internal actual fun readFileBytes(path: String): ByteArray? =
    try { java.io.File(path).readBytes() } catch (_: Exception) { null }

private fun ByteArray.maybeGunzip(): ByteArray {
    if (size < 2 || this[0] != 0x1F.toByte() || this[1] != 0x8B.toByte()) return this
    return java.util.zip.GZIPInputStream(inputStream()).use { it.readBytes() }
}
