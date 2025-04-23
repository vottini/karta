package systems.untangle.karta.input

data class ButtonEvent (
    val button: PointerButton,
    val action: ButtonAction,
    val position: PointerPosition
)
