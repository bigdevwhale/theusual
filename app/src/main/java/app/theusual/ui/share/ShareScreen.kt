package app.theusual.ui.share

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.container
import app.theusual.domain.PacketCard
import app.theusual.domain.Packets
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.art.Packet
import app.theusual.ui.art.PacketState
import app.theusual.ui.art.Tape
import app.theusual.ui.art.lamplight
import app.theusual.ui.art.wood
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.U

/**
 * A sugar packet for a friend: the café's packet and a sticky note with what to order and where
 * to sit, signed. It goes as plain text through any messenger.
 */
@Composable
fun ShareScreen(id: Long, h: Home, go: Go) {
    val cafe = h.cafe(id) ?: return
    val c = ctx()
    val sugar = stringResource(R.string.sugar)
    val usual = h.usual(cafe)?.first
    val suggestion = buildList {
        usual?.let { add(c.getString(R.string.share_take, it)) }
        if (cafe.spot.isNotBlank()) add(c.getString(R.string.share_sit, cafe.spot))
        if (cafe.barista.isNotBlank()) add(c.getString(R.string.share_ask, cafe.barista))
    }.joinToString(" ")
    var note by rememberSaveable { mutableStateOf(suggestion) }
    var name by rememberSaveable { mutableStateOf(h.settings.name) }

    Box(Modifier.fillMaxSize().wood().lamplight()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState()).imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth()) {
                IconButton(onClick = go::back, modifier = Modifier.padding(4.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink) }
            }
            Text(stringResource(R.string.share_title), fontFamily = Hand, fontSize = 40.sp, color = U.c.ink, modifier = Modifier.rotate(-1f))
            Spacer(Modifier.height(14.dp))
            Packet(h.look(cafe, sugar).copy(state = PacketState.PLAIN, city = ""), Modifier.width(150.dp).rotate(-6f))
            Spacer(Modifier.height(26.dp))
            Box(Modifier.padding(horizontal = 28.dp)) {
                Column(Modifier.rotate(2f).shadow(4.dp).background(U.c.sticky).padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth()) {
                    val hand = TextStyle(fontFamily = Hand, fontSize = 24.sp, lineHeight = 26.sp, color = U.c.pen)
                    BasicTextField(note, { note = it }, textStyle = hand, cursorBrush = SolidColor(U.c.pen), modifier = Modifier.fillMaxWidth(), decorationBox = { inner ->
                        if (note.isEmpty()) Text(stringResource(R.string.share_note_hint), style = hand.copy(color = U.c.penSoft))
                        inner()
                    })
                    Spacer(Modifier.height(6.dp))
                    BasicTextField(name, { name = it }, textStyle = hand.copy(color = U.c.penSoft), singleLine = true, cursorBrush = SolidColor(U.c.pen), modifier = Modifier.align(Alignment.End), decorationBox = { inner ->
                        Box { if (name.isEmpty()) Text("— " + stringResource(R.string.share_sign), style = hand.copy(color = U.c.penSoft.copy(alpha = .6f))) else Text("— ", style = hand.copy(color = U.c.penSoft), modifier = Modifier.offset(x = (-18).dp)); inner() }
                    })
                }
                Tape(Modifier.align(Alignment.TopCenter).offset(y = (-10).dp), pink = true, w = 70.dp, h = 20.dp)
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.share_how), fontFamily = Hand, fontSize = 19.sp, color = U.c.muted, modifier = Modifier.padding(horizontal = 30.dp))
            Spacer(Modifier.height(24.dp))
            PrimaryButton(stringResource(R.string.send_packet), {
                if (name.trim() != h.settings.name) c.container.write { settings.update { it.copy(name = name.trim()) } }
                val text = Packets.text(PacketCard(cafe.name, cafe.address, cafe.city, cafe.sub, cafe.patternOf, cafe.palette, note, name, cafe.lat, cafe.lon))
                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                c.startActivity(Intent.createChooser(send, c.getString(R.string.send_packet)))
            }, Modifier.padding(horizontal = 24.dp).fillMaxWidth(), icon = Icons.AutoMirrored.Rounded.Send)
            Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(24.dp))
        }
    }
}
