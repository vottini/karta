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
                                        if (awaitPointerEvent().type == PointerEventType.Press) {
                                            option.onClick.invoke()
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
