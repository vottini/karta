package systems.untangle.karta.input

import kotlinx.coroutines.flow.SharedFlow

/**
 * Collection of shared flows that expose raw pointer input from the map.
 *
 * Available inside a [systems.untangle.karta.Karta] `content` lambda via
 * [systems.untangle.karta.base.LocalPointerEvents]. Collect from these flows inside
 * `LaunchedEffect` blocks to react to map input in custom overlay composables.
 *
 * @property moveFlow Emits the current [PointerPosition] on every pointer move, or `null` when
 *   the pointer leaves the map area.
 * @property clickFlow Emits a [ButtonEvent] on every button press and release.
 * @property shortPressFlow Emits the [PointerPosition] of a quick click (press + release with
 *   minimal movement and duration).
 * @property longPressFlow Emits the [PointerPosition] after the pointer has been held for ~500 ms.
 * @property dragFlow Emits a [DeltaPosition] on every frame while the pointer is being dragged.
 */
data class PointerFlows(
    val moveFlow: SharedFlow<PointerPosition?>,
    val clickFlow: SharedFlow <ButtonEvent>,
    val shortPressFlow: SharedFlow <PointerPosition>,
    val longPressFlow: SharedFlow <PointerPosition>,
    val dragFlow: SharedFlow <DeltaPosition>
)
