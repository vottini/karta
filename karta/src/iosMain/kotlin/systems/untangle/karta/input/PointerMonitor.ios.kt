package systems.untangle.karta.input

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

import androidx.compose.ui.input.pointer.isPrimaryPressed

class iOSPointerMonitor(
    inputButtonFlow: SharedFlow <AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow <PointerPosition?>,
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
): PointerMonitor = iOSPointerMonitor(
    inputButtonFlow,
    rawMoveFlow,
    longPressDuration
)
