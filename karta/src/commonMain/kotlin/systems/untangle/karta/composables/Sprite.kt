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

/**
 * Displays a drawable resource as a fixed-size image, optionally constrained to a given size.
 *
 * Typically used inside a [Marker] to render a pin icon or any other map symbol.
 *
 * When [dimensions] is provided and [keepAspectRatio] is `true`, the image is scaled to fit
 * within [dimensions] while preserving its intrinsic aspect ratio. When [dimensions] is `null`,
 * the image is rendered at its intrinsic pixel size.
 *
 * @param resource The drawable resource to display (from `Res.drawable.*`).
 * @param dimensions Maximum bounding box for the image in absolute pixels, or `null` to use the
 *   resource's intrinsic size.
 * @param keepAspectRatio When `true` (default), the image is scaled proportionally within
 *   [dimensions]. When `false`, it is stretched to fill [dimensions] exactly.
 */
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
