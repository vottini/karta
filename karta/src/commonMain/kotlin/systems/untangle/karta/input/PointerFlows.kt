package systems.untangle.karta.input

import kotlinx.coroutines.flow.SharedFlow

data class PointerFlows(
    val moveFlow: SharedFlow<PointerPosition?>,
    val clickFlow: SharedFlow <ButtonEvent>,
    val shortPressFlow: SharedFlow <PointerPosition>,
    val longPressFlow: SharedFlow <PointerPosition>,
    val dragFlow: SharedFlow <DeltaPosition>
)
