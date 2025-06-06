package systems.untangle.karta.data

import androidx.compose.ui.unit.IntOffset
import kotlin.math.max
import kotlin.math.min

data class TileRegion(
    val topLeft: IntOffset,
    val bottomRight: IntOffset
)

fun defineTileRegion(base: IntOffset, dimensions: PxSize) : TileRegion {
    return TileRegion(
        IntOffset(base.x - dimensions.width.value.toInt(), base.y - dimensions.height.value.toInt()),
        IntOffset(base.x + dimensions.width.value.toInt(), base.y + dimensions.height.value.toInt())
    )
}

fun TileRegion.intersects(other: TileRegion) : Boolean {

    /**
     * .--------> (x)
     * |
     * |
     * V
     *
     * (y)

     * Sq1 = (A,B)
     * Sq2 = (C,D)
     *                          ProjY(1)
     *  A-----------.             _
     *  |           |             {   ProjY(2)
     *  |      C----+-----.       { . .  _ . . _
     *  |      |    |     |       {      {     }   IntercY(1,2)
     *  |      |    |     |       {      {     }
     *  '------+----B     |       - . .  { . . -
     *         |          |              {
     *         '----------D              -
     *
     *
     *  |~~~~~~~~~~| ProjX(1)
     *         '   '
     *         '   '
     *         |~~~~~~~~~~| ProjX(2)
     *         '   '
     *         '   '
     *         (~~~) IntercX(1,2)
     *
     */

    val topLeftIntersection = IntOffset(
        max(this.topLeft.x, other.topLeft.x),
        max(this.topLeft.y, other.topLeft.y)
    )

    val bottomRightIntersection = IntOffset(
        min(this.bottomRight.x, other.bottomRight.x),
        min(this.bottomRight.y, other.bottomRight.y)
    )

    return (
        (bottomRightIntersection.x > topLeftIntersection.x) &&
        (bottomRightIntersection.y > topLeftIntersection.y)
    )
}
