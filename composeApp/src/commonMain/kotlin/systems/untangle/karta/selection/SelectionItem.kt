package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

const val emptySelection = ""

data class ItemState(
    private val currentState: SelectionState,
    private val emitter: suspend (SelectionState) -> Unit,
    val itemId: String
) {
    val hovered = currentState.currentHover == itemId
    val selected = currentState.currentSelection == itemId

    val noneHovered = currentState.currentHover == emptySelection
    val noneSelected = currentState.currentSelection == emptySelection

    suspend fun setHovered() {
        val selfHovered = currentState.copy(currentHover = itemId)
        emitter(selfHovered)
    }

    suspend fun clearHovered() {
        val noHover = currentState.copy(currentHover = emptySelection)
        emitter(noHover)
    }

    suspend fun setSelected() = emitter(currentState.copy(currentSelection = itemId))
    suspend fun clearSelected() = emitter(currentState.copy(currentSelection = emptySelection))

    override fun toString() : String {
        return "hovered=$hovered selected=$selected itemId=$itemId"
    }
}

@Composable
fun SelectionItem(
    selectionContext: SelectionFlowContext,
    itemId: String,
    content: @Composable (ownState: ItemState) -> Unit
) {
    var itemState by remember(selectionContext, itemId) {
        mutableStateOf(ItemState(
            SelectionState(),
            selectionContext.selectionEmitter,
            itemId))
    }

    LaunchedEffect(selectionContext) {
        val (flow, emitter) = selectionContext
        flow.collect { currentState ->
            itemState = ItemState(
                currentState,
                emitter,
                itemId
            )
        }
    }

	content(itemState)
}
