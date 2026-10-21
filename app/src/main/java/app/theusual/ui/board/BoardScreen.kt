package app.theusual.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.data.Cafe
import app.theusual.domain.Rules
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.Standing
import app.theusual.ui.T
import app.theusual.ui.art.Packet
import app.theusual.ui.art.PacketLook
import app.theusual.ui.art.PacketState
import app.theusual.ui.art.StringLights
import app.theusual.ui.art.cork
import app.theusual.ui.art.lamplight
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType
import java.time.Instant
import java.time.ZoneId

/**
 * The board: a cork board in a wooden frame under string lights, a sugar packet pinned up for
 * every café, with a paper tag under each. Yours first, then the rest, then the ones a friend
 * sent (dashed), then the ones that closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(h: Home, go: Go) {
    val c = ctx()
    val sugar = stringResource(R.string.sugar)
    var picking by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(U.c.bg)) {
        Row(Modifier.windowInsetsPadding(WindowInsets.statusBars).fillMaxWidth().padding(start = 20.dp, end = 6.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = UType.gold(32f))
                val cups = h.visits.size
                if (cups > 0) Text(
                    T.plural(c, R.plurals.cups, cups) + " · " + stringResource(R.string.by_window, T.window(c, h.windowMin)),
                    fontFamily = Mono, fontSize = 12.sp, color = U.c.faint,
                )
                h.latest?.let { (v, cafe) ->
                    Text(
                        stringResource(R.string.last_cup, cafe.name, T.ago(c, v.at)),
                        fontFamily = Hand, fontSize = 21.sp, color = U.c.amber, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp).clip(U.RSm).clickable { go.cafe(cafe.id) },
                    )
                }
            }
            IconButton(onClick = { go.map() }) { Icon(Icons.Rounded.Map, stringResource(R.string.map_title), tint = U.c.muted) }
            IconButton(onClick = go::year) { Icon(Icons.Rounded.AutoStories, stringResource(R.string.year_title), tint = U.c.muted) }
            IconButton(onClick = go::settings) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = U.c.muted) }
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)
                .background(Color(0xFF3B2618)).padding(10.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)).cork().lamplight(),
        ) {
            if (h.cafes.isEmpty()) Empty(sugar, go)
            else LazyVerticalGrid(
                GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 58.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(h.board, key = { it.id }) { cafe -> Pinned(cafe, h, sugar) { go.cafe(cafe.id) } }
                item(span = { GridItemSpan(3) }) { Spacer(Modifier.height(8.dp)) }
            }
            StringLights(Modifier.align(Alignment.TopCenter))
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC1D1712))))
                    .windowInsetsPadding(WindowInsets.navigationBars).padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrimaryButton(stringResource(R.string.im_here), { if (h.open.isEmpty()) go.add() else picking = true }, Modifier.weight(1f), icon = Icons.Rounded.LocalCafe)
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier.size(56.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(U.c.paper).clickable { go.add() },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Add, stringResource(R.string.new_cafe), tint = U.c.pen) }
            }
        }
    }

    if (picking) {
        ModalBottomSheet(onDismissRequest = { picking = false }, containerColor = U.c.surface) {
            Text(stringResource(R.string.where_are_you), style = UType.Title, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            val list = h.open.sortedByDescending { h.last(it.id)?.at ?: it.addedAt }
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(list, key = { it.id }) { cafe ->
                    Row(
                        Modifier.fillMaxWidth().clickable { picking = false; go.visit(cafe.id) }.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Packet(h.look(cafe, sugar).copy(state = PacketState.PLAIN), Modifier.width(38.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cafe.name, style = UType.Strong, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val usual = h.usual(cafe)?.first
                            Text(usual ?: cafe.address.ifBlank { cafe.city }, style = UType.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().clickable { picking = false; go.add() }.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Add, null, tint = U.c.amber, modifier = Modifier.width(38.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(stringResource(R.string.somewhere_new), style = UType.Strong.copy(color = U.c.amber))
                    }
                }
            }
        }
    }
}

/** A packet pinned to the cork, a little crooked, with its paper tag under it. */
@Composable
private fun Pinned(cafe: Cafe, h: Home, sugar: String, onClick: () -> Unit) {
    val c = ctx()
    val angle = ((cafe.seed % 9).toInt() - 4).toFloat().let { if (it == 0f) 2f else it }
    val standing = h.standing(cafe)
    val label = when (standing) {
        Standing.REGULAR -> stringResource(R.string.tag_regular, h.count(cafe.id))
        Standing.VISITED -> T.plural(c, R.plurals.visits, h.count(cafe.id))
        Standing.WANT -> if (cafe.from.isNotBlank()) stringResource(R.string.tag_from, cafe.from) else stringResource(R.string.tag_want)
        Standing.CLOSED -> years(cafe, h)
    }
    val pins = listOf(Color(0xFFC4453A), Color(0xFF3D7470), Color(0xFFD39B3A))
    Column(Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopCenter) {
            Packet(h.look(cafe, sugar), Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp).rotate(angle))
            Box(
                Modifier.offset(y = 6.dp).size(13.dp).shadow(3.dp, CircleShape).clip(CircleShape)
                    .background(if (standing == Standing.CLOSED) Color(0xFF555555) else pins[(cafe.id % 3).toInt()]),
            )
        }
        Box(
            Modifier.padding(top = 4.dp).rotate(-angle / 2).shadow(2.dp).background(Color(0xFFF4E6C8)).padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(label, fontFamily = Hand, fontSize = 19.sp, color = U.c.pen, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

/** "2019–2026": from the first cup to the closing. */
private fun years(cafe: Cafe, h: Home): String {
    val zone = ZoneId.systemDefault()
    val first = h.visits(cafe.id).lastOrNull()?.at ?: cafe.addedAt
    val a = Instant.ofEpochMilli(first).atZone(zone).year
    val b = Rules.year(cafe.closedAt, zone)
    return if (a == b) "$a" else "$a–$b"
}

@Composable
private fun Empty(sugar: String, go: Go) {
    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Packet(
            PacketLook(stringResource(R.string.first_cafe), state = PacketState.GHOST, sugar = sugar),
            Modifier.width(130.dp).rotate(-3f).clickable { go.add() },
            ghostInk = U.c.pen,
        )
        Spacer(Modifier.height(18.dp))
        Box(Modifier.shadow(3.dp).background(Color(0xFFF4E6C8)).border(0.dp, Color.Transparent).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(stringResource(R.string.empty_board), fontFamily = Hand, fontSize = 22.sp, lineHeight = 24.sp, color = U.c.pen, textAlign = TextAlign.Center)
        }
    }
}
