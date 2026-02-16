package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import systems.untangle.karta.base.LocalPointerEvents
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.defineTileRegion
import systems.untangle.karta.input.ButtonEvent
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.isInside
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.data.px

/*
 *   ,------,------,
 *   |      |      |
 *   |      |      |
 *   |------o------|  anchoring
 *   |      |      |  [defaults to (0.5,0,5)]
 *   |      |      |
 *   '------'------'
 *
 */


/**
 *
 */

@Composable
fun Marker(
    coords: Coordinates,
    anchoring: DoubleOffset = DoubleOffset(0.5, 0.5),
    wrapLongitude: Boolean = true,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {},
    onShortPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onLongPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    contents: @Composable () -> Unit
) {
    val markerPxSize = remember { mutableStateOf(PxSize(0.px ,0.px)) }

    Geolocated(
        coordinates = coords,
        extension = markerPxSize.value,
        wrapLongitude = wrapLongitude
    ) { coordsOffset ->
        val pointerEvents = LocalPointerEvents.current
        val markerOffset = remember(coordsOffset, markerPxSize) { IntOffset(
            coordsOffset.x - (anchoring.x * markerPxSize.value.width.value).toInt(),
            coordsOffset.y - (anchoring.y * markerPxSize.value.height.value).toInt()
        )}

        var isHovered by remember { mutableStateOf(false) }
        val ownExtension = remember(markerOffset, markerPxSize) {
            defineTileRegion(
                markerOffset,
                markerPxSize.value
            )
        }

        LaunchedEffect(pointerEvents, isHovered, ownExtension, onHover) {
            pointerEvents.moveFlow.collect { pointerPosition ->
                val newHoverState = pointerPosition?.isInside(ownExtension) ?: false
                if (newHoverState != isHovered) {
                    isHovered = newHoverState
                    onHover(newHoverState)
                }
            }
        }

        LaunchedEffect(pointerEvents, isHovered, onClick, onShortPress) {
            if (isHovered) {
                listOf(
                    launch { pointerEvents.clickFlow.collect { ev -> onClick(ev) } },
                    launch { pointerEvents.shortPressFlow.collect { position -> onShortPress(position) } },
                    launch { pointerEvents.longPressFlow.collect { position -> onLongPress(position) } },
                ).forEach { job ->
                    job.join()
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { markerOffset }
                .onSizeChanged { size ->
                    markerPxSize.value = PxSize(
                        size.width.px,
                        size.height.px,
                    )
                }
        ) {
            contents()
        }
    }
}
