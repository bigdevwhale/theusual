package app.theusual.ui.visit

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.container
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.art.WindowScene
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.Mono
import app.theusual.ui.theme.U
import kotlinx.coroutines.delay

/** The table by the window and the rain, cropped tall for a phone. */
private val Tall = Rect(170f, 96f, 560f, 600f)

/**
 * An hour at the window: put the phone face down and look out. Time counts only while it lies
 * face down; pick it up and the count waits. Getting up writes the minutes onto the cup.
 */
@Composable
fun WindowScreen(visitId: Long, h: Home, go: Go) {
    val c = ctx()
    var down by remember { mutableStateOf(false) }
    var total by remember { mutableLongStateOf(0L) }
    var since by remember { mutableLongStateOf(0L) }

    // The screen stays on while here: face down it can't be seen anyway, and it must not sleep.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    FaceDown { isDown ->
        val now = System.currentTimeMillis()
        if (isDown && !down) since = now
        if (!isDown && down) total += now - since
        down = isDown
    }
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { tick = System.currentTimeMillis(); delay(1000) } }
    val ms = total + if (down) tick - since else 0L
    val minutes = (ms / 60_000).toInt()

    fun leave() {
        val m = ((total + if (down) System.currentTimeMillis() - since else 0L) / 60_000).toInt()
        if (m > 0) c.container.write { diary.window(visitId, m) }
        go.back()
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        WindowScene(Modifier.fillMaxSize(), lights = true, name = "", crop = Tall)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xCC1D1712), Color.Transparent, Color.Transparent, Color(0xEE1D1712)))))
        Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 28.dp, vertical = 24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.window_title), fontFamily = Hand, fontSize = 40.sp, color = U.c.amber, textAlign = TextAlign.Center)
            Text(stringResource(R.string.window_how), fontFamily = Hand, fontSize = 23.sp, lineHeight = 25.sp, color = U.c.ink, textAlign = TextAlign.Center)
        }
        Column(Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 24.dp, vertical = 18.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%d:%02d".format(minutes / 60, minutes % 60), fontFamily = Mono, fontSize = 48.sp, color = U.c.ink)
            Text(stringResource(if (down) R.string.window_counting else R.string.window_waiting), fontFamily = Hand, fontSize = 22.sp, color = U.c.muted)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(stringResource(R.string.window_leave), ::leave, Modifier.fillMaxWidth())
        }
    }
}

/** Tells whether the phone lies face down (screen to the table), from the accelerometer. */
@Composable
private fun FaceDown(onChange: (Boolean) -> Unit) {
    val c = ctx()
    DisposableEffect(Unit) {
        val sm = c.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var last: Boolean? = null
        val l = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                // Face down, gravity points out of the screen: z is strongly negative.
                val now = e.values[2] < -7.5f
                if (now != last) { last = now; onChange(now) }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (acc != null) sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sm.unregisterListener(l) }
    }
}
