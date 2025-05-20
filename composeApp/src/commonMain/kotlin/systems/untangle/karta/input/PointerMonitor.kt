package systems.untangle.karta.input

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.isPrimaryPressed

class PointerMonitor(
    val inputButtonFlow: SharedFlow <AugmentedPointerEvent>,
    val rawMoveFlow: SharedFlow <PointerPosition>
) {
    private var clicked : Boolean = false
    private var lastButtonState : PointerButtons? = null
    private var lastPosition : PointerPosition? = null
    private var longPressJob : Job? = null

    private val _moveFlow = MutableSharedFlow <PointerPosition> ()
    private val _clickFlow = MutableSharedFlow <ButtonEvent> ()
    private val _longPressFlow = MutableSharedFlow <ButtonEvent> ()
    private val _dragFlow = MutableSharedFlow <DeltaPosition> ()

    val moveFlow: SharedFlow <PointerPosition> get() = _moveFlow
    val clickFlow: SharedFlow <ButtonEvent> get() = _clickFlow
    val longPressFlow: SharedFlow <ButtonEvent> get() = _longPressFlow
    val dragFlow: SharedFlow <DeltaPosition> get() = _dragFlow
    val dragSubscribersFlow: SharedFlow <Int> get() = _dragFlow.subscriptionCount

    fun checkLongPress(scope: CoroutineScope, position: PointerPosition) {
        longPressJob = scope.launch {
            delay(500)

            _longPressFlow.emit(
                ButtonEvent(
                    PointerButton.LEFT,
                    ButtonAction.PRESS,
                    position
                )
            )
        }
    }

    fun cancelLongPress() {
        longPressJob?.cancel()
        longPressJob = null
    }

    fun listen(scope: CoroutineScope) {
        scope.launch {
            rawMoveFlow.collect { position ->
                cancelLongPress()

                if (!clicked) {
                    _moveFlow.emit(position)
                    return@collect
                }

                lastPosition?.let { previous ->
                    val diff = Offset(
                        position.offset.x - previous.offset.x,
                        position.offset.y - previous.offset.y
                    )

                    val delta = DeltaPosition(
                        previous,
                        position,
                        diff
                    )

                    lastPosition = position
                    _dragFlow.emit(delta)
                }
            }
        }

        scope.launch {
            inputButtonFlow.collect { augmentedEvent ->
                val ( event, position ) = augmentedEvent
                val current = event.buttons

                lastButtonState?.let { previous ->
                    if (current.isPrimaryPressed != previous.isPrimaryPressed) {
                        if (current.isPrimaryPressed) {
                            checkLongPress(this, position)
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
                            clicked = false

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
                        checkLongPress(this, position)
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
    }

}

