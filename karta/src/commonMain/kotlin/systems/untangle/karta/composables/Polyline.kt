package systems.untangle.karta.composables

import kotlin.math.max
import kotlin.math.min

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

import systems.untangle.karta.LocalConverter
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.data.TileRegion
import systems.untangle.karta.data.intersects
import systems.untangle.karta.selection.SelectionItem
import systems.untangle.karta.selection.rememberSelectionContext

import systems.untangle.karta.generated.resources.Res
import systems.untangle.karta.generated.resources.blueDot
import systems.untangle.karta.generated.resources.greenDot

val blueDot = Res.drawable.blueDot
val greenDot = Res.drawable.greenDot

fun IntOffset.toOffset()  = Offset(this.x.toFloat(), this.y.toFloat())

@Composable
fun Polyline(
    coordsList: List <Coordinates>,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    fillColor: Color? = null,
    fillAlpha: Float = 1f,
    closed: Boolean = false
) {
    val converter = LocalConverter.current
    val offsets = remember(coordsList, converter) {
        coordsList.map { coords ->
            val intOffset = converter.convertToOffset(coords)
            intOffset.toOffset()
        }
    }

    val path = remember(offsets, closed) {
        val newPath = Path()

        if (offsets.isNotEmpty()) {
            val start = offsets[0]
            newPath.moveTo(
                start.x,
                start.y
            )

            for (i in 1..< offsets.size) {
                val next = offsets[i]
                newPath.lineTo(
                    next.x,
                    next.y
                )
            }

            if (closed && offsets.size > 2) {
                newPath.close()
            }
        }

        newPath
    }

    val polylineBoundaries = remember(offsets) {
        var xMin = converter.tileRegion.bottomRight.x
        var yMin = converter.tileRegion.bottomRight.y
        var xMax = converter.tileRegion.topLeft.x
        var yMax = converter.tileRegion.topLeft.y

        offsets.forEach { offset ->
            xMin = min(xMin, offset.x.toInt())
            yMin = min(yMin, offset.y.toInt())
            xMax = max(xMax, offset.x.toInt())
            yMax = max(yMax, offset.y.toInt())
        }

        TileRegion(
            IntOffset(xMin, yMin),
            IntOffset(xMax, yMax)
        )
    }

    if (!converter.tileRegion.intersects(polylineBoundaries)) {
        return
    }

    if (null != fillColor) {
        Canvas(modifier = Modifier) {
            drawPath(
                path = path,
                color = fillColor,
                alpha = fillAlpha,
                style = Fill
            )
        }
    }

    Canvas(modifier = Modifier) {
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(strokeWidth)
        )
    }
}

@Composable
fun EditablePolyline(
    coordsList: List <Coordinates>,
    coordsSetter: (Int, Coordinates) -> Any,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    fillColor: Color? = null,
    fillAlpha: Float = 1f,
    closed: Boolean = false
) {
    val selectionContext = rememberSelectionContext()

    Polyline(
        coordsList,
        strokeColor,
        strokeWidth,
        fillColor,
        fillAlpha,
        closed
    )

    coordsList.forEachIndexed { index, coords ->

        SelectionItem(
            selectionContext = selectionContext,
            itemId = "$index"
        ) { itemState ->
            MovablePin(
                coords = coords,
                coordsSetter = { coords -> coordsSetter(index, coords) },
                itemSelectionState = itemState,
                sprite = if (itemState.hovered) blueDot else greenDot
            )
        }
    }
}

/**
 *  Circle(
 *    coord,
 *    radius = 5f,
 *    radiusUnit = DistanceUnit.PIXELS,
 *    borderWidth = 1f,
 *    borderColor = strokeColor,
 *    fillColor = Color.Blue
 *  )
 **/
