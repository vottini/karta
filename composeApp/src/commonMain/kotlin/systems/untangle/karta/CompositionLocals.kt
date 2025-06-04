package systems.untangle.karta

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import systems.untangle.karta.conversion.Converter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.BoundingBox
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.input.PointerFlows

val LocalCursor = compositionLocalOf { Coordinates(0.0, 0.0) }
val LocalZoom = compositionLocalOf { 14 }

val LocalViewingBoundingBox = compositionLocalOf {
    BoundingBox(
        Coordinates(0.0, 0.0),
        Coordinates(0.0, 0.0)
    )
}

val LocalConverter = compositionLocalOf {
    Converter(
        BoundingBox(
            Coordinates(1.0, 0.0),
            Coordinates(0.0, 1.0)
        ),
        PxSize(0, 0),
        1f
    )
}

val LocalPointerEvents = compositionLocalOf {
    PointerFlows(
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow(),
        MutableSharedFlow()
    )
}
