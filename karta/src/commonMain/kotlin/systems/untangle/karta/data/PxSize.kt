package systems.untangle.karta.data

import androidx.compose.ui.unit.IntOffset

/**
 * Rectangular dimensions in absolute pixel units.
 *
 * @property width Horizontal extent in pixels.
 * @property height Vertical extent in pixels.
 */
data class PxSize(
    val width: Px,
    val height: Px
) {
    /** Half of [width], computed lazily. */
    val halfWidth: Px by lazy { width / 2f }
    /** Half of [height], computed lazily. */
    val halfHeight: Px by lazy { height / 2f }

    override fun equals(other: Any?) : Boolean {
        return (other is PxSize)
            && (other.width == width)
            && (other.height == height)
    }

    override fun hashCode() : Int {
        return width.hashCode().xor(height.hashCode())
    }

    fun div(k: Int) = PxSize(
        width = width / k,
        height = width / k
    )
}

fun PxSize.toIntOffset(): IntOffset {
    return IntOffset(
        this.width.value.toInt(),
        this.height.value.toInt(),
    )
}
