package systems.untangle.karta.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalDensity

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

import systems.untangle.karta.data.PxSize
import systems.untangle.karta.conversion.toDp
import systems.untangle.karta.data.px

@Composable
fun Sprite(
    resource: DrawableResource,
    dimensions: PxSize? = null,
    keepAspectRatio: Boolean = true
) {
    val pinPainter = painterResource(resource)
    val density = LocalDensity.current.density

    val finalDimensions = remember(pinPainter, dimensions) {
        if (dimensions != null && !keepAspectRatio) {
            return@remember dimensions
        }

        val (width, height) = pinPainter.intrinsicSize
        if (null == dimensions) {
            return@remember PxSize(
                width.px,
                height.px
            )
        }

        val widthDeformation = width / dimensions.width.value
        val heightDeformation = height / dimensions.height.value

        if (widthDeformation > heightDeformation) {
            val proportionalHeight = height / widthDeformation
            PxSize(dimensions.width, proportionalHeight.px)
        } else {
            val proportionalWidth = width / heightDeformation
            PxSize(proportionalWidth.px, dimensions.height)
        }
    }

    Image(
        modifier = Modifier
            .width(finalDimensions.width.toDp(density))
            .height(finalDimensions.height.toDp(density)),

        painter = pinPainter,
        contentDescription = null
    )
}
