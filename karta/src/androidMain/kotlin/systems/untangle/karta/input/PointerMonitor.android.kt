package systems.untangle.karta.input

import androidx.compose.ui.input.pointer.isPrimaryPressed
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration
import kotlin.time.TimeSource

class AndroidPointerMonitor(
    inputButtonFlow: SharedFlow<AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow<PointerPosition?>,
    longPressDuration: Duration = 500.milliseconds
) : PointerMonitor(inputButtonFlow, rawMoveFlow, longPressDuration) {

    override suspend fun processButtonPress(
        coroutineScope: CoroutineScope,
        augmentedEvent: AugmentedPointerEvent
    ) {
        val (event, position) = augmentedEvent
        val inputChange = event.changes.first()


        lastButtonState?.let { previous -> 
            if (inputChange.pressed) {
                clickStart = TimeSource.Monotonic.markNow()
                checkLongPress(coroutineScope, position)
                lastPosition = position
                clicked = true
                println("clicked")
                _clickFlow.emit(
                    ButtonEvent(
                        PointerButton.LEFT,
                        ButtonAction.PRESS,
                        position
                    )
                )
            } else {
                cancelLongPress()
                val elapsed = TimeSource.Monotonic.markNow() - clickStart
                if (clicked && elapsed < longPressDuration && !dragging) {
                    _shortPressFlow.emit(position)
                }

                clicked = false
                dragging = false
                println("released")
                _clickFlow.emit(
                    ButtonEvent(
                        PointerButton.LEFT,
                        ButtonAction.RELEASE,
                        position
                    )
                )

            }
            //  }
        } ?: run {          
             if (inputChange.pressed) {
            clickStart = TimeSource.Monotonic.markNow()
            checkLongPress(coroutineScope, position)
            lastPosition = position
            clicked = true           
            _clickFlow.emit(
                ButtonEvent(
                    PointerButton.LEFT,
                    ButtonAction.PRESS,
                    position
                )
            )
             }
        }

        lastButtonState = event.buttons
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
