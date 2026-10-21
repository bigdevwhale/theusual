package app.theusual.ui.year

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.domain.Rules
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.T
import app.theusual.ui.art.Dotted
import app.theusual.ui.art.Packet
import app.theusual.ui.art.lamplight
import app.theusual.ui.art.laid
import app.theusual.ui.art.wood
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.U
import java.time.LocalDate

/**
 * The year in cups, printed like a long till receipt: cups, cafés, the new ones, the hours at the
 * window, the most faithful place, the drink of the year, who came along most, the ones that
 * closed, and the personal price index of the usual drink.
 */
@Composable
fun YearScreen(h: Home, go: Go) {
    val c = ctx()
    val years = remember(h.visits) { (h.visits.map { Rules.year(it.at) } + LocalDate.now().year).distinct().sorted() }
    var year by rememberSaveable { mutableIntStateOf(years.last()) }
    val y = remember(year, h.cafes, h.visits) { Rules.year(year, h.cafes, h.visits) }
    val index = remember(h.visits) { Rules.priceIndex(h.visits) }
    val sugar = stringResource(R.string.sugar)

    Box(Modifier.fillMaxSize().wood().lamplight()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = go::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink) }
                Spacer(Modifier.weight(1f))
                val i = years.indexOf(year)
                IconButton(onClick = { year = years[i - 1] }, enabled = i > 0) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null, tint = if (i > 0) U.c.ink else U.c.faint) }
                Text("$year", fontFamily = Mono, fontSize = 18.sp, color = U.c.ink)
                IconButton(onClick = { year = years[i + 1] }, enabled = i < years.size - 1) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = if (i < years.size - 1) U.c.ink else U.c.faint) }
            }
            Column(Modifier.padding(horizontal = 36.dp, vertical = 8.dp).rotate(-.8f).laid().background(Color(0xFFFDFAF2)).padding(horizontal = 18.dp, vertical = 20.dp)) {
                Text(stringResource(R.string.app_name), fontFamily = Hand, fontSize = 34.sp, color = U.c.red, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(stringResource(R.string.year_receipt, year).uppercase(), fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp, color = U.c.pen, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(10.dp))
                Dotted()
                Line(stringResource(R.string.y_cups), "${y.visits}")
                Line(stringResource(R.string.y_cafes), "${y.cafes}")
                Line(stringResource(R.string.y_found), "${y.found}")
                Line(stringResource(R.string.y_window), T.window(c, y.windowMin))
                if (h.settings.currency.isNotBlank() && y.spent > 0) Line(stringResource(R.string.y_spent), T.price(y.spent, h.settings.currency))
                Dotted(Modifier.padding(vertical = 6.dp))
                y.drink?.let { Line(stringResource(R.string.y_drink), T.drink(c, it)) }
                y.company?.let { (who, n) -> Line(stringResource(R.string.y_company), "$who ×$n") }
                y.loyal?.let { (cafe, n) ->
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.y_loyal).uppercase(), fontFamily = Mono, fontSize = 12.sp, letterSpacing = 1.sp, color = U.c.penSoft)
                    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Packet(h.look(cafe, sugar), Modifier.width(64.dp).rotate(-4f))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(cafe.name, fontFamily = Hand, fontSize = 28.sp, color = U.c.pen, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(T.plural(c, R.plurals.cups, n), fontFamily = Mono, fontSize = 12.sp, color = U.c.penSoft)
                        }
                    }
                }
                if (y.closed.isNotEmpty()) {
                    Dotted(Modifier.padding(vertical = 10.dp))
                    Text(stringResource(R.string.y_closed).uppercase(), fontFamily = Mono, fontSize = 12.sp, letterSpacing = 1.sp, color = U.c.penSoft)
                    y.closed.forEach { Text("† " + it.name, fontFamily = Hand, fontSize = 24.sp, color = U.c.pen) }
                }
                index?.let { (drink, rows) ->
                    if (rows.size >= 1) {
                        Dotted(Modifier.padding(vertical = 10.dp))
                        Text(stringResource(R.string.y_index, T.drink(c, drink)).uppercase(), fontFamily = Mono, fontSize = 12.sp, letterSpacing = 1.sp, color = U.c.penSoft)
                        val top = rows.maxOf { it.second }.coerceAtLeast(1)
                        rows.forEach { (yr, avg) ->
                            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$yr", fontFamily = Mono, fontSize = 12.sp, color = U.c.pen, modifier = Modifier.width(44.dp))
                                Box(Modifier.weight(1f).height(10.dp)) {
                                    Box(Modifier.fillMaxWidth(avg.toFloat() / top).height(10.dp).background(Color(0xFFA5774B)))
                                }
                                Text(T.price(avg, h.settings.currency), fontFamily = Mono, fontSize = 12.sp, color = U.c.pen, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                Dotted(Modifier.padding(vertical = 10.dp))
                Text(stringResource(R.string.thanks_receipt), fontFamily = Mono, fontSize = 11.sp, color = U.c.penSoft, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(28.dp))
        }
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(label, fontFamily = Mono, fontSize = 13.sp, color = U.c.pen, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = U.c.pen)
    }
}
