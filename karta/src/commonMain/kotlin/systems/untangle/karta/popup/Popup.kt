package systems.untangle.karta.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import systems.untangle.karta.composables.Geolocated

import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.selection.SelectionItem
import systems.untangle.karta.selection.rememberSelectionContext

/**
 * Renders a floating context menu at the geographic position stored in [context].
 *
 * The menu is invisible when [PopupContext.hasContents] is `false`. Each menu item highlights
 * on hover and dismisses the popup automatically after being clicked.
 *
 * Typically placed inside a [systems.untangle.karta.Karta] `content` lambda alongside a
 * [systems.untangle.karta.Karta] `onLongPress` handler that calls [PopupContext.show].
 *
 * @param context State holder created by [rememberPopupContext]. Controls visibility and items.
 * @param background Background color of the popup card and the hover highlight text.
 * @param color Text color of menu items and the hover highlight background.
 */
@Composable
fun Popup(
    context: PopupContext,
    background: Color = Color.LightGray,
    color: Color = Color.Black
) {
    val selectionContext = rememberSelectionContext()
    val (coords, options) = context.value
    if (null == coords) {
        return
    }

    Geolocated(coordinates = coords) { coordsOffset ->
        Column(
            modifier = Modifier
                .offset(coordsOffset.x.dp, coordsOffset.y.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(background)
                .width(IntrinsicSize.Max)
                .padding(8.dp),

            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            options.forEach { option ->
                SelectionItem(
                    selectionContext,
                    option.label
                ) { itemState ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered = interactionSource.collectIsHoveredAsState()

                    LaunchedEffect(isHovered.value) {
                        if (isHovered.value) itemState.setHovered()
                        else itemState.clearHovered()
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (itemState.hovered) color else background)
                            .hoverable(interactionSource)
                            .padding(4.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Press) {
                                            option.onClick.invoke(coords)
                                        }
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = option.label,
                            color = if (itemState.hovered) background else color,
                        )
                    }
                }
            }
        }
    }
}
