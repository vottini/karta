package systems.untangle.karta.selection

/**
 * Shared state snapshot for a group of selectable items managed by a [SelectionFlowContext].
 *
 * Only one item can be hovered and one item can be selected at a time within a context.
 * Items are identified by string IDs that match the `itemId` passed to [SelectionItem].
 *
 * @property currentHover ID of the item currently under the cursor, or `""` when none.
 * @property currentSelection ID of the currently selected item, or `""` when none.
 * @property grabbing `true` when the selected item is actively being dragged.
 */
data class SelectionState(
    val currentHover: String = "",
    val currentSelection: String = "",
    val grabbing: Boolean = false
)
