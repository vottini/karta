package systems.untangle.karta

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.compose_multiplatform
val tile_width = 512

@Composable
fun Tile(row: Int, column: Int, xOffset: Int, yOffset: Int) {
	val top = yOffset + column * tile_width
	val left = xOffset + row * tile_width

	Box(modifier = Modifier
		.offset { IntOffset(left, top) }
		.background(Color.Blue)
		.size(tile_width.dp)
	) {
		AsyncImage(
			model = "http://localhost:8077/14/${6358+row}/${9136+column}",
			contentDescription = ""
		)
	}
}

@Composable
@Preview
@OptIn(ExperimentalFoundationApi::class)
fun App() {
	var xOffset by remember { mutableStateOf(0f) }
	var yOffset by remember { mutableStateOf(0f) }

	Box(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()
			.onDrag { dragged ->
				xOffset += dragged.x
				yOffset += dragged.y
			}
	) {
		Tile(-1, -1, xOffset.toInt(), yOffset.toInt())
		Tile(+0, -1, xOffset.toInt(), yOffset.toInt())
		Tile(+1, -1, xOffset.toInt(), yOffset.toInt())

		Tile(-1, +0, xOffset.toInt(), yOffset.toInt())
		Tile(+0, +0, xOffset.toInt(), yOffset.toInt())
		Tile(+1, +0, xOffset.toInt(), yOffset.toInt())

		Tile(-1, +1, xOffset.toInt(), yOffset.toInt())
		Tile(+0, +1, xOffset.toInt(), yOffset.toInt())
		Tile(+1, +1, xOffset.toInt(), yOffset.toInt())
	}
}
