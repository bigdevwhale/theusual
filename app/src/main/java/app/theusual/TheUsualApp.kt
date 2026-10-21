package app.theusual

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.theusual.data.DemoSeed
import app.theusual.data.Diary
import app.theusual.data.DiaryDatabase
import app.theusual.data.SettingsStore
import app.theusual.domain.PacketCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration as OsmConfig
import java.io.File

class AppContainer(context: Context) {
    val db = DiaryDatabase.create(context)
    val settings = SettingsStore(context)
    val diary = Diary(context, db)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** A packet shared into the app by a friend, waiting to be put on the board. */
    val packet = MutableStateFlow<PacketCard?>(null)

    /** Putting a café on the map: where the picker starts, and the point it hands back. */
    val pickFrom = MutableStateFlow<Pair<Double, Double>?>(null)
    val picked = MutableStateFlow<Pair<Double, Double>?>(null)

    /** The address the picker looks up when there's no point yet. */
    val pickQuery = MutableStateFlow("")

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }
}

class TheUsualApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Tiles come from openstreetmap.org, whose policy asks for an app-specific user agent.
        OsmConfig.getInstance().apply {
            userAgentValue = "TheUsual/${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})"
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
            tileFileSystemCacheMaxBytes = 150L * 1024 * 1024
            tileFileSystemCacheTrimBytes = 120L * 1024 * 1024
        }
        if (BuildConfig.DEMO) container.scope.launch { DemoSeed.seedIfEmpty(this@TheUsualApp, container) }
    }
}

val Context.container: AppContainer get() = (applicationContext as TheUsualApp).container

/** Resources in the in-app language, which AppCompat doesn't localise outside activities before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

/** Is this context in Russian? */
val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
