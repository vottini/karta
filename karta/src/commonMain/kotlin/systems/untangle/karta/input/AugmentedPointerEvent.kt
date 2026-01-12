package systems.untangle.karta.input

import androidx.compose.ui.input.pointer.PointerEvent

/**
 * Augmented pointer event that includes
 * the corresponding position of the original event
 */

data class AugmentedPointerEvent (
    val event: PointerEvent,
    val position: PointerPosition
)
