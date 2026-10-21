package app.theusual.domain

import app.theusual.data.Cafe
import app.theusual.data.Drink
import app.theusual.data.Visit
import java.time.Instant
import java.time.ZoneId
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/** Where a ring lands on a café's page, in parts of the page (0..1), and how big the cup was. */
data class Ring(val x: Float, val y: Float, val r: Float, val drink: Drink, val strength: Float, val partial: Boolean)

/** A year in cups. */
data class Year(
    val year: Int,
    val visits: Int,
    val cafes: Int,
    /** Cafés whose first visit fell in this year. */
    val found: Int,
    val windowMin: Int,
    val spent: Int,
    /** The café with the most rings this year, and how many. */
    val loyal: Pair<Cafe, Int>?,
    val drink: Drink?,
    /** Who shared the most cups, and how many. */
    val company: Pair<String, Int>?,
    val closed: List<Cafe>,
)

object Rules {
    const val DAY = 86_400_000L
    const val HOUR = 3_600_000L

    /** From the tenth visit on, a café is "yours": the barista knows the order. */
    const val REGULAR = 10

    fun regular(visits: Int) = visits >= REGULAR

    /** Where the cup always lands, on the right-hand page. */
    const val SPOT_X = 0.64f
    const val SPOT_Y = 0.34f

    /**
     * The usual: what the owner wrote down, or else what they ordered at least twice of the last
     * ten visits, the most often. Null while there's nothing usual yet.
     */
    fun usual(c: Cafe, visits: List<Visit>): Pair<String, Drink?>? {
        if (c.usual.isNotBlank()) return c.usual to c.usualDrinkOf
        val recent = visits.sortedByDescending { it.at }.take(10).filter { it.what.isNotBlank() }
        val top = recent.groupBy { it.what.trim().lowercase() }.maxByOrNull { it.value.size } ?: return null
        if (top.value.size < 2) return null
        val v = top.value.first()
        return v.what.trim() to v.drinkOf
    }

    /**
     * Rings for a page: cups of the usual pile up around the spot, the others are scattered
     * around it. Positions come from the visit itself, so a page always looks the same.
     */
    fun rings(visits: List<Visit>, usual: Drink?): List<Ring> = visits.map { v ->
        val rnd = Random(v.id * 7_919L + v.at / 60_000)
        val atSpot = usual != null && v.drinkOf == usual && rnd.nextFloat() < 0.85f
        val (x, y) = if (atSpot) {
            val a = rnd.nextFloat() * 6.283f
            val d = rnd.nextFloat() * 0.09f
            SPOT_X + cos(a) * d to SPOT_Y + sin(a) * d * 0.8f
        } else {
            var px: Float
            var py: Float
            var tries = 0
            do {
                px = 0.14f + rnd.nextFloat() * 0.72f
                py = 0.12f + rnd.nextFloat() * 0.80f
                tries++
            } while (hypot(px - SPOT_X, (py - SPOT_Y) * 1.2f) < 0.2f && tries < 12)
            px to py
        }
        Ring(x, y, 0.085f + rnd.nextFloat() * 0.03f, v.drinkOf, 0.55f + rnd.nextFloat() * 0.45f, rnd.nextFloat() < 0.4f)
    }

    fun year(at: Long, zone: ZoneId = ZoneId.systemDefault()): Int = Instant.ofEpochMilli(at).atZone(zone).year

    fun year(year: Int, cafes: List<Cafe>, visits: List<Visit>, zone: ZoneId = ZoneId.systemDefault()): Year {
        val inYear = visits.filter { year(it.at, zone) == year }
        val byCafe = cafes.associateBy { it.id }
        val first = visits.groupBy { it.cafeId }.mapValues { (_, l) -> l.minOf { it.at } }
        val loyal = inYear.groupBy { it.cafeId }.maxByOrNull { it.value.size }?.let { (id, l) -> byCafe[id]?.let { it to l.size } }
        val company = inYear.flatMap { v -> v.with.split(',').map { it.trim() }.filter { it.isNotEmpty() } }
            .groupingBy { it }.eachCount().maxByOrNull { it.value }?.toPair()
        return Year(
            year = year,
            visits = inYear.size,
            cafes = inYear.map { it.cafeId }.distinct().size,
            found = first.count { (id, at) -> year(at, zone) == year && byCafe[id] != null },
            windowMin = inYear.sumOf { it.windowMin },
            spent = inYear.sumOf { it.price ?: 0 },
            loyal = loyal,
            drink = inYear.groupBy { it.drinkOf }.maxByOrNull { it.value.size }?.key,
            company = company,
            closed = cafes.filter { it.closed && year(it.closedAt, zone) == year },
        )
    }

    /**
     * The personal flat white index: what the most ordered drink cost on average, year by year.
     * Only years with a price written down count.
     */
    fun priceIndex(visits: List<Visit>, zone: ZoneId = ZoneId.systemDefault()): Pair<Drink, List<Pair<Int, Int>>>? {
        val priced = visits.filter { (it.price ?: 0) > 0 }
        val drink = priced.groupBy { it.drinkOf }.maxByOrNull { it.value.size }?.key ?: return null
        val years = priced.filter { it.drinkOf == drink }.groupBy { year(it.at, zone) }
            .map { (y, l) -> y to l.sumOf { it.price ?: 0 } / l.size }.sortedBy { it.first }
        return if (years.isEmpty()) null else drink to years
    }
}
