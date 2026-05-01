package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Shared context that coordinates hover and selection state across a group of [SelectionItem]s.
 *
 * Create an instance with [rememberSelectionContext] and pass it to every [SelectionItem] that
 * should participate in the same selection group.
 *
 * @property selectionFlow Emits the latest [SelectionState] whenever it changes.
 * @property selectionEmitter Suspend function to push a new [SelectionState] into the flow.
 */
data class SelectionFlowContext (
    val selectionFlow: SharedFlow<SelectionState>,
    val selectionEmitter: suspend (SelectionState) -> Unit
) {
    /** Resets hover, selection, and grabbing state to the default (nothing selected). */
    suspend fun clearSelection() = selectionEmitter.invoke(SelectionState())
}

/**
 * Creates and remembers a [SelectionFlowContext] tied to the current composition.
 * Pass the returned context to every [SelectionItem] that should share selection state.
 */
@Composable
fun rememberSelectionContext(): SelectionFlowContext {
    val mutableFlow = remember { MutableStateFlow(SelectionState()) }
    val emitter : suspend (SelectionState) -> Unit = remember(mutableFlow) {
        { newState -> mutableFlow.emit(newState) }
    }

    return SelectionFlowContext(
        mutableFlow,
        emitter
    )
}