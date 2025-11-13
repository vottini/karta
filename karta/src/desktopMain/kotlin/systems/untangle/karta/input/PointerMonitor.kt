package systems.untangle.karta.input

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

import androidx.compose.ui.input.pointer.isPrimaryPressed

class DesktopPointerMonitor(
    inputButtonFlow: SharedFlow <AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow <PointerPosition>,
    longPressDuration: Duration = 500.milliseconds
) : PointerMonitor(inputButtonFlow, rawMoveFlow, longPressDuration) {

    override suspend fun processButtonPress(
        coroutineScope: CoroutineScope,
        augmentedEvent: AugmentedPointerEvent
    ) {
        val ( event, position ) = augmentedEvent
        val current = event.buttons

        lastButtonState?.let { previous ->
            if (current.isPrimaryPressed != previous.isPrimaryPressed) {
                if (current.isPrimaryPressed) {
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

                else {
                    cancelLongPress()
                    val elapsed = TimeSource.Monotonic.markNow() - clickStart
                    if (clicked && elapsed < longPressDuration && !dragging) {
                        _shortPressFlow.emit(position)
                    }

                    clicked = false
                    dragging = false
                    _clickFlow.emit(
                        ButtonEvent(
                            PointerButton.LEFT,
                            ButtonAction.RELEASE,
                            position
                        )
                    )

                }
            }
        } ?: run {
            if (current.isPrimaryPressed) {
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
    rawMoveFlow: SharedFlow<PointerPosition>,
    longPressDuration: Duration
): PointerMonitor = DesktopPointerMonitor(
    inputButtonFlow,
    rawMoveFlow,
    longPressDuration
)
