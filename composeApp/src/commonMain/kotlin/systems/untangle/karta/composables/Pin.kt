package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope

import systems.untangle.karta.LocalPointerEvents
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.Size
import systems.untangle.karta.data.defineTileRegion
import systems.untangle.karta.input.ButtonEvent
import systems.untangle.karta.input.isInside
import systems.untangle.karta.selection.SelectionState

const val redPin = "composeResources/karta.composeapp.generated.resources/drawable/pin.png"
const val greenPin = "composeResources/karta.composeapp.generated.resources/drawable/greenPin.png"
const val bluePin = "composeResources/karta.composeapp.generated.resources/drawable/bluePin.png"

fun IntOffset.minus(x: Int, y: Int) = IntOffset(this.x - x, this.y - y)

@Composable
fun Pin(
    coords: Coordinates,
    dimensions: Size,
    sprite: String = redPin,
    onHover: suspend CoroutineScope.(Boolean) -> Unit = {},
    onClick: suspend CoroutineScope.(ButtonEvent) -> Unit = {}
) {
    val pinPainter = painterResource(sprite)
    val pinSize = remember(pinPainter, dimensions) {
        val (width, height) = pinPainter.intrinsicSize

        if (width > height) {
            val aspectRatio = height / width
            val proportionalWidth = (dimensions.width * aspectRatio).toInt()
            Size(proportionalWidth, dimensions.height)
        }

        else {
            val aspectRatio = width / height
            val proportionalHeight = (dimensions.height * aspectRatio).toInt()
            Size(dimensions.width, proportionalHeight)
        }
    }

    Geolocated(
        coordinates = coords,
        extension = Size(
            pinSize.width,
            pinSize.height * 2
        )
    ) { coordsOffset ->
        val pointerEvents = LocalPointerEvents.current
        val compensedOffset = remember(coordsOffset, pinSize) { IntOffset(
            coordsOffset.x - (pinSize.width / 2),
            coordsOffset.y - pinSize.height
        )}

        var isHovered by remember { mutableStateOf(false) }
        val ownExtension = remember(coordsOffset, pinSize) {
            val halfSize = pinSize.div(2)

            defineTileRegion(
                coordsOffset.minus(0, halfSize.height),
                halfSize)
        }

        LaunchedEffect(pointerEvents, ownExtension) {
            pointerEvents.moveFlow.collect { event ->
                val newHoverState = event.isInside(ownExtension)

                if (newHoverState != isHovered) {
                    isHovered = newHoverState
                    onHover(newHoverState)
                }
            }
        }

        LaunchedEffect(isHovered) {
            if (isHovered) {
                pointerEvents.clickFlow.collect { event ->
                    onClick(event)
                }
            }
        }

        Image(
            modifier = Modifier
                .offset { compensedOffset }
                .width(pinSize.width.dp)
                .height(pinSize.height.dp),

            painter = pinPainter,
            contentDescription = null
        )
    }
}

@Composable
fun Pin(
    coords: Coordinates,
    dimensions: Size,
    sprite: String = redPin,
    selectionGroup: MutableState <SelectionState>
) {

}
