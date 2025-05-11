package systems.untangle.karta.selection

const val emptySelection = ""

data class SelectionContext(
    val state: SelectionState,
    val setter: (SelectionState) -> Unit,
    val itemId: String
) {
    fun setSelfHovered() {
        val selfHovered = state.copy(currentHover = itemId)
        println("1111 - USING SETTER TO $selfHovered")
        setter(selfHovered)
    }

    fun clearSelfHovered() {
        val noHover = state.copy(currentHover = emptySelection)
        println("2222 - USING CLEARER TO $noHover")
        setter(noHover)
    }

    fun setSelfSelected() = setter(state.copy(currentSelection = itemId))
    fun clearSelected() = setter(state.copy(currentSelection = emptySelection))
}
