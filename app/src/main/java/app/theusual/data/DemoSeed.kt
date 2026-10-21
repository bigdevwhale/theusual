package app.theusual.data

import android.content.Context
import app.theusual.AppContainer
import app.theusual.domain.Rules
import app.theusual.ru
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * A lived-in diary for the demo build and the screenshots: a café that became yours, a few
 * regular haunts, two from trips, one a friend sent and one that closed, with a few years of cups.
 */
object DemoSeed {
    private class Spot(
        val en: String, val ru: String, val subEn: String, val subRu: String,
        val addressEn: String, val addressRu: String, val cityEn: String, val cityRu: String,
        val pattern: Pattern, val palette: Int, val tags: Set<Tag> = emptySet(),
        val usualEn: String = "", val usualRu: String = "", val usualDrink: Drink? = null,
        val spotEn: String = "", val spotRu: String = "", val baristaEn: String = "", val baristaRu: String = "",
        val storyEn: String = "", val storyRu: String = "",
    )

    private val spots = listOf(
        Spot("Grain & Co", "Зерно и Ко", "by the park", "у парка", "12 Ryleeva St", "ул. Рылеева, 12", "St Petersburg", "Петербург",
            Pattern.STRIPES, 0, setOf(Tag.SOCKETS, Tag.QUIET, Tag.LAPTOP), "flat white on oat, no sugar", "флэт уайт на овсяном, без сахара", Drink.FLAT_WHITE,
            "by the window, left of the fig tree", "у окна, слева от фикуса", "Lyosha (a cat tattooed on his hand)", "Лёша (кот на руке)",
            "Found it hiding from the rain in February. Stayed for the window.", "Нашёл, когда прятался от дождя в феврале. Остался ради окна."),
        Spot("Penka", "Пенка", "by the bridge", "у моста", "3 Fontanka Emb", "наб. Фонтанки, 3", "St Petersburg", "Петербург",
            Pattern.DOTS, 1, setOf(Tag.TERRACE, Tag.DOGS), spotEn = "the stools at the bar", spotRu = "высокие стулья у стойки"),
        Spot("Owl", "Сова", "till 2 am", "до 2 ночи", "40 Liteyny Ave", "Литейный, 40", "St Petersburg", "Петербург",
            Pattern.CHEVRON, 2, setOf(Tag.LATE, Tag.LAPTOP, Tag.SOCKETS, Tag.WIFI)),
        Spot("Ray", "Луч", "brunches", "бранчи", "7 Bolshaya Konyushennaya", "Б. Конюшенная, 7", "St Petersburg", "Петербург",
            Pattern.SUN, 3, setOf(Tag.BRUNCH, Tag.TERRACE)),
        Spot("Pier", "Причал", "coffee", "кофе", "Rua da Boavista 22", "Rua da Boavista, 22", "Lisbon", "Лиссабон", Pattern.BAND, 4),
        Spot("Ficus", "Фикус", "on the corner", "на углу", "Abashidze St 9", "ул. Абашидзе, 9", "Tbilisi", "Тбилиси", Pattern.STRIPES, 5, setOf(Tag.DOGS)),
        Spot("Cinnamon & Vanilla", "Корица и ваниль", "and vanilla", "и ваниль", "18 Zhukovskogo St", "ул. Жуковского, 18", "St Petersburg", "Петербург",
            Pattern.DOTS, 6, setOf(Tag.QUIET), "raf with cinnamon", "раф с корицей", Drink.RAF, "the armchair by the radiator", "кресло у батареи"),
        Spot("The Cellar", "Подвал", "", "", "5 Rubinshteyna St", "ул. Рубинштейна, 5", "St Petersburg", "Петербург", Pattern.CHEVRON, 7),
    )

    suspend fun seedIfEmpty(c: Context, container: AppContainer) {
        val dao = container.db.dao()
        if (dao.cafes().isNotEmpty()) return
        val ru = c.ru
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        fun ago(days: Double) = now - (days * Rules.DAY).toLong()
        // Morning and afternoon cups, not three in the morning.
        fun at(days: Double, hour: Int) = LocalDate.now(zone).minusDays(days.toLong()).atTime(hour, (days * 37).toInt() % 60).atZone(zone).toInstant().toEpochMilli()

        val ids = spots.mapIndexed { i, s ->
            dao.insert(
                Cafe(
                    name = if (ru) s.ru else s.en, sub = if (ru) s.subRu else s.subEn, address = if (ru) s.addressRu else s.addressEn, city = if (ru) s.cityRu else s.cityEn,
                    pattern = s.pattern.name, palette = s.palette, tags = Tag.join(s.tags), usual = if (ru) s.usualRu else s.usualEn, usualDrink = s.usualDrink?.name.orEmpty(),
                    spot = if (ru) s.spotRu else s.spotEn, barista = if (ru) s.baristaRu else s.baristaEn, story = if (ru) s.storyRu else s.storyEn,
                    addedAt = ago(400.0 - i * 30), seed = 1000L + i * 7L,
                    from = if (s.en == "The Cellar") (if (ru) "Аня" else "Anya") else "",
                    fromNote = if (s.en == "The Cellar") (if (ru) "Возьми фильтр на эфиопии и садись на подоконник. Спроси про пластинки :)" else "Get the Ethiopian filter and sit on the windowsill. Ask about the records :)") else "",
                ),
            )
        }
        val r = Random(5)
        val cur = if (ru) "₽" else "€"
        fun price(drink: Drink, at: Long): Int {
            val year = Rules.year(at, zone)
            val base = if (ru) mapOf(Drink.ESPRESSO to 150, Drink.FLAT_WHITE to 250, Drink.RAF to 280, Drink.CAPPUCCINO to 230, Drink.LATTE to 240, Drink.FILTER to 220)
            else mapOf(Drink.ESPRESSO to 2, Drink.FLAT_WHITE to 4, Drink.RAF to 5, Drink.CAPPUCCINO to 3, Drink.LATTE to 4, Drink.FILTER to 3)
            val b = base[drink] ?: if (ru) 240 else 4
            return if (ru) b + (year - 2024) * 30 else b + if (year >= 2026) 1 else 0
        }
        suspend fun cup(cafe: Int, days: Double, drink: Drink, what: String, with: String = "", note: String = "", window: Int = 0, hour: Int = 9 + r.nextInt(8)) {
            val t = at(days, hour)
            dao.insert(Visit(cafeId = ids[cafe], at = t, drink = drink.name, what = what, price = price(drink, t), with = with, note = note, windowMin = window))
        }
        val fw = if (ru) "флэт уайт на овсяном, без сахара" else "flat white on oat, no sugar"

        // Grain & Co: twenty-seven cups since February, the usual piling up at the spot.
        val notesRu = mapOf(0 to "первый раз, дождь", 4 to "дождь, сидел час", 9 to "Лёша нарисовал кота", 15 to "новая Эфиопия — огонь", 21 to "день рождения ♡")
        val notesEn = mapOf(0 to "first time, rain", 4 to "rain, sat for an hour", 9 to "Lyosha drew a cat", 15 to "new Ethiopian, wow", 21 to "birthday ♡")
        for (i in 0 until 27) {
            val days = 250.0 - i * 9.2
            val other = i == 3 || i == 15 || i == 21
            val drink = when (i) { 3 -> Drink.ESPRESSO; 15 -> Drink.FILTER; 21 -> Drink.RAF; else -> Drink.FLAT_WHITE }
            val what = if (other) when (drink) { Drink.ESPRESSO -> if (ru) "эспрессо" else "espresso"; Drink.FILTER -> if (ru) "фильтр, Эфиопия" else "filter, Ethiopia"; else -> if (ru) "раф" else "raf" } else fw
            val with = if (i == 11) (if (ru) "Дима" else "Dima") else if (i == 21) (if (ru) "Аня" else "Anya") else ""
            val note = if (i == 11) (if (ru) "спорили три часа" else "argued for three hours") else (if (ru) notesRu[i] else notesEn[i]).orEmpty()
            cup(0, days, drink, what, with, note, window = if (i % 3 == 0) 30 + r.nextInt(70) else 0)
        }
        // Penka, Owl and Ray: now and then.
        listOf(200.0, 170.0, 140.0, 110.0, 80.0, 50.0, 30.0, 12.0).forEachIndexed { i, d ->
            cup(1, d, Drink.CAPPUCCINO, if (ru) "капучино" else "cappuccino", with = if (i == 2) (if (ru) "мама" else "Mum") else "", note = if (i == 5) (if (ru) "собака у соседнего столика" else "a dog at the next table") else "")
        }
        listOf(160.0, 120.0, 75.0, 40.0, 6.0).forEach { d -> cup(2, d, Drink.FILTER, if (ru) "фильтр" else "filter", hour = 22, window = 45) }
        listOf(90.0, 60.0, 20.0).forEach { d -> cup(3, d, Drink.LATTE, if (ru) "латте" else "latte", with = if (ru) "Дима" else "Dima", hour = 12) }
        // Trips.
        listOf(330.0, 329.0).forEach { d -> cup(4, d, Drink.ESPRESSO, "bica", note = if (d == 330.0) (if (ru) "у реки, ветер" else "by the river, windy") else "") }
        listOf(220.0, 219.0, 218.0).forEach { d -> cup(5, d, Drink.CAPPUCCINO, if (ru) "капучино" else "cappuccino") }
        // Cinnamon & Vanilla: years of rafs, then it closed in the summer.
        for (i in 0 until 40) {
            val days = 1150.0 - i * 26
            cup(6, days, Drink.RAF, if (ru) "раф с корицей" else "raf with cinnamon", window = if (i % 4 == 0) 60 else 0,
                note = if (i == 39) (if (ru) "последний раз, не знал" else "the last time, didn't know") else "")
        }
        val closed = dao.cafes().first { it.id == ids[6] }
        dao.update(closed.copy(closedAt = ago(110.0), farewell = if (ru) "Спасибо за тихие утра и за корицу на пенке." else "Thank you for the quiet mornings and the cinnamon on the foam."))

        container.settings.update { it.copy(onboarded = true, name = if (ru) "Саша" else "Alex", city = if (ru) "Петербург" else "St Petersburg", currency = cur) }
    }
}
