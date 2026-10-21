package app.theusual.ui.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import app.theusual.container
import app.theusual.data.Cafe
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.Standing
import app.theusual.ui.T
import app.theusual.ui.art.Packet
import app.theusual.ui.art.PacketState
import app.theusual.ui.components.Chip
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.mutableDoubleStateOf
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/** The cafés on an old city plan, a city at a time. [focus] opens on one café. */
@Composable
fun MapScreen(focus: Long, h: Home, go: Go) {
    val c = ctx()
    val sugar = stringResource(R.string.sugar)
    val placed = h.cafes.filter { it.onMap }
    var picked by rememberSaveable { mutableStateOf(if (focus > 0) focus else 0L) }
    val cities = remember(placed) { placed.map { it.city.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() } }
    val start = remember { h.cafe(focus)?.city?.trim() ?: h.latest?.second?.takeIf { it.onMap }?.city?.trim() ?: h.settings.city.trim().ifEmpty { cities.firstOrNull().orEmpty() } }
    var city by rememberSaveable { mutableStateOf(start) }

    val overlay = remember { PinOverlay(c) { id -> picked = id ?: 0L } }
    val map = rememberTileMap { m -> m.overlays.add(overlay) }
    overlay.pins = placed.map { Pin(it.id, it.lat!!, it.lon!!, it.name, it.patternOf, it.palette, h.look(it, sugar).state) }
    overlay.picked = picked.takeIf { it > 0 }
    map.invalidate()

    LaunchedEffect(city) {
        val focused = h.cafe(focus)?.takeIf { it.onMap && it.city.trim().equals(city, true) }
        val inCity = placed.filter { it.city.trim().equals(city, true) }.ifEmpty { placed }
        map.post { frame(map, focused?.let { listOf(it) } ?: inCity) }
    }
    val locate = rememberHere { l ->
        if (l == null) Toast.makeText(c, R.string.no_location, Toast.LENGTH_SHORT).show()
        else map.controller.animateTo(GeoPoint(l.latitude, l.longitude), 16.0, 600L)
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        AndroidMap(map)
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xEE1D1712), Color.Transparent)))
                .windowInsetsPadding(WindowInsets.statusBars).padding(bottom = 18.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = go::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink) }
                Text(stringResource(R.string.map_title), style = UType.gold(26f), modifier = Modifier.weight(1f))
                IconButton(onClick = locate) { Icon(Icons.Rounded.MyLocation, stringResource(R.string.im_here_map), tint = U.c.ink) }
            }
            if (cities.size > 1) Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cities.forEach { name -> Chip(name, name.equals(city, true)) { city = name; picked = 0L } }
            }
        }
        if (placed.isEmpty()) Box(Modifier.align(Alignment.Center).padding(32.dp).rotate(-2f).shadow(4.dp).background(Color(0xFFF4E6C8)).padding(16.dp)) {
            Text(stringResource(R.string.map_empty), fontFamily = Hand, fontSize = 23.sp, lineHeight = 25.sp, color = U.c.pen, textAlign = TextAlign.Center)
        }
        h.cafe(picked)?.let { cafe -> Card(cafe, h, sugar, go, Modifier.align(Alignment.BottomCenter)) }
        Text(
            stringResource(R.string.osm_credit), fontSize = 10.sp, color = Color(0xAA2A1F17),
            modifier = Modifier.align(Alignment.BottomEnd).windowInsetsPadding(WindowInsets.navigationBars).padding(6.dp)
                .background(Color(0x99F4E6C8)).padding(horizontal = 4.dp),
        )
    }
}

/** The map view made by [rememberTileMap], on screen behind everything. */
@Composable
private fun AndroidMap(map: MapView) {
    androidx.compose.ui.viewinterop.AndroidView({ map }, Modifier.fillMaxSize())
}

/** Show these cafés: the one close up, several all in view. */
fun frame(map: MapView, cafes: List<Cafe>) {
    val pts = cafes.mapNotNull { c -> c.lat?.let { GeoPoint(it, c.lon!!) } }
    when {
        pts.isEmpty() -> map.controller.setZoom(3.0)
        pts.size == 1 -> { map.controller.setZoom(16.0); map.controller.setCenter(pts[0]) }
        else -> {
            val box = BoundingBox.fromGeoPointsSafe(pts)
            val pad = (64 * map.resources.displayMetrics.density).toInt()
            if (box.latitudeSpan < 0.002 && box.longitudeSpanWithDateLine < 0.002) { map.controller.setZoom(16.0); map.controller.setCenter(box.centerWithDateLine) }
            else map.zoomToBoundingBox(box, false, pad)
        }
    }
}

@Composable
private fun Card(cafe: Cafe, h: Home, sugar: String, go: Go, modifier: Modifier) {
    val c = ctx()
    Row(
        modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(start = 16.dp, end = 16.dp, bottom = 26.dp).fillMaxWidth()
            .shadow(8.dp, U.RLg).clip(U.RLg).background(U.c.surface).clickable { go.cafe(cafe.id) }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Packet(h.look(cafe, sugar).copy(state = if (h.standing(cafe) == Standing.WANT) PacketState.PLAIN else h.look(cafe, sugar).state), Modifier.width(52.dp).rotate(-4f))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(cafe.name, style = UType.Title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val line = when (h.standing(cafe)) {
                Standing.WANT -> if (cafe.from.isNotBlank()) stringResource(R.string.tag_from, cafe.from) else stringResource(R.string.tag_want)
                else -> h.last(cafe.id)?.let { stringResource(R.string.last_here, T.ago(c, it.at)) } ?: cafe.address
            }
            Text(line, style = UType.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            h.usual(cafe)?.let { Text(it.first, fontFamily = Hand, fontSize = 20.sp, color = U.c.amber, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

/**
 * Putting a café on the map: drag the plan under the pin, or "I'm here". [result] receives the
 * point when it's confirmed.
 */
@Composable
fun PickScreen(h: Home, go: Go, result: MutableStateFlow<Pair<Double, Double>?>, startLat: Double?, startLon: Double?, query: String) {
    val c = ctx()
    var zoom by remember { mutableDoubleStateOf(if (startLat != null) 17.0 else 3.0) }
    val map = rememberTileMap { m ->
        m.addMapListener(object : MapListener {
            override fun onScroll(e: ScrollEvent?) = false
            override fun onZoom(e: ZoomEvent?): Boolean { e?.let { zoom = it.zoomLevel }; return false }
        })
        if (startLat != null && startLon != null) { m.controller.setZoom(17.0); m.controller.setCenter(GeoPoint(startLat, startLon)) }
        else {
            val other = h.cafes.filter { it.onMap }
            if (other.isEmpty()) m.controller.setZoom(3.0) else m.post { frame(m, other) }
        }
    }
    var located by remember { mutableStateOf(false) }
    val locate = rememberHere { l ->
        if (l == null) Toast.makeText(c, R.string.no_location, Toast.LENGTH_SHORT).show()
        else { located = true; map.controller.animateTo(GeoPoint(l.latitude, l.longitude), 18.0, 600L) }
    }
    LaunchedEffect(Unit) {
        if (startLat != null) return@LaunchedEffect
        // Open on the cafe's street when its address can be found; else where the phone is, if allowed.
        val found = Geocode.find(query, if (h.ru) "ru" else "en")
        if (found != null) map.controller.animateTo(GeoPoint(found.first, found.second), 17.0, 600L)
        else if (Here.allowed(c)) locate()
    }
    val close = zoom >= 13.0

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        AndroidMap(map)
        // The pin stays in the middle; the plan moves under it.
        Column(Modifier.align(Alignment.Center).offset(y = (-22).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(26.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(U.c.red))
            Box(Modifier.size(width = 3.dp, height = 20.dp).background(Color(0xFF2A1F17)))
        }
        Row(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xEE1D1712), Color.Transparent)))
                .windowInsetsPadding(WindowInsets.statusBars).padding(start = 4.dp, end = 4.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = go::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = U.c.ink) }
            Text(stringResource(R.string.pick_title), style = UType.Title, modifier = Modifier.weight(1f))
        }
        Column(Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp).fillMaxWidth()) {
            Text(
                stringResource(if (close) R.string.pick_hint else R.string.pick_closer), fontFamily = Hand, fontSize = 21.sp, color = U.c.pen,
                modifier = Modifier.align(Alignment.CenterHorizontally).shadow(3.dp).background(Color(0xFFF4E6C8)).padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                app.theusual.ui.components.SoftButton(stringResource(R.string.im_here_map), locate, Modifier.weight(1f), icon = Icons.Rounded.MyLocation)
                PrimaryButton(stringResource(R.string.pick_done), {
                    val p = map.mapCenter
                    result.value = p.latitude to p.longitude
                    go.back()
                }, Modifier.weight(1f), enabled = close)
            }
        }
        Text(
            stringResource(R.string.osm_credit), fontSize = 10.sp, color = Color(0xAA2A1F17),
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = 60.dp, end = 6.dp).background(Color(0x99F4E6C8)).padding(horizontal = 4.dp),
        )
    }
}
