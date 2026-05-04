package systems.untangle.karta.composables

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Path

import systems.untangle.karta.base.LocalConverter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.FillPattern
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.intersects
import systems.untangle.karta.selection.SelectionItem
import systems.untangle.karta.selection.rememberSelectionContext

import systems.untangle.karta.resources.Res
import systems.untangle.karta.resources.blueDot
import systems.untangle.karta.resources.greenDot
import systems.untangle.karta.selection.ItemSelectionState

val blueDot = Res.drawable.blueDot
val greenDot = Res.drawable.greenDot

fun IntOffset.toOffset() = Offset(
    this.x.toFloat(),
    this.y.toFloat()
)

/**
 * Draws a polyline (or filled polygon) through a list of geographic coordinates.
 *
 * The polyline is culled when its bounding box does not intersect the current viewport, so
 * only visible segments incur draw cost.
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coordsList Ordered list of geographic vertices. Empty lists are a no-op.
 * @param strokeColor Color of the line stroke.
 * @param strokeWidth Stroke width in pixels.
 * @param fillPattern Optional [FillPattern] for the enclosed area — [FillPattern.Solid],
 *   [FillPattern.Hatched], [FillPattern.Crossed], or [FillPattern.Dotted]. Pass `null`
 *   (default) for no fill.
 * @param closed When `true`, a closing segment is drawn from the last vertex back to the first,
 *   forming a polygon. Has no effect when [coordsList] has fewer than three points.
 * @param pathEffect Optional [PathEffect] applied to the stroke, e.g.
 *   `PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)` for a dashed line.
 *   Pass `null` (default) for a solid stroke.
 */
@Composable
fun Polyline(
    coordsList: List<Coordinates>,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    fillPattern: FillPattern? = null,
    closed: Boolean = false,
    pathEffect: PathEffect? = null
) {
    if (coordsList.isEmpty()) {
        return
    }

    val converter = LocalConverter.current
    val offsets = remember(coordsList, converter) {
        coordsList.map { coords ->
            converter.convertToOffset(coords).toOffset()
        }
    }

    val path = remember(offsets, closed) {
        val newPath = Path()
        val start = offsets[0]
        newPath.moveTo(start.x, start.y)

        for (i in 1..<offsets.size) {
            val next = offsets[i]
            newPath.lineTo(next.x, next.y)
        }

        if (closed && offsets.size > 2) {
            newPath.close()
        }

        newPath
    }

    val polylineBoundaries = remember(offsets) {
        var xMin = offsets[0].x.toInt()
        var xMax = offsets[0].x.toInt()
        var yMin = offsets[0].y.toInt()
        var yMax = offsets[0].y.toInt()

        offsets.drop(1).forEach { offset ->
            xMin = min(xMin, offset.x.toInt())
            yMin = min(yMin, offset.y.toInt())
            xMax = max(xMax, offset.x.toInt())
            yMax = max(yMax, offset.y.toInt())
        }

        TileRegion(IntOffset(xMin, yMin), IntOffset(xMax, yMax))
    }

    if (!converter.tileRegion.intersects(polylineBoundaries)) {
        return
    }

    if (fillPattern != null) {
        Canvas(modifier = Modifier) {
            when (fillPattern) {
                is FillPattern.Solid -> drawPath(
                    path = path,
                    color = fillPattern.color,
                    alpha = fillPattern.alpha,
                    style = Fill
                )
                is FillPattern.Hatched -> clipPath(path) {
                    drawHatchLines(polylineBoundaries, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                }
                is FillPattern.Crossed -> clipPath(path) {
                    drawHatchLines(polylineBoundaries, fillPattern.color, fillPattern.spacing, fillPattern.angle, fillPattern.strokeWidth)
                    drawHatchLines(polylineBoundaries, fillPattern.color, fillPattern.spacing, fillPattern.angle + 90f, fillPattern.strokeWidth)
                }
                is FillPattern.Dotted -> clipPath(path) {
                    drawDottedPattern(polylineBoundaries, fillPattern.color, fillPattern.spacing, fillPattern.radius)
                }
            }
        }
    }

    Canvas(modifier = Modifier) {
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidth, pathEffect = pathEffect)
        )
    }
}

/**
 * A [Polyline] whose vertices can be repositioned by dragging.
 *
 * Each vertex is rendered as a [MovableMarker]. When a vertex is dragged, [coordsSetter] is
 * called with the vertex index and its new [Coordinates]. The caller must update [coordsList]
 * in response (typically via `mutableStateOf`).
 *
 * Must be called inside a [systems.untangle.karta.Karta] `content` lambda.
 *
 * @param coordsList Ordered list of geographic vertices.
 * @param coordsSetter Called with `(index, newCoords)` on each drag frame for a vertex.
 * @param strokeColor Color of the line stroke.
 * @param strokeWidth Stroke width in pixels.
 * @param fillPattern Optional [FillPattern] for the enclosed area. Pass `null` for no fill.
 * @param closed When `true`, closes the polygon by connecting the last vertex to the first.
 * @param pathEffect Optional [PathEffect] applied to the stroke (e.g. dashes). Pass `null`
 *   (default) for a solid stroke.
 * @param edgeContents Composable rendered for each vertex handle. Receives [ItemSelectionState]
 *   so the handle can change appearance on hover. Defaults to a green dot that turns blue on hover.
 */
@Composable
fun EditablePolyline(
    coordsList: List<Coordinates>,
    coordsSetter: (Int, Coordinates) -> Unit,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    fillPattern: FillPattern? = null,
    closed: Boolean = false,
    pathEffect: PathEffect? = null,
    edgeContents: @Composable (ItemSelectionState) -> Unit = { itemState ->
        val resource = if (itemState.hovered) blueDot else greenDot
        Sprite(resource = resource)
    }
) {
    val selectionContext = rememberSelectionContext()

    Polyline(
        coordsList,
        strokeColor,
        strokeWidth,
        fillPattern,
        closed,
        pathEffect
    )

    coordsList.forEachIndexed { index, coords ->
        SelectionItem(
            selectionContext = selectionContext,
            itemId = "$index"
        ) { itemState ->
            MovableMarker(
                coords = coords,
                coordsSetter = { coords -> coordsSetter(index, coords) },
                itemSelectionState = itemState,
                wrapLongitude = false
            ) {
                edgeContents(itemState)
            }
        }
    }
}

// Draws parallel lines at [angleDeg] degrees spaced [spacing] pixels apart, covering [bounds].
// Intended to be called inside a clipPath block.
internal fun DrawScope.drawHatchLines(
    bounds: TileRegion,
    color: Color,
    spacing: Float,
    angleDeg: Float,
    strokeWidth: Float
) {
    val angleRad = (angleDeg * PI / 180.0).toFloat()
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    // perpendicular direction (lines are spaced along this axis)
    val px = -sinA
    val py = cosA

    val xMin = bounds.topLeft.x.toFloat()
    val yMin = bounds.topLeft.y.toFloat()
    val xMax = bounds.bottomRight.x.toFloat()
    val yMax = bounds.bottomRight.y.toFloat()

    val cx = (xMin + xMax) / 2f
    val cy = (yMin + yMax) / 2f
    val dx = xMax - xMin
    val dy = yMax - yMin
    val halfDiag = sqrt(dx * dx + dy * dy) / 2f

    // project each corner onto the perpendicular axis to find the coverage range
    val t0 = (xMin - cx) * px + (yMin - cy) * py
    val t1 = (xMax - cx) * px + (yMin - cy) * py
    val t2 = (xMin - cx) * px + (yMax - cy) * py
    val t3 = (xMax - cx) * px + (yMax - cy) * py
    val tMin = minOf(t0, t1, t2, t3)
    val tMax = maxOf(t0, t1, t2, t3)

    var t = floor((tMin / spacing).toDouble()).toFloat() * spacing
    while (t <= tMax) {
        val ox = cx + t * px
        val oy = cy + t * py
        drawLine(
            color = color,
            start = Offset(ox - halfDiag * cosA, oy - halfDiag * sinA),
            end = Offset(ox + halfDiag * cosA, oy + halfDiag * sinA),
            strokeWidth = strokeWidth
        )
        t += spacing
    }
}

// Draws a staggered dot grid covering [bounds], with odd rows offset by spacing/2.
// Intended to be called inside a clipPath block.
internal fun DrawScope.drawDottedPattern(
    bounds: TileRegion,
    color: Color,
    spacing: Float,
    radius: Float
) {
    val xMin = bounds.topLeft.x.toFloat()
    val yMin = bounds.topLeft.y.toFloat()
    val xMax = bounds.bottomRight.x.toFloat()
    val yMax = bounds.bottomRight.y.toFloat()

    var row = 0
    var y = yMin
    while (y <= yMax + radius) {
        val xOffset = if (row % 2 == 0) 0f else spacing / 2f
        var x = xMin + xOffset
        while (x <= xMax + radius) {
            drawCircle(color = color, radius = radius, center = Offset(x, y))
            x += spacing
        }
        y += spacing
        row++
    }
}
