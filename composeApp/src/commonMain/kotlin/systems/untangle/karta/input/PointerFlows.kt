package systems.untangle.karta.input

import kotlinx.coroutines.flow.SharedFlow

data class PointerFlows(
    val moveFlow: SharedFlow<PointerPosition>,
    val clickFlow: SharedFlow <ButtonEvent>,
    val longPressFlow: SharedFlow <ButtonEvent>,
    val dragFlow: SharedFlow <DeltaPosition>
)
