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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.grid

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
fun Size.div(k: Int) = Size(this.width / k, this.width / k)


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
	val bottomRight: Coordinates
)

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

class Converter(val viewingRegion: Region, val viewSize: Size) {
	val horizontalPixelDensity = viewSize.width.toDouble() / viewingRegion.deltaLongitude()
	val verticalPixelDensity = viewSize.height.toDouble() / viewingRegion.deltaLatitude()

	val tileRegion by lazy { TileRegion(
		this.convertToOffset(viewingRegion.topLeft),
		this.convertToOffset(viewingRegion.bottomRight)
	)}

	fun convertToOffset(coords: Coordinates) : IntOffset {
		val origin = viewingRegion.topLeft
		val diff = coords.minus(origin)

		return IntOffset(
			(diff.longitude * horizontalPixelDensity).toInt(),
			(diff.latitude * verticalPixelDensity).toInt()
		)
	}

	fun insideView(coords: Coordinates, extension: Size?) : Boolean {
		if (null != extension) {
			val apothems = extension.div(2)
			val offset = this.convertToOffset(coords)

			return this.tileRegion.intersects(TileRegion(
				IntOffset(offset.x - apothems.width, offset.y - apothems.height),
				IntOffset(offset.x + apothems.width, offset.y + apothems.height)
			))
		}

		val (topLeft, bottomRight) = viewingRegion

		return (
			(coords.latitude in bottomRight.latitude..topLeft.latitude) &&
			(coords.longitude in topLeft.longitude..bottomRight.longitude)
		)
	}

	fun metersToPixels(distanceInMeters: Float) : Float {
		val angle = distanceInMeters / earthRadiusMeters
		return abs(angle * horizontalPixelDensity * radiansToDegrees).toFloat()
	}
}

data class TileRegion(
	val topLeft: IntOffset,
	val bottomRight: IntOffset
)

fun defineTileRegion(base: IntOffset, dimensions: Size) : TileRegion {
	return TileRegion(
		IntOffset(base.x - dimensions.width, base.y - dimensions.height),
		IntOffset(base.x + dimensions.width, base.y + dimensions.height)
	)
}

fun IntOffset.minus(x: Int, y: Int) = IntOffset(this.x - x, this.y - y)

/**
 *
 * .--------> (x)
 * |
 * |
 * V
 *
 * (y)

 * Sq1 = (A,B)
 * Sq2 = (C,D)
 *                          ProjY(1)
 *  A-----------.             _ 
 *  |           |             {   ProjY(2)
 *  |      C----+-----.       } . .  _ . . _
 *  |      |    |     |       {      {     }   IntercY(1,2)
 *  |      |    |     |       }      }     {
 *  '------+----B     |       - . .  { . . -
 *         |          |              }
 *         '----------D              -
 *
 *
 *  |~~~~~~~~~~| ProjX(1)
 *         '   '
 *         '   '
 *         |~~~~~~~~~~| ProjX(2)
 *         '   '
 *         '   '
 *         (~~~) IntercX(1,2)
 */

fun TileRegion.intersects(other: TileRegion) : Boolean {
	val topLeftIntersection = IntOffset(
		max(this.topLeft.x, other.topLeft.x),
		max(this.topLeft.y, other.topLeft.y)
	)

	val bottomRightIntersection = IntOffset(
		min(this.bottomRight.x, other.bottomRight.x),
		min(this.bottomRight.y, other.bottomRight.y)
	)

	return (
		(bottomRightIntersection.x > topLeftIntersection.x) &&
		(bottomRightIntersection.y > topLeftIntersection.y)
	)
}

const val kartaTileSize = 256
const val earthRadiusMeters = 6378137.0
const val radiansToDegrees = 180.0 / PI

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
		.size(kartaTileSize.dp)
	) {
		AsyncImage(
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			placeholder = painterResource("composeResources/karta.composeapp.generated.resources/drawable/grid.png"),
			modifier = Modifier.height(kartaTileSize.dp).width(kartaTileSize.dp),
			model = ImageRequest.Builder(LocalPlatformContext.current)
				.data(formattedUrl)
				.httpHeaders(headers.build())
				.build()
		)
	}
}

class PointerFlows(
	mutableMoveFlow: MutableSharedFlow <PointerEvent>,
	mutableClickFlow: MutableSharedFlow <PointerEvent>
) {
	val moveFlow: SharedFlow <PointerEvent> = mutableMoveFlow
	val clickFlow: SharedFlow <PointerEvent> = mutableClickFlow
}

/* ------------------------------------------------------------------------- */

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

val LocalPointerEvents = compositionLocalOf { PointerFlows(
	MutableSharedFlow <PointerEvent> (),
	MutableSharedFlow <PointerEvent> ()
)}

/* ------------------------------------------------------------------------- */

data class Header(
	val key: String,
	val value: String
)

data class TileServer(
	val tileUrl: String,
	val requestHeaders: List <Header> = listOf()
)


@Composable
fun Karta(
	tileServer: TileServer,
	initialCoords: Coordinates,
	initialZoom: Int = 14,
	requestHeaders: String? = null,
	content: @Composable () -> Unit = {})
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
				content
			)
		}
	}
}

data class PointerEvent(
	val coordinates: Coordinates,
	val position: Offset
)

fun PointerEvent.isInside(tileRegion: TileRegion) : Boolean {
	val (x, y) = this.position

	return (
		x.toInt() in tileRegion.topLeft.x..tileRegion.bottomRight.x &&
		y.toInt() in tileRegion.topLeft.y..tileRegion.bottomRight.y
	)
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
	content: @Composable () -> Unit)
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

	val moveFlow = remember { MutableSharedFlow <PointerEvent> (extraBufferCapacity = 1) }
	val clickFlow = remember { MutableSharedFlow <PointerEvent> (extraBufferCapacity = 1) }

	val pointerEvents = remember(moveFlow, clickFlow) {
		PointerFlows(
			moveFlow,
			clickFlow
	)}

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

				moveFlow.tryEmit(
					PointerEvent(
						cursor,
						position
					)
				)
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
			LocalConverter provides converter,
			LocalPointerEvents provides pointerEvents
		) {
			content()
		}
	}
}


@Composable
fun Marker(
	coords: Coordinates,
	offset: IntOffset? = null,
	minZoom: Int = 13,
	extension: Size? = null,
	onClick: () -> Unit = {},
	content: @Composable (coordsOffset: IntOffset) -> Unit
) {
	val zoom = LocalZoom.current
	val converter = LocalConverter.current

	val coordsOffset = remember(coords, converter, offset) {
		val finalOffset = offset ?: IntOffset(0, 0)
		converter.convertToOffset(coords)
			.minus(finalOffset)
	}

	if (zoom >= minZoom) {
		if (converter.insideView(coords, extension)) {
			content(coordsOffset)
		}
	}
}

@Composable
fun Pin(
	coords: Coordinates,
	dimensions: Size,
	path: String = "composeResources/karta.composeapp.generated.resources/drawable/pin.png",
	hovered: String = "composeResources/karta.composeapp.generated.resources/drawable/bluePin.png",
) {
	val painter = painterResource(path)
	val hoveredPainter = painterResource(hovered)

	val pinSize = remember(painter, dimensions) {
		val (width, height) = painter.intrinsicSize

		if (width > height) {
			val aspectRatio = height.toFloat() / width.toFloat()
			val proportionalWidth = (dimensions.width * aspectRatio).toInt()
			Size(proportionalWidth, dimensions.height)
		}

		else {
			val aspectRatio = width.toFloat() / height.toFloat()
			val proportionalHeight = (dimensions.height * aspectRatio).toInt()
			Size(dimensions.width, proportionalHeight)
		}
	}

	Marker(
		coords = coords,
		extension = Size(
			pinSize.width,
			pinSize.height * 2
		)
	) { coordsOffset ->
		val pointerEvents = LocalPointerEvents.current
		val compensedOffset = remember(coordsOffset, pinSize) { IntOffset(
			coordsOffset.x - (pinSize.width / 2).toInt(),
			coordsOffset.y - pinSize.height
		)}

		var isHovered by remember { mutableStateOf(false) }
		val ownExtension = remember(coordsOffset, pinSize) {
			val halfSize = pinSize.div(2)

			defineTileRegion(
				coordsOffset.minus(0, halfSize.height),
				halfSize)
		}

		LaunchedEffect(pointerEvents, ownExtension) {
			pointerEvents.moveFlow.collect { event -> 
				isHovered = event.isInside(ownExtension)
			}
		}

		Image(
			modifier = Modifier
				.offset { compensedOffset }
				.width(pinSize.width.dp)
				.height(pinSize.height.dp),

			painter = if (isHovered) hoveredPainter else painter,
			contentDescription = null
		)
	}
}

enum class DistanceUnit {
	METERS,
	PIXELS
}

@Composable
fun Circle(
	coords: Coordinates,
	radius: Float,
	radiusUnit: DistanceUnit = DistanceUnit.PIXELS,
	borderWidth: Float = 0f,
	borderColor: Color = Color.Black,
	fillColor: Color? = Color.Black
) {
	val converter = LocalConverter.current
	val radiusInPixels = remember(radius, radiusUnit, converter) {
		when (radiusUnit) {
			DistanceUnit.METERS -> converter.metersToPixels(radius)
			DistanceUnit.PIXELS -> radius
		}
	}

	Marker(
		coords = coords,
		extension = Size(
			(2f * radiusInPixels).toInt(),
			(2f * radiusInPixels).toInt()
		)
	) { coordsOffset ->
		Canvas(modifier = Modifier.offset { coordsOffset }) {
			if (null != fillColor) {
				drawCircle(
					color = fillColor,
					radius = radiusInPixels
				)
			}

			if (borderWidth > 0) {
				drawCircle(
					color = borderColor,
					style = Stroke(borderWidth),
					radius = radiusInPixels
				)
			}
		}
	}
}

@Composable
fun Polyline(
	coordsList: List <Coordinates>,
	strokeColor: Color = Color.Black,
	strokeWidth: Float = 1.0f,
	fillColor: Color? = null,
	fillAlpha: Float = 1f,
	closed: Boolean = false
) {
	val converter = LocalConverter.current
	val offsets = remember(coordsList, converter) {
		coordsList.map { coords ->
			val intOffset = converter.convertToOffset(coords)
			intOffset.toOffset()
		}
	}

	val path = remember(offsets, closed) {
		val newPath = Path()

		if (offsets != null && offsets.size > 0) {
			val start = offsets.get(0)
			newPath.moveTo(
				start.x,
				start.y
			)

			for (i in 1 .. offsets.size-1) {
				val next = offsets.get(i)
				newPath.lineTo(
					next.x,
					next.y
				)
			}

			if (closed && offsets.size > 2) {
				newPath.close()
			}
		}

		newPath
	}

	val polylineBoundaries = remember(offsets) {
		var xMin = converter.tileRegion.bottomRight.x
		var yMin = converter.tileRegion.bottomRight.y
		var xMax = converter.tileRegion.topLeft.x
		var yMax = converter.tileRegion.topLeft.y

		offsets.forEach { offset ->
			xMin = min(xMin, offset.x.toInt())
			yMin = min(yMin, offset.y.toInt())
			xMax = max(xMax, offset.x.toInt())
			yMax = max(yMax, offset.y.toInt())
		}

		TileRegion(
			IntOffset(xMin, yMin),
			IntOffset(xMax, yMax)
		)
	}

	if (!converter.tileRegion.intersects(polylineBoundaries)) {
		return
	}

	if (null != fillColor) {
		Canvas(modifier = Modifier) {
			drawPath(
				path = path,
				color = fillColor,
				alpha = fillAlpha,
				style = Fill
			)
		}
	}

	Canvas(modifier = Modifier) {
		drawPath(
			path = path,
			color = strokeColor,
			style = Stroke(strokeWidth)
		)
	}
}

val home = Coordinates(-20.296099, -40.348038)
val cefet = Coordinates(-20.310563, -40.318772)
val ilhaBoi = Coordinates(-20.310662, -40.2815008)

val rota = listOf(
	Coordinates(-20.311070, -40.302298),
	Coordinates(-20.307223, -40.302819),
	Coordinates(-20.301494, -40.298605),
	Coordinates(-20.287535, -40.304205)
)

val aeroporto = listOf(
	Coordinates(-20.265507, -40.296735),
	Coordinates(-20.272795, -40.284709),
	Coordinates(-20.271891, -40.283167),
	Coordinates(-20.273302, -40.280667),
	Coordinates(-20.269135, -40.274689),
	Coordinates(-20.244144, -40.278206),
	Coordinates(-20.242743, -40.280783)
)

val smapsServer = TileServer("http://localhost:8077/{zoom}/{x}/{y}")
val googleSatelliteServer = TileServer("https://mt0.google.com/vt/lyrs=s&x={x}&y={y}&z={zoom}")

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
	TileServerOption("OpenStreetMaps", openStreetMapServer),
	TileServerOption("Google Satellite", googleSatelliteServer)
)


@Composable
fun App() {
	var tileServerIndex by remember { mutableStateOf(0) }
	val selectedTileServer = tileServerOptions.get(tileServerIndex)

	Karta(
		tileServer = selectedTileServer.server,
		initialCoords = home,
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

		Pin(
			coords = home,
			dimensions = Size(40, 40)
		)

		for (k in 1..3) {
			Circle(
				coords = home,
				radius = k * 500f,
				radiusUnit = DistanceUnit.METERS,
				borderWidth = 2f,
				fillColor = null
			)
		}

		Circle(
			coords = ilhaBoi,
			radius = 10f,
			borderWidth = 1f,
			fillColor = Color.Blue
		)

		Pin(
			coords = cefet,
			dimensions = Size(50, 50)
		)

		Polyline(
			coordsList = rota,
			strokeColor = Color.Blue,
			strokeWidth = 5.0f
		)

		Polyline(
			coordsList = aeroporto,
			strokeColor = Color.Green,
			fillColor = Color.Green,
			fillAlpha = 0.6f
		)
	}
}

