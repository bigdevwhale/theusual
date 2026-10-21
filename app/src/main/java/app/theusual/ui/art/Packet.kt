package app.theusual.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import app.theusual.data.Pattern
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import kotlin.math.cos
import kotlin.math.sin

/** How a packet sits on the board. */
enum class PacketState {
    /** A café you've been to. */
    PLAIN,

    /** It has closed: printed in greys, with a black ribbon across the corner. */
    CLOSED,

    /** Not been yet ("want to go"): only a dashed outline with the name written in. */
    GHOST,
}

@Immutable
data class PacketLook(
    val name: String,
    val sub: String = "",
    val pattern: Pattern = Pattern.STRIPES,
    val palette: Int = 0,
    /** A three-letter stamp of the city, for a café away from home. */
    val city: String = "",
    val state: PacketState = PacketState.PLAIN,
    /** "SUGAR · 5 g", in the app's language. */
    val sugar: String = "SUGAR · 5 g",
)

/** Packet units: 130 × 180. */
private const val PW = 130f
private const val PH = 180f

/**
 * A sugar packet: a paper sachet with torn zigzag ends, a print over the whole of it, a round
 * cream label with the café's name, a band with the weight at the bottom.
 */
@Composable
fun Packet(look: PacketLook, modifier: Modifier = Modifier, ghostInk: Color = Color(0xFFFBF3E2)) {
    val m = rememberTextMeasurer()
    val d = LocalDensity.current
    Canvas(modifier.aspectRatio(PW / PH)) {
        val k = size.width / PW
        drawContext.transform.scale(k, k, Offset.Zero)
        packet(look, m, d, ghostInk, k)
    }
}

private fun outline(): Path = Path().apply {
    val teeth = 11
    val step = PW / teeth
    moveTo(0f, 6f)
    for (i in 0 until teeth) {
        lineTo(i * step + step / 2, 2f)
        lineTo((i + 1) * step, 6f)
    }
    lineTo(PW, PH - 6f)
    for (i in teeth - 1 downTo 0) {
        lineTo(i * step + step / 2, PH - 2f)
        lineTo(i * step, PH - 6f)
    }
    close()
}

private fun DrawScope.packet(look: PacketLook, m: TextMeasurer, d: Density, ghostInk: Color, k: Float) {
    val (bg0, ac0) = Art.packet(look.palette)
    val closed = look.state == PacketState.CLOSED
    val bg = if (closed) bg0.grey() else bg0
    val ac = if (closed) ac0.grey() else ac0
    val ink = Color(0xFF2A1F17)
    val shape = outline()
    fun u(px: Float) = with(d) { (px * k).toSp() }

    if (look.state == PacketState.GHOST) {
        drawPath(shape, ghostInk.copy(alpha = .06f))
        drawPath(shape, ghostInk.copy(alpha = .8f), style = Stroke(1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
        val name = m.measure(look.name, TextStyle(fontFamily = Hand, fontSize = u(26f), color = ghostInk), overflow = TextOverflow.Ellipsis, maxLines = 2,
            constraints = Constraints(maxWidth = ((PW - 20f) * k).toInt()))
        scaleDown(k) { drawText(name, topLeft = Offset((PW * k - name.size.width) / 2f, PH * k * .36f - name.size.height / 2f)) }
        if (look.sub.isNotEmpty()) {
            val sub = m.measure(look.sub, TextStyle(fontFamily = Hand, fontSize = u(19f), color = ghostInk.copy(alpha = .8f)), maxLines = 1, overflow = TextOverflow.Ellipsis,
                constraints = Constraints(maxWidth = ((PW - 20f) * k).toInt()))
            scaleDown(k) { drawText(sub, topLeft = Offset((PW * k - sub.size.width) / 2f, PH * k * .56f)) }
        }
        return
    }

    translate(3f, 4f) { drawPath(shape, Color(0x33000000)) }
    drawPath(shape, bg)
    clipPath(shape) {
        when (look.pattern) {
            Pattern.STRIPES -> {
                var x = 0f
                while (x < PW) { drawRect(ac.copy(alpha = .92f), Offset(x, 0f), Size(7f, PH)); x += 14f }
            }
            Pattern.DOTS -> for (row in 0..9) for (col in 0..6) {
                drawCircle(ac, 3.2f, Offset(10f + col * 18f + if (row % 2 == 1) 9f else 0f, 14f + row * 18f))
            }
            Pattern.CHEVRON -> for (i in 0..8) {
                val y = 26f + i * 22f
                drawPath(Path().apply { moveTo(-10f, y); lineTo(PW / 2, y - 14f); lineTo(PW + 10f, y) }, ac, style = Stroke(6f))
            }
            Pattern.BAND -> {
                drawRect(ac, Offset(0f, PH * .55f), Size(PW, PH * .2f))
                drawRect(ac.copy(alpha = .5f), Offset(0f, PH * .1f), Size(PW, 6f))
            }
            Pattern.SUN -> {
                val c = Offset(PW / 2, PH * .42f)
                for (a in 0 until 16) {
                    val t = a / 8f * Math.PI.toFloat()
                    drawLine(ac, c, c + Offset(cos(t) * 170f, sin(t) * 170f), 7f)
                }
            }
        }
    }
    drawPath(shape, ink, style = Stroke(2f))

    // The round label with the name.
    val c = Offset(PW / 2, PH * .42f)
    drawCircle(Color(0xFFFBF3E2), PW * .32f, c)
    drawCircle(ink, PW * .32f, c, style = Stroke(1.6f))
    val name = m.measure(look.name.uppercase(), TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = u(if (look.name.length > 8) 12f else 15f), color = ink),
        maxLines = 2, overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = (PW * .58f * k).toInt()))
    val sub = look.sub.takeIf { it.isNotEmpty() }?.let {
        m.measure(it, TextStyle(fontFamily = Hand, fontSize = u(18f), color = Color(0xFFA8392B)), maxLines = 1, overflow = TextOverflow.Ellipsis,
            constraints = Constraints(maxWidth = (PW * .58f * k).toInt()))
    }
    val total = name.size.height + (sub?.size?.height ?: 0) * .85f
    scaleDown(k) {
        val top = c.y * k - total / 2f
        drawText(name, topLeft = Offset(c.x * k - name.size.width / 2f, top))
        sub?.let { drawText(it, topLeft = Offset(c.x * k - it.size.width / 2f, top + name.size.height * .92f)) }
    }
    // The band with the weight.
    drawRect(Color(0xEBFBF3E2), Offset(10f, PH - 32f), Size(PW - 20f, 16f))
    val sugar = m.measure(look.sugar, TextStyle(fontFamily = Mono, fontSize = u(9.5f), color = ink), maxLines = 1)
    scaleDown(k) { drawText(sugar, topLeft = Offset((PW * k - sugar.size.width) / 2f, (PH - 24f) * k - sugar.size.height / 2f)) }

    if (look.city.isNotEmpty()) {
        val at = Offset(PW - 24f, 32f)
        rotate(-14f, at) {
            drawCircle(Color(0xFFFBF3E2), 19f, at)
            drawCircle(Color(0xFFA8392B), 19f, at, style = Stroke(2f))
            val t = m.measure(look.city.take(3).uppercase(), TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = u(10f), color = Color(0xFFA8392B)))
            scaleDown(k) { drawText(t, topLeft = Offset(at.x * k - t.size.width / 2f, at.y * k - t.size.height / 2f)) }
        }
    }
    if (closed) {
        drawPath(Path().apply { moveTo(-6f, 40f); lineTo(40f, -6f); lineTo(58f, -6f); lineTo(-6f, 58f); close() }, ink)
    }
}

/** Text is measured in real pixels; this undoes the packet's scale while it is drawn. */
private inline fun DrawScope.scaleDown(k: Float, block: DrawScope.() -> Unit) {
    drawContext.transform.scale(1f / k, 1f / k, Offset.Zero)
    block()
    drawContext.transform.scale(k, k, Offset.Zero)
}
