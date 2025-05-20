package systems.untangle.karta.popup

import systems.untangle.karta.data.Coordinates

data class PopupState(
    val coordinates: Coordinates? = null,
    val options: List <PopupItem> = listOf()
)
