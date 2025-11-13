package systems.untangle.karta.popup

import systems.untangle.karta.data.Coordinates

data class PopupItem(
    val label: String,
    val onClick: (Coordinates) -> Unit
)
