package systems.untangle.karta.popup

import systems.untangle.karta.data.Coordinates

/**
 * A single entry in a [Popup] context menu.
 *
 * @property label Text displayed for this menu item.
 * @property onClick Invoked with the popup's geographic position when the item is clicked.
 *   The popup is automatically hidden after this callback returns.
 */
data class PopupItem(
    val label: String,
    val onClick: (Coordinates) -> Unit
)
