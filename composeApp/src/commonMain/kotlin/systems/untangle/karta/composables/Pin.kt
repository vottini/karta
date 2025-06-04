package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import systems.untangle.karta.LocalPointerEvents
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.defineTileRegion
import systems.untangle.karta.input.ButtonEvent
import systems.untangle.karta.input.PointerPosition
import systems.untangle.karta.input.isInside
import systems.untangle.karta.selection.ItemSelectionState

import karta.composeapp.generated.resources.Res
import karta.composeapp.generated.resources.redPin
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import systems.untangle.karta.conversion.toPixels

fun IntOffset.minus(x: Int, y: Int) = IntOffset(this.x - x, this.y - y)

@Composable
fun Pin(
    coords: Coordinates,
    dimensions: PxSize,
    sprite: DrawableResource = Res.drawable.redPin,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {},
    onShortPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onLongPress: suspend CoroutineScope.(PointerPosition) -> Unit = {}
) {
    val pinPainter = painterResource(sprite)
    val density = LocalDensity.current.density

    val pinPxSize = remember(pinPainter, dimensions) {
        val (width, height) = pinPainter.intrinsicSize

        if (width > height) {
            val aspectRatio = height / width
            val proportionalWidth = (dimensions.width * aspectRatio).toInt()
            PxSize(proportionalWidth, dimensions.height)
        }

        else {
            val aspectRatio = width / height
            val proportionalHeight = (dimensions.height * aspectRatio).toInt()
            PxSize(dimensions.width, proportionalHeight)
        }
    }

    Geolocated(
        coordinates = coords,
        extension = PxSize(
            pinPxSize.width,
            pinPxSize.height * 2
        )
    ) { coordsOffset ->
        val pointerEvents = LocalPointerEvents.current
        val pinOffset = remember(coordsOffset, pinPxSize) { IntOffset(
            coordsOffset.x - (pinPxSize.width.dp.toPixels(density) / 2),
            coordsOffset.y - pinPxSize.height.dp.toPixels(density)
        )}

        var isHovered by remember { mutableStateOf(false) }
        val ownExtension = remember(coordsOffset, pinPxSize) {
            val halfSize = pinPxSize.div(2)

            defineTileRegion(
                coordsOffset.minus(0, halfSize.height),
                halfSize)
        }

        LaunchedEffect(pointerEvents, isHovered, ownExtension, onHover) {
            pointerEvents.moveFlow.collect { pointerPosition ->
                val newHoverState = pointerPosition.isInside(ownExtension)

                if (newHoverState != isHovered) {
                    isHovered = newHoverState
                    onHover(newHoverState)
                }
            }
        }

        LaunchedEffect(pointerEvents, isHovered, onClick, onShortPress) {
            if (isHovered) {
                listOf(
                    launch { pointerEvents.clickFlow.collect { ev -> onClick(ev) } },
                    launch { pointerEvents.shortPressFlow.collect { position -> onShortPress(position) } },
                    launch { pointerEvents.longPressFlow.collect { position -> onLongPress(position) } },
                ).forEach { job ->
                    job.join()
                }
            }
        }

        Image(
            modifier = Modifier
                .offset { pinOffset }
                .width(pinPxSize.width.dp)
                .height(pinPxSize.height.dp),

            painter = pinPainter,
            contentDescription = null
        )
    }
}

@Composable
fun Pin(
    coords: Coordinates,
    itemSelectionState: ItemSelectionState,
    dimensions: PxSize,
    sprite: DrawableResource = Res.drawable.redPin,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {},
    onShortPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
    onLongPress: suspend CoroutineScope.(PointerPosition) -> Unit = {},
) {
    val decoratedOnHover: suspend CoroutineScope.(Boolean) -> Unit =
        remember(itemSelectionState, onHover) {
            { hoveredNow ->
                if (hoveredNow && itemSelectionState.noneHovered) itemSelectionState.setHovered()
                if (itemSelectionState.hovered && !hoveredNow) itemSelectionState.clearHovered()
                onHover(hoveredNow)
            }
        }

    val decoratedOnShortPress: suspend CoroutineScope.(PointerPosition) -> Unit =
        remember (itemSelectionState, onShortPress) {
            { position ->
                if (!itemSelectionState.selected) itemSelectionState.setSelected()
                onShortPress(position)
            }
        }

    val decoratedOnLongPress: suspend CoroutineScope.(PointerPosition) -> Unit =
        remember (itemSelectionState, onLongPress) {
            { position ->
                if (!itemSelectionState.selected) itemSelectionState.setSelected()
                onLongPress(position)
            }
        }

    Pin(
        coords,
        dimensions,
        sprite,
        decoratedOnHover,
        onClick,
        decoratedOnShortPress,
        decoratedOnLongPress
    )
}
