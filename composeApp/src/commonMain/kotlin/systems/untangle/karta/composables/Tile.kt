package systems.untangle.karta.composables

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

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.grid
import org.jetbrains.compose.resources.painterResource

import androidx.compose.ui.platform.LocalDensity
import systems.untangle.karta.conversion.dpToPx

@Composable
fun Tile(
    zoom: Int,
    xIndex: Int,
    yIndex: Int,
    center: DoubleOffset,
    viewSize: systems.untangle.karta.data.Size,
    tileServer: TileServer
) {
    val formattedUrl = remember(tileServer, zoom, xIndex, yIndex) {
        tileServer.tileUrl
            .replace("{zoom}", zoom.toString())
            .replace("{x}", xIndex.toString())
            .replace("{y}", yIndex.toString())
    }

    val pixelDensity = LocalDensity.current.density
    val xOffset = ((viewSize.halfWidth / pixelDensity) + (xIndex - center.x) * kartaTileSize).dp
    val yOffset = ((viewSize.halfHeight / pixelDensity) + (yIndex - center.y) * kartaTileSize).dp

    val headers = NetworkHeaders.Builder()
    tileServer.requestHeaders.forEach { header ->
        headers[header.key] = header.value
    }

    //getLogger().d("CORTE", "X,Y = ${center.x.toInt()}, ${center.y.toInt()}")
    //getLogger().i("CORTE", "XOFF,YOFF = $xOffset, $yOffset")
    //getLogger().i("CORTE", "SIZES = ${kartaTileSize.dp}")

    val offset = IntOffset(
        xOffset.dpToPx(),
        yOffset.dpToPx())

    Box(modifier = Modifier
        .offset { offset }
        .requiredSize(kartaTileSize.dp)
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

    Box(
        modifier = Modifier
            .offset { offset }
    ) {
        Text(text = "$xIndex,$yIndex")
    }

    Canvas(modifier = Modifier.offset { offset }) {
        drawRect(
            color = Color.Black,
            size = Size(kartaTileSize.dp.toPx(), kartaTileSize.dp.toPx()),
            style = Stroke(1.0f),
        )
    }

}
