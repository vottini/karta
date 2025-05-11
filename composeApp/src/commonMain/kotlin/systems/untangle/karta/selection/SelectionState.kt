package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

data class SelectionState(
    val currentHover: String = "",
    val currentSelection: String = ""
)

data class SelectionAccessors(
    val getter: SelectionState,
    val setter: (SelectionState) -> Unit
)

@Composable
fun rememberSelection(): SelectionAccessors {
    val (getter, setter) = remember { mutableStateOf(SelectionState()) }
    return SelectionAccessors(getter, setter)
}
