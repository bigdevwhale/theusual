package app.theusual.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.theusual.R
import app.theusual.container
import app.theusual.data.CafeDraft
import app.theusual.data.Diary
import app.theusual.data.Drink
import app.theusual.data.Pattern
import app.theusual.data.Sketches
import app.theusual.data.Tag
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.T
import app.theusual.ui.art.Art
import app.theusual.ui.art.Packet
import app.theusual.ui.art.PacketLook
import app.theusual.ui.components.Chip
import app.theusual.ui.components.Field
import app.theusual.ui.components.Polaroid
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.components.SectionTitle
import app.theusual.ui.components.SoftButton
import app.theusual.ui.components.TopBar
import app.theusual.ui.ctx
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

/**
 * A new café, or changes to one: its name and where it is, a photo of the front to sketch, the
 * sugar packet's print, the usual, the stamps.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditScreen(id: Long, h: Home, go: Go) {
    val c = ctx()
    val existing = h.cafe(id)
    var d by remember { mutableStateOf(existing?.let(CafeDraft::of) ?: CafeDraft(city = h.settings.city, palette = Random.nextInt(Diary.PACKET_COLOURS), pattern = Pattern.entries.random())) }
    var sketching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sugar = stringResource(R.string.sugar)

    fun sketch(uri: Uri?) {
        uri ?: return
        sketching = true
        scope.launch {
            val name = Sketches.fromPhoto(c, uri)
            // A sketch made here and replaced before saving is nobody's: drop it.
            if (name != null && d.sketch != null && d.sketch != existing?.sketch) Sketches.delete(c, d.sketch)
            if (name != null) d = d.copy(sketch = name)
            sketching = false
        }
    }
    val shot = remember { File(c.cacheDir, "photos").apply { mkdirs() }.let { File(it, "front.jpg") } }
    val shotUri = remember { FileProvider.getUriForFile(c, c.packageName + ".files", shot) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) sketch(shotUri) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { sketch(it) }

    Column(Modifier.fillMaxSize().background(U.c.bg).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        TopBar(stringResource(if (existing == null) R.string.new_cafe else R.string.edit_cafe), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Field(d.name, { d = d.copy(name = it) }, stringResource(R.string.f_name))
            Spacer(Modifier.height(8.dp))
            Field(d.address, { d = d.copy(address = it) }, stringResource(R.string.f_address))
            Spacer(Modifier.height(8.dp))
            Field(d.city, { d = d.copy(city = it) }, stringResource(R.string.f_city))

            SectionTitle(stringResource(R.string.f_front))
            Box(Modifier.fillMaxWidth().clip(U.RMd).background(U.c.wood).padding(vertical = 22.dp), contentAlignment = Alignment.Center) {
                Polaroid(d.sketch, d.palette, d.name, stringResource(if (d.sketch != null) R.string.sketch_ready else R.string.sketch_from_memory), Modifier.fillMaxWidth(.7f))
                if (sketching) CircularProgressIndicator(color = U.c.amber)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftButton(stringResource(R.string.f_shoot), { runCatching { camera.launch(shotUri) } }, Modifier.weight(1f), icon = Icons.Rounded.PhotoCamera)
                SoftButton(stringResource(R.string.f_gallery), { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f), icon = Icons.Rounded.PhotoLibrary)
            }
            Text(stringResource(R.string.f_front_sub), style = UType.Small, modifier = Modifier.padding(top = 6.dp, start = 4.dp))

            SectionTitle(stringResource(R.string.f_packet))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(118.dp).clip(U.RMd).background(U.c.cork).padding(14.dp)) {
                    Packet(PacketLook(d.name.ifBlank { "…" }, d.sub, d.pattern, d.palette, sugar = sugar), Modifier.fillMaxWidth().rotate(-3f))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Field(d.sub, { d = d.copy(sub = it.take(18)) }, stringResource(R.string.f_sub))
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Art.packetColours.forEachIndexed { i, (bg, ac) ->
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(bg)
                                    .border(if (i == d.palette) 3.dp else 1.dp, if (i == d.palette) U.c.amber else ac, CircleShape)
                                    .clickable { d = d.copy(palette = i) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pattern.entries.forEach { p -> Chip(pattern(p), p == d.pattern) { d = d.copy(pattern = p) } }
            }

            SectionTitle(stringResource(R.string.as_usual_q))
            Field(d.usual, { d = d.copy(usual = it) }, stringResource(R.string.f_usual))
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Drink.entries.forEach { k -> Chip(T.drink(c, k), k == d.usualDrink) { d = d.copy(usualDrink = if (d.usualDrink == k) null else k) } }
            }
            Spacer(Modifier.height(8.dp))
            Field(d.spot, { d = d.copy(spot = it) }, stringResource(R.string.f_spot))
            Spacer(Modifier.height(8.dp))
            Field(d.barista, { d = d.copy(barista = it) }, stringResource(R.string.f_barista))
            Text(stringResource(R.string.f_usual_sub), style = UType.Small, modifier = Modifier.padding(top = 6.dp, start = 4.dp))

            SectionTitle(stringResource(R.string.f_stamps))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag.entries.forEach { t -> Chip(T.tag(c, t), t in d.tags) { d = d.copy(tags = if (t in d.tags) d.tags - t else d.tags + t) } }
            }

            SectionTitle(stringResource(R.string.f_story))
            Field(d.story, { d = d.copy(story = it) }, stringResource(R.string.f_story_hint), singleLine = false, minLines = 3)
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton(stringResource(R.string.save), {
                val draft = d
                scope.launch {
                    if (existing == null) {
                        val newId = c.container.diary.add(draft)
                        go.added(newId)
                    } else {
                        c.container.diary.edit(id, draft)
                        go.back()
                    }
                }
            }, Modifier.fillMaxWidth(), enabled = d.name.isNotBlank() && !sketching)
        }
    }
}

@Composable
private fun pattern(p: Pattern) = stringResource(
    when (p) {
        Pattern.STRIPES -> R.string.p_stripes
        Pattern.DOTS -> R.string.p_dots
        Pattern.CHEVRON -> R.string.p_chevron
        Pattern.BAND -> R.string.p_band
        Pattern.SUN -> R.string.p_sun
    },
)
