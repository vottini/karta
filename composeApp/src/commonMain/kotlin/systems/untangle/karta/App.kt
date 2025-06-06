package systems.untangle.karta

import systems.untangle.karta.selection.SelectionItem
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

import systems.untangle.karta.composables.Circle
import systems.untangle.karta.composables.Karta
import systems.untangle.karta.composables.Pin
import systems.untangle.karta.composables.Polyline
import systems.untangle.karta.popup.Popup
import systems.untangle.karta.popup.PopupItem

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.DistanceUnit
import systems.untangle.karta.network.Header
import systems.untangle.karta.network.TileServer
import systems.untangle.karta.popup.rememberPopupContext
import systems.untangle.karta.selection.rememberSelectionContext
import systems.untangle.karta.selection.ItemSelectionState

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.redPin
import karta.composeapp.generated.resources.bluePin
import karta.composeapp.generated.resources.greenPin
import org.jetbrains.compose.resources.DrawableResource
import systems.untangle.karta.data.px

val redPin = Res.drawable.redPin
val bluePin = Res.drawable.bluePin
val greenPin = Res.drawable.greenPin

fun choosePinResource(itemState: ItemSelectionState): DrawableResource {
	return if (itemState.selected) greenPin
		else if (itemState.hovered) bluePin
		else redPin
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
	TileServerOption("OpenStreetMaps", openStreetMapServer),
	TileServerOption("Google Satellite", googleSatelliteServer),
	TileServerOption("SMAPS", smapsServer)
)

data class PointOfInterest(
	val name: String,
	val coordinates: Coordinates
)


@Composable
fun App() {
	var tileServerIndex by remember { mutableStateOf(0) }
	val selectedTileServer = tileServerOptions[tileServerIndex]

	val createdPins = remember { mutableStateListOf<PointOfInterest>() }
	val createdCircles = remember { mutableStateListOf<PointOfInterest>() }

	val selectionContext = rememberSelectionContext()
	val popupContext = rememberPopupContext()

	val options = remember(createdPins) {
		listOf(
			PopupItem("Create new Pin") { coords ->
				val pinName = "PIN${createdPins.size}"
				val poi = PointOfInterest(pinName, coords)
				createdPins.add(poi)
			},

			PopupItem("Create new Circle") { coords ->
				val pinName = "CIRCLE${createdCircles.size}"
				val poi = PointOfInterest(pinName, coords)
				createdCircles.add(poi)
			},

			PopupItem("Start new Polyline") { println("LILI") }
		)
	}


	Karta(
		initialCoords = home,
		initialZoom = 14,
		tileServer = selectedTileServer.server,
		onMapDragged = { popupContext.hide() },
		onPress = {
			selectionContext.clearSelection()
			popupContext.hide()
		},
		onLongPress = { pointerPosition ->
			val coordinates = pointerPosition.coordinates
			popupContext.show(
				coordinates,
				options)
		}
	) {
		val cursor = LocalCursor.current
		val viewingRegion = LocalViewingBoundingBox.current
		val pointerEvents = LocalPointerEvents.current
		val converter = LocalConverter.current
		val zoom = LocalZoom.current

		var homeCoords by remember { mutableStateOf(home) }
		val cefetCoords by remember { mutableStateOf(cefet) }

		SelectionItem(
			selectionContext = selectionContext,
			itemId = "home"
		) { itemState ->
			for (k in 1..3) {
				Circle(
					coords = homeCoords,
					radius = k * 500f,
					radiusUnit = DistanceUnit.METERS,
					borderWidth = 2f,
					fillColor = null
				)
			}

			Pin(
				coords = homeCoords,
				itemSelectionState = itemState,
				sprite = choosePinResource(itemState),
				dimensions = PxSize(60.px, 60.px),
				onClick = { event ->
					if (itemState.selected) {
						launch {
							val offset = event.position.coordinates.minus(homeCoords)
							pointerEvents.dragFlow.collect { deltaPosition ->
								homeCoords = deltaPosition.current.coordinates
									.plus(offset)
							}
						}
					}
				}
			)
		}

		SelectionItem(
			selectionContext = selectionContext,
			itemId = "cefet"
		) { itemState ->
			Pin(
				coords = cefetCoords,
				itemSelectionState = itemState,
				sprite = choosePinResource(itemState),
				dimensions = PxSize(60.px, 60.px)
			)
		}

		createdPins.forEach { poi ->
			SelectionItem(
				selectionContext = selectionContext,
				itemId = poi.name
			) { itemState ->
				Pin(
					coords = poi.coordinates,
					itemSelectionState = itemState,
					sprite = choosePinResource(itemState),
					dimensions = PxSize(60.px, 60.px),
					onLongPress = {
						popupContext.show(
							poi.coordinates,
							listOf (
								PopupItem("Remove Pin") {
									createdPins.remove(poi)
								},
							))
					}
				)
			}
		}

		createdCircles.forEach { poi ->
			SelectionItem(
				selectionContext = selectionContext,
				itemId = poi.name
			) { itemState ->
				Circle(
					coords = poi.coordinates,
					radius = 10f,
					borderWidth = 1f,
					fillColor = if (itemState.selected) Color.Green else Color.Blue
				)
			}
		}

		Circle(
			coords = ilhaBoi,
			radius = 10f,
			borderWidth = 1f,
			fillColor = Color.Blue
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

		if (popupContext.hasContents) {
			Popup(popupContext)
		}

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
