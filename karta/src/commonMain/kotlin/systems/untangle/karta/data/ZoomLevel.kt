package systems.untangle.karta.data

/**
 * Data struct used to specify the behaviour of the zoom of the map.
 * Controls whether the zoom can be incremented or decremented,
 * gating access to direct modifications
 */

class ZoomSpecs (val value: Int, val minValue: Int, val maxValue: Int) {
    fun incrementable() = (value < maxValue)
    fun decrementable() = (value > minValue)
    fun increment() = if (incrementable()) ZoomSpecs(value + 1, minValue, maxValue) else this
    fun decrement() = if (decrementable()) ZoomSpecs(value - 1, minValue, maxValue) else this
}

/**
 * Stores and allows control of the zoom of the map
 */

data class ZoomLevel(
    val level: Int,
    val increment: () -> Unit,
    val decrement: () -> Unit
) {
    private val maxSize: Double = (1 shl level).toDouble()

    /**
     * Limits the passed parameter to be inside
     * the boundaries of the current zoom size, relocating
     * it to a wrapped value if needed.
     */

    fun curtail(value: Double): Double {
        var mutableValue = value
        while (mutableValue < 0) mutableValue += maxSize
        while (mutableValue >= maxSize) mutableValue -= maxSize
        return mutableValue
    }

    fun curtail(coords: DoubleOffset): DoubleOffset {
        return DoubleOffset(
            curtail(coords.component1()),
            curtail(coords.component2())
        )
    }
}

const val ZOOM_DECREMENT = 0
const val ZOOM_INCREMENT = 1
