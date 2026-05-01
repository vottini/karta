package systems.untangle.karta.popup

import systems.untangle.karta.data.Coordinates

/**
 * Immutable snapshot of a [Popup]'s visibility and content.
 *
 * @property coordinates Geographic position where the popup is anchored, or `null` when hidden.
 * @property options Menu items to display. An empty list with a non-null [coordinates] is treated
 *   as hidden by [PopupContext.show].
 */
data class PopupState(
    val coordinates: Coordinates? = null,
    val options: List <PopupItem> = listOf()
)
