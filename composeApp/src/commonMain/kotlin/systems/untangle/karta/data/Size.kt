package systems.untangle.karta.data

import androidx.compose.ui.unit.IntOffset

data class Size(
    val width: Int,
    val height: Int
) {
    val halfWidth: Double by lazy { width.toDouble() / 2.0 }
    val halfHeight: Double by lazy { height.toDouble() / 2.0 }

    override fun equals(other: Any?) : Boolean {
        return (other is Size)
            && (other.width == width)
            && (other.height == height)
    }

    override fun hashCode() : Int {
        return width.hashCode().xor(height.hashCode())
    }

    fun div(k: Int) = Size(
        width = width / k,
        height = width / k
    )
}
