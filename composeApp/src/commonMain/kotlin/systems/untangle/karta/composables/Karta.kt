package systems.untangle.karta.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Size
import systems.untangle.karta.network.TileServer

@Composable
fun Karta(
    tileServer: TileServer,
    initialCoords: Coordinates,
    initialZoom: Int = 14,
    content: @Composable () -> Unit = {})
{
    var viewSize: Size? by remember { mutableStateOf(null) }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()

            .onSizeChanged { size ->
                val newViewSize = Size(size.width, size.height)
                if (newViewSize != viewSize) {
                    viewSize = newViewSize
                }
            }
    ) {
        viewSize?.let { concreteSize ->
            KMap(
                tileServer,
                initialZoom,
                initialCoords,
                concreteSize,
                content
            )
        }
    }
}

