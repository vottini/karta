package systems.untangle.karta.input

import androidx.compose.ui.input.pointer.PointerEvent

data class AugmentedPointerEvent (
    val event: PointerEvent,
    val position: PointerPosition
)
