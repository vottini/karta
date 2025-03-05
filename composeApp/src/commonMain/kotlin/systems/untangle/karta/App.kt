package systems.untangle.karta

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import kotlin.io.println

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.compose_multiplatform

@Composable
fun Tile(offset: Offset) {
	println("Redrawing with ${offset}")

	Box(modifier = Modifier
		.background(Color.Blue)
		.size(150.dp)
		.absoluteOffset {
			IntOffset(
				offset.x.toInt(),
				offset.y.toInt())
		}
	)
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun App() {
	var offset by remember { mutableStateOf(Offset(0f, 0f)) }

	Box(modifier = Modifier
		.fillMaxWidth()
		.fillMaxHeight()
		.background(Color.Yellow)
		.onDrag(matcher = PointerMatcher.mouse(PointerButton.Primary)) {
			offset += it
		}
	) {
		Tile(offset)
  }
}
