package systems.untangle.karta.input

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButtons

abstract class PointerMonitor(
    protected val inputButtonFlow: SharedFlow <AugmentedPointerEvent>,
    protected val rawMoveFlow: SharedFlow <PointerPosition?>,
    protected val longPressDuration: Duration = 500.milliseconds
) {
    protected var clicked: Boolean = false
    protected var clickStart = TimeSource.Monotonic.markNow()
    protected var lastButtonState: PointerButtons? = null
    protected var lastPosition: PointerPosition? = null
    protected var longPressJob: Job? = null
    protected var dragging: Boolean = false

    protected val _moveFlow = MutableSharedFlow <PointerPosition?> ()
    protected val _clickFlow = MutableSharedFlow <ButtonEvent> ()
    protected val _shortPressFlow = MutableSharedFlow <PointerPosition> ()
    protected val _longPressFlow = MutableSharedFlow <PointerPosition> ()
    protected val _dragFlow = MutableSharedFlow <DeltaPosition> ()

    val pressSubscribersFlow: SharedFlow <Int> get() = _shortPressFlow.subscriptionCount
    val dragSubscribersFlow: SharedFlow <Int> get() = _dragFlow.subscriptionCount

    protected fun checkLongPress(scope: CoroutineScope, position: PointerPosition) {
        longPressJob = scope.launch {
            delay(longPressDuration)
            _longPressFlow.emit(position)
        }
    }

    protected fun cancelLongPress() {
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

                dragging = true
                lastPosition?.let { previous ->
                    if (null == position) {
                        return@let
                    }

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
            inputButtonFlow.collect { input ->
                processButtonPress(this, input)
            }
        }
    }

    protected abstract suspend fun processButtonPress(
        coroutineScope: CoroutineScope,
        augmentedEvent: AugmentedPointerEvent
    )

    val pointerFlows: PointerFlows get() = PointerFlows(
        _moveFlow,
        _clickFlow,
        _shortPressFlow,
        _longPressFlow,
        _dragFlow
    )
}

expect fun getPlatformSpecificPointerMonitor(
    inputButtonFlow: SharedFlow<AugmentedPointerEvent>,
    rawMoveFlow: SharedFlow<PointerPosition?>,
    longPressDuration: Duration
) : PointerMonitor
