package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

data class SelectionFlowContext (
    val selectionFlow: SharedFlow<SelectionState>,
    val selectionEmitter: suspend (SelectionState) -> Unit
) {
    suspend fun clearSelection() = selectionEmitter.invoke(SelectionState())
}

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