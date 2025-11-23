package systems.untangle.karta.data

class ZoomSpecs (val value: Int, val minValue: Int, val maxValue: Int) {
    fun incrementable() = (value < maxValue)
    fun decrementable() = (value > minValue)
    fun increment() = if (incrementable()) ZoomSpecs(value + 1, minValue, maxValue) else this
    fun decrement() = if (decrementable()) ZoomSpecs(value - 1, minValue, maxValue) else this
}

data class ZoomLevel(
    val level: Int,
    val increment: () -> Unit,
    val decrement: () -> Unit
)

const val ZOOM_DECREMENT = 0
const val ZOOM_INCREMENT = 1
