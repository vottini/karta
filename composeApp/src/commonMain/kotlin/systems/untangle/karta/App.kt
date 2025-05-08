package systems.untangle.karta

import SelectionItem
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.LaunchedEffect
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
import systems.untangle.karta.selection.rememberSelection


/* -------------------------------------------------------------------------- */

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
		var cefetPressed by remember { mutableStateOf(false) }

		var homeCoords by remember { mutableStateOf(home) }
		var homePressed by remember { mutableStateOf(false) }

		var hoveredElement by remember { mutableStateOf("") }
		var selectedElement by remember { mutableStateOf("") }

		for (k in 1..3) {
			Circle(
				coords = homeCoords,
				radius = k * 500f,
				radiusUnit = DistanceUnit.METERS,
				borderWidth = 2f,
				fillColor = null
			)
		}

		var mapSelection by rememberSelection()

		SelectionItem(
			selectionState = mapSelection,
			itemId = "cefet"
		) { ownState ->
			println("CEFET State is $ownState")
		}

		SelectionItem(
			selectionState = mapSelection,
			itemId = "home"
		) { ownState ->
			println("HOME State is $ownState")
		}

		/*

		var mapSelection: SelectionState = rememberSelection()
		val elements = remember { mutableStateListOf <GeoObj> () }

		for (i in elements.indices) {
			val obj = elements[i]

			SelectionItem(
				selectionState = mapSelection,
				itemId = obj.id
			) { ownState ->
				val (hovered, selected) = ownState

				Pin(
					coords = obj.coords,
					sprite = if (selected) greenPin else if (hovered) bluePin else redPin,
					dimensions = Size(40, 40),
					selectionGroup = mapSelection,
					onClick = { event ->
						if (event.action == ButtonAction.PRESS) {
							pointerEvents.dragFlow.collect { deltaPosition ->
								elements[i] = obj.copy(coords = deltaPosition.current.coordinates)
							}
						}
					}
				)
			}
		}

		*/

		Pin(
			coords = homeCoords,
			sprite = if (selectedElement == "home") greenPin else if (hoveredElement == "home") bluePin else redPin,
			dimensions = Size(40, 40),
			onHover = { hovered ->
				if (hovered) {
					mapSelection = mapSelection.copy(currentHover = "home")
					hoveredElement = "home"
				}

				if (!hovered && hoveredElement == "home") {
					mapSelection = mapSelection.copy(currentHover = "")
					hoveredElement = ""
				}
			},
			onClick = { event ->
				selectedElement = "home"
				mapSelection = mapSelection.copy(currentSelection = "home")
				homePressed = (event.action == ButtonAction.PRESS)
			}
		)

		Circle(
			coords = ilhaBoi,
			radius = 10f,
			borderWidth = 1f,
			fillColor = Color.Blue
		)

		Pin(
			coords = cefetCoords,
			sprite = if (selectedElement == "cefet") greenPin else if (hoveredElement == "cefet") bluePin else redPin,
			dimensions = Size(50, 50),
			onHover = { hovered ->
				if (hovered) {
					hoveredElement = "cefet"
					mapSelection = mapSelection.copy(currentHover = "cefet")
				}

				if (!hovered && hoveredElement == "cefet") {
					mapSelection = mapSelection.copy(currentHover = "")
					hoveredElement = ""
				}
			},
			onClick = { event ->
				selectedElement = "cefet"
				cefetPressed = (event.action == ButtonAction.PRESS)
				mapSelection = mapSelection.copy(currentSelection = "cefet")
			}
		)

		LaunchedEffect(selectedElement, cefetPressed) {
			if (cefetPressed && selectedElement == "cefet") {
				pointerEvents.dragFlow.collect { deltaPosition ->
					cefetCoords = deltaPosition.current.coordinates
				}
			}
		}

		LaunchedEffect(selectedElement, homePressed) {
			if (homePressed && selectedElement == "home") {
				pointerEvents.dragFlow.collect { deltaPosition ->
					homeCoords = deltaPosition.current.coordinates
				}
			}
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

		//Row(

		//) {
		//}
	}
}
