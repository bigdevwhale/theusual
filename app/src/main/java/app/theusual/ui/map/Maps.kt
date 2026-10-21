package app.theusual.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.theusual.R
import app.theusual.data.Pattern
import app.theusual.ui.art.Art
import app.theusual.ui.art.PacketState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.hypot

/** OpenStreetMap tiles printed like an old city plan: colour drained toward sepia on cream paper. */
fun sepia(): ColorMatrixColorFilter {
    val m = ColorMatrix()
    m.setSaturation(0.18f)
    m.postConcat(ColorMatrix(floatArrayOf(0.98f, 0f, 0f, 0f, 14f, 0f, 0.9f, 0f, 0f, 6f, 0f, 0f, 0.72f, 0f, -8f, 0f, 0f, 0f, 1f, 0f)))
    return ColorMatrixColorFilter(m)
}

/** A map view on a lifecycle, with the sepia tiles; [setup] runs once when it is made. Put it on screen with AndroidView. */
@Composable
fun rememberTileMap(setup: (MapView) -> Unit): MapView {
    val context = LocalContext.current
    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            minZoomLevel = 3.0
            maxZoomLevel = 19.0
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude, 0)
            overlayManager.tilesOverlay.setColorFilter(sepia())
            overlayManager.tilesOverlay.loadingBackgroundColor = 0xFFEADBBB.toInt()
            overlayManager.tilesOverlay.loadingLineColor = 0xFFDCCAA4.toInt()
            setup(this)
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        onDispose {
            lifecycle.removeObserver(obs)
            map.onPause()
            map.onDetach()
        }
    }
    return map
}

/** One café on the map. */
data class Pin(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val name: String,
    val pattern: Pattern,
    val palette: Int,
    val state: PacketState,
)

/**
 * The cafés as little sugar packets stuck into the map, a paper tag with the name under each once
 * the map is close enough (or when one is picked).
 */
class PinOverlay(context: Context, private val onTap: (Long?) -> Unit) : Overlay() {
    var pins: List<Pin> = emptyList()
    var picked: Long? = null

    private val dp = context.resources.displayMetrics.density
    private val hand = ResourcesCompat.getFont(context, R.font.caveat)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.4f * dp; color = 0xFF2A1F17.toInt() }
    private val dashed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.6f * dp; color = 0xFF2A1F17.toInt(); pathEffect = DashPathEffect(floatArrayOf(4 * dp, 3 * dp), 0f)
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = hand; textSize = 17 * dp; color = 0xFF2E2219.toInt(); textAlign = Paint.Align.CENTER }
    private val pt = android.graphics.Point()

    private val w get() = 22 * dp
    private val h get() = 30 * dp

    override fun draw(c: Canvas, map: MapView, shadow: Boolean) {
        if (shadow) return
        val proj = map.projection
        val names = map.zoomLevelDouble >= 14.5
        // The picked one goes on top.
        pins.sortedBy { it.id == picked }.forEach { p ->
            proj.toPixels(GeoPoint(p.lat, p.lon), pt)
            val x = pt.x.toFloat()
            val y = pt.y.toFloat()
            val big = p.id == picked
            val k = if (big) 1.35f else 1f
            packet(c, x, y - h * k, k, p)
            if (names || big) tag(c, x, y + 4 * dp, p.name)
        }
    }

    private fun shape(x: Float, top: Float, k: Float): Path {
        val ww = w * k
        val hh = h * k
        val left = x - ww / 2
        val teeth = 6
        val step = ww / teeth
        return Path().apply {
            moveTo(left, top + 3 * dp)
            for (i in 0 until teeth) { lineTo(left + i * step + step / 2, top); lineTo(left + (i + 1) * step, top + 3 * dp) }
            lineTo(left + ww, top + hh - 3 * dp)
            for (i in teeth - 1 downTo 0) { lineTo(left + i * step + step / 2, top + hh); lineTo(left + i * step, top + hh - 3 * dp) }
            close()
        }
    }

    private fun packet(c: Canvas, x: Float, top: Float, k: Float, p: Pin) {
        val s = shape(x, top, k)
        val ww = w * k
        val hh = h * k
        val left = x - ww / 2
        if (p.state == PacketState.GHOST) {
            fill.color = 0x66FBF3E2
            c.drawPath(s, fill)
            c.drawPath(s, dashed)
            return
        }
        val (bg, ac) = Art.packet(p.palette)
        fun argb(col: androidx.compose.ui.graphics.Color): Int {
            val g = if (p.state == PacketState.CLOSED) (0.299f * col.red + 0.587f * col.green + 0.114f * col.blue) else -1f
            return if (g >= 0) android.graphics.Color.argb(255, (g * 255).toInt(), (g * 255).toInt(), (g * 255).toInt())
            else android.graphics.Color.argb(255, (col.red * 255).toInt(), (col.green * 255).toInt(), (col.blue * 255).toInt())
        }
        fill.color = 0x40000000
        c.save(); c.translate(2 * dp, 3 * dp); c.drawPath(s, fill); c.restore()
        fill.color = argb(bg)
        c.drawPath(s, fill)
        c.save()
        c.clipPath(s)
        fill.color = argb(ac)
        when (p.pattern) {
            Pattern.STRIPES -> { var xx = left; while (xx < left + ww) { c.drawRect(xx, top, xx + 3 * dp * k, top + hh, fill); xx += 6 * dp * k } }
            Pattern.DOTS -> for (r in 0..5) for (q in 0..3) c.drawCircle(left + (4 + q * 6 + if (r % 2 == 1) 3 else 0) * dp * k, top + (4 + r * 5.5f) * dp * k, 1.3f * dp * k, fill)
            Pattern.CHEVRON -> for (i in 0..5) {
                val yy = top + (7 + i * 6) * dp * k
                c.drawPath(Path().apply { moveTo(left - 2 * dp, yy); lineTo(x, yy - 4 * dp * k); lineTo(left + ww + 2 * dp, yy) }, Paint(line).apply { color = argb(ac); strokeWidth = 2 * dp * k })
            }
            Pattern.BAND -> c.drawRect(left, top + hh * .55f, left + ww, top + hh * .75f, fill)
            Pattern.SUN -> for (a in 0 until 12) {
                val t = a / 6.0 * Math.PI
                c.drawLine(x, top + hh * .42f, x + (Math.cos(t) * 40 * dp).toFloat(), top + hh * .42f + (Math.sin(t) * 40 * dp).toFloat(), Paint(fill).apply { strokeWidth = 2.4f * dp * k })
            }
        }
        c.restore()
        c.drawPath(s, line)
        fill.color = 0xFFFBF3E2.toInt()
        c.drawCircle(x, top + hh * .42f, ww * .3f, fill)
        c.drawCircle(x, top + hh * .42f, ww * .3f, line)
        if (p.state == PacketState.CLOSED) {
            fill.color = 0xFF2A1F17.toInt()
            c.drawPath(Path().apply { moveTo(left - 2 * dp, top + 9 * dp); lineTo(left + 9 * dp, top - 2 * dp); lineTo(left + 14 * dp, top - 2 * dp); lineTo(left - 2 * dp, top + 14 * dp); close() }, fill)
        }
    }

    private fun tag(c: Canvas, x: Float, y: Float, name: String) {
        val text = if (name.length > 22) name.take(21) + "…" else name
        val tw = label.measureText(text) + 12 * dp
        val r = RectF(x - tw / 2, y, x + tw / 2, y + 22 * dp)
        fill.color = 0x40000000
        c.drawRect(RectF(r.left + 1.5f * dp, r.top + 2 * dp, r.right + 1.5f * dp, r.bottom + 2 * dp), fill)
        fill.color = 0xFFF4E6C8.toInt()
        c.drawRect(r, fill)
        c.drawText(text, x, r.bottom - 6 * dp, label)
    }

    override fun onSingleTapConfirmed(e: MotionEvent, map: MapView): Boolean {
        val proj = map.projection
        val hit = pins.minByOrNull { p ->
            proj.toPixels(GeoPoint(p.lat, p.lon), pt)
            hypot(e.x - pt.x, e.y - (pt.y - h / 2))
        }?.takeIf { p ->
            proj.toPixels(GeoPoint(p.lat, p.lon), pt)
            hypot(e.x - pt.x, e.y - (pt.y - h / 2)) < 28 * dp
        }
        onTap(hit?.id)
        return hit != null
    }
}

/** Where the phone is: a fresh fix from GPS or the network, or the last one known; null if none. */
object Here {
    fun allowed(c: Context) = ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun locate(c: Context, done: (Location?) -> Unit) {
        if (!allowed(c)) return done(null)
        val lm = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        val last = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
        if (last != null && System.currentTimeMillis() - last.time < 2 * 60_000) return done(last)
        if (providers.isEmpty()) return done(last)
        var answered = false
        val handler = android.os.Handler(Looper.getMainLooper())
        lateinit var listener: LocationListener
        fun answer(l: Location?) {
            if (answered) return
            answered = true
            runCatching { lm.removeUpdates(listener) }
            done(l)
        }
        listener = LocationListener { answer(it) }
        providers.forEach { runCatching { lm.requestLocationUpdates(it, 0L, 0f, listener, Looper.getMainLooper()) } }
        // Indoors GPS may never answer: settle for the last known place after a while.
        handler.postDelayed({ answer(last) }, 12_000)
    }
}

/** "I'm here": asks for location once if needed, then finds the phone. */
@Composable
fun rememberHere(done: (Location?) -> Unit): () -> Unit {
    val c = LocalContext.current
    val latest = rememberUpdatedState(done)
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { Here.locate(c) { latest.value(it) } }
    return remember(c) {
        {
            if (Here.allowed(c)) Here.locate(c) { latest.value(it) }
            else ask.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}
