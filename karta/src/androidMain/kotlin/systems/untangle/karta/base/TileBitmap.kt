package systems.untangle.karta.base

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bytes = maybeGunzip()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}

private fun ByteArray.maybeGunzip(): ByteArray {
    if (size < 2 || this[0] != 0x1F.toByte() || this[1] != 0x8B.toByte()) return this
    return java.util.zip.GZIPInputStream(inputStream()).use { it.readBytes() }
}
