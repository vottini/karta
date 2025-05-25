package systems.untangle.karta.composables

import kotlin.math.sign
import kotlinx.coroutines.flow.MutableSharedFlow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider

import kotlinx.coroutines.launch
import systems.untangle.karta.LocalConverter
import systems.untangle.karta.LocalCursor
import systems.untangle.karta.LocalPointerEvents
import systems.untangle.karta.LocalViewingRegion
import systems.untangle.karta.LocalZoom

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Size
import systems.untangle.karta.data.Region
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.conversion.Converter

import systems.untangle.karta.conversion.convertToLatLong
import systems.untangle.karta.conversion.convertToTileCoordinates
import systems.untangle.karta.input.AugmentedPointerEvent
import systems.untangle.karta.input.ButtonAction
import systems.untangle.karta.input.PointerButton
import systems.untangle.karta.input.PointerFlows
import systems.untangle.karta.input.PointerMonitor
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.exclusiveListener
import systems.untangle.karta.kartaTileSize
import systems.untangle.karta.network.TileServer

/**
 *
 * Indexed Coordinates
 *
 * x,y in [0,N) where N = 2^zoom
 *
 * .-------------------------> (x)
 * | (0,0) | (1,0) | (2,0) ...
 * | (0,1) | (1,1) | (2,1) ...
 * | (0,2) | (1,2) | (2,2) ...
 * |
 * V
 * (y)
 *
 *   0        1              2
 * .---.  .---.---.  .---.---.---.---.
 * |   |  |   |   |  |   |   |   |   |
 * '---'  |---|---|  |---|---|---|---|
 *        |   |   |  |   |   |   |   |
 *        '---'---'  |---|---|---|---|
 *                   |   |   |   |   |
 *                   |---|---|---|---|
 *                   |   |   |   |   |
 *                   '---'---'---'---'
 *
 *
 * C => center of screen coordinates (float)
 * T => tile top left coordinates (integer)
 *
 * T(x,y) .------------.               -.-
 *        |            |                |
 *        |            |                | dY
 *        |            |                |
 *        |            |                |
 *        '------------'               -'-
 *
 *        |-----------------------------| C(x,y)
 *                       dX
 *
 * Dragging to RIGHT moves the center of screen to the LEFT and vice versa
 * Dragging to TOP moves the center of screen to the BOTTOM and vice versa
 * Thus => center -= drag
 *
 * When Cx > Tx ===> offset Tile to the LEFT (xOffset < 0) ===> xOffset = Tx - Cx
 * When Cy > Ty ===> offset Tile to the TOP  (yOffset < 0) ===> yOffset = Ty - Cy
 *
 * C must match the middle of window
 *
 */

@Composable
fun KMap(
    tileServer: TileServer,
    initialZoom: Int,
    initialCoords: Coordinates,
    viewSize: Size,
    iteractive: Boolean,
    onPress: suspend (PointerPosition) -> Unit,
    onLongPress: suspend (PointerPosition) -> Unit,
    onMapDragged: suspend () -> Unit,
    content: @Composable () -> Unit)
{
    var zoom by remember { mutableStateOf(initialZoom) }
    var center by remember {
        mutableStateOf(
            convertToTileCoordinates(
                initialZoom,
                initialCoords
            )
        )
    }

    var viewingRegion by remember(center, viewSize, zoom) {
        val topLeft = DoubleOffset(
            center.x - (viewSize.halfWidth / kartaTileSize),
            center.y - (viewSize.halfHeight / kartaTileSize)
        )

        val bottomRight = DoubleOffset(
            center.x + (viewSize.halfWidth / kartaTileSize),
            center.y + (viewSize.halfHeight / kartaTileSize)
        )

        mutableStateOf(
            Region(
                convertToLatLong(zoom, topLeft),
                convertToLatLong(zoom, bottomRight)
            )
        )
    }

    var cursor by remember { mutableStateOf(Coordinates(0.0, 0.0)) }

    var converter by remember(viewingRegion, viewSize) {
        mutableStateOf(
            Converter(
                viewingRegion,
                viewSize
            )
        )
    }

    val rawMoveFlow = remember { MutableSharedFlow<PointerPosition>(extraBufferCapacity = 1) }
    val rawButtonFlow = remember { MutableSharedFlow<AugmentedPointerEvent>(extraBufferCapacity = 1) }
    val pointerMonitor = remember(rawButtonFlow, rawMoveFlow) {
        PointerMonitor(rawButtonFlow, rawMoveFlow)
    }

    val pointerEvents = remember(pointerMonitor) {
        PointerFlows(
            pointerMonitor.moveFlow,
            pointerMonitor.clickFlow,
            pointerMonitor.shortPressFlow,
            pointerMonitor.longPressFlow,
            pointerMonitor.dragFlow
        )
    }

    var leftPressed by remember { mutableStateOf(false) }
    var draggingAvailable by remember { mutableStateOf(true) }
    var pressAvailable by remember { mutableStateOf(true) }

    LaunchedEffect(pointerMonitor, iteractive) {
        if (!iteractive) {
            return@LaunchedEffect
        }

        exclusiveListener(pointerMonitor.dragSubscribersFlow) { draggingAvailable = it }
        exclusiveListener(pointerMonitor.pressSubscribersFlow) { pressAvailable = it }
        pointerMonitor.listen(this)
    }

    LaunchedEffect(pointerEvents, pressAvailable) {
        if (pressAvailable) {
            val shortPressMonitoring = launch {
                pointerEvents.shortPressFlow.collect { position ->
                    onPress.invoke(position)
                }
            }

            val longPressMonitoring = launch {
                pointerEvents.longPressFlow.collect { position ->
                    onLongPress.invoke(position)
                    leftPressed = false
                }
            }

            listOf(
                shortPressMonitoring,
                longPressMonitoring
            ).forEach { job ->  job.join() }
        }
    }

    LaunchedEffect(pointerEvents) {
        pointerEvents.clickFlow.collect { event ->
            if (event.button == PointerButton.LEFT) {
                leftPressed = (event.action == ButtonAction.PRESS)
            }
        }
    }

    LaunchedEffect(pointerEvents, leftPressed, draggingAvailable) {
        if (leftPressed && draggingAvailable) {
            pointerEvents.dragFlow.collect { deltaPosition ->
                val dragged = deltaPosition.diff
                onMapDragged.invoke()

                center = DoubleOffset(
                    center.x - (dragged.x / kartaTileSize),
                    center.y - (dragged.y / kartaTileSize)
                )
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        val position = change.position

                        val coordinates = convertToLatLong(zoom, DoubleOffset(
                            center.x + (position.x - viewSize.halfWidth)  / kartaTileSize,
                            center.y + (position.y - viewSize.halfHeight) / kartaTileSize
                        ))

                        when (event.type) {
                            PointerEventType.Press,
                            PointerEventType.Release ->
                                rawButtonFlow.tryEmit(
                                    AugmentedPointerEvent(event,
                                        PointerPosition(coordinates, position)
                                    )
                                )

                            PointerEventType.Move -> {
                                cursor = coordinates
                                rawMoveFlow.tryEmit(
                                    PointerPosition(
                                        cursor,
                                        position
                                    )
                                )
                            }

                            PointerEventType.Scroll -> {
                                val value = change.scrollDelta.y.toInt().sign
                                center = if (value < 0) center.times(2.0) else center.div(2.0)
                                zoom -= value
                            }
                        }

                    }
                }
            }
    ) {
        val horizontalTiles = remember(viewSize) { ((viewSize.width / kartaTileSize) / 2) + 1 }
        val verticalTiles = remember(viewSize) { ((viewSize.height / kartaTileSize) / 2) + 1 }

        for (x in -horizontalTiles..horizontalTiles) {
            for (y in -verticalTiles..verticalTiles) {
                Tile(
                    zoom,
                    center.x.toInt() + x,
                    center.y.toInt() + y,
                    center,
                    viewSize,
                    tileServer)
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        CompositionLocalProvider(
            LocalZoom provides zoom,
            LocalCursor provides cursor,
            LocalViewingRegion provides viewingRegion,
            LocalConverter provides converter,
            LocalPointerEvents provides pointerEvents
        ) {
            content()
        }
    }
}
