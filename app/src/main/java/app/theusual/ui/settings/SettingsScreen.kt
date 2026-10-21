package app.theusual.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.theusual.R
import app.theusual.container
import app.theusual.data.Settings
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.Language
import app.theusual.ui.components.Chip
import app.theusual.ui.components.Field
import app.theusual.ui.components.SectionTitle
import app.theusual.ui.components.Segmented
import app.theusual.ui.components.TopBar
import app.theusual.ui.components.UCard
import app.theusual.ui.ctx
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType

/** The currencies offered as chips; anything else is typed in. */
private val COMMON = listOf("₽", "€", "$", "£", "₸", "₴", "₾", "֏", "BYN", "zł", "Kč", "₺", "¥", "₹")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(h: Home, go: Go) {
    val c = ctx()
    val s = h.settings
    fun set(change: (Settings) -> Settings) = c.container.write { settings.update(change) }
    var name by rememberSaveable { mutableStateOf(s.name) }
    var city by rememberSaveable { mutableStateOf(s.city) }
    var currency by rememberSaveable { mutableStateOf(s.currency) }

    Column(Modifier.fillMaxSize().background(U.c.bg).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        TopBar(stringResource(R.string.settings), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            SectionTitle(stringResource(R.string.s_you))
            UCard {
                Text(stringResource(R.string.s_name_sub), style = UType.Muted, modifier = Modifier.padding(bottom = 10.dp))
                Field(name, { name = it; set { st -> st.copy(name = it.trim()) } }, stringResource(R.string.s_name))
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.s_city_sub), style = UType.Muted, modifier = Modifier.padding(bottom = 10.dp))
                Field(city, { city = it; set { st -> st.copy(city = it.trim()) } }, stringResource(R.string.s_city))
            }
            SectionTitle(stringResource(R.string.s_prices))
            UCard {
                Text(stringResource(R.string.s_prices_sub), style = UType.Muted, modifier = Modifier.padding(bottom = 10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf("") + COMMON).forEach { cur ->
                        Chip(cur.ifEmpty { stringResource(R.string.s_no_prices) }, s.currency == cur) {
                            currency = cur
                            set { st -> st.copy(currency = cur) }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Field(
                    if (currency in COMMON) "" else currency,
                    { currency = it.take(5); set { st -> st.copy(currency = it.trim().take(5)) } },
                    stringResource(R.string.s_currency_own),
                )
            }
            SectionTitle(stringResource(R.string.s_language))
            Segmented(listOf(Language.SYSTEM, Language.EN, Language.RU), Language.current(), {
                when (it) {
                    Language.EN -> "English"
                    Language.RU -> "Русский"
                    else -> stringResource(R.string.s_lang_system)
                }
            }, { Language.set(it) })
            SectionTitle(stringResource(R.string.s_about))
            Text(stringResource(R.string.s_about_text), style = UType.Muted)
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(28.dp))
        }
    }
}
