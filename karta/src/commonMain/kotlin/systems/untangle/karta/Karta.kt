package systems.untangle.karta

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.flow.Flow
import systems.untangle.karta.base.KMap

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.ViewSpec
import systems.untangle.karta.data.px
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.network.LocalTileDirectory
import systems.untangle.karta.network.TileServer
import systems.untangle.karta.network.TileSource

/**
 * Root composable that renders an interactive slippy map.
 *
 * Place overlay composables ([Marker], [Circle], [Polyline], [Popup], etc.) inside [content].
 * They will automatically receive the current map state via composition locals
 * ([LocalZoom], [LocalConverter], [LocalCursor], [LocalViewingBoundingBox], [LocalPointerEvents]).
 *
 * @param tileSource Tile source — either a [TileServer] (remote XYZ URL template with optional
 *   HTTP headers) or a [LocalTileDirectory] (local directory laid out as `<path>/<zoom>/<x>/<y>.<ext>`,
 *   matching gdal2tiles `--xyz` output).
 * @param interactive When `false`, disables all pan and zoom gestures.
 * @param initialCoords Geographic coordinates of the map center on first composition.
 * @param initialZoom Zoom level shown on first composition (default 14).
 * @param maxZoom Upper zoom boundary — scroll/pinch will not go above this level (default 19).
 * @param minZoom Lower zoom boundary — scroll/pinch will not go below this level (default 2).
 * @param viewFlow Optional [Flow] of [ViewSpec] for programmatic pan/zoom. Emit a [ViewSpec]
 *   to move or zoom the map from outside the composable.
 * @param onPress Suspend callback invoked on a pointer press, with the geographic position.
 * @param onLongPress Suspend callback invoked after a ~500 ms press.
 * @param onCursorMove Suspend callback invoked on every pointer move with the current position.
 * @param onMapDragged Suspend callback invoked with the new map center after a drag gesture ends.
 * @param onZoomChange Suspend callback invoked with the new zoom level after a scroll or pinch.
 * @param content Composable slot for overlay elements rendered on top of the tile layer.
 */
@Suppress("unused")
@Composable
fun Karta(
    tileSource: TileSource,
    interactive: Boolean = true,
    initialCoords: Coordinates,
    initialZoom: Int = 14,
    maxZoom: Int = 19,
    minZoom: Int = 2,
    viewFlow: Flow<ViewSpec>? = null,
    onPress: suspend (PointerPosition) -> Unit = {},
    onLongPress: suspend (PointerPosition) -> Unit = {},
    onCursorMove: suspend (PointerPosition) -> Unit = {},
    onMapDragged: suspend (Coordinates) -> Unit = {},
    onZoomChange: suspend (Int) -> Unit = {},
    content: @Composable () -> Unit = {})
{
    var nullableViewSize: PxSize? by remember { mutableStateOf(null) }

    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer(clip = true)

            .onSizeChanged { size ->
                val newViewPxSize = PxSize(size.width.px, size.height.px)
                if (newViewPxSize != nullableViewSize) {
                    nullableViewSize = newViewPxSize
                }
            }
    ) {
        nullableViewSize?.let { viewSize ->
            KMap(
                tileSource,
                initialZoom,
                initialCoords,
                viewSize,
                interactive,
                maxZoom,
                minZoom,
                viewFlow,
                onPress,
                onLongPress,
                onCursorMove,
                onMapDragged,
                onZoomChange,
                content
            )
        }
    }
}

