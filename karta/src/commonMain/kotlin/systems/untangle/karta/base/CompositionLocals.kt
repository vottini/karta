package systems.untangle.karta.base

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import systems.untangle.karta.conversion.Converter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.DoubleOffset
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.ZoomLevel
import systems.untangle.karta.data.px
import systems.untangle.karta.input.PointerFlows

/** Current zoom level of the map. Provides [ZoomLevel.increment] and [ZoomLevel.decrement]
 *  callbacks. Only meaningful inside a [systems.untangle.karta.Karta] `content` lambda. */
val LocalZoom = compositionLocalOf { ZoomLevel(14, {}, {}) }

/** Geographic coordinates currently under the pointer, or `null` when the cursor is outside the
 *  map. Only meaningful inside a [systems.untangle.karta.Karta] `content` lambda. */
val LocalCursor = compositionLocalOf <Coordinates?> { null }

/** The geographic [BoundingBox] currently visible in the map viewport.
 *  Only meaningful inside a [systems.untangle.karta.Karta] `content` lambda. */
val LocalViewingBoundingBox = compositionLocalOf {
    BoundingBox(
        Coordinates(0.0, 0.0),
        Coordinates(0.0, 0.0)
    )
}

/** [Converter] instance for the current zoom and viewport. Use it to convert between geographic
 *  coordinates and screen pixels. Only meaningful inside a [systems.untangle.karta.Karta]
 *  `content` lambda. */
val LocalConverter = compositionLocalOf {
    Converter(
        BoundingBox(Coordinates(1.0, 0.0), Coordinates(0.0, 1.0)),
        PxSize(0.px, 0.px),
        DoubleOffset(0.5, 0.5),
        1
    )
}

/** [PointerFlows] for the current map instance. Collect from the individual flows inside
 *  `LaunchedEffect` blocks to react to pointer input in custom overlays.
 *  Only meaningful inside a [systems.untangle.karta.Karta] `content` lambda. */
val LocalPointerEvents = compositionLocalOf {
    PointerFlows(
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow()
    )
}
