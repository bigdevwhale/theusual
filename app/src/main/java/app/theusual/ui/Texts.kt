package app.theusual.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import app.theusual.R
import app.theusual.data.Drink
import app.theusual.data.Tag
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Words and dates in the app's language. */
object T {
    private fun locale(c: Context): Locale = c.resources.configuration.locales[0]
    private fun zone() = ZoneId.systemDefault()

    fun plural(c: Context, id: Int, n: Int): String = c.resources.getQuantityString(id, n, n)

    /** "24 October", or with the year when it isn't this one. */
    fun date(c: Context, at: Long, today: LocalDate = LocalDate.now()): String {
        val d = Instant.ofEpochMilli(at).atZone(zone()).toLocalDate()
        return d.format(DateTimeFormatter.ofPattern(if (d.year == today.year) "d MMMM" else "d MMMM yyyy", locale(c)))
    }

    /** "12.03", for a note beside a ring. */
    fun short(at: Long): String = Instant.ofEpochMilli(at).atZone(zone()).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM"))

    /** "12.03.24", for a receipt. */
    fun stamp(at: Long): String = Instant.ofEpochMilli(at).atZone(zone()).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yy"))

    /** "today", "yesterday", "5 days ago", or the date for anything older than two weeks. */
    fun ago(c: Context, at: Long, today: LocalDate = LocalDate.now()): String {
        val d = Instant.ofEpochMilli(at).atZone(zone()).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(d, today).toInt()
        return when {
            days <= 0 -> c.getString(R.string.today)
            days == 1 -> c.getString(R.string.yesterday)
            days < 14 -> plural(c, R.plurals.days_ago, days)
            else -> date(c, at, today)
        }
    }

    /** "41 h" or "35 min". */
    fun window(c: Context, min: Int): String =
        if (min >= 60) c.getString(R.string.hours_only, min / 60) else c.getString(R.string.minutes_short, min)

    fun price(amount: Int, currency: String): String = if (currency.isBlank()) "$amount" else "$amount $currency"

    fun drink(c: Context, d: Drink): String = c.getString(
        when (d) {
            Drink.ESPRESSO -> R.string.d_espresso
            Drink.FILTER -> R.string.d_filter
            Drink.CAPPUCCINO -> R.string.d_cappuccino
            Drink.FLAT_WHITE -> R.string.d_flat_white
            Drink.LATTE -> R.string.d_latte
            Drink.RAF -> R.string.d_raf
            Drink.COCOA -> R.string.d_cocoa
            Drink.TEA -> R.string.d_tea
            Drink.OTHER -> R.string.d_other
        },
    )

    fun tag(c: Context, t: Tag): String = c.getString(
        when (t) {
            Tag.SOCKETS -> R.string.t_sockets
            Tag.QUIET -> R.string.t_quiet
            Tag.LAPTOP -> R.string.t_laptop
            Tag.WIFI -> R.string.t_wifi
            Tag.TERRACE -> R.string.t_terrace
            Tag.DOGS -> R.string.t_dogs
            Tag.BRUNCH -> R.string.t_brunch
            Tag.LATE -> R.string.t_late
        },
    )
}

object Language {
    const val SYSTEM = ""
    const val EN = "en"
    const val RU = "ru"

    fun current(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-').substringBefore(',')

    fun set(tag: String) {
        AppCompatDelegate.setApplicationLocales(if (tag == SYSTEM) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
    }
}

@Composable
fun ctx(): Context = LocalContext.current
