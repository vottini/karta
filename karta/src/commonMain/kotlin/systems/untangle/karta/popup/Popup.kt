package systems.untangle.karta.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import systems.untangle.karta.composables.Geolocated

/**
 * Renders a native [DropdownMenu] anchored to the geographic position stored in [context].
 *
 * The menu is invisible when [PopupContext.hasContents] is `false`. Each item dismisses the
 * popup automatically after being clicked.
 *
 * Typically placed inside a [systems.untangle.karta.Karta] `content` lambda alongside an
 * `onLongPress` handler that calls [PopupContext.show].
 *
 * @param context State holder created by [rememberPopupContext]. Controls visibility and items.
 */
@Composable
fun Popup(context: PopupContext) {
    val (coords, options) = context.value
    if (null == coords) return

    Geolocated(coordinates = coords) { coordsOffset ->
        Box(modifier = Modifier.offset { coordsOffset }) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { context.hide() }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = { option.onClick.invoke(coords) }) {
                        Text(option.label)
                    }
                }
            }
        }
    }
}
