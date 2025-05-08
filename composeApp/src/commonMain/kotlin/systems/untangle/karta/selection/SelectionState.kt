package systems.untangle.karta.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Stable
data class SelectionState(
    val currentHover: String = "",
    val currentSelection: String = ""
) {

}

@Composable
fun rememberSelection(): MutableState<SelectionState> = remember {
    mutableStateOf(SelectionState())
}

