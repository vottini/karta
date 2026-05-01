# Karta

A tile map component in pure Compose Multiplatform — no native map SDKs required.

Karta renders interactive slippy maps (pan, zoom, overlays) using any XYZ tile server, built entirely with Kotlin and Compose. Supports **Android** and **Desktop (JVM)**.

---

## Installation

```kotlin
// build.gradle.kts
implementation("systems.untangle:karta:0.2.0")
```

Published to Maven Central. Requires Compose Multiplatform and a `commonMain` or platform-specific source set.

---

## Quick Start

```kotlin
Karta(
    tileServer = TileServer("https://tile.openstreetmap.org/{zoom}/{x}/{y}.png"),
    initialCoords = Coordinates(48.8566, 2.3522), // Paris
    initialZoom = 13
)
```

---

## The `Karta` Composable

```kotlin
@Composable
fun Karta(
    tileServer: TileServer,
    interactive: Boolean = true,
    initialCoords: Coordinates,
    initialZoom: Int = 14,
    maxZoom: Int = 19,
    minZoom: Int = 2,
    viewFlow: Flow<ViewSpec>? = null,
    onPress: suspend (PointerPosition) -> Unit = {},
    onLongPress: suspend (PointerPosition) -> Unit = {},
    onCursorMove: suspend (PointerPosition) -> Unit = {},
    onMapDragged: suspend (Coordinates) -> Unit = {},
    onZoomChange: suspend (Int) -> Unit = {},
    content: @Composable () -> Unit = {}
)
```

| Parameter | Description |
|-----------|-------------|
| `tileServer` | Tile source with URL template and optional HTTP headers |
| `interactive` | Enable/disable pan and zoom gestures |
| `initialCoords` | Starting map center as `Coordinates(latitude, longitude)` |
| `initialZoom` | Starting zoom level |
| `minZoom` / `maxZoom` | Zoom boundaries (defaults: 2–19) |
| `viewFlow` | `Flow<ViewSpec>` to programmatically move/zoom the map |
| `onPress` | Called when the user clicks the map |
| `onLongPress` | Called after a ~500 ms press |
| `onCursorMove` | Called on pointer movement with the geographic position |
| `onMapDragged` | Called with the new center after a drag |
| `onZoomChange` | Called with the new zoom level after a scroll/pinch |
| `content` | Composable slot for overlays (markers, polylines, etc.) |

### Tile Server

```kotlin
// Public tile server
val osm = TileServer("https://tile.openstreetmap.org/{zoom}/{x}/{y}.png")

// Authenticated tile server
val private = TileServer(
    tileUrl = "https://api.example.com/tiles/{zoom}/{x}/{y}.png",
    requestHeaders = listOf(Header("Authorization", "Bearer $token"))
)
```

---

## Overlays

All overlay composables are placed inside the `content` lambda of `Karta`. They automatically receive the current zoom, converter, and cursor state via composition locals.

### Marker

A clickable, hoverable point at a geographic location.

```kotlin
Marker(
    coords = Coordinates(48.8566, 2.3522),
    anchoring = DoubleOffset(0.5, 1.0), // bottom-center anchor
    onClick = { event -> /* ButtonEvent */ },
    onShortPress = { position -> /* quick tap */ },
    onLongPress = { position -> /* long press */ },
    onHover = { isHovered -> /* hover state */ }
) {
    Icon(
        painter = painterResource(Res.drawable.pin),
        contentDescription = null,
        modifier = Modifier.size(32.dp)
    )
}
```

`anchoring` is a `DoubleOffset(x, y)` in the `[0, 1]` range — `(0.5, 0.5)` centers the composable on the coordinate; `(0.5, 1.0)` pins the bottom-center to the coordinate (typical for pin icons).

#### Marker with Selection State

When managing multiple selectable markers, use `ItemSelectionState` to track hover, selection, and drag across items:

```kotlin
val selectionContext = rememberSelectionContext()

SelectionItem(selectionContext, itemId = "marker-1") { selectionState ->
    Marker(
        coords = myCoords,
        itemSelectionState = selectionState,
        onSelectionChange = { /* react to selection */ }
    ) {
        // use selectionState.hovered / .selected to style the marker
    }
}
```

#### Movable Marker

A marker the user can drag to a new position:

```kotlin
var markerCoords by remember { mutableStateOf(Coordinates(48.8566, 2.3522)) }

MovableMarker(
    coords = markerCoords,
    coordsSetter = { newCoords -> markerCoords = newCoords }
) {
    /* marker content */
}
```

---

### Circle

```kotlin
Circle(
    coords = Coordinates(48.8566, 2.3522),
    radius = 500f,
    radiusUnit = DistanceUnit.METERS, // or DistanceUnit.PIXELS
    fillColor = Color.Blue.copy(alpha = 0.2f),
    borderColor = Color.Blue,
    borderWidth = 2f
)
```

---

### Polyline / Polygon

```kotlin
// Open polyline (route)
Polyline(
    coordsList = listOf(
        Coordinates(48.85, 2.35),
        Coordinates(48.86, 2.36),
        Coordinates(48.87, 2.34)
    ),
    strokeColor = Color.Red,
    strokeWidth = 4f
)

// Dashed polyline
Polyline(
    coordsList = route,
    strokeColor = Color.Blue,
    strokeWidth = 3f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
)

// Filled polygon
Polyline(
    coordsList = polygonCoords,
    strokeColor = Color.Green,
    strokeWidth = 2f,
    fillColor = Color.Green,
    fillAlpha = 0.3f,
    closed = true
)
```

#### Editable Polyline

Renders drag handles at each vertex so the user can reshape the line:

```kotlin
var coords by remember { mutableStateOf(initialCoords) }

EditablePolyline(
    coordsList = coords,
    strokeColor = Color.Blue,
    strokeWidth = 3f,
    coordsSetter = { index, newCoords ->
        coords = coords.toMutableList().also { it[index] = newCoords }
    }
)
```

---

### Sprite (Image Overlay)

Display a drawable resource at a geographic position (typically inside a `Marker`):

```kotlin
Marker(coords = myCoords) {
    Sprite(
        resource = Res.drawable.my_icon,
        dimensions = PxSize(48.px, 48.px)
    )
}
```

---

### Geolocated

Low-level helper that converts a `Coordinates` to a screen `IntOffset`. Use it when building custom overlays that need pixel-accurate positioning:

```kotlin
Geolocated(
    coordinates = Coordinates(48.8566, 2.3522),
    wrapLongitude = true
) { screenOffset ->
    // place arbitrary composable at screenOffset
}
```

---

### Popup / Context Menu

```kotlin
val popupContext = rememberPopupContext()

Karta(
    ...,
    onLongPress = { position ->
        popupContext.show(
            coordinates = position.coordinates,
            options = listOf(
                PopupItem("Add waypoint") { coords -> /* ... */ },
                PopupItem("Measure distance") { coords -> /* ... */ }
            )
        )
    }
) {
    Popup(
        context = popupContext,
        background = Color.White,
        color = Color.Black
    )
}
```

---

## Programmatic Map Control

Use `viewFlow` to move or zoom the map from outside the composable:

```kotlin
val viewFlow = remember { MutableSharedFlow<ViewSpec>(extraBufferCapacity = 1) }

Karta(tileServer = osm, initialCoords = paris, viewFlow = viewFlow) { /* ... */ }

// Elsewhere — e.g. in response to a button click:
scope.launch {
    viewFlow.emit(ViewSpec(centerCoordinates = Coordinates(51.5074, -0.1278), zoom = 12))
}
```

`ViewSpec` fields are nullable — pass only the ones you want to change.

---

## Accessing Map State from Overlays

Inside the `content` lambda, map state is available via composition locals:

```kotlin
Karta(...) {
    val zoom = LocalZoom.current               // ZoomLevel
    val cursor = LocalCursor.current           // Coordinates? (null when outside map)
    val bounds = LocalViewingBoundingBox.current  // BoundingBox
    val converter = LocalConverter.current     // Converter
    val pointerEvents = LocalPointerEvents.current // PointerFlows
}
```

### Converter

Convert between geographic and pixel coordinates at the current zoom:

```kotlin
val converter = LocalConverter.current
val pixels = converter.metersToPixels(distanceInMeters = 100f)
```

### Pointer Events

Subscribe to raw map pointer events inside an overlay:

```kotlin
val events = LocalPointerEvents.current

LaunchedEffect(Unit) {
    events.dragFlow.collect { delta ->
        // delta.previous, delta.current, delta.diff
    }
}
```

| Flow | Type | Description |
|------|------|-------------|
| `moveFlow` | `PointerPosition?` | Cursor movement |
| `clickFlow` | `ButtonEvent` | Button press / release |
| `shortPressFlow` | `PointerPosition` | Quick click |
| `longPressFlow` | `PointerPosition` | ~500 ms press |
| `dragFlow` | `DeltaPosition` | Drag delta |

---

## Core Types

### `Coordinates`
```kotlin
Coordinates(latitude = 48.8566, longitude = 2.3522)
```

### `BoundingBox`
```kotlin
BoundingBox(
    topLeft = Coordinates(49.0, 2.0),
    bottomRight = Coordinates(48.0, 3.0)
)
```

### `Px` / `PxSize`
```kotlin
val size = PxSize(width = 256.px, height = 256.px)
val half = size.halfWidth   // 128.px
```

### `DoubleOffset`
```kotlin
DoubleOffset(x = 0.5, y = 1.0)
```

### `ViewSpec`
```kotlin
ViewSpec(centerCoordinates = Coordinates(...), zoom = 15)
```

---

## Coordinate Utilities

```kotlin
// Decimal degrees → DMS string
latitudeDMS(48.8566)                    // "48°51'23"N"
longitudeDMS(2.3522)                    // "002°21'07"E"

// With one decimal digit of seconds
latitudeDMS(48.8566, decimalSeconds = true)   // "48°51'23.4"N"
longitudeDMS(2.3522, decimalSeconds = true)   // "002°21'07.3"E"

// Normalize longitude to [-180, 180]
val normalized = Coordinates(0.0, 370.0).wrapLongitude()
```

---

## Selection System

For maps with many interactive items, use the selection system to coordinate hover/select state across markers:

```kotlin
val ctx = rememberSelectionContext()

// Observe selection changes
LaunchedEffect(ctx) {
    ctx.selectionFlow.collect { state ->
        println("selected: ${state.currentSelection}, hovered: ${state.currentHover}")
    }
}

// Wrap each marker
SelectionItem(ctx, itemId = "poi-42") { sel ->
    Marker(coords = ..., itemSelectionState = sel) {
        Box(Modifier.background(if (sel.selected) Color.Blue else Color.Gray)) { /* ... */ }
    }
}
```

---

## Platform Notes

| Platform | Status | Notes |
|----------|--------|-------|
| Android | Supported | minSdk 21, Jetpack Compose interop |
| Desktop (JVM) | Supported | Mouse events, scroll wheel zoom |

---

## License

[MIT](LICENSE) © Gustavo Venturini
