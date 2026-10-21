package app.theusual

import app.theusual.data.Cafe
import app.theusual.data.Drink
import app.theusual.data.Pattern
import app.theusual.data.Visit
import app.theusual.domain.PacketCard
import app.theusual.domain.Packets
import app.theusual.domain.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class RulesTest {
    private val zone = ZoneOffset.UTC
    private fun day(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).atTime(10, 0).toInstant(zone).toEpochMilli()

    @Test fun packetGoesThereAndBack() {
        val p = PacketCard("Grain & Co", "12 Ryleeva St", "St Petersburg", "by the park", Pattern.SUN, 3, "Get the flat white. Sit by the window.", "Anya", 59.94371, 30.35772)
        assertEquals(p, Packets.parse(Packets.text(p)))
    }

    @Test fun packetSurvivesAMessengerAndOddBits() {
        val p = PacketCard("Пенка", note = "Капучино у стойки", from = "Саша", palette = 1, pattern = Pattern.DOTS)
        val sent = "Смотри!\n\n" + Packets.text(p) + "\n"
        val back = Packets.parse(sent)!!
        assertEquals("Пенка", back.name)
        assertEquals("Капучино у стойки", back.note)
        assertEquals("Саша", back.from)
        assertEquals(Pattern.DOTS, back.pattern)
        assertEquals(1, back.palette)
    }

    @Test fun plainTextIsNotAPacket() {
        assertNull(Packets.parse("Let's grab a coffee at Penka tomorrow?"))
    }

    @Test fun usualWrittenDownWins() {
        val c = Cafe(id = 1, name = "x", usual = "flat white on oat", usualDrink = Drink.FLAT_WHITE.name)
        assertEquals("flat white on oat" to Drink.FLAT_WHITE, Rules.usual(c, emptyList()))
    }

    @Test fun usualIsLearnedFromRepeatedOrders() {
        val c = Cafe(id = 1, name = "x")
        val v = listOf(
            Visit(id = 1, cafeId = 1, at = 1, drink = Drink.LATTE.name, what = "Latte"),
            Visit(id = 2, cafeId = 1, at = 2, drink = Drink.ESPRESSO.name, what = "espresso"),
        )
        assertNull(Rules.usual(c, v))
        val usual = Rules.usual(c, v + Visit(id = 3, cafeId = 1, at = 3, drink = Drink.LATTE.name, what = "latte "))
        assertNotNull(usual)
        assertEquals(Drink.LATTE, usual!!.second)
    }

    @Test fun regularFromTheTenthCup() {
        assertTrue(!Rules.regular(9))
        assertTrue(Rules.regular(10))
    }

    @Test fun usualCupsPileUpAtTheSpot() {
        val visits = (1..30).map { Visit(id = it.toLong(), cafeId = 1, at = it * 86_400_000L, drink = Drink.FLAT_WHITE.name) }
        val rings = Rules.rings(visits, Drink.FLAT_WHITE)
        val near = rings.count { kotlin.math.hypot(it.x - Rules.SPOT_X, it.y - Rules.SPOT_Y) < 0.12f }
        assertTrue("most usual cups land at the spot, got $near", near >= 20)
        assertEquals(rings, Rules.rings(visits, Drink.FLAT_WHITE))
    }

    @Test fun aYearInCups() {
        val a = Cafe(id = 1, name = "A")
        val b = Cafe(id = 2, name = "B", closedAt = day(2026, 6, 1))
        val v = listOf(
            Visit(id = 1, cafeId = 1, at = day(2025, 12, 30), drink = Drink.RAF.name, price = 300),
            Visit(id = 2, cafeId = 1, at = day(2026, 1, 3), drink = Drink.FLAT_WHITE.name, price = 280, with = "Dima", windowMin = 40),
            Visit(id = 3, cafeId = 1, at = day(2026, 2, 3), drink = Drink.FLAT_WHITE.name, price = 300, with = "Dima, Anya"),
            Visit(id = 4, cafeId = 2, at = day(2026, 3, 3), drink = Drink.ESPRESSO.name),
        )
        val y = Rules.year(2026, listOf(a, b), v, zone)
        assertEquals(3, y.visits)
        assertEquals(2, y.cafes)
        assertEquals(1, y.found)
        assertEquals(40, y.windowMin)
        assertEquals(580, y.spent)
        assertEquals(a, y.loyal?.first)
        assertEquals(Drink.FLAT_WHITE, y.drink)
        assertEquals("Dima" to 2, y.company)
        assertEquals(listOf(b), y.closed)
        val index = Rules.priceIndex(v, zone)!!
        assertEquals(Drink.FLAT_WHITE, index.first)
        assertEquals(listOf(2026 to 290), index.second)
    }
}
