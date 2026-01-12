package systems.untangle.karta.base

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.Text

import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders

import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.kartaTileSize
import systems.untangle.karta.network.TileServer

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
    tileServer: TileServer,
    xModulo: Int,
    displayBoundaries: Boolean = false
) {
    var wrappedXIndex = xIndex
    while (wrappedXIndex >= xModulo) wrappedXIndex -= xModulo
    while (wrappedXIndex < 0) wrappedXIndex += xModulo

    val formattedUrl = remember(tileServer, zoom, xIndex, yIndex) {
        tileServer.tileUrl
            .replace("{zoom}", zoom.toString())
            .replace("{x}", wrappedXIndex.toString())
            .replace("{y}", yIndex.toString())
    }

    val pixelDensity = LocalDensity.current.density
    val tileSize = kartaTileSize.toDp(pixelDensity)
    val xOffset = viewSize.halfWidth + ((xIndex - center.x) * tileSize.value).dp.toPx(pixelDensity)
    val yOffset = viewSize.halfHeight + ((yIndex - center.y) * tileSize.value).dp.toPx(pixelDensity)

    val headers = NetworkHeaders.Builder()
    tileServer.requestHeaders.forEach { header ->
        headers[header.key] = header.value
    }

    val offset = IntOffset(
        xOffset.value.toInt(),
        yOffset.value.toInt())

    Box(modifier = Modifier
        .offset { offset }
        .requiredSize(tileSize)
    ) {
        AsyncImage(
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            placeholder = painterResource(Res.drawable.grid),
            modifier = Modifier.height(tileSize).width(tileSize),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(formattedUrl)
                .httpHeaders(headers.build())
                .build()
        )
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
