package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

const val emptySelection = ""

/**
 * Per-item view of the shared [SelectionState] within a [SelectionFlowContext].
 *
 * Passed by [SelectionItem] to its `content` lambda. Use the computed properties to style the
 * item and call the suspend mutators from coroutine callbacks (e.g. inside `onHover`).
 *
 * @property itemId Unique identifier for this item within its [SelectionFlowContext].
 */
data class ItemSelectionState(
    private val currentState: SelectionState,
    private val emitter: suspend (SelectionState) -> Unit,
    val itemId: String
) {
    /** `true` when this item's ID matches the hovered ID in the shared state. */
    val hovered = currentState.currentHover == itemId
    /** `true` when this item's ID matches the selected ID in the shared state. */
    val selected = currentState.currentSelection == itemId
    /** `true` when this item is both hovered and selected and is actively being dragged. */
    val grabbed = hovered && selected && currentState.grabbing

    /** Marks this item as the currently hovered item. */
    suspend fun setHovered() = emitter(currentState.copy(currentHover = itemId))
    /** Marks this item as the currently selected item. */
    suspend fun setSelected() = emitter(currentState.copy(currentSelection = itemId))
    /** Marks this item as selected and sets the grabbing flag (drag started). */
    suspend fun setClicked() = emitter(currentState.copy(
        currentSelection = itemId,
        grabbing = true))

    /** Clears the hover state (no item is hovered). */
    suspend fun clearHovered() = emitter(currentState.copy(currentHover = emptySelection))
    /** Clears the selection state (no item is selected). */
    suspend fun clearSelected() = emitter(currentState.copy(currentSelection = emptySelection))
    /** Clears the grabbing flag without changing hover or selection. */
    suspend fun clearGrabbing() = emitter(currentState.copy(grabbing = false))

    /** `true` when no item in the context is currently hovered. */
    val noneHovered = currentState.currentHover == emptySelection
    /** `true` when no item in the context is currently selected. */
    val noneSelected = currentState.currentSelection == emptySelection

    override fun toString() : String {
        return "hovered=$hovered selected=$selected itemId=$itemId"
    }
}


/**
 * Subscribes to a [SelectionFlowContext] and provides a per-item [ItemSelectionState] to
 * [content], recomposing whenever the shared selection state changes.
 *
 * Wrap each selectable overlay (e.g. a [systems.untangle.karta.composables.Marker]) in a
 * `SelectionItem` to coordinate hover and selection across multiple items without prop-drilling.
 *
 * @param selectionContext Shared context created by [rememberSelectionContext].
 * @param itemId Unique string ID for this item within [selectionContext].
 * @param content Called with the current [ItemSelectionState] for this item.
 */
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
