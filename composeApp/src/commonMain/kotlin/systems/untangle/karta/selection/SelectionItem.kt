package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue

data class ItemState(
    val hovered: Boolean,
    val selected: Boolean,
    val itemId: String
) {
    fun createContext(state: SelectionState, setter: (SelectionState) -> Unit) =
        SelectionContext(
            state,
            setter,
            itemId
        )
}

@Composable
fun SelectionItem(
    selectionState: SelectionState,
    itemId: String,
    content: @Composable (ownState: ItemState) -> Unit
) {
    println("SELECTION_ITEM STATE IS $selectionState")
    val currentState by rememberUpdatedState(selectionState)

    LaunchedEffect(selectionState) {
        println("WOOOOW IT CHANGED")
    }

    val itemState = ItemState(
		currentState.currentHover == itemId,
		currentState.currentSelection == itemId,
        itemId
	)

	content(itemState)
}
