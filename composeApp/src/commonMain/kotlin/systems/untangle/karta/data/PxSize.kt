package systems.untangle.karta.data

data class PxSize(
    val width: Int,
    val height: Int
) {
    val halfWidth: Double by lazy { width.toDouble() / 2.0 }
    val halfHeight: Double by lazy { height.toDouble() / 2.0 }

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
