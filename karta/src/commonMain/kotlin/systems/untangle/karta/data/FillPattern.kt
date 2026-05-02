package systems.untangle.karta.data

import androidx.compose.ui.graphics.Color

/**
 * Describes how the interior of a [systems.untangle.karta.composables.Polyline] polygon is filled.
 *
 * Pass an instance as the `fillPattern` parameter of
 * [systems.untangle.karta.composables.Polyline] or
 * [systems.untangle.karta.composables.EditablePolyline].
 *
 * Patterns other than [Solid] are rendered by clipping to the polygon path and drawing the
 * pattern strokes or dots inside it, so they work correctly for any polygon shape.
 */
sealed class FillPattern {

    /**
     * Solid fill with a uniform color.
     *
     * @property color Fill color.
     * @property alpha Opacity in the `[0, 1]` range.
     */
    data class Solid(
        val color: Color,
        val alpha: Float = 1f
    ) : FillPattern()

    /**
     * Parallel hatch lines drawn at the given angle inside the polygon.
     *
     * @property color Line color.
     * @property spacing Distance between line centers in pixels.
     * @property angle Angle of the lines in degrees, measured from the positive X axis.
     *   `0°` → horizontal, `45°` → diagonal, `90°` → vertical.
     * @property strokeWidth Width of each hatch line in pixels.
     */
    data class Hatched(
        val color: Color,
        val spacing: Float = 12f,
        val angle: Float = 45f,
        val strokeWidth: Float = 1f
    ) : FillPattern()

    /**
     * Two perpendicular sets of hatch lines forming a crosshatch pattern.
     *
     * One set is drawn at [angle] and the other at [angle] + 90°.
     *
     * @property color Line color.
     * @property spacing Distance between line centers in pixels.
     * @property angle Angle of one set of lines in degrees. The other set is perpendicular to it.
     * @property strokeWidth Width of each line in pixels.
     */
    data class Crossed(
        val color: Color,
        val spacing: Float = 12f,
        val angle: Float = 45f,
        val strokeWidth: Float = 1f
    ) : FillPattern()

    /**
     * A staggered grid of dots drawn inside the polygon.
     *
     * Odd rows are offset by half of [spacing] horizontally to produce a denser, more natural
     * arrangement than a plain square grid.
     *
     * @property color Dot color.
     * @property spacing Distance between dot centers in pixels.
     * @property radius Radius of each dot in pixels.
     */
    data class Dotted(
        val color: Color,
        val spacing: Float = 16f,
        val radius: Float = 3f
    ) : FillPattern()
}
