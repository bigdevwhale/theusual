package app.theusual.ui.cafe

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.container
import app.theusual.data.Cafe
import app.theusual.data.Visit
import app.theusual.domain.Rules
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.Standing
import app.theusual.ui.T
import app.theusual.ui.art.Dotted
import app.theusual.ui.art.InsideDark
import app.theusual.ui.art.Rings
import app.theusual.ui.art.RoundStamp
import app.theusual.ui.art.Stamp
import app.theusual.ui.art.Tape
import app.theusual.ui.art.WindowScene
import app.theusual.ui.art.lamplight
import app.theusual.ui.art.laid
import app.theusual.ui.art.paper
import app.theusual.ui.art.wood
import app.theusual.ui.components.Field
import app.theusual.ui.components.Polaroid
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.components.SoftButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType

/**
 * A café's spread in the notebook, lying on the table under the lamp. The left page: the front,
 * taped in; "the usual"; the stamps. The right page: the rings, one per cup, piling up where the
 * cup always lands, a few lines in pencil beside them; then every cup, receipt style.
 */
@Composable
fun CafeScreen(id: Long, h: Home, go: Go) {
    val cafe = h.cafe(id)
    LaunchedEffect(cafe == null) { if (cafe == null && h.cafes.isNotEmpty()) go.back() }
    cafe ?: return
    val c = ctx()
    val visits = h.visits(id)
    var menu by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    var forgetting by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().wood().lamplight()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
            item {
                Row(Modifier.windowInsetsPadding(WindowInsets.statusBars).fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = go::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink) }
                    Spacer(Modifier.weight(1f))
                    if (!cafe.closed) IconButton(onClick = { go.share(id) }) { Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.send_packet), tint = U.c.ink) }
                    IconButton(onClick = { go.edit(id) }) { Icon(Icons.Rounded.Edit, stringResource(R.string.edit), tint = U.c.ink) }
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, stringResource(R.string.more), tint = U.c.ink) }
                        DropdownMenu(menu, { menu = false }) {
                            if (cafe.closed) DropdownMenuItem({ Text(stringResource(R.string.reopened)) }, { menu = false; c.container.write { diary.reopen(id) } })
                            else DropdownMenuItem({ Text(stringResource(R.string.it_closed)) }, { menu = false; closing = true })
                            DropdownMenuItem({ Text(stringResource(R.string.tear_out)) }, { menu = false; forgetting = true })
                        }
                    }
                }
            }
            if (cafe.closed) item { Memorial(cafe, h) }
            item { LeftPage(cafe, h, visits, go) }
            if (visits.isNotEmpty()) {
                item { RightPage(cafe, h, visits) }
                item { Spacer(Modifier.height(18.dp)) }
                item { ReceiptHead(visits.size) }
                items(visits, key = { it.id }) { v -> ReceiptLine(v, h) }
                item { ReceiptFoot() }
            }
        }
        if (!cafe.closed) Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE61D1712))))
                .windowInsetsPadding(WindowInsets.navigationBars).padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 14.dp),
        ) {
            PrimaryButton(stringResource(if (h.standing(cafe) == Standing.WANT) R.string.first_visit else R.string.im_here), { go.visit(id) }, Modifier.weight(1f), icon = Icons.Rounded.LocalCafe)
        }
    }

    if (closing) {
        var farewell by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { closing = false },
            title = { Text(stringResource(R.string.closed_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.closed_text), style = UType.Muted)
                    Spacer(Modifier.height(12.dp))
                    Field(farewell, { farewell = it }, stringResource(R.string.closed_farewell), singleLine = false, minLines = 2)
                }
            },
            confirmButton = { androidx.compose.material3.TextButton({ closing = false; c.container.write { diary.close(id, farewell) } }) { Text(stringResource(R.string.closed_yes)) } },
            dismissButton = { androidx.compose.material3.TextButton({ closing = false }) { Text(stringResource(R.string.cancel)) } },
            containerColor = U.c.surface,
        )
    }
    if (forgetting) {
        AlertDialog(
            onDismissRequest = { forgetting = false },
            title = { Text(stringResource(R.string.tear_out_title)) },
            text = { Text(stringResource(R.string.tear_out_text), style = UType.Muted) },
            confirmButton = { androidx.compose.material3.TextButton({ forgetting = false; c.container.write { diary.forget(id) }; go.board() }) { Text(stringResource(R.string.tear_out)) } },
            dismissButton = { androidx.compose.material3.TextButton({ forgetting = false }) { Text(stringResource(R.string.cancel)) } },
            containerColor = U.c.surface,
        )
    }
}

/** The window with the lights out, the chairs up, and a last line under it. */
@Composable
private fun Memorial(cafe: Cafe, h: Home) {
    val c = ctx()
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        WindowScene(Modifier.fillMaxWidth().aspectRatio(1.45f).shadow(8.dp, RoundedCornerShape(6.dp)), lights = false, name = cafe.name, closedSign = stringResource(R.string.closed_sign), crop = InsideDark)
        Spacer(Modifier.height(14.dp))
        val first = h.visits(cafe.id).lastOrNull()?.at ?: cafe.addedAt
        Text("${Rules.year(first)} — ${Rules.year(cafe.closedAt)}", fontFamily = Hand, fontSize = 30.sp, color = U.c.ink)
        val window = h.window(cafe.id)
        Text(
            T.plural(c, R.plurals.visits, h.count(cafe.id)) + if (window > 0) " · " + stringResource(R.string.by_window, T.window(c, window)) else "",
            fontFamily = Mono, fontSize = 12.sp, color = U.c.muted,
        )
        if (cafe.farewell.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(cafe.farewell, fontFamily = Hand, fontSize = 26.sp, lineHeight = 28.sp, color = U.c.ink, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeftPage(cafe: Cafe, h: Home, visits: List<Visit>, go: Go) {
    val c = ctx()
    val standing = h.standing(cafe)
    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp).rotate(-.6f).laid().paper().padding(horizontal = 18.dp, vertical = 20.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Polaroid(
                cafe.sketch, cafe.palette, cafe.name,
                if (cafe.sketch != null) stringResource(R.string.sketch_from_photo, T.short(cafe.addedAt)) else stringResource(R.string.sketch_from_memory),
                Modifier.fillMaxWidth(.78f).padding(top = 10.dp),
            )
            if (standing == Standing.REGULAR) Column(Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundStamp(stringResource(R.string.stamp_cafe), stringResource(R.string.stamp_yours))
                h.regularSince(cafe.id)?.let { Text(stringResource(R.string.yours_since, T.short(it)), fontFamily = Hand, fontSize = 18.sp, color = U.c.teal, modifier = Modifier.rotate(-4f)) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(cafe.name.uppercase(), fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 1.5.sp, color = U.c.pen)
        val place = listOf(cafe.address, cafe.city).filter { it.isNotBlank() }.joinToString(" · ")
        if (place.isNotEmpty()) Text(place, style = UType.Typed.copy(color = U.c.penSoft))
        if (cafe.onMap) Text(
            stringResource(R.string.on_the_map), fontFamily = Hand, fontSize = 21.sp, color = U.c.red,
            modifier = Modifier.padding(top = 2.dp).clip(U.RSm).clickable { go.map(cafe.id) },
        )
        Spacer(Modifier.height(8.dp))
        Dotted()
        if (cafe.fromNote.isNotBlank() || (standing == Standing.WANT && cafe.from.isNotBlank())) {
            Spacer(Modifier.height(14.dp))
            Box {
                Column(Modifier.rotate(1.5f).shadow(3.dp).background(U.c.sticky).padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth()) {
                    if (cafe.fromNote.isNotBlank()) Text(cafe.fromNote, fontFamily = Hand, fontSize = 23.sp, lineHeight = 25.sp, color = U.c.pen)
                    if (cafe.from.isNotBlank()) Text("— " + cafe.from, fontFamily = Hand, fontSize = 21.sp, color = U.c.penSoft, modifier = Modifier.align(Alignment.End))
                }
                Tape(Modifier.align(Alignment.TopCenter).offset(y = (-9).dp), pink = true, w = 64.dp, h = 18.dp, angle = -4f)
            }
        }
        val usual = h.usual(cafe)
        if (standing != Standing.WANT || usual != null || cafe.spot.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.as_usual_q), fontFamily = Hand, fontSize = 40.sp, color = U.c.red, modifier = Modifier.rotate(-1f))
            val lines = buildList {
                usual?.let { add(it.first) }
                if (cafe.spot.isNotBlank()) add(stringResource(R.string.line_spot, cafe.spot))
                if (cafe.barista.isNotBlank()) add(stringResource(R.string.line_barista, cafe.barista))
            }
            if (lines.isEmpty()) Text(stringResource(R.string.nothing_usual), fontFamily = Hand, fontSize = 21.sp, lineHeight = 23.sp, color = U.c.penSoft)
            lines.forEach { Text("→ $it", fontFamily = Hand, fontSize = 24.sp, lineHeight = 27.sp, color = U.c.pen, modifier = Modifier.padding(start = 10.dp)) }
        }
        if (cafe.tagsOf.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cafe.tagsOf.sortedBy { it.ordinal }.forEachIndexed { i, t -> Stamp(T.tag(c, t), angle = listOf(-4f, 3f, -2f, 2f)[i % 4]) }
            }
        }
        if (cafe.story.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(cafe.story, fontFamily = Hand, fontSize = 22.sp, lineHeight = 25.sp, color = U.c.pen)
        }
        if (visits.isEmpty() && standing != Standing.WANT) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.no_rings_yet), fontFamily = Hand, fontSize = 20.sp, color = U.c.penSoft)
        }
    }
}

@Composable
private fun RightPage(cafe: Cafe, h: Home, visits: List<Visit>) {
    val c = ctx()
    val usual = h.usual(cafe)
    val rings = remember(visits, usual) { Rules.rings(visits.reversed(), usual?.second) }
    val notes = remember(visits, rings) {
        val byId = visits.reversed().zip(rings)
        byId.filter { (v, _) -> v.note.isNotBlank() || v.with.isNotBlank() }.takeLast(5).map { (v, r) ->
            r to (v.note.ifBlank { c.getString(R.string.with_who, v.with) })
        }
    }
    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp).rotate(.5f).laid().paper().padding(horizontal = 18.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(T.plural(c, R.plurals.visits, visits.size), fontFamily = Hand, fontSize = 32.sp, color = U.c.pen, modifier = Modifier.rotate(-2f))
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.ring_is_cup), fontFamily = Mono, fontSize = 11.sp, color = U.c.penSoft, modifier = Modifier.padding(bottom = 6.dp))
        }
        Box(Modifier.fillMaxWidth().aspectRatio(.95f)) {
            Rings(rings, notes, Modifier.fillMaxSize())
            if (usual != null && visits.size >= 3) Text(
                stringResource(R.string.my_spot), fontFamily = Hand, fontSize = 21.sp, color = U.c.red,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 4.dp).rotate(-6f),
            )
        }
        val window = h.window(cafe.id)
        if (window > 0) Text(stringResource(R.string.by_window, T.window(c, window)), fontFamily = Hand, fontSize = 22.sp, color = U.c.teal, modifier = Modifier.align(Alignment.End).rotate(-3f))
    }
}

@Composable
private fun ReceiptHead(n: Int) {
    Column(Modifier.padding(horizontal = 40.dp).fillMaxWidth().background(Color(0xFFFDFAF2)).padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
        Text(stringResource(R.string.all_cups).uppercase(), fontFamily = Mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp, color = U.c.pen, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Dotted()
    }
}

@Composable
private fun ReceiptLine(v: Visit, h: Home) {
    val c = ctx()
    Column(Modifier.padding(horizontal = 40.dp).fillMaxWidth().background(Color(0xFFFDFAF2)).padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row {
            Text(T.stamp(v.at), fontFamily = Mono, fontSize = 12.sp, color = U.c.penSoft, modifier = Modifier.width(76.dp))
            Text(v.what.ifBlank { T.drink(c, v.drinkOf) }, fontFamily = Mono, fontSize = 12.sp, color = U.c.pen, modifier = Modifier.weight(1f), maxLines = 1)
            if (v.price != null && v.price > 0) Text(T.price(v.price, h.settings.currency), fontFamily = Mono, fontSize = 12.sp, color = U.c.pen)
        }
        val extra = listOfNotNull(
            v.with.takeIf { it.isNotBlank() }?.let { c.getString(R.string.with_who, it) },
            v.note.takeIf { it.isNotBlank() },
            v.windowMin.takeIf { it > 0 }?.let { c.getString(R.string.by_window, T.window(c, it)) },
        )
        if (extra.isNotEmpty()) Text(extra.joinToString(" · "), fontFamily = Hand, fontSize = 19.sp, lineHeight = 20.sp, color = U.c.penSoft, modifier = Modifier.padding(start = 76.dp))
    }
}

@Composable
private fun ReceiptFoot() {
    Column(Modifier.padding(horizontal = 40.dp).fillMaxWidth().background(Color(0xFFFDFAF2)).padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        Spacer(Modifier.height(6.dp))
        Dotted()
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.thanks_receipt), fontFamily = Mono, fontSize = 11.sp, color = U.c.penSoft, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
