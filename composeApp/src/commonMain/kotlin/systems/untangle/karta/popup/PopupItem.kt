package systems.untangle.karta.popup

data class PopupItem(
    val label: String,
    val onClick: () -> Unit
)
