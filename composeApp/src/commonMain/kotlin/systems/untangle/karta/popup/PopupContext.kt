package systems.untangle.karta.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import systems.untangle.karta.data.Coordinates

data class PopupContext(
    val value: PopupState,
    private val setter: (PopupState) -> Unit
) {
    fun hide() {
        val newState = PopupState()
        setter(newState)
    }

    fun show(coordinates: Coordinates, options: List <PopupItem>) {
        if (options.isEmpty()) {
            return
        }

        val decoratedOptions = options.map { item ->
            PopupItem(item.label) {
                item.onClick.invoke()
                hide()
            }
        }

        val newState = PopupState(coordinates, decoratedOptions)
        setter(newState)
    }

    val hasContents: Boolean
        get() = null != value.coordinates
}

@Composable
fun rememberPopupContext(): PopupContext {
    val (value, setValue) = remember { mutableStateOf(PopupState()) }
    return PopupContext(value, setValue)
}
