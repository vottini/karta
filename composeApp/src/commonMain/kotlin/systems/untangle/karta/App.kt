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

const val tile_width = 512

@Composable
fun Tile(zoom: Int, xIndex: Int, yIndex: Int, center: Offset) {
	val xDelta = (center.x - xIndex) * tile_width
	val yDelta = (center.y - yIndex) * tile_width

	//println("(${xIndex},${yIndex}) - (${center.x}, ${center.y}) * ${tile_width}  => xDelta = ${xDelta} / yDelta = ${yDelta}")
	println("(${xIndex},${yIndex}) - (${center.x}, ${center.y}) => (${xDelta},${yDelta})")

	Box(modifier = Modifier
		.offset { IntOffset(xDelta.toInt(), yDelta.toInt()) }
		.background(Color.Blue)
		.size(tile_width.dp)
	) {
		AsyncImage(
			model = "http://localhost:8077/${zoom}/${yIndex}/${xIndex}",
			contentDescription = ""
		)
	}
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun App() {
	var xOffset by remember { mutableStateOf(0f) }
	var yOffset by remember { mutableStateOf(0f) }

	var zoom by remember { mutableStateOf(14f) }
	var center by remember { mutableStateOf(Offset(9136.5f, 6358.5f)) }

	Box(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()
			.onDrag { dragged ->
				center = Offset(
					center.x + (dragged.x / tile_width),
					center.y + (dragged.y / tile_width)
				)
			}
	) {
		Tile(14, 9135, 6357, center)
		Tile(14, 9136, 6357, center)
		Tile(14, 9137, 6357, center)

		//Tile(14, 9135, 6358, center)
		//Tile(14, 9136, 6358, center)
		//Tile(14, 9137, 6358, center)

		//Tile(14, 9135, 6359, center)
		//Tile(14, 9136, 6359, center)
		//Tile(14, 9137, 6359, center)
	}
}
