package app.theusual.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.Sign
import kotlin.math.sin
import kotlin.random.Random

/** The scene's size, in its own units. */
const val SCENE_W = 1120f
const val SCENE_H = 600f

/** Crops of the scene: the whole front, and the table by the window with the reader. */
val WholeFront = Rect(0f, 0f, SCENE_W, SCENE_H)
val ByTheWindow = Rect(140f, 100f, 640f, 476f)
val InsideDark = Rect(170f, 80f, 930f, 560f)

/**
 * The café from the street on a rainy evening: a striped awning, a big window full of lamplight,
 * someone at the table by it with a cup, shelves and the counter behind, the name in gold
 * leaf on the glass, a fogged-up corner with a heart drawn in it, a black cat on the sill, a
 * street lamp, wet cobbles. With [lights] off the café has closed: blue dark inside, chairs up on
 * the table, a sign on the door.
 */
@Composable
fun WindowScene(
    modifier: Modifier = Modifier,
    lights: Boolean = true,
    name: String = "The Usual",
    tagline: String = "COFFEE · NOTES · REGULARS WELCOME",
    closedSign: String = "",
    crop: Rect = WholeFront,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val rain by phase(1400)
    val steam by phase(5200)
    val bits = remember { Bits.make() }
    Canvas(modifier.clipToBounds()) {
        scene(SCENE_W, SCENE_H, crop) {
            front(lights, bits)
            inside(lights, bits, steam)
            if (lights && name.isNotEmpty()) lettering(measurer, density, name, tagline)
            if (!lights) closed(measurer, density, closedSign, name)
            glass(lights, bits)
            outside(lights)
            rainfall(bits, rain)
        }
    }
}

/** Fixed random bits of the scene, so it's the same every frame. */
private class Bits(
    val shelf: List<Triple<Int, Float, Float>>,
    val drops: List<Triple<Float, Float, Float>>,
    val streaks: List<Triple<Float, Float, Float>>,
    val rain: List<Triple<Float, Float, Float>>,
    val ripples: List<Triple<Float, Float, Float>>,
) {
    companion object {
        fun make(): Bits {
            val r = Random(11)
            val shelf = ArrayList<Triple<Int, Float, Float>>()
            for (row in 0..1) {
                var x = 650f
                while (x < 960f) {
                    val k = r.nextFloat()
                    val kind = if (k < .4f) 0 else if (k < .75f) 1 else 2
                    shelf += Triple(row * 10 + kind, x, 22f + r.nextFloat() * 14f)
                    x += when (kind) { 0 -> 26f; 1 -> 24f; else -> 30f }
                }
            }
            val drops = List(90) { Triple(130f + r.nextFloat() * 860f, 96f + r.nextFloat() * 380f, 1.2f + r.nextFloat() * 2.4f) }
            val streaks = List(14) { Triple(140f + r.nextFloat() * 840f, 116f + r.nextFloat() * 280f, 40f + r.nextFloat() * 40f) }
            val rain = List(240) { Triple(r.nextFloat() * SCENE_W, r.nextFloat() * SCENE_H, .12f + r.nextFloat() * .23f) }
            val ripples = List(18) { Triple(130f + r.nextFloat() * 700f, 556f + r.nextFloat() * 40f, 40f + r.nextFloat() * 100f) }
            return Bits(shelf, drops, streaks, rain, ripples)
        }
    }
}

private val WX = 130f
private val WY = 96f
private val WW = 860f
private val WH = 380f

private fun DrawScope.front(lights: Boolean, b: Bits) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF121722), Color(0xFF231C1F))), size = Size(SCENE_W, SCENE_H))
    drawRect(Color(0xFF2A1E18), size = Size(SCENE_W, 540f))
    var x = 20f
    while (x < SCENE_W) {
        drawLine(Color(0xFF1D1410), Offset(x, 60f), Offset(x, 540f), 2f)
        x += 38f
    }
    // The street lamp and its pool of light.
    drawCircle(Brush.radialGradient(listOf(Color(0x88FFE2A8), Color(0x22FFB04A), Color.Transparent), Offset(58f, 150f), 140f), 140f, Offset(58f, 150f))
    drawLine(Color(0xFF121012), Offset(58f, 600f), Offset(58f, 170f), 9f)
    drawPath(Path().apply { moveTo(40f, 170f); lineTo(76f, 170f); lineTo(68f, 148f); lineTo(48f, 148f); close() }, Color(0xFF121012))
    drawOval(Color(0xFFFFE7B0), Offset(44f, 168f), Size(28f, 8f))
}

private fun DrawScope.inside(lights: Boolean, b: Bits, steam: Float) {
    val sil = if (lights) Color(0xFF5A2C14) else Color(0xFF10121A)
    val shelf = if (lights) Color(0xFF8D4A22) else Color(0xFF272B37)
    clipRect(WX, WY, WX + WW, WY + WH) {
        val glow = if (lights) listOf(Color(0xFFFFDD9C), Color(0xFFE99A4C), Color(0xFF6E3418)) else listOf(Color(0xFF3E4555), Color(0xFF191B24))
        drawRect(Brush.radialGradient(glow, Offset(WX + WW * .45f, WY + WH * .25f), WW * .75f), Offset(WX, WY), Size(WW, WH))
        // Shelves of jars and cups behind the counter.
        for (row in 0..1) {
            val sy = WY + 140f + row * 56f
            drawRect(shelf, Offset(640f, sy), Size(340f, 6f))
            b.shelf.filter { it.first / 10 == row }.forEach { (k, x, h) ->
                when (k % 10) {
                    0 -> drawRoundRect(shelf.copy(alpha = .9f), Offset(x, sy - h), Size(18f, h), CornerRadius(3f))
                    1 -> drawPath(Path().apply { moveTo(x, sy - 16f); lineTo(x + 18f, sy - 16f); lineTo(x + 15f, sy); lineTo(x + 3f, sy); close() }, shelf)
                    else -> drawOval(shelf, Offset(x - 1f, sy - 24f), Size(26f, 24f))
                }
            }
        }
        // The counter and the machine on it.
        drawRect(sil.copy(alpha = .85f), Offset(660f, WY + 250f), Size(340f, 140f))
        drawRect(shelf, Offset(660f, WY + 246f), Size(340f, 8f))
        drawRoundRect(sil, Offset(770f, WY + 196f), Size(130f, 52f), CornerRadius(6f))
        drawRect(shelf, Offset(784f, WY + 210f), Size(20f, 14f))
        drawRect(shelf, Offset(866f, WY + 210f), Size(20f, 14f))
        // Pendant lamps.
        for (lx in listOf(300f, 560f, 820f)) {
            if (lights) drawCircle(Brush.radialGradient(listOf(Color(0xF2FFF2C8), Color(0x73FFCF7A), Color.Transparent), Offset(lx, WY + 92f), 110f), 110f, Offset(lx, WY + 92f))
            drawLine(sil, Offset(lx, WY), Offset(lx, WY + 62f), 2f)
            drawPath(Path().apply { moveTo(lx - 26f, WY + 88f); lineTo(lx - 12f, WY + 62f); lineTo(lx + 12f, WY + 62f); lineTo(lx + 26f, WY + 88f); close() }, sil)
            drawOval(if (lights) Color(0xFFFFF1C4) else Color(0xFF3A3F4C), Offset(lx - 12f, WY + 85f), Size(24f, 10f))
        }
        // The table by the window.
        drawLine(sil, Offset(190f, WY + 300f), Offset(470f, WY + 300f), 8f)
        drawLine(sil, Offset(220f, WY + 304f), Offset(214f, WY + 380f), 7f)
        drawLine(sil, Offset(440f, WY + 304f), Offset(446f, WY + 380f), 7f)
        if (lights) reader(sil, steam) else chairsUp(sil)
        // A heart drawn with a finger in the fogged glass.
        val heart = Path().apply {
            moveTo(820f, WY + 330f)
            relativeQuadraticTo(-22f, -22f, -6f, -34f)
            relativeQuadraticTo(10f, -6f, 16f, 6f)
            relativeQuadraticTo(6f, -12f, 16f, -6f)
            relativeQuadraticTo(16f, 12f, -6f, 34f)
            close()
        }
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x47FFFFFF)), WY + WH - 110f, WY + WH), Offset(WX, WY + WH - 110f), Size(WW, 110f))
        drawPath(heart, if (lights) Color(0xB3A65424) else Color(0xB3262A35), style = Stroke(5f, cap = StrokeCap.Round))
    }
}

/** Someone at the table, looking out, with a cup of something hot. */
private fun DrawScope.reader(sil: Color, steam: Float) {
    drawCircle(sil, 24f, Offset(300f, WY + 196f))
    drawCircle(sil, 11f, Offset(316f, WY + 176f))
    drawPath(Path().apply { moveTo(258f, WY + 300f); quadraticTo(256f, WY + 236f, 300f, WY + 226f); quadraticTo(344f, WY + 236f, 342f, WY + 300f); close() }, sil)
    // A cup on its saucer.
    drawOval(sil, Offset(412f, WY + 293.5f), Size(40f, 7f))
    drawPath(Path().apply { moveTo(419f, WY + 276f); lineTo(445f, WY + 276f); quadraticTo(444f, WY + 295f, 432f, WY + 295f); quadraticTo(420f, WY + 295f, 419f, WY + 276f); close() }, sil)
    drawPath(Path().apply { moveTo(444f, WY + 280f); relativeQuadraticTo(9f, 0f, 7f, 7f); relativeQuadraticTo(-2f, 5f, -9f, 4f) }, sil, style = Stroke(3.5f))
    steamWisps(Offset(430f, WY + 266f), 46f, steam, Color(0x8CFFF6DD), 4f)
}

/** Closed for good: the chairs are up on the table. */
private fun DrawScope.chairsUp(sil: Color) {
    for (x in listOf(250f, 360f)) {
        val p = Path().apply {
            moveTo(x, WY + 296f); lineTo(x, WY + 240f); lineTo(x + 50f, WY + 240f); lineTo(x + 50f, WY + 296f)
            moveTo(x, WY + 262f); lineTo(x + 50f, WY + 262f)
            moveTo(x, WY + 240f); lineTo(x - 4f, WY + 200f)
            moveTo(x + 50f, WY + 240f); lineTo(x + 54f, WY + 200f)
        }
        drawPath(p, sil, style = Stroke(6f))
    }
}

/** Rising steam: two soft curls that sway with [t]. */
fun DrawScope.steamWisps(at: Offset, h: Float, t: Float, color: Color, width: Float) {
    for (i in 0..1) {
        val sway = sin((t + i * .37f) * 6.283f) * 5f
        val x = at.x + i * 16f
        val y = at.y + i * 6f
        val hh = h * (1f - i * .2f)
        val p = Path().apply {
            moveTo(x, y)
            quadraticTo(x - 12f + sway, y - hh * .25f, x + sway * .6f, y - hh * .5f)
            quadraticTo(x + 12f + sway, y - hh * .75f, x - sway * .4f, y - hh)
        }
        drawPath(p, color.copy(alpha = color.alpha * (1f - i * .25f)), style = Stroke(width * (1f - i * .2f), cap = StrokeCap.Round))
    }
}

private fun gold() = Brush.verticalGradient(
    0f to Color(0xFFFBE6A6), .5f to Color(0xFFD9A24A), .55f to Color(0xFFB98232), 1f to Color(0xFFF1CF7C),
)

private fun Density.units(px: Float): TextUnit = px.toSp()

private fun DrawScope.lettering(m: TextMeasurer, d: Density, name: String, tagline: String) {
    val big = TextStyle(fontFamily = Sign, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = d.units(if (name.length > 12) 84f else 108f))
    val layout = m.measure(name, big)
    val top = Offset(560f - layout.size.width / 2f, WY + 168f - layout.firstBaseline)
    drawText(layout, color = Color(0x661A0E05), topLeft = top + Offset(1.5f, 3f))
    drawText(layout, color = Color(0xFF3A210E), topLeft = top, drawStyle = Stroke(5f))
    drawText(layout, brush = gold(), topLeft = top)
    drawPath(Path().apply { moveTo(420f, WY + 190f); quadraticTo(560f, WY + 206f, 700f, WY + 190f) }, gold(), style = Stroke(3f))
    val small = m.measure(tagline, TextStyle(fontFamily = Mono, fontSize = d.units(15f), letterSpacing = d.units(5f)))
    val st = Offset(560f - small.size.width / 2f, WY + 220f - small.firstBaseline)
    drawText(small, color = Color(0xFF3A210E), topLeft = st, drawStyle = Stroke(3f))
    drawText(small, color = Color(0xFFE7BF69), topLeft = st)
}

private fun DrawScope.closed(m: TextMeasurer, d: Density, sign: String, name: String) {
    clipRect(WX, WY, WX + WW, WY + WH) {
        if (name.isNotEmpty()) {
            val faded = m.measure(name, TextStyle(fontFamily = Sign, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = d.units(if (name.length > 12) 84f else 108f)))
            drawText(faded, color = Color(0x408A8F99), topLeft = Offset(560f - faded.size.width / 2f, WY + 168f - faded.firstBaseline))
        }
        if (sign.isNotEmpty()) {
            drawPath(Path().apply { moveTo(520f, WY + 4f); lineTo(560f, WY + 46f); lineTo(600f, WY + 4f) }, Color(0xFF8B8F9C), style = Stroke(2f))
            val t = m.measure(sign, TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = d.units(22f), letterSpacing = d.units(3f)))
            val w = t.size.width + 40f
            rotate(-3f, Offset(560f, WY + 78f)) {
                drawRoundRect(Color(0xFFE9E1CF), Offset(560f - w / 2, WY + 46f), Size(w, 62f), CornerRadius(6f))
                drawRoundRect(Color(0xFF2A1F17), Offset(560f - w / 2, WY + 46f), Size(w, 62f), CornerRadius(6f), style = Stroke(3f))
                drawText(t, color = Color(0xFF2A1F17), topLeft = Offset(560f - t.size.width / 2f, WY + 77f - t.size.height / 2f))
            }
        }
    }
}

private fun DrawScope.glass(lights: Boolean, b: Bits) {
    clipRect(WX, WY, WX + WW, WY + WH) {
        b.drops.forEach { (x, y, r) ->
            drawCircle(Color(0x38FFFFFF), r, Offset(x, y))
            drawCircle(Color(0x8CFFFFFF), r * .35f, Offset(x - r * .3f, y - r * .3f))
        }
        b.streaks.forEach { (x, y, l) ->
            drawPath(Path().apply { moveTo(x, y); relativeQuadraticTo(2f, l * .5f, -1f, l) }, Color(0x40FFFFFF), style = Stroke(2.2f))
        }
    }
    // The frame, the awning over it, the sill under it.
    drawRect(Color(0xFF3D2A1E), Offset(WX - 14f, WY - 14f), Size(WW + 28f, WH + 28f), style = Stroke(22f))
    drawRect(Color(0xFF1A110C), Offset(WX, WY), Size(WW, WH), style = Stroke(3f))
    val awning = Path().apply {
        moveTo(110f, 0f); lineTo(1010f, 0f); lineTo(1010f, 46f)
        for (i in 15 downTo 0) quadraticTo(110f + i * 56f + 28f, 70f, 110f + i * 56f, 46f)
        close()
    }
    clipPath(awning) {
        var x = 110f
        var i = 0
        while (x < 1010f) {
            drawRect(if (i % 2 == 0) Color(0xFF7A2A22) else Color(0xFFE3CFA6), Offset(x, 0f), Size(28f, 72f))
            x += 28f; i++
        }
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x33000000)), 30f, 70f), Offset(110f, 30f), Size(900f, 40f))
    }
    drawPath(awning, Color(0xFF2A1510), style = Stroke(2f))
    drawRect(Color(0xFF4B3324), Offset(96f, WY + WH + 10f), Size(928f, 16f))
    drawRect(Color(0x59000000), Offset(96f, WY + WH + 24f), Size(928f, 5f))
}

private fun DrawScope.outside(lights: Boolean) {
    // A black cat on the sill, looking in.
    val cat = Color(0xFF0F0B09)
    drawOval(cat, Offset(886f, 444f), Size(52f, 60f))
    drawCircle(cat, 17f, Offset(912f, 436f))
    drawPath(Path().apply { moveTo(898f, 428f); lineTo(900f, 410f); lineTo(910f, 424f); close(); moveTo(926f, 428f); lineTo(924f, 410f); lineTo(914f, 424f); close() }, cat)
    drawPath(Path().apply { moveTo(936f, 494f); quadraticTo(968f, 496f, 960f, 470f); quadraticTo(956f, 458f, 964f, 452f) }, cat, style = Stroke(7f, cap = StrokeCap.Round))
    // The window's light on the wet cobbles.
    if (lights) {
        withTransform({ scale(1f, .18f, Offset(560f, 575f)) }) {
            drawCircle(Brush.radialGradient(listOf(Color(0x80F2B25C), Color(0x30F2B25C), Color.Transparent), Offset(560f, 575f), 460f), 460f, Offset(560f, 575f))
        }
    }
}

private fun DrawScope.rainfall(b: Bits, t: Float) {
    b.rain.forEach { (x0, y0, a) ->
        val y = (y0 + t * SCENE_H) % SCENE_H
        val x = x0 - t * 40f
        drawLine(Color(0xFFCFD8EA).copy(alpha = a), Offset(x, y), Offset(x - 6f, y + 22f), 1.2f)
    }
    b.ripples.forEach { (x, y, l) -> drawLine(Color(0x59FFD79A), Offset(x, y), Offset(x + l, y), 2f) }
}
