package app.theusual.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.theusual.domain.Ring
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Dark planks of a café table, with their grain. */
fun Modifier.wood(): Modifier = drawWithCache {
    val r = Random(7)
    val plank = 130.dp.toPx()
    val grain = ArrayList<Triple<Path, Float, Float>>()
    val shades = ArrayList<Color>()
    var y = 0f
    while (y < size.height) {
        shades += listOf(Color(0xFF5F3E28), Color(0xFF56371F), Color(0xFF634129), Color(0xFF523420))[r.nextInt(4)]
        repeat(9) {
            val gy = y + 8f + r.nextFloat() * (plank - 16f)
            val amp = 2f + r.nextFloat() * 5f
            val p = Path().apply {
                moveTo(0f, gy)
                var x = 0f
                var up = true
                while (x < size.width) {
                    quadraticTo(x + 60f, gy + if (up) amp else -amp, x + 120f, gy)
                    x += 120f; up = !up
                }
            }
            grain += Triple(p, .6f + r.nextFloat(), .2f + r.nextFloat() * .25f)
        }
        y += plank
    }
    onDrawBehind {
        shades.forEachIndexed { i, c ->
            drawRect(c, Offset(0f, i * plank), Size(size.width, plank))
            drawLine(Color(0xFF2C1A0F), Offset(0f, i * plank), Offset(size.width, i * plank), 3f)
        }
        grain.forEach { (p, w, a) -> drawPath(p, Color(0xFF2C1A0F).copy(alpha = a), style = Stroke(w)) }
    }
}

/** Lamplight from the upper left and dark corners, over whatever is under it. */
fun Modifier.lamplight(): Modifier = drawWithCache {
    val b = Brush.radialGradient(
        0f to Color(0x38FFD99A), .55f to Color.Transparent, 1f to Color(0x8C000000),
        center = Offset(size.width * .38f, size.height * .22f), radius = size.maxDimension * .85f,
    )
    onDrawWithContent {
        drawContent()
        drawRect(b)
    }
}

/** A cork board's speckles. */
fun Modifier.cork(): Modifier = drawWithCache {
    val r = Random(4)
    val colours = listOf(Color(0xB38A5C32), Color(0xB3C99A66), Color(0xB37A4F2A), Color(0xB3D6AB78))
    val dots = List((size.width * size.height / 900f).toInt().coerceAtMost(6000)) {
        Triple(Offset(r.nextFloat() * size.width, r.nextFloat() * size.height), .8f + r.nextFloat() * 2f, colours[r.nextInt(4)])
    }
    onDrawBehind {
        drawRect(Color(0xFFB4834F))
        dots.forEach { (o, rad, c) -> drawCircle(c, rad, o) }
    }
}

/** Notebook paper, lighter where the lamp falls. */
fun Modifier.paper(shape: RoundedCornerShape = RoundedCornerShape(3.dp)): Modifier = this
    .background(Brush.radialGradient(listOf(Color(0xFFFFF6DC), Color(0xFFE6D2AA)), center = Offset(260f, 160f), radius = 1500f), shape)

/**
 * String lights hanging in loose swags across the top of the board, the bulbs breathing a little.
 */
@Composable
fun StringLights(modifier: Modifier = Modifier) {
    val t by phase(4000)
    Canvas(modifier.fillMaxWidth().height(64.dp)) {
        val swag = size.width / 3f
        val sag = 26.dp.toPx()
        val top = 10.dp.toPx()
        fun y(x: Float) = top + sag * sin(PI.toFloat() * ((x % swag) / swag))
        val wire = Path().apply {
            moveTo(0f, y(0f))
            var x = 0f
            while (x <= size.width) { lineTo(x, y(x)); x += 6f }
        }
        drawPath(wire, Color(0xFF2A2420), style = Stroke(1.6.dp.toPx()))
        val step = size.width / 14f
        for (i in 0..14) {
            val x = step * i + step / 2
            if (x > size.width) break
            val yy = y(x)
            val c = listOf(Color(0xFFFFD27A), Color(0xFFFFBE6B), Color(0xFFFFE2A0))[i % 3]
            val breathe = .75f + .25f * sin((t + i * .13f) * 2f * PI.toFloat())
            val b = 4.dp.toPx()
            drawCircle(Brush.radialGradient(listOf(c.copy(alpha = .55f * breathe), Color.Transparent), Offset(x, yy + b * 3), b * 7), b * 7, Offset(x, yy + b * 3))
            drawRect(Color(0xFF2A2420), Offset(x - b * .6f, yy), Size(b * 1.2f, b * 1.2f))
            drawOval(c, Offset(x - b, yy + b), Size(b * 2, b * 3.2f))
            drawOval(Color.White.copy(alpha = .8f), Offset(x - b * .55f, yy + b * 1.5f), Size(b * .6f, b * 1.1f))
        }
    }
}

/** A strip of washi tape. */
@Composable
fun Tape(modifier: Modifier = Modifier, w: Dp = 72.dp, h: Dp = 20.dp, angle: Float = -6f, pink: Boolean = false) {
    Canvas(modifier.size(w, h).rotate(angle)) {
        val base = if (pink) Color(0xD9E9B7A3) else Color(0xD99CC7BD)
        val light = if (pink) Color(0xD9F7DCCF) else Color(0xD9B9DBD2)
        drawRect(base)
        if (pink) {
            var x = 5f
            while (x < size.width) { var yy = 5f; while (yy < size.height) { drawCircle(light, 1.8.dp.toPx(), Offset(x, yy)); yy += 10.dp.toPx() }; x += 10.dp.toPx() }
        } else {
            val s = 8.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawPath(Path().apply { moveTo(x, size.height); lineTo(x + size.height, 0f); lineTo(x + size.height + s * .4f, 0f); lineTo(x + s * .4f, size.height); close() }, light)
                x += s
            }
        }
    }
}

/** A rubber stamp: letters in a box, a little crooked. */
@Composable
fun Stamp(text: String, modifier: Modifier = Modifier, color: Color = Color(0xFF3D7470), angle: Float = -3f) {
    Box(modifier.rotate(angle).border(2.dp, color.copy(alpha = .85f), RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text.uppercase(), fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = .14.em, color = color.copy(alpha = .9f), maxLines = 1)
    }
}

/** A round stamp: a word in the middle, a smaller one over it, three stars under. */
@Composable
fun RoundStamp(top: String, mid: String, modifier: Modifier = Modifier, color: Color = Color(0xFF3D7470), angle: Float = 12f) {
    val m = rememberTextMeasurer()
    Canvas(modifier.size(88.dp).rotate(angle)) {
        val c = center
        val r = size.minDimension / 2 - 3.dp.toPx()
        drawCircle(color.copy(alpha = .85f), r, c, style = Stroke(2.4.dp.toPx()))
        drawCircle(color.copy(alpha = .85f), r * .82f, c, style = Stroke(1.1.dp.toPx()))
        val a = m.measure(top.uppercase(), TextStyle(fontFamily = Mono, fontSize = 8.sp, letterSpacing = .12.em, color = color))
        val b = m.measure(mid.uppercase(), TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = .08.em, color = color), maxLines = 1,
            overflow = TextOverflow.Clip, constraints = Constraints(maxWidth = (r * 1.5f).toInt()))
        val s = m.measure("★ ★ ★", TextStyle(fontSize = 7.sp, color = color))
        drawText(a, topLeft = Offset(c.x - a.size.width / 2, c.y - r * .62f))
        drawText(b, topLeft = Offset(c.x - b.size.width / 2, c.y - b.size.height / 2))
        drawText(s, topLeft = Offset(c.x - s.size.width / 2, c.y + r * .38f))
    }
}

/**
 * The rings on a page: each cup's mark, a darker rim and a faint wash inside (the coffee-ring
 * effect), wobbly, some with a second, half-printed rim where the cup was set down twice. Beside a
 * few of them, a line in pencil.
 */
@Composable
fun Rings(rings: List<Ring>, notes: List<Pair<Ring, String>>, modifier: Modifier = Modifier, ink: Color = Color(0xFF5D4A36)) {
    val m = rememberTextMeasurer()
    val density = LocalDensity.current
    Canvas(modifier) {
        val unit = size.width
        rings.forEach { ring(it, unit) }
        // Each line goes under its ring, or over it, wherever it doesn't run into a line already written.
        val placed = ArrayList<Rect>()
        notes.asReversed().forEach { (r, text) ->
            val t = m.measure(text, TextStyle(fontFamily = Hand, fontSize = with(density) { (unit * .046f).toSp() }, color = ink), maxLines = 1, overflow = TextOverflow.Ellipsis,
                constraints = Constraints(maxWidth = (unit * .5f).toInt()))
            val x = (r.x * size.width - t.size.width / 2f).coerceIn(4f, size.width - t.size.width - 4f)
            val cy = r.y * size.height
            val spots = listOf(cy + r.r * unit + 4f, cy - r.r * unit - t.size.height - 4f, cy - t.size.height / 2f)
            val y = spots.map { it.coerceIn(0f, size.height - t.size.height) }.firstOrNull { y ->
                val box = Rect(x - 6f, y - 2f, x + t.size.width + 6f, y + t.size.height + 2f)
                placed.none { it.overlaps(box) }
            } ?: return@forEach
            placed += Rect(x, y, x + t.size.width, y + t.size.height)
            drawText(t, topLeft = Offset(x, y))
        }
    }
}

private fun DrawScope.ring(r: Ring, unit: Float) {
    val c = Offset(r.x * size.width, r.y * size.height)
    val rad = r.r * unit
    val color = Color(r.drink.ring)
    val rnd = Random((r.x * 10_000).toInt() * 31 + (r.y * 10_000).toInt())
    val p1 = rnd.nextFloat() * 6.28f
    val p2 = rnd.nextFloat() * 6.28f
    fun wobbly(rr: Float, dx: Float = 0f, dy: Float = 0f) = Path().apply {
        for (i in 0..64) {
            val a = i / 64f * 2f * PI.toFloat()
            val k = rr * (1f + .03f * sin(3 * a + p1) + .018f * sin(7 * a + p2))
            val o = Offset(c.x + dx + cos(a) * k, c.y + dy + sin(a) * k)
            if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
        }
        close()
    }
    val w = unit * .0075f
    val outer = wobbly(rad)
    drawPath(outer, color.copy(alpha = .08f * r.strength))
    drawPath(outer, color.copy(alpha = .62f * r.strength), style = Stroke(w, join = StrokeJoin.Round))
    drawPath(wobbly(rad - w * 1.2f, w * .5f, -w * .3f), color.copy(alpha = .22f * r.strength), style = Stroke(w * .45f))
    if (r.partial) {
        val off = Offset(rad * .12f, -rad * .1f)
        drawArc(color.copy(alpha = .35f * r.strength), 180f + p1 * 10f, 110f, false, c - Offset(rad, rad) + off, Size(rad * 2, rad * 2), style = Stroke(w * .7f, cap = StrokeCap.Round))
    }
}

/**
 * The café front drawn in ink, as on a napkin, when there's no photo to sketch from. The awning is
 * printed in the packet's colours.
 */
@Composable
fun FacadeSketch(awning: Color, modifier: Modifier = Modifier, sign: String = "") {
    val m = rememberTextMeasurer()
    val density = LocalDensity.current
    Canvas(modifier) {
        scene(280f, 222f, Rect(0f, 20f, 280f, 222f)) {
            val ink = Color(0xFF2A1F17)
            val line = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(Path().apply { moveTo(20f, 40f); lineTo(20f, 210f); lineTo(260f, 210f); lineTo(260f, 40f) }, ink, style = line)
            drawRect(Color(0xFFFBF3E2), Offset(10f, 28f), Size(260f, 12f))
            drawRect(ink, Offset(10f, 28f), Size(260f, 12f), style = Stroke(2f))
            if (sign.isNotEmpty()) {
                val t = m.measure(sign.uppercase(), TextStyle(fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = with(density) { 10f.toSp() }, letterSpacing = .3.em, color = ink), maxLines = 1,
                    overflow = TextOverflow.Clip, constraints = Constraints(maxWidth = 240))
                drawText(t, topLeft = Offset(140f - t.size.width / 2f, 34f - t.size.height / 2f))
            }
            val canopy = Path().apply {
                moveTo(14f, 52f); lineTo(266f, 52f); lineTo(262f, 78f)
                for (i in 0 until 11) quadraticTo(262f - i * 22f - 11f, 88f, 262f - (i + 1) * 22f, 78f)
                close()
            }
            clipPath(canopy) {
                var x = 14f
                var i = 0
                while (x < 266f) { drawRect(if (i % 2 == 0) awning else Color(0xFFFBF3E2), Offset(x, 50f), Size(12f, 40f)); x += 12f; i++ }
            }
            drawPath(canopy, ink, style = Stroke(2f, join = StrokeJoin.Round))
            drawRect(Color(0xFFF6DFAE), Offset(34f, 96f), Size(116f, 82f))
            drawRect(ink, Offset(34f, 96f), Size(116f, 82f), style = line)
            drawLine(ink, Offset(92f, 96f), Offset(92f, 178f), 2f)
            drawLine(ink, Offset(34f, 134f), Offset(150f, 134f), 2f)
            // Someone at the window, and a cup with steam.
            drawPath(Path().apply { moveTo(50f, 128f); relativeQuadraticTo(8f, -14f, 16f, 0f) }, ink, style = line)
            drawCircle(ink, 5f, Offset(58f, 114f), style = line)
            drawPath(Path().apply { moveTo(110f, 128f); lineTo(110f, 118f); lineTo(126f, 118f); lineTo(126f, 128f); close(); moveTo(126f, 120f); relativeQuadraticTo(6f, 0f, 4f, 5f); relativeLineTo(-4f, 1f) }, ink, style = line)
            drawPath(Path().apply { moveTo(114f, 114f); relativeQuadraticTo(-3f, -5f, 1f, -9f); moveTo(120f, 114f); relativeQuadraticTo(-3f, -5f, 1f, -9f) }, ink, style = line)
            drawRect(Color(0xFFC9A77D), Offset(182f, 96f), Size(58f, 114f))
            drawRect(ink, Offset(182f, 96f), Size(58f, 114f), style = line)
            drawRect(ink, Offset(192f, 108f), Size(38f, 32f), style = line)
            drawCircle(ink, 3f, Offset(230f, 156f))
            // A plant in a pot, a little table outside.
            drawPath(Path().apply {
                moveTo(40f, 210f); lineTo(40f, 196f); lineTo(60f, 196f); lineTo(60f, 210f)
                moveTo(50f, 196f); relativeQuadraticTo(-12f, -24f, 0f, -34f); relativeQuadraticTo(12f, 10f, 0f, 34f)
                moveTo(50f, 190f); relativeQuadraticTo(-14f, -6f, -12f, -18f); moveTo(50f, 184f); relativeQuadraticTo(14f, -4f, 12f, -16f)
                moveTo(160f, 210f); lineTo(160f, 186f); moveTo(150f, 186f); lineTo(172f, 186f); moveTo(166f, 186f); lineTo(166f, 210f)
            }, ink, style = line)
            drawLine(ink, Offset(8f, 214f), Offset(272f, 214f), 2f)
        }
    }
}

/** A receipt-like line: typed text, dotted leader, a value. */
@Composable
fun Dotted(modifier: Modifier = Modifier, color: Color = Color(0xFF2E2219)) {
    Canvas(modifier.fillMaxWidth().height(2.dp)) {
        drawLine(color.copy(alpha = .6f), Offset(0f, 1f), Offset(size.width, 1f), 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx())))
    }
}

/** A thin card's shadow under paper laid on the table. */
fun Modifier.laid(radius: Dp = 3.dp): Modifier = drawBehind {
    drawRoundRect(Color(0x55000000), Offset(4.dp.toPx(), 8.dp.toPx()), size, CornerRadius(radius.toPx()))
}
