package app.theusual.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.max

object Art {
    /** Screenshots and renders: no rain, no flicker, no steam drifting. */
    var still = false

    /** The packet colourways: the print's ground and its second ink. */
    val packetColours = listOf(
        Color(0xFF2F5D62) to Color(0xFFD39B3A),
        Color(0xFFA8392B) to Color(0xFFFBF3E2),
        Color(0xFF4B3A6B) to Color(0xFFE0B5D0),
        Color(0xFFD39B3A) to Color(0xFFF3E2A8),
        Color(0xFF1F3A5A) to Color(0xFFD24C36),
        Color(0xFF6C8C4A) to Color(0xFFE6EFC9),
        Color(0xFF874A2B) to Color(0xFFF0C99A),
        Color(0xFF2A241D) to Color(0xFFE7BF69),
        Color(0xFFC46A72) to Color(0xFFF6DFE0),
        Color(0xFF4F7FA6) to Color(0xFFF1E6C8),
    )

    fun packet(i: Int) = packetColours[i.mod(packetColours.size)]
}

/** A looping 0..1 phase, or a fixed one when [Art.still]. */
@Composable
fun phase(ms: Int, fixed: Float = 0.3f): State<Float> {
    if (Art.still) return remember { mutableFloatStateOf(fixed) }
    val t = rememberInfiniteTransition(label = "phase")
    return t.animateFloat(0f, 1f, infiniteRepeatable(tween(ms, easing = LinearEasing), RepeatMode.Restart), label = "phase")
}

/** Draws [block] in a scene of [w]×[h] units, cropped to [crop] and scaled to fill the canvas. */
inline fun DrawScope.scene(w: Float, h: Float, crop: Rect = Rect(0f, 0f, w, h), block: DrawScope.() -> Unit) {
    val k = max(size.width / crop.width, size.height / crop.height)
    val dx = (size.width - crop.width * k) / 2f - crop.left * k
    val dy = (size.height - crop.height * k) / 2f - crop.top * k
    withTransform({
        translate(dx, dy)
        scale(k, k, Offset.Zero)
    }) { block() }
}

/** The same colour in greys: for a café that has closed. */
fun Color.grey(): Color {
    val l = 0.299f * red + 0.587f * green + 0.114f * blue
    return Color(l, l, l, alpha)
}
