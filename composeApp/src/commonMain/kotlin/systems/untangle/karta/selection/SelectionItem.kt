package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow

const val emptySelection = ""

class ItemState(
    private val currentState: SelectionState,
    private val setter: suspend (SelectionState) -> Unit,
    val itemId: String
) {
    val hovered = currentState.currentHover == itemId
    val selected = currentState.currentSelection == itemId

    val noneHovered = currentState.currentHover == emptySelection
    val noneSelected = currentState.currentSelection == emptySelection

    suspend fun setHovered() {
        val selfHovered = currentState.copy(currentHover = itemId)
        setter(selfHovered)
    }

    suspend fun clearHovered() {
        val noHover = currentState.copy(currentHover = emptySelection)
        setter(noHover)
    }

    suspend fun setSelected() = setter(currentState.copy(currentSelection = itemId))
    suspend fun clearSelected() = setter(currentState.copy(currentSelection = emptySelection))

    override fun toString() : String {
        return "hovered=$hovered selected=$selected itemId=$itemId"
    }
}

@Composable
fun SelectionItem(
    selectionFlow: MutableStateFlow<SelectionState>,
    itemId: String,
    content: @Composable (ownState: ItemState) -> Unit
) {
    val setter : suspend (SelectionState) -> Unit = { newState ->
        selectionFlow.emit(newState)
    }

    var itemState by remember(selectionFlow, itemId) {
        mutableStateOf(ItemState(SelectionState(), setter, itemId))
    }

    LaunchedEffect(selectionFlow) {
        selectionFlow.collect { currentState ->
            itemState = ItemState(
                currentState,
                setter,
                itemId
            )
        }
    }

	content(itemState)
}
