package systems.untangle.karta.base

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun ByteArray.toImageBitmap(): ImageBitmap
internal expect fun readFileBytes(path: String): ByteArray?
