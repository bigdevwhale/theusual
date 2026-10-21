package app.theusual.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.theusual.data.Drink

/**
 * A cup on its saucer from above, as it stands on the table: a spoon on the saucer, the drink in
 * it with a rosetta poured in milk (or plain for espresso, tea and filter), steam rising.
 */
@Composable
fun CupFromAbove(drink: Drink, modifier: Modifier = Modifier, steamy: Boolean = true) {
    val t by phase(5200)
    Canvas(modifier) {
        scene(240f, 240f) {
            val c = Offset(120f, 130f)
            drawCircle(Color(0x55000000), 100f, c + Offset(6f, 12f))
            drawCircle(Color(0xFFF4EDE0), 100f, c)
            drawCircle(Color(0xFFE1D6C2), 86f, c, style = Stroke(3f))
            drawRoundRect(Color(0xFFEFE6D6), Offset(c.x + 58f, c.y - 12f), Size(48f, 24f), androidx.compose.ui.geometry.CornerRadius(12f))
            drawCircle(Color(0xFFFBF7EF), 66f, c)
            drawCircle(Color(0xFFD9CDB8), 66f, c, style = Stroke(2f))
            val coffee = Color(drink.ring)
            val surface = when (drink) {
                Drink.ESPRESSO, Drink.FILTER -> Color(0xFF4A2A16)
                Drink.TEA -> Color(0xFFB9853E)
                Drink.COCOA -> Color(0xFF6B3F27)
                else -> Color(0xFFB07C4C)
            }
            drawCircle(surface, 55f, c)
            drawCircle(coffee.copy(alpha = .6f), 55f, c, style = Stroke(5f))
            when (drink) {
                Drink.ESPRESSO -> drawCircle(Color(0xFFC58B4F), 46f, c)
                Drink.FILTER, Drink.TEA -> drawCircle(Color.White.copy(alpha = .18f), 18f, c + Offset(-16f, -18f))
                Drink.COCOA -> for (i in 0..7) drawCircle(Color(0xFFF1E3CF), 6f + i % 3, c + Offset((i * 23 % 50 - 25).toFloat(), (i * 37 % 46 - 23).toFloat()))
                else -> rosetta(c)
            }
            // The spoon on the saucer.
            drawLine(Color(0xFFC9C2B6), c + Offset(-90f, 70f), c + Offset(-24f, 22f), 7f, StrokeCap.Round)
            rotate(-35f, c + Offset(-14f, 15f)) { drawOval(Color(0xFFD8D1C5), c + Offset(-27f, 6f), Size(26f, 18f)) }
            if (steamy) {
                steamWisps(c + Offset(-14f, -20f), 90f, t, Color(0x80FFFFFF), 7f)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.rosetta(c: Offset) {
    val milk = Color(0xFFF7ECD8)
    for (i in 0..6) {
        val w = 26f - i * 3f
        val y = 14f - i * 5f
        drawPath(Path().apply { moveTo(c.x - w, c.y + y); quadraticTo(c.x, c.y + y + 10f, c.x + w, c.y + y) }, milk, style = Stroke(4f, cap = StrokeCap.Round))
    }
    drawCircle(milk, 6f, c + Offset(0f, -26f))
    drawLine(milk, c + Offset(0f, -26f), c + Offset(0f, 30f), 3f, StrokeCap.Round)
}
