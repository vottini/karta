package systems.untangle.karta

import systems.untangle.karta.selection.SelectionItem
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

import kotlinx.coroutines.flow.MutableStateFlow
import systems.untangle.karta.composables.Circle
import systems.untangle.karta.composables.Karta
import systems.untangle.karta.composables.Pin
import systems.untangle.karta.composables.Polyline
import systems.untangle.karta.composables.bluePin
import systems.untangle.karta.composables.greenPin
import systems.untangle.karta.composables.redPin

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Size
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.input.ButtonAction
import systems.untangle.karta.network.Header
import systems.untangle.karta.network.TileServer
import systems.untangle.karta.selection.SelectionState

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
	TileServerOption("OpenStreetMaps", openStreetMapServer),
	TileServerOption("Google Satellite", googleSatelliteServer),
	TileServerOption("SMAPS", smapsServer)
)


@Composable
fun App() {
	var tileServerIndex by remember { mutableStateOf(0) }
	val selectedTileServer = tileServerOptions[tileServerIndex]

	Karta(
		tileServer = selectedTileServer.server,
		initialCoords = home,
	) {
		val cursor = LocalCursor.current
		val viewingRegion = LocalViewingRegion.current
		val converter = LocalConverter.current
		val zoom = LocalZoom.current

		val pointerEvents = LocalPointerEvents.current
		var cefetCoords by remember { mutableStateOf(cefet) }
		var homeCoords by remember { mutableStateOf(home) }

		Circle(
			coords = ilhaBoi,
			radius = 10f,
			borderWidth = 1f,
			fillColor = Color.Blue
		)

		for (k in 1..3) {
			Circle(
				coords = homeCoords,
				radius = k * 500f,
				radiusUnit = DistanceUnit.METERS,
				borderWidth = 2f,
				fillColor = null
			)
		}

		val selectionFlux = remember {
			MutableStateFlow(SelectionState())
		}

		SelectionItem(
			selectionFlow = selectionFlux,
			itemId = "home"
		) { itemState ->
			Pin(
				coords = homeCoords,
				selectionContext = itemState,
				sprite = if (itemState.selected) greenPin else if (itemState.hovered) bluePin else redPin,
				dimensions = Size(40, 40),
				onClick = { event ->
					if (event.action == ButtonAction.PRESS) {
						pointerEvents.dragFlow.collect { deltaPosition ->
							homeCoords = deltaPosition.current.coordinates
						}
					}
				}
			)
		}

		SelectionItem(
			selectionFlow = selectionFlux,
			itemId = "cefet"
		) { itemState ->
			Pin(
				coords = cefetCoords,
				selectionContext = itemState,
				sprite = if (itemState.selected) greenPin else if (itemState.hovered) bluePin else redPin,
				dimensions = Size(50, 50)
			)
		}

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

		Column {
			Text("${cursor.latitude}")
			Text("${cursor.longitude}")
			Text("${viewingRegion.topLeft}")
			Text("${viewingRegion.bottomRight}")
			Text("${converter.convertToOffset(cursor)}")
			Text("Zoom = $zoom")

			Button(
				onClick = {
					val nextIndex = (tileServerIndex + 1) % tileServerOptions.size
					tileServerIndex = nextIndex
				}
			) {
				Text("Mudar Mapa")
			}
		}

		//Row(

		//) {
		//}
	}
}
