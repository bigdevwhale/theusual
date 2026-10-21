package app.theusual

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.theusual.data.Settings
import app.theusual.domain.Packets
import app.theusual.ui.TheUsualRoot
import app.theusual.ui.theme.TheUsualTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val t = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(t), navigationBarStyle = SystemBarStyle.dark(t))
        if (savedInstanceState == null) opened(intent)
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            // Wait for settings so the welcome screen doesn't flash for someone already here.
            settings?.let { s -> TheUsualTheme { TheUsualRoot(s) } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        opened(intent)
    }

    /** "Share → The Usual": a sugar packet from a friend. */
    private fun opened(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        Packets.parse(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty())?.let { container.packet.value = it }
    }
}
