package systems.untangle.karta.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier

import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders

import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.data.Size
import systems.untangle.karta.kartaTileSize
import systems.untangle.karta.network.TileServer

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.grid
import org.jetbrains.compose.resources.painterResource

@Composable
fun Tile(
    zoom: Int,
    xIndex: Int,
    yIndex: Int,
    center: DoubleOffset,
    viewSize: Size,
    tileServer: TileServer
) {
    val formattedUrl = remember(tileServer, zoom, xIndex, yIndex) {
        tileServer.tileUrl
            .replace("{zoom}", zoom.toString())
            .replace("{x}", xIndex.toString())
            .replace("{y}", yIndex.toString())
    }

    val xOffset = viewSize.halfWidth  + (xIndex - center.x) * kartaTileSize
    val yOffset = viewSize.halfHeight + (yIndex - center.y) * kartaTileSize

    val headers = NetworkHeaders.Builder()
    tileServer.requestHeaders.forEach { header ->
        headers[header.key] = header.value
    }

    Box(modifier = Modifier
        .offset { IntOffset(xOffset.toInt(), yOffset.toInt()) }
        .size(kartaTileSize.dp)
    ) {
        AsyncImage(
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            placeholder = painterResource(Res.drawable.grid),
            modifier = Modifier.height(kartaTileSize.dp).width(kartaTileSize.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(formattedUrl)
                .httpHeaders(headers.build())
                .build()
        )
    }
}
