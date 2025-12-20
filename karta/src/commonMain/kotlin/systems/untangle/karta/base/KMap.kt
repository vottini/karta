package systems.untangle.karta.base

import kotlin.math.sign
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration.Companion.milliseconds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.conversion.Converter

import systems.untangle.karta.conversion.convertToLatLong
import systems.untangle.karta.conversion.convertToTileCoordinates
import systems.untangle.karta.conversion.wrapLongitude
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.ZOOM_DECREMENT
import systems.untangle.karta.data.ZOOM_INCREMENT
import systems.untangle.karta.data.ZoomLevel
import systems.untangle.karta.data.ZoomSpecs
import systems.untangle.karta.data.div
import systems.untangle.karta.data.minus
import systems.untangle.karta.data.px
import systems.untangle.karta.input.AugmentedPointerEvent
import systems.untangle.karta.input.ButtonAction
import systems.untangle.karta.input.PointerButton
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.exclusiveListener
import systems.untangle.karta.input.getPlatformSpecificPointerMonitor
import systems.untangle.karta.kartaTileSize
import systems.untangle.karta.network.TileServer
import kotlin.math.pow

/*
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

/**
 * Event capture and tile display composable
 */

@Composable
fun KMap(
    tileServer: TileServer,
    initialZoom: Int,
    initialCoords: Coordinates,
    viewSize: PxSize,
    interactive: Boolean,
    maxZoom: Int,
    minZoom: Int,
    onPress: suspend (PointerPosition) -> Unit,
    onLongPress: suspend (PointerPosition) -> Unit,
    onMapDragged: suspend () -> Unit,
    content: @Composable () -> Unit)
{
    var center by remember {
        mutableStateOf(
            convertToTileCoordinates(
                initialZoom,
                initialCoords
            )
        )
    }

    var zoomSpecs by remember(initialZoom, minZoom, maxZoom) { mutableStateOf(
        ZoomSpecs(initialZoom, minZoom, maxZoom)
    )}

    val zoom = remember(zoomSpecs) { ZoomLevel(
        zoomSpecs.value,
        { zoomSpecs = zoomSpecs.increment(); center = center.scale(2.0) },
        { zoomSpecs = zoomSpecs.decrement(); center = center.scale(0.5) }
    )}

    val viewingBoundingBox by remember(center, viewSize, zoom) {
        val deltaWidth = (viewSize.halfWidth / kartaTileSize)
        val deltaHeight = (viewSize.halfHeight / kartaTileSize)

        val topLeft = DoubleOffset(
            center.x - deltaWidth.value,
            center.y - deltaHeight.value
        )

        val bottomRight = DoubleOffset(
            center.x + deltaWidth.value,
            center.y + deltaHeight.value
        )

        mutableStateOf(
            BoundingBox(
                convertToLatLong(zoom.level, topLeft),
                convertToLatLong(zoom.level, bottomRight)
            )
        )
    }

    var cursor by remember { mutableStateOf <Coordinates?> (null) }
    val converter by remember(viewingBoundingBox, viewSize, zoom) {
        mutableStateOf(
            Converter(
                viewingBoundingBox,
                viewSize,
                center,
                zoom.level
            )
        )
    }

    val rawMoveFlow = remember { MutableSharedFlow<PointerPosition?>(extraBufferCapacity = 1) }
    val rawButtonFlow = remember { MutableSharedFlow<AugmentedPointerEvent>(extraBufferCapacity = 1) }

    val pointerMonitor = remember(rawButtonFlow, rawMoveFlow) {
        getPlatformSpecificPointerMonitor(
            rawButtonFlow,
            rawMoveFlow,
            500.milliseconds)
    }

    var leftPressed by remember { mutableStateOf(false) }
    var draggingAvailable by remember { mutableStateOf(true) }
    var pressAvailable by remember { mutableStateOf(true) }

    LaunchedEffect(pointerMonitor, interactive) {
        if (!interactive) return@LaunchedEffect
        exclusiveListener(pointerMonitor.dragSubscribersFlow) { draggingAvailable = it }
        exclusiveListener(pointerMonitor.pressSubscribersFlow) { pressAvailable = it }
        pointerMonitor.listen(this)
    }

    val pointerEvents = remember(pointerMonitor) {
        pointerMonitor.pointerFlows
    }

    LaunchedEffect(pointerEvents, pressAvailable) {
        if (pressAvailable) {
            launch {
                pointerEvents.shortPressFlow.collect { position ->
                    onPress.invoke(position)
                }
            }

            launch {
                pointerEvents.longPressFlow.collect { position ->
                    onLongPress.invoke(position)
                    leftPressed = false
                }
            }
        }
    }

    LaunchedEffect(pointerEvents) {
        pointerEvents.clickFlow.collect { event ->
            if (event.button == PointerButton.LEFT) {
                leftPressed = (event.action == ButtonAction.PRESS)
            }
        }
    }

    LaunchedEffect(pointerEvents, leftPressed, draggingAvailable, zoom) {
        if (leftPressed && draggingAvailable) {
            pointerEvents.dragFlow.collect { deltaPosition ->
                val dragged = deltaPosition.diff
                onMapDragged.invoke()

                center = zoom.curtail(DoubleOffset(
                    center.x - (dragged.x.px / kartaTileSize).value,
                    center.y - (dragged.y.px / kartaTileSize).value
                ))
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pointerInput(center, zoom, viewSize) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        val position = change.position

                        val offsets = DoubleOffset(
                            ((position.x.px - viewSize.halfWidth)  / kartaTileSize).value.toDouble(),
                            ((position.y.px - viewSize.halfHeight) / kartaTileSize).value.toDouble()
                        )

                        val eventOffset = center.add(offsets)
                        val coordinates = convertToLatLong(zoom.level, eventOffset).wrapLongitude()

                        when (event.type) {
                            PointerEventType.Press,
                            PointerEventType.Release ->
                                rawButtonFlow.tryEmit(
                                    AugmentedPointerEvent(event,
                                        PointerPosition(coordinates, position)
                                    )
                                )

                            PointerEventType.Enter,
                            PointerEventType.Move -> {
                                cursor = coordinates
                                rawMoveFlow.tryEmit(
                                    PointerPosition(
                                        coordinates,
                                        position
                                    )
                                )
                            }

                            PointerEventType.Exit -> {
                                cursor = null
                            }

                            PointerEventType.Scroll -> {
                                val value = change.scrollDelta.y.toInt().sign
                                val action = if (value < 0) ZOOM_INCREMENT else ZOOM_DECREMENT

                                // the cursor should stay in the
                                // same place when changing the zoom

                                when (action) {
                                    ZOOM_INCREMENT -> {
                                        if (zoomSpecs.incrementable()) {
                                            zoomSpecs = zoomSpecs.increment()
                                            center = eventOffset.scale(2.0).minus(offsets)
                                        }
                                    }

                                    ZOOM_DECREMENT -> {
                                        if (zoomSpecs.decrementable()) {
                                            zoomSpecs = zoomSpecs.decrement()
                                            center = eventOffset.scale(0.5).minus(offsets)
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
    ) {
        val horizontalTiles = remember(viewSize) { ((viewSize.width / kartaTileSize).value.toInt() / 2) + 1 }
        val verticalTiles = remember(viewSize) { ((viewSize.height / kartaTileSize).value.toInt() / 2) + 1 }
        val maxZoomIndex = remember(zoom) { 2.toFloat().pow(zoom.level).toInt() }
        val validYRange = remember(maxZoomIndex) { 0 .. maxZoomIndex - 1 }

        for (y in -verticalTiles..verticalTiles) {
            val resultingY = center.y.toInt() + y
            if (resultingY !in validYRange) {
                continue
            }

            for (x in -horizontalTiles..horizontalTiles) {
                val resultingX = center.x.toInt() + x

                Tile(
                    zoom.level,
                    resultingX,
                    resultingY,
                    center,
                    viewSize,
                    tileServer,
                    maxZoomIndex)
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
            LocalViewingBoundingBox provides viewingBoundingBox,
            LocalConverter provides converter,
            LocalPointerEvents provides pointerEvents
        ) {
            content()
        }
    }
}
