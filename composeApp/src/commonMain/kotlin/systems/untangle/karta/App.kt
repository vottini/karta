package systems.untangle.karta

import kotlin.io.println
import kotlin.math.sign
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.ln
import kotlin.math.atan
import kotlin.math.sinh
import kotlin.math.PI

import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Button
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
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
	val halfWidth: Double by lazy { width.toDouble() / 2.0 }
	val halfHeight: Double by lazy { height.toDouble() / 2.0 }

	override fun equals(o: Any?) : Boolean {
		return (o is Size)
			&& (o.width == width)
			&& (o.height == height)
	}

	override fun hashCode() : Int {
		return width.hashCode().xor(height.hashCode())
	}
}

fun Size.toIntOffset() : IntOffset = IntOffset(this.width, this.height)


data class Coordinates(
	val latitude: Double,
	val longitude: Double)

fun Coordinates.minus(coords: Coordinates) : Coordinates {
	return Coordinates(
		coords.latitude - this.latitude,
		coords.longitude - this.longitude
	)
}

data class Region(
	val topLeft: Coordinates,
	val bottomRight: Coordinates)

fun Region.deltaLatitude() = this.topLeft.latitude - this.bottomRight.latitude
fun Region.deltaLongitude() = this.topLeft.longitude - this.bottomRight.longitude

data class DoubleOffset(
	val x: Double,
	val y: Double
)

fun Offset.toDouble() = DoubleOffset(this.x.toDouble(), this.y.toDouble())
fun DoubleOffset.toFloat() = Offset(this.x.toFloat(), this.y.toFloat())
fun DoubleOffset.times(k: Double) = DoubleOffset(this.x * k, this.y * k)
fun DoubleOffset.div(k: Double) = DoubleOffset(this.x / k, this.y / k)

fun convertToLatLong(zoom: Int, tileCoords: DoubleOffset) : Coordinates {
	val numberOfTiles = 2.0.pow(zoom)
	val longitude = (tileCoords.x / numberOfTiles) * 360.0 - 180.0
	val latRadians = atan(sinh(PI * (1.0 - (2.0 * tileCoords.y / numberOfTiles))))
	val latitude = (latRadians * 180.0) / PI
	return Coordinates(latitude, longitude)
}

fun convertToTileCoords(zoom: Int, coords: Coordinates) : DoubleOffset {
	val latitudeRads = (coords.latitude / 180.0) * PI
	val secLatitude = 1.0 / cos(latitudeRads)
	val tanLatitude = tan(latitudeRads)

	val numberOfTiles = 2.0.pow(zoom)
	val xOffset = numberOfTiles * ((coords.longitude + 180.0) / 360.0)
	val yOffset = numberOfTiles * (1.0 - (ln(tanLatitude + secLatitude) / PI)) / 2.0

	return DoubleOffset(
		xOffset,
		yOffset)
}

class Converter(val viewingRegion: Region, viewSize: Size) {
	val horizontalPixelDensity =  viewSize.width.toDouble() / viewingRegion.deltaLongitude()
	val verticalPixelDensity = viewSize.height.toDouble() / viewingRegion.deltaLatitude()

	fun convertToOffset(coords: Coordinates) : IntOffset {
		val origin = viewingRegion.topLeft
		val diff = coords.minus(origin)

		return IntOffset(
			(diff.longitude * horizontalPixelDensity).toInt(),
			(diff.latitude * verticalPixelDensity).toInt()
		)
	}

	fun insideView(coords: Coordinates) : Boolean {
		val (topLeft, bottomRight) = viewingRegion

		return (
			(coords.latitude in bottomRight.latitude..topLeft.latitude) &&
			(coords.longitude in topLeft.longitude..bottomRight.longitude)
		)
	}
}

const val kartaTileSize = 256

@Composable
fun Tile(
	zoom: Int,
	xIndex: Int,
	yIndex: Int,
	center: DoubleOffset,
	viewSize: Size,
	tileServer: TileServer)
{
	val formattedUrl = remember(tileServer, zoom, xIndex, yIndex) {
		tileServer.tileUrl
			.replace("{zoom}", zoom.toString())
			.replace("{x}", xIndex.toString())
			.replace("{y}", yIndex.toString())
	}

	val xOffset = viewSize.halfWidth  + (xIndex - center.x) * kartaTileSize
	val yOffset = viewSize.halfHeight + (yIndex - center.y) * kartaTileSize

	val headers = NetworkHeaders.Builder()
	tileServer.requestHeaders.forEach { header ->
		headers.set(header.key, header.value)
	}
	
	Box(modifier = Modifier
		.offset { IntOffset(xOffset.toInt(), yOffset.toInt()) }
		.background(Color.Blue)
		.size(kartaTileSize.dp)
	) {
		AsyncImage(
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier.height(kartaTileSize.dp).width(kartaTileSize.dp),
			model = ImageRequest.Builder(LocalPlatformContext.current)
				.data(formattedUrl)
				.httpHeaders(headers.build())
				.build()
		)
	}
}

val LocalCursor = compositionLocalOf { Coordinates(0.0, 0.0) }
val LocalZoom = compositionLocalOf { 14 }

val LocalViewingRegion = compositionLocalOf { Region(
	Coordinates(0.0, 0.0),
	Coordinates(0.0, 0.0)
)}

val LocalConverter = compositionLocalOf { Converter(
	Region(Coordinates(1.0, 0.0), Coordinates(0.0, 1.0)),
	Size(0, 0)
)}

data class Header(
	val key: String,
	val value: String)


data class TileServer(
	val tileUrl: String,
	val requestHeaders: List <Header> = listOf()
)

val smapsServer = TileServer("http://localhost:8077/{zoom}/{x}/{y}")

val openStreetMapServer = TileServer(
	tileUrl = "https://tile.openstreetmap.org/{zoom}/{x}/{y}.png",
	requestHeaders = listOf(
		Header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
		Header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:136.0) Gecko/20100101 Firefox/136.0"),
		Header("Host", "tile.openstreetmap.org")
	)
)

data class TileServerOption(
	val name: String,
	val server: TileServer)

val tileServerOptions = listOf(
	TileServerOption("SMAPS", smapsServer),
	TileServerOption("OpenStreetMaps", openStreetMapServer)
)


@Composable
fun Karta(
	tileServer: TileServer,
	initialCoords: Coordinates,
	initialZoom: Int = 14,
	requestHeaders: String? = null,
	child: @Composable () -> Unit = {})
{
	var viewSize: Size? by remember { mutableStateOf(null) }

	Box(
		Modifier
			.fillMaxWidth()
			.fillMaxHeight()

			.onSizeChanged { size ->
				val newViewSize = Size(size.width, size.height)
				if (newViewSize != viewSize) {
					viewSize = newViewSize
				}
			}
	) {
		viewSize?.let { concreteSize ->
			KMap(
				tileServer,
				initialZoom,
				initialCoords,
				concreteSize,
				requestHeaders,
				child
			)
		}
	}
}

@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalComposeUiApi::class
)
@Composable
fun KMap(
	tileServer: TileServer,
	initialZoom: Int,
	initialCoords: Coordinates,
	viewSize: Size,
	requestHeaders: String?,
	child: @Composable () -> Unit)
{
	var zoom by remember { mutableStateOf(initialZoom) }
	var center by remember { mutableStateOf(
		convertToTileCoords(
			initialZoom,
			initialCoords))
	}

	var viewingRegion by remember(center, viewSize, zoom) {
		val topLeft = DoubleOffset(
			center.x - (viewSize.halfWidth  / kartaTileSize),
			center.y - (viewSize.halfHeight / kartaTileSize)
		)

		val bottomRight = DoubleOffset(
			center.x + (viewSize.halfWidth  / kartaTileSize),
			center.y + (viewSize.halfHeight / kartaTileSize)
		)

		mutableStateOf(Region(
			convertToLatLong(zoom.toInt(), topLeft),
			convertToLatLong(zoom.toInt(), bottomRight)
		))
	}

	var cursor by remember { mutableStateOf(Coordinates(0.0, 0.0)) }

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
				center = DoubleOffset(
					center.x - (dragged.x / kartaTileSize),
					center.y - (dragged.y / kartaTileSize)
				)
			}

			.onPointerEvent(PointerEventType.Move) {
				val position = it.changes.first().position
				cursor = convertToLatLong(zoom.toInt(), DoubleOffset(
					center.x + (position.x - viewSize.halfWidth)  / kartaTileSize,
					center.y + (position.y - viewSize.halfHeight) / kartaTileSize
				))
			}

			.onPointerEvent(PointerEventType.Scroll) {
				val change = it.changes.first()
				val value = change.scrollDelta.y.toInt().sign
				center = if (value < 0) center.times(2.0) else center.div(2.0)
				zoom -= value
			}
	) {
		val horizontalTiles = remember(viewSize) { ((viewSize.width / kartaTileSize) / 2) + 1 }
		val verticalTiles = remember(viewSize) { ((viewSize.height / kartaTileSize) / 2) + 1 }

		for (x in -horizontalTiles..horizontalTiles) {
			for (y in -verticalTiles..verticalTiles) {
				Tile(
					zoom.toInt(),
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
			LocalConverter provides converter
		) {
			child()
		}
	}
}


@Composable
fun Marker(
	coords: Coordinates,
	offset: IntOffset? = null,
	minZoom: Int = 13,
	child: @Composable (coordsOffset: IntOffset) -> Unit = {}
) {
	val zoom = LocalZoom.current
	val converter = LocalConverter.current

	val coordsOffset = remember(coords, converter, offset) {
		val finalOffset = offset ?: IntOffset(0, 0)
		converter.convertToOffset(coords).minus(finalOffset)
	}

	if (zoom >= minZoom) {
		child(coordsOffset)
	}
}

@Composable
fun Circle(
	coords: Coordinates,
	radius: Float,
	borderWidth: Float = 0f,
	borderColor: Color = Color.Black,
	fillColor: Color? = Color.Black
) {
	Marker(coords) { coordsOffset ->
		Canvas(modifier = Modifier.offset { coordsOffset }) {
			if (null != fillColor) {
				drawCircle(
					color = fillColor,
					radius = radius
				)
			}

			if (borderWidth > 0) {
				drawCircle(
					color = borderColor,
					style = Stroke(borderWidth),
					radius = radius
				)
			}
		}
	}
}

@Composable
fun Polyline(
	coordsList: List <Coordinates>,
	color: Color = Color.Black,
	width: Float = 1.0f,
	closed: Boolean = false
) {
	val converter = LocalConverter.current
	val coordsListOffset = remember(coordsList, converter) {
		coordsList.map { coords -> converter.convertToOffset(coords) }
	}

	for (i in 0 .. (coordsList.size-2)) {
		val start = coordsListOffset.get(i)
		val end = coordsListOffset.get(i+1)

		Canvas(modifier = Modifier) {
			drawLine(
				color = color,
				strokeWidth = width,
				cap = StrokeCap.Round,
				start = start.toOffset(),
				end = end.toOffset()
			)
		}
	}
}

val vitoriaHome = Coordinates(-20.295934, -40.347966)
val ilhaBoi = Coordinates(-20.310662, -40.2815008)

val rota = listOf(
	Coordinates(-20.311070, -40.302298),
	Coordinates(-20.307223, -40.302819),
	Coordinates(-20.301494, -40.298605),
	Coordinates(-20.287535, -40.304205)
)

@Composable
fun App() {
	var tileServerIndex by remember { mutableStateOf(0) }
	val selectedTileServer = tileServerOptions.get(tileServerIndex)

	Karta(
		tileServer = selectedTileServer.server,
		initialCoords = vitoriaHome,
	) {
		val cursor = LocalCursor.current
		val viewingRegion = LocalViewingRegion.current
		val converter = LocalConverter.current
		val zoom = LocalZoom.current

		Column {
			Text("${cursor.latitude}")
			Text("${cursor.longitude}")
			Text("${viewingRegion.topLeft}")
			Text("${viewingRegion.bottomRight}")
			Text("${converter.convertToOffset(cursor)}")
			Text("Zoom = ${zoom}")

			Button(
				onClick = {
					val nextIndex = (tileServerIndex + 1) % tileServerOptions.size
					tileServerIndex = nextIndex
				}
			) {
				Text("Mudar Mapa")
			}
		}

		for (k in 1..3) {
			Circle(
				coords = vitoriaHome,
				radius = k.toFloat() * 55f,
				borderWidth = 2f,
				fillColor = null
			)
		}

		Circle(
			coords = ilhaBoi,
			radius = 5f,
			borderWidth = 1f,
			fillColor = Color.Blue
		)

		Polyline(
			coordsList = rota,
			color = Color.Blue,
			width = 5.0f
		)
	}
}

