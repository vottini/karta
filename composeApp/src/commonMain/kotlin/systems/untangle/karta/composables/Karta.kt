package systems.untangle.karta.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.network.TileServer

@Composable
fun Karta(
    tileServer: TileServer,
    initialCoords: Coordinates,
    initialZoom: Int = 14,
    iteractive: Boolean = true,
    onPress: suspend (PointerPosition) -> Unit = {},
    onLongPress: suspend (PointerPosition) -> Unit = {},
    onMapDragged: suspend () -> Unit = {},
    content: @Composable () -> Unit = {})
{
    var nullableViewPxSize: PxSize? by remember { mutableStateOf(null) }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()

            .onSizeChanged { size ->
                val newViewPxSize = PxSize(size.width, size.height)
                if (newViewPxSize != nullableViewPxSize) {
                    nullableViewPxSize = newViewPxSize
                }
            }
    ) {
        nullableViewPxSize?.let { viewSize ->
            KMap(
                tileServer,
                initialZoom,
                initialCoords,
                viewSize,
                iteractive,
                onPress,
                onLongPress,
                onMapDragged,
                content
            )
        }
    }
}

