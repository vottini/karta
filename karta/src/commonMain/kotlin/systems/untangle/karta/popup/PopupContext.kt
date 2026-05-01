package systems.untangle.karta.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import systems.untangle.karta.data.Coordinates

/**
 * State holder for a [Popup] context menu.
 *
 * Create an instance with [rememberPopupContext] and pass it to both [Popup] (to render the menu)
 * and any event handler (e.g. `onLongPress`) that should show it.
 *
 * @property value Current [PopupState], observed by [Popup] for recomposition.
 */
data class PopupContext(
    val value: PopupState,
    private val setter: (PopupState) -> Unit
) {
    /** Hides the popup by clearing its coordinates and options. */
    fun hide() {
        val newState = PopupState()
        setter(newState)
    }

    /**
     * Shows the popup at [coordinates] with the provided [options].
     * Each item's [PopupItem.onClick] is automatically wrapped to hide the popup after invocation.
     * Does nothing when [options] is empty.
     */
    fun show(coordinates: Coordinates, options: List <PopupItem>) {
        if (options.isEmpty()) {
            return
        }

        val decoratedOptions = options.map { item ->
            PopupItem(item.label) { coords ->
                item.onClick.invoke(coords)
                hide()
            }
        }

        val newState = PopupState(coordinates, decoratedOptions)
        setter(newState)
    }

    /** `true` when the popup has a location and at least one option to display. */
    val hasContents: Boolean
        get() = null != value.coordinates
}

/**
 * Creates and remembers a [PopupContext] tied to the current composition.
 * Pass the returned context to [Popup] and to any handler that needs to show the menu.
 */
@Composable
fun rememberPopupContext(): PopupContext {
    val (value, setValue) = remember { mutableStateOf(PopupState()) }
    return PopupContext(value, setValue)
}
