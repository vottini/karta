package systems.untangle.karta

import kotlin.io.println
import coil3.compose.AsyncImage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Button
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged

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

data class Size(
	val width: Int,
	val height: Int
) {
	val halfWidth: Int by lazy { width / 2 }
	val halfHeight: Int by lazy { height / 2 }
	override fun equals(o: Any?) = (o is Size) && (o.width == width) && (o.height == height)
	override fun hashCode() : Int = width.hashCode().xor(height.hashCode())
}

const val tile_width = 512

@Composable
fun Tile(zoom: Float, xIndex: Int, yIndex: Int, center: Offset, viewSize: Size) {
	val xOffset = viewSize.halfWidth  + (xIndex - center.x) * tile_width
	val yOffset = viewSize.halfHeight + (yIndex - center.y) * tile_width
	val sanedZoom = zoom.toInt()
	
	Box(modifier = Modifier
		.offset { IntOffset(xOffset.toInt(), yOffset.toInt()) }
		.background(Color.Blue)
		.size(tile_width.dp)
	) {
		AsyncImage(
			model = "http://localhost:8077/${sanedZoom}/${xIndex}/${yIndex}",
			contentDescription = ""
		)
	}
}

@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalComposeUiApi::class
)
@Composable
fun App() {
	var zoom by remember { mutableStateOf(14f) }
	var center by remember { mutableStateOf(Offset(6358.5f, 9136.5f)) }
	var viewSize by remember { mutableStateOf(Size(0, 0)) }
	var cursor by remember { mutableStateOf(Offset(0f, 0f)) } 

	BoxWithConstraints(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()
			.onDrag { dragged ->
				center = Offset(
					center.x - (dragged.x / tile_width),
					center.y - (dragged.y / tile_width)
				)
			}

			.onPointerEvent(PointerEventType.Move) {
				val position = it.changes.first().position

				cursor = Offset(
					center.x + (position.x - viewSize.halfWidth)  / tile_width,
					center.y + (position.y - viewSize.halfHeight) / tile_width
				)

				//println("Mouse at ${position} => cursor = ${cursor} vs center = ${center}")
			}

			.onSizeChanged { size ->
				val newViewSize = Size(size.width, size.height)
				if (newViewSize != viewSize) {
					viewSize = newViewSize
				}
			}
	) {

		for (x in -2..2) {
			for (y in -2..2) {
				Tile(
					zoom,
					center.x.toInt() + x,
					center.y.toInt() + y,
					center,
					viewSize)
			}
		}
	}
}

