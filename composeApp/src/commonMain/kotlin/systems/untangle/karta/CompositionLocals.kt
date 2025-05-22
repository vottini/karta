package systems.untangle.karta

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import systems.untangle.karta.conversion.Converter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Region
import systems.untangle.karta.data.Size
import systems.untangle.karta.input.ButtonEvent
import systems.untangle.karta.input.DeltaPosition
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.PointerFlows

val LocalCursor = compositionLocalOf { Coordinates(0.0, 0.0) }
val LocalZoom = compositionLocalOf { 14 }

val LocalViewingRegion = compositionLocalOf {
    Region(
        Coordinates(0.0, 0.0),
        Coordinates(0.0, 0.0)
    )
}

val LocalConverter = compositionLocalOf {
    Converter(
        Region(
            Coordinates(1.0, 0.0),
        Coordinates(0.0, 1.0)
        ),
        Size(0, 0)
    )
}

val LocalPointerEvents = compositionLocalOf {
    PointerFlows(
        MutableSharedFlow<PointerPosition>(),
        MutableSharedFlow<ButtonEvent>(),
        MutableSharedFlow<PointerPosition>(),
        MutableSharedFlow<PointerPosition>(),
        MutableSharedFlow<DeltaPosition>()
    )
}
