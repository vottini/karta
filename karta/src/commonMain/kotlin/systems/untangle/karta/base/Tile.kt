package systems.untangle.karta.base

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.Text

import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.kartaTileSize
import systems.untangle.karta.network.LocalTileDirectory
import systems.untangle.karta.network.LocalTileStore
import systems.untangle.karta.network.MBTilesSource
import systems.untangle.karta.network.MBTileStore
import systems.untangle.karta.network.TileServer
import systems.untangle.karta.network.TileSource

import androidx.compose.ui.platform.LocalDensity
import systems.untangle.karta.conversion.toDp
import systems.untangle.karta.conversion.toPx
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.plus

import org.jetbrains.compose.resources.painterResource
import systems.untangle.karta.resources.Res
import systems.untangle.karta.resources.grid

/**
 * Individual tile component.
 * They are instantiated and panned around by KMap,
 * don't use them directly.
 */

@Composable
fun Tile(
    zoom: Int,
    xIndex: Int,
    yIndex: Int,
    center: DoubleOffset,
    viewSize: PxSize,
    tileSource: TileSource,
    xModulo: Int,
    alpha: Float = 1f,
    displayBoundaries: Boolean = false
) {
    var wrappedXIndex = xIndex
    while (wrappedXIndex >= xModulo) wrappedXIndex -= xModulo
    while (wrappedXIndex < 0) wrappedXIndex += xModulo

    val resolvedYIndex = remember(tileSource, xModulo, yIndex) {
        val tms = when (tileSource) {
            is TileServer -> tileSource.tms
            is LocalTileDirectory -> tileSource.tms
            is MBTilesSource -> true
        }

        when (tms) {
            true -> (xModulo - 1) - yIndex
            false -> yIndex
        }
    }

    val gridPainter = painterResource(Res.drawable.grid)
    val pixelDensity = LocalDensity.current.density
    val tileSize = kartaTileSize.toDp(pixelDensity)
    val xOffset = viewSize.halfWidth + ((xIndex - center.x) * tileSize.value).dp.toPx(pixelDensity)
    val yOffset = viewSize.halfHeight + ((yIndex - center.y) * tileSize.value).dp.toPx(pixelDensity)

    val offset = IntOffset(
        xOffset.value.toInt(),
        yOffset.value.toInt())

    val context = LocalPlatformContext.current
    val imageModel = remember(tileSource, zoom, xIndex, yIndex) {
        if (tileSource is TileServer) {
            val url = tileSource.tileUrl
                .replace("{zoom}", zoom.toString())
                .replace("{x}", wrappedXIndex.toString())
                .replace("{y}", resolvedYIndex.toString())
            val headers = NetworkHeaders.Builder()
            tileSource.requestHeaders.forEach { header -> headers[header.key] = header.value }
            ImageRequest.Builder(context)
                .data(url)
                .httpHeaders(headers.build())
                .build()
        } else null
    }

    val localFilePath = remember(tileSource, zoom, xIndex, yIndex) {
        when (tileSource) {
            is LocalTileDirectory -> "${tileSource.path}/$zoom/$wrappedXIndex/$resolvedYIndex.${tileSource.extension}"
            else -> null
        }
    }

    val localBitmap: MutableState<ImageBitmap?> = remember(tileSource, zoom, xIndex, yIndex) {
        when {
            tileSource is LocalTileDirectory && localFilePath != null ->
                LocalTileStore.getTileState(localFilePath)
            tileSource is MBTilesSource ->
                mutableStateOf(MBTileStore.peekTile(tileSource.path, zoom, wrappedXIndex, resolvedYIndex))
            else -> mutableStateOf<ImageBitmap?>(null)
        }
    }

    val localBitmapChecked: MutableState<Boolean> = remember(tileSource, zoom, xIndex, yIndex) {
        when {
            tileSource is LocalTileDirectory && localFilePath != null ->
                LocalTileStore.getTileCheckedState(localFilePath)
            else -> mutableStateOf(localBitmap.value != null)
        }
    }

    LaunchedEffect(tileSource, zoom, xIndex, yIndex) {
        if (localBitmapChecked.value) return@LaunchedEffect
        val bitmap = withContext(NonCancellable + Dispatchers.IO) {
            when (tileSource) {
                is MBTilesSource -> MBTileStore.getTile(tileSource.path, zoom, wrappedXIndex, resolvedYIndex)
                is LocalTileDirectory -> localFilePath?.let { LocalTileStore.getTile(it) }
                else -> null
            }
        }
        localBitmap.value = bitmap
        localBitmapChecked.value = true
    }

    Box(modifier = Modifier
        .offset { offset }
        .requiredSize(tileSize)
        .graphicsLayer { this.alpha = alpha }
    ) {
        if (tileSource is TileServer) {
            AsyncImage(
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                placeholder = gridPainter,
                modifier = Modifier.height(tileSize).width(tileSize),
                model = imageModel
            )
        } else {
            val bitmap = localBitmap.value
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.height(tileSize).width(tileSize)
                )
            } else if (!localBitmapChecked.value) {
                Image(
                    painter = gridPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.height(tileSize).width(tileSize)
                )
            }
        }
    }

    if (displayBoundaries) {
        Box(modifier = Modifier.offset { offset }) {
            Text(text = "$xIndex,$yIndex")
        }

        Canvas(modifier = Modifier.offset { offset }) {
            drawRect(
                color = Color.Black,
                size = Size(kartaTileSize.value, kartaTileSize.value),
                style = Stroke(1.0f),
            )
        }
    }

}
