package app.theusual.data

import android.content.Context
import app.theusual.domain.PacketCard
import kotlin.random.Random

/** A café as typed into the form, before it is written down. */
data class CafeDraft(
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val sub: String = "",
    val pattern: Pattern = Pattern.STRIPES,
    val palette: Int = 0,
    val usual: String = "",
    val usualDrink: Drink? = null,
    val spot: String = "",
    val barista: String = "",
    val tags: Set<Tag> = emptySet(),
    val story: String = "",
    /** A fresh sketch waiting in the sketches folder, or the café's own. */
    val sketch: String? = null,
) {
    companion object {
        fun of(c: Cafe) = CafeDraft(c.name, c.address, c.city, c.sub, c.patternOf, c.palette, c.usual, c.usualDrinkOf, c.spot, c.barista, c.tagsOf, c.story, c.sketch)
    }
}

/** Writing in the diary: cafés, cups, the time at the window, packets from friends, closings. */
class Diary(private val context: Context, private val db: DiaryDatabase) {
    private val dao get() = db.dao()

    suspend fun add(d: CafeDraft, now: Long = System.currentTimeMillis()): Long = dao.insert(
        Cafe(
            name = d.name.trim(), address = d.address.trim(), city = d.city.trim(), sub = d.sub.trim(),
            pattern = d.pattern.name, palette = d.palette, usual = d.usual.trim(), usualDrink = d.usualDrink?.name.orEmpty(),
            spot = d.spot.trim(), barista = d.barista.trim(), tags = Tag.join(d.tags), story = d.story.trim(),
            sketch = d.sketch, addedAt = now, seed = Random.nextLong(),
        ),
    )

    suspend fun edit(id: Long, d: CafeDraft) {
        val c = dao.cafe(id) ?: return
        if (c.sketch != d.sketch) Sketches.delete(context, c.sketch)
        dao.update(
            c.copy(
                name = d.name.trim(), address = d.address.trim(), city = d.city.trim(), sub = d.sub.trim(),
                pattern = d.pattern.name, palette = d.palette, usual = d.usual.trim(), usualDrink = d.usualDrink?.name.orEmpty(),
                spot = d.spot.trim(), barista = d.barista.trim(), tags = Tag.join(d.tags), story = d.story.trim(), sketch = d.sketch,
            ),
        )
    }

    /** A cup: one more ring on the page. */
    suspend fun visit(cafeId: Long, drink: Drink, what: String, price: Int? = null, with: String = "", note: String = "", at: Long = System.currentTimeMillis()): Long =
        dao.insert(Visit(cafeId = cafeId, at = at, drink = drink.name, what = what.trim(), price = price, with = with.trim(), note = note.trim()))

    suspend fun editVisit(id: Long, change: (Visit) -> Visit) {
        val v = dao.visit(id) ?: return
        dao.update(change(v))
    }

    /** Time with the phone face down, added to the cup it was spent over. */
    suspend fun window(visitId: Long, minutes: Int) = editVisit(visitId) { it.copy(windowMin = it.windowMin + minutes.coerceAtLeast(0)) }

    suspend fun forgetVisit(id: Long) = dao.deleteVisit(id)

    suspend fun close(id: Long, farewell: String, at: Long = System.currentTimeMillis()) {
        val c = dao.cafe(id) ?: return
        dao.update(c.copy(closedAt = at, farewell = farewell.trim()))
    }

    /** Closed by mistake, or opened again. */
    suspend fun reopen(id: Long) {
        val c = dao.cafe(id) ?: return
        dao.update(c.copy(closedAt = 0, farewell = ""))
    }

    suspend fun forget(id: Long) {
        val c = dao.cafe(id) ?: return
        dao.forget(id)
        Sketches.delete(context, c.sketch)
    }

    /** A packet from a friend: onto the board, dashed, until the first visit. Same café twice is kept once. */
    suspend fun receive(p: PacketCard): Long {
        dao.cafes().firstOrNull { it.name.equals(p.name, ignoreCase = true) && it.address.equals(p.address, ignoreCase = true) }?.let { return it.id }
        return dao.insert(
            Cafe(
                name = p.name, address = p.address, city = p.city, sub = p.sub, pattern = p.pattern.name,
                palette = p.palette.coerceIn(0, PACKET_COLOURS - 1), from = p.from, fromNote = p.note, seed = Random.nextLong(),
            ),
        )
    }

    companion object {
        /** How many colourways a packet can be printed in (see the art). */
        const val PACKET_COLOURS = 10
    }
}
