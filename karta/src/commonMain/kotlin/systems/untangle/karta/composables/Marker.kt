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
import systems.untangle.karta.conversion.wrapLongitude
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.defineTileRegion
import systems.untangle.karta.input.ButtonEvent
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.isInside
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.data.px
import systems.untangle.karta.input.ButtonAction
import systems.untangle.karta.selection.ItemSelectionState

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
 * An overlay composable that places [contents] at a fixed geographic location on the map.
 *
 * The marker reacts to pointer events only when the cursor is inside its rendered bounds.
 * Use [anchoring] to control which point of [contents] aligns with [coords] —
 * e.g. `DoubleOffset(0.5, 1.0)` pins the bottom-center to the coordinate (typical for pin icons).
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coords Geographic position where the marker is anchored.
 * @param anchoring Normalized anchor point within the marker bounds, in the `[0, 1]` range on
 *   each axis. `(0.5, 0.5)` centers the composable; `(0.5, 1.0)` pins the bottom-center.
 * @param wrapLongitude When `true`, the marker is also rendered at ±360° offsets so it stays
 *   visible during continuous horizontal panning (world wrapping).
 * @param onHover Called with `true` when the cursor enters the marker bounds, `false` on exit.
 * @param onClick Called on every button press or release event while the cursor is inside.
 * @param onShortPress Called on a quick click (press + release without significant movement).
 * @param onLongPress Called after the pointer has been held for ~500 ms inside the marker.
 * @param contents The composable to display at the marker position.
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
        val markerOffset = remember(coordsOffset, markerPxSize.value) { IntOffset(
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

        LaunchedEffect(pointerEvents, ownExtension, onHover) {
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

/**
 * Variant of [Marker] that integrates with the selection system.
 *
 * Automatically updates [itemSelectionState] on hover, click, short press, and long press, so
 * the selection state is kept in sync across all markers sharing the same
 * [systems.untangle.karta.selection.SelectionFlowContext].
 *
 * @param coords Geographic position where the marker is anchored.
 * @param itemSelectionState Per-item selection state obtained from [SelectionItem].
 * @param anchoring Normalized anchor point within the marker bounds (see [Marker]).
 * @param wrapLongitude Render at ±360° offsets for world-wrap continuity.
 * @param onHover Called with `true`/`false` as the cursor enters or leaves the marker.
 * @param onClick Called on every button event while hovered.
 * @param onShortPress Called on a quick click.
 * @param onLongPress Called after a ~500 ms hold.
 * @param onSelectionChange Called whenever [itemSelectionState] changes.
 * @param contents The composable to display at the marker position.
 */
@Composable
fun Marker(
    coords: Coordinates,
    itemSelectionState: ItemSelectionState,
    anchoring: DoubleOffset = DoubleOffset(0.5, 0.5),
    wrapLongitude: Boolean = true,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {},
    onShortPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onLongPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onSelectionChange: suspend CoroutineScope.() -> Unit = {},
    contents: @Composable () -> Unit
) {
    LaunchedEffect(itemSelectionState) {
        onSelectionChange()
    }

    val decoratedOnHover: suspend CoroutineScope.(Boolean) -> Unit =
        remember(itemSelectionState, onHover) {
            { hoveredNow ->
                if (hoveredNow) itemSelectionState.setHovered()
                else itemSelectionState.clearHovered()
                onHover(hoveredNow)
            }
        }

    val decoratedOnClick: suspend CoroutineScope.(ButtonEvent) -> Unit =
        remember (itemSelectionState, onClick) {
            { buttonEvent ->
                itemSelectionState.setClicked()
                onClick(buttonEvent)
            }
        }

    val decoratedOnShortPress: suspend CoroutineScope.(PointerPosition) -> Unit =
        remember (itemSelectionState, onShortPress) {
            { position ->
                if (!itemSelectionState.selected) itemSelectionState.setSelected()
                onShortPress(position)
            }
        }

    val decoratedOnLongPress: suspend CoroutineScope.(PointerPosition) -> Unit =
        remember (itemSelectionState, onLongPress) {
            { position ->
                if (!itemSelectionState.selected) itemSelectionState.setSelected()
                onLongPress(position)
            }
        }

    Marker(
        coords,
        anchoring,
        wrapLongitude,
        decoratedOnHover,
        decoratedOnClick,
        decoratedOnShortPress,
        decoratedOnLongPress,
        contents
    )
}

/**
 * A [Marker] that the user can drag to a new geographic position.
 *
 * While the marker is grabbed (clicked and held), drag events are translated into geographic
 * coordinate updates and forwarded to [coordsSetter]. The caller is responsible for updating
 * [coords] in response, typically via `mutableStateOf`.
 *
 * @param coords Current geographic position of the marker.
 * @param coordsSetter Called with the updated [Coordinates] on each drag frame.
 * @param itemSelectionState Per-item selection state from [SelectionItem]; tracks grab state.
 * @param anchoring Normalized anchor point within the marker bounds (see [Marker]).
 * @param wrapLongitude Render at ±360° offsets for world-wrap continuity.
 * @param onHover Called with `true`/`false` as the cursor enters or leaves the marker.
 * @param onClick Called on every button event while hovered.
 * @param onShortPress Called on a quick click.
 * @param onLongPress Called after a ~500 ms hold.
 * @param onSelectionChange Called whenever [itemSelectionState] changes.
 * @param contents The composable to display at the marker position.
 */
@Composable
fun MovableMarker(
    coords: Coordinates,
    coordsSetter: (Coordinates) -> Unit,
    itemSelectionState: ItemSelectionState,
    anchoring: DoubleOffset = DoubleOffset(0.5, 0.5),
    wrapLongitude: Boolean = true,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {},
    onShortPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onLongPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onSelectionChange: suspend CoroutineScope.() -> Unit = {},
    contents: @Composable () -> Unit
) {
    val pointerEvents = LocalPointerEvents.current
    val offset = remember { mutableStateOf(Coordinates(0.0, 0.0)) }

    val decoratedOnClick: suspend CoroutineScope.(ButtonEvent) -> Unit =
        remember (itemSelectionState, onClick) {
            { event ->
                if (event.action == ButtonAction.PRESS) {
                    offset.value = event.position.coordinates.minus(coords)
                }

                else itemSelectionState.clearGrabbing()
                onClick(event)
            }
        }

    val decoratedSelectionChange: suspend CoroutineScope.() -> Unit =
        remember (itemSelectionState, onSelectionChange, offset) {
            {
                if (itemSelectionState.grabbed) {
                    launch {
                        pointerEvents.dragFlow.collect { deltaPosition ->
                            val newCoordinates = deltaPosition.current.coordinates.plus(offset.value)
                            coordsSetter(newCoordinates.wrapLongitude())
                        }
                    }
                }

                onSelectionChange()
            }
        }

    Marker(
        coords,
        itemSelectionState,
        anchoring,
        wrapLongitude,
        onHover,
        decoratedOnClick,
        onShortPress,
        onLongPress,
        decoratedSelectionChange,
        contents
    )
}
