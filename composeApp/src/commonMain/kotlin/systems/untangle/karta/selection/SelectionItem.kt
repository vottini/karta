package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

const val emptySelection = ""

data class ItemSelectionState(
    private val currentState: SelectionState,
    private val emitter: suspend (SelectionState) -> Unit,
    val itemId: String
) {
    val hovered = currentState.currentHover == itemId
    val selected = currentState.currentSelection == itemId
    val grabbed = selected && currentState.grabbing

    suspend fun setHovered() = emitter(currentState.copy(currentHover = itemId))
    suspend fun setSelected() = emitter(currentState.copy(currentSelection = itemId))
    suspend fun setClicked() = emitter(currentState.copy(
        currentSelection = itemId,
        grabbing = true))

    suspend fun clearHovered() = emitter(currentState.copy(currentHover = emptySelection))
    suspend fun clearSelected() = emitter(currentState.copy(currentSelection = emptySelection))
    suspend fun clearGrabbing() = emitter(currentState.copy(grabbing = false))

    val noneHovered = currentState.currentHover == emptySelection
    val noneSelected = currentState.currentSelection == emptySelection

    override fun toString() : String {
        return "hovered=$hovered selected=$selected itemId=$itemId"
    }
}


@Composable
fun SelectionItem(
    selectionContext: SelectionFlowContext,
    itemId: String,
    content: @Composable (ownState: ItemSelectionState) -> Unit
) {
    var itemState by remember(selectionContext, itemId) {
        mutableStateOf(ItemSelectionState(
            SelectionState(),
            selectionContext.selectionEmitter,
            itemId))
    }

    LaunchedEffect(selectionContext, itemId) {
        val (flow, emitter) = selectionContext
        flow.collect { currentState ->
            itemState = ItemSelectionState(
                currentState,
                emitter,
                itemId
            )
        }
    }

	content(itemState)
}
