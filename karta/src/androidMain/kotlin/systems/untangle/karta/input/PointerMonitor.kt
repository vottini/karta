package systems.untangle.karta.input

import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration

class AndroidPointerMonitor(
    inputButtonFlow: SharedFlow <AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow<PointerPosition?>,
    longPressDuration: Duration = 500.milliseconds
) : PointerMonitor(inputButtonFlow, rawMoveFlow, longPressDuration) {

    override suspend fun processButtonPress(
        coroutineScope: CoroutineScope,
        augmentedEvent: AugmentedPointerEvent
    ) {

    }
}

actual fun getPlatformSpecificPointerMonitor(
    inputButtonFlow: SharedFlow<AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow<PointerPosition?>,
    longPressDuration: Duration
): PointerMonitor = AndroidPointerMonitor(
    inputButtonFlow,
    rawMoveFlow,
    longPressDuration
)
