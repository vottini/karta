package systems.untangle.karta

import kotlin.io.println
import coil3.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/*

Indexed Coordinates

x,y in [0,N] where N = 2^zoom

.-------------------------> (x)
| (0,0) | (1,0) | (2,0) ...
| (0,1) | (1,1) | (2,1) ...
| (0,2) | (1,2) | (2,2) ...
|
V
(y)

  0        1              2
.---.  .---.---.  .---.---.---.---.
|   |  |   |   |  |   |   |   |   |
'---'  |---|---|  |---|---|---|---|
       |   |   |  |   |   |   |   |
			 '---'---'  |---|---|---|---|
			            |   |   |   |   |
									|---|---|---|---|
									|   |   |   |   |
									'---'---'---'---'


C => center of screen coordinates (float)
T => tile top left coordinates (integer)

T(x,y) .------------.               -.-
       |            |                |
       |            |                | dY
       |            |                |
       |            |                |
       '------------'               -'-

       |-----------------------------| C(x,y)
                      dX

Dragging to RIGHT moves the center of screen to the LEFT and vice versa
Dragging to TOP moves the center of screen to the BOTTOM and vice versa
Thus => center -= drag

When Cx > Tx ===> offset Tile to the LEFT (xOffset < 0) ===> xOffset = Tx - Cx
When Cy > Ty ===> offset Tile to the TOP  (yOffset < 0) ===> yOffset = Ty - Cy

*/

const val tile_width = 512

@Composable
fun Tile(zoom: Int, xIndex: Int, yIndex: Int, center: Offset) {
	val xOffset = (xIndex - center.x) * tile_width
	val yOffset = (yIndex - center.y) * tile_width

	Box(modifier = Modifier
		.offset { IntOffset(xOffset.toInt(), yOffset.toInt()) }
		.background(Color.Blue)
		.size(tile_width.dp)
	) {
		AsyncImage(
			model = "http://localhost:8077/${zoom}/${xIndex}/${yIndex}",
			contentDescription = ""
		)
	}
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun App() {
	var zoom by remember { mutableStateOf(14f) }
	var center by remember { mutableStateOf(Offset(6358f, 9136f)) }

	Box(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()
			.onDrag { dragged ->
				center = Offset(
					center.x - (dragged.x / tile_width),
					center.y - (dragged.y / tile_width)
				)
			}
	) {
		Tile(14, 6357, 9135, center)
		Tile(14, 6358, 9135, center)
		Tile(14, 6359, 9135, center)

		Tile(14, 6357, 9136, center)
		Tile(14, 6358, 9136, center)
		Tile(14, 6359, 9136, center)

		Tile(14, 6357, 9137, center)
		Tile(14, 6358, 9137, center)
		Tile(14, 6359, 9137, center)
	}
}

