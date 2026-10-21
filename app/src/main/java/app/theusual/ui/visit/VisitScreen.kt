package app.theusual.ui.visit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.container
import app.theusual.data.Cafe
import app.theusual.data.Drink
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.T
import app.theusual.ui.art.ByTheWindow
import app.theusual.ui.art.CupFromAbove
import app.theusual.ui.art.WindowScene
import app.theusual.ui.components.Chip
import app.theusual.ui.components.Field
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.components.SoftButton
import app.theusual.ui.components.TextLink
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType
import kotlinx.coroutines.launch

private enum class Stage { ASK, CHOOSE, DONE }

/**
 * Walking in: the window from inside, and the barista's question, "The usual?" One tap writes the
 * usual down; "something else today" opens the drinks. Then the cup on the table, a line about
 * the day if you like, and the time at the window.
 */
@Composable
fun VisitScreen(cafeId: Long, h: Home, go: Go) {
    val cafe = h.cafe(cafeId) ?: return
    val usual = h.usual(cafe)
    var stage by rememberSaveable { mutableStateOf(if (usual != null) Stage.ASK else Stage.CHOOSE) }
    var visitId by rememberSaveable { mutableLongStateOf(0L) }
    var drink by rememberSaveable { mutableStateOf(usual?.second ?: h.last(cafeId)?.drinkOf ?: Drink.FLAT_WHITE) }
    val c = ctx()
    var what by rememberSaveable { mutableStateOf(usual?.first ?: "") }
    val scope = rememberCoroutineScope()

    fun write(d: Drink, w: String) {
        scope.launch {
            visitId = c.container.diary.visit(cafeId, d, w)
            drink = d
            stage = Stage.DONE
        }
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
            Box(Modifier.fillMaxWidth().height(300.dp)) {
                WindowScene(Modifier.fillMaxSize(), lights = true, name = "", crop = ByTheWindow)
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(90.dp).background(Brush.verticalGradient(listOf(Color.Transparent, U.c.bg))))
                IconButton(onClick = go::back, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(4.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink)
                }
            }
            Text(
                "${cafe.name.uppercase()} · " + stringResource(R.string.visit_n, h.count(cafeId) + if (stage == Stage.DONE) 0 else 1),
                fontFamily = Mono, fontSize = 12.sp, letterSpacing = 1.sp, color = U.c.muted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            AnimatedContent(stage, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }, label = "stage") { s ->
                when (s) {
                    Stage.ASK -> Ask(cafe, usual?.first.orEmpty(), { write(usual?.second ?: Drink.OTHER, usual?.first.orEmpty()) }, { stage = Stage.CHOOSE })
                    Stage.CHOOSE -> Choose(drink, what, { d -> if (what.isBlank() || Drink.entries.any { T.drink(c, it) == what }) what = T.drink(c, d); drink = d }, { what = it }) { write(drink, what) }
                    Stage.DONE -> Done(visitId, drink, h, go)
                }
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(24.dp))
        }
    }
}

@Composable
private fun Ask(cafe: Cafe, usual: String, yes: () -> Unit, other: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.as_usual_q), fontFamily = Hand, fontSize = 58.sp, color = U.c.amber, modifier = Modifier.rotate(-2f))
        Text(usual, fontFamily = Hand, fontSize = 30.sp, lineHeight = 32.sp, color = U.c.ink, textAlign = TextAlign.Center)
        if (cafe.spot.isNotBlank()) Text(cafe.spot, fontFamily = Hand, fontSize = 23.sp, color = U.c.muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(26.dp))
        PrimaryButton(stringResource(R.string.yes_usual), yes, Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        SoftButton(stringResource(R.string.something_else), other, Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Choose(drink: Drink, what: String, pick: (Drink) -> Unit, type: (String) -> Unit, write: () -> Unit) {
    val c = ctx()
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(stringResource(R.string.whats_today), fontFamily = Hand, fontSize = 40.sp, color = U.c.amber, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Drink.entries.forEach { d -> Chip(T.drink(c, d), d == drink) { pick(d) } }
        }
        Spacer(Modifier.height(10.dp))
        Field(what, type, stringResource(R.string.what_exactly))
        Spacer(Modifier.height(18.dp))
        PrimaryButton(stringResource(R.string.write_it_down), write, Modifier.fillMaxWidth(), enabled = what.isNotBlank())
    }
}

/** The cup is on the table and its ring is on the page. A line about the day, if you like. */
@Composable
private fun Done(visitId: Long, drink: Drink, h: Home, go: Go) {
    val c = ctx()
    var with by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    fun save() {
        val p = price.filter { it.isDigit() }.toIntOrNull()
        if (with.isNotBlank() || note.isNotBlank() || p != null) c.container.write {
            diary.editVisit(visitId) { it.copy(with = with.trim(), note = note.trim(), price = p ?: it.price) }
        }
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CupFromAbove(drink, Modifier.size(190.dp))
        Text(stringResource(R.string.ring_written), fontFamily = Hand, fontSize = 26.sp, color = U.c.ink, textAlign = TextAlign.Center)
        // Right under the news, where a slip is noticed at once, not below the fold.
        TextLink(stringResource(R.string.undo_cup), { c.container.write { diary.forgetVisit(visitId) }; go.back() })
        Spacer(Modifier.height(6.dp))
        Field(with, { with = it }, stringResource(R.string.with_whom))
        Spacer(Modifier.height(8.dp))
        Field(note, { note = it }, stringResource(R.string.a_line), singleLine = false)
        if (h.settings.currency.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                price, { price = it.filter { ch -> ch.isDigit() }.take(6) },
                label = { Text(stringResource(R.string.price_in, h.settings.currency)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = U.RMd,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = U.c.amber, unfocusedBorderColor = U.c.line, focusedLabelColor = U.c.amber, unfocusedLabelColor = U.c.faint, cursorColor = U.c.amber, focusedContainerColor = U.c.surface, unfocusedContainerColor = U.c.surface),
            )
        }
        Spacer(Modifier.height(18.dp))
        SoftButton(stringResource(R.string.by_the_window_btn), { save(); go.window(visitId) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.WbTwilight)
        Spacer(Modifier.height(10.dp))
        PrimaryButton(stringResource(R.string.done), { save(); go.back() }, Modifier.fillMaxWidth())
    }
}
