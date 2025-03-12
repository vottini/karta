package systems.untangle.karta

import kotlin.io.println
import kotlin.math.sign
import kotlin.math.pow
import kotlin.math.atan
import kotlin.math.sinh
import kotlin.math.PI
import coil3.compose.AsyncImage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

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
	val halfWidth: Float by lazy { width.toFloat() / 2f }
	val halfHeight: Float by lazy { height.toFloat() / 2f }

	override fun equals(o: Any?) : Boolean {
		return (o is Size)
			&& (o.width == width)
			&& (o.height == height)
	}

	override fun hashCode() : Int {
		return width.hashCode().xor(height.hashCode())
	}
}

data class Coordinates(
	val latitude: Double,
	val longitude: Double
)

fun Coordinates.minus(coords: Coordinates) : Coordinates {
	return Coordinates(
		coords.latitude - this.latitude,
		coords.longitude - this.longitude
	)
}

data class Region(
	val topLeft: Coordinates,
	val bottomRight: Coordinates
)

fun Region.deltaLatitude() = this.topLeft.latitude - this.bottomRight.latitude
fun Region.deltaLongitude() = this.topLeft.longitude - this.bottomRight.longitude

fun convertToLatLong(zoom: Int, tileCoords: Offset) : Coordinates {
	val numberOfTiles = 2.0.pow(zoom)
	val longitude = (tileCoords.x / numberOfTiles) * 360.0 - 180.0
	val latRadians = atan(sinh(PI * (1.0 - (2.0 * tileCoords.y / numberOfTiles))))
	val latitude = (latRadians * 180.0) / PI
	return Coordinates(latitude, longitude)
}

class Converter(viewingRegion: Region, viewSize: Size) {
	val horizontalPixelDensity =  viewSize.width.toFloat() / viewingRegion.deltaLongitude()
	val verticalPixelDensity = viewSize.height.toFloat() / viewingRegion.deltaLatitude()
	val origin = viewingRegion.topLeft

	fun convertToOffset(coords: Coordinates) : IntOffset {
		val diff = coords.minus(origin)

		return IntOffset(
			(diff.longitude * horizontalPixelDensity).toInt(),
			(diff.latitude * verticalPixelDensity).toInt()
		)
	}
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

val LocalCursor = compositionLocalOf { Coordinates(0.0, 0.0) }

val LocalViewingRegion = compositionLocalOf { Region(
	Coordinates(0.0, 0.0),
	Coordinates(0.0, 0.0)
)}

val LocalConverter = compositionLocalOf { Converter(
	Region(Coordinates(1.0, 0.0), Coordinates(0.0, 1.0)),
	Size(0, 0)
)}

@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalComposeUiApi::class
)
@Composable
fun Karta(children: @Composable () -> Unit = {}) {
	var zoom by remember { mutableStateOf(14f) }
	var center by remember { mutableStateOf(Offset(6358.5f, 9136.5f)) }
	var viewSize by remember { mutableStateOf(Size(0, 0)) }

	var cursor by remember { mutableStateOf(Coordinates(0.0, 0.0)) }
	var viewingRegion by remember(center, viewSize, zoom) {
		val topLeft = Offset(
			center.x - (viewSize.halfWidth  / tile_width),
			center.y - (viewSize.halfHeight / tile_width)
		)

		val bottomRight = Offset(
			center.x + (viewSize.halfWidth  / tile_width),
			center.y + (viewSize.halfHeight / tile_width)
		)

		mutableStateOf(Region(
			convertToLatLong(zoom.toInt(), topLeft),
			convertToLatLong(zoom.toInt(), bottomRight)
		))
	}

	var converter by remember(viewingRegion, viewSize) {
		mutableStateOf(Converter(
			viewingRegion,
			viewSize
		))
	}

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

			.onPointerEvent(PointerEventType.Move) {
				val position = it.changes.first().position

				val cursorOffset = Offset(
					center.x + (position.x - viewSize.halfWidth)  / tile_width,
					center.y + (position.y - viewSize.halfHeight) / tile_width
				)

				cursor = convertToLatLong(zoom.toInt(), cursorOffset)
				//val coords = convertToLatLong(zoom.toInt(), cursor)
				//println("Mouse at ${coords}")
			}

			.onPointerEvent(PointerEventType.Scroll) {
				val change = it.changes.first()
				val value = change.scrollDelta.y.toInt().sign
				center = if (value < 0) center.times(2f) else center.div(2f)
				zoom -= value
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

	Box(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()
	) {
		CompositionLocalProvider(LocalCursor provides cursor) {
			CompositionLocalProvider(LocalViewingRegion provides viewingRegion) {
				CompositionLocalProvider(LocalConverter provides converter) {
					children()
				}
			}
		}
	}
}

@Composable
fun App() {
	Karta() {
		val cursor = LocalCursor.current
		val viewingRegion = LocalViewingRegion.current
		val converter = LocalConverter.current

		Column {
			Text("${cursor.latitude}")
			Text("${cursor.longitude}")
			Text("${viewingRegion.topLeft}")
			Text("${viewingRegion.bottomRight}")
			Text("${converter.convertToOffset(cursor)}")
		}

		val ilhaBoi = remember { Coordinates(-20.310662, -40.2815008) }

		Canvas(
			modifier = Modifier
				.offset { converter.convertToOffset(ilhaBoi) }
				.size(5.dp)
		) {
			drawCircle(
				color = Color.Blue,
				radius = 10f
			)
		}
	}
}

