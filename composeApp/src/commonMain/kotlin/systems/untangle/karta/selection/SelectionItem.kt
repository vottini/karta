
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import systems.untangle.karta.selection.SelectionState

@Stable
data class ItemState(
    val hovered: Boolean,
    val selected: Boolean
)

@Composable
fun SelectionItem(
    selectionState: SelectionState,
    itemId: String,
    content: @Composable (ownState: ItemState) -> Unit
) {
    val itemState = remember(selectionState, itemId) { ItemState(
		selectionState.currentHover == itemId,
		selectionState.currentSelection == itemId
	)}

	content(itemState)
}
