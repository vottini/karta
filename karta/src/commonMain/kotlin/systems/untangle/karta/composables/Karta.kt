package systems.untangle.karta.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.px
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.network.TileServer

@Composable
fun Karta(
    tileServer: TileServer,
    interactive: Boolean = true,
    initialCoords: Coordinates,
    initialZoom: Int = 14,
    maxZoom: Int = 19,
    minZoom: Int = 2,
    onPress: suspend (PointerPosition) -> Unit = {},
    onLongPress: suspend (PointerPosition) -> Unit = {},
    onMapDragged: suspend () -> Unit = {},
    content: @Composable () -> Unit = {})
{
    var nullableViewSize: PxSize? by remember { mutableStateOf(null) }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()

            .onSizeChanged { size ->
                val newViewPxSize = PxSize(size.width.px, size.height.px)
                if (newViewPxSize != nullableViewSize) {
                    nullableViewSize = newViewPxSize
                }
            }
    ) {
        nullableViewSize?.let { viewSize ->
            KMap(
                tileServer,
                initialZoom,
                initialCoords,
                viewSize,
                interactive,
                maxZoom,
                minZoom,
                onPress,
                onLongPress,
                onMapDragged,
                content
            )
        }
    }
}

