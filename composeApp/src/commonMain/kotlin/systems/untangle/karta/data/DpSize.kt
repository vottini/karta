package systems.untangle.karta.data

import androidx.compose.ui.unit.Dp

data class DpSize(
    val width: Dp,
    val height: Dp
) {
    val halfWidth: Dp by lazy { width / 2f }
    val halfHeight: Dp by lazy { height / 2f }

    override fun equals(other: Any?) : Boolean {
        return (other is DpSize)
            && (other.width == width)
            && (other.height == height)
    }

    override fun hashCode() : Int {
        return width.hashCode().xor(height.hashCode())
    }

    fun div(k: Int) = DpSize(
        width = width / k,
        height = width / k
    )
}
