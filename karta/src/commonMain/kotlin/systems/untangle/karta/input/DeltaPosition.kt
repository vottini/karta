package systems.untangle.karta.input

import androidx.compose.ui.geometry.Offset

/**
 * A single drag step emitted on [PointerFlows.dragFlow].
 *
 * @property previous Pointer position at the previous drag frame.
 * @property current Pointer position at the current drag frame.
 * @property diff Screen-pixel delta between [previous] and [current].
 */
data class DeltaPosition(
    val previous: PointerPosition,
    val current: PointerPosition,
    val diff: Offset
)
