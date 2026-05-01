package systems.untangle.karta.input

/**
 * A mouse or touch button press or release event.
 *
 * Emitted on [PointerFlows.clickFlow]. Use [action] to distinguish press from release,
 * and [button] to identify which button was involved.
 *
 * @property button Which pointer button triggered the event.
 * @property action Whether the button was pressed or released.
 * @property position Geographic and screen position at the time of the event.
 */
data class ButtonEvent (
    val button: PointerButton,
    val action: ButtonAction,
    val position: PointerPosition
)
