package systems.untangle.karta.input

import androidx.compose.ui.geometry.Offset

data class DeltaPosition(
    val previous: PointerPosition,
    val current: PointerPosition,
    val diff: Offset
)
