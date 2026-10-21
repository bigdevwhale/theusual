package app.theusual

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import app.theusual.data.DemoSeed
import app.theusual.ui.Go
import app.theusual.ui.Home
import app.theusual.ui.Standing
import app.theusual.ui.art.Art
import app.theusual.ui.board.BoardScreen
import app.theusual.ui.cafe.CafeScreen
import app.theusual.ui.onboarding.WelcomeScreen
import app.theusual.ui.share.ShareScreen
import app.theusual.ui.theme.TheUsualTheme
import app.theusual.ui.theme.U
import app.theusual.ui.visit.VisitScreen
import app.theusual.ui.visit.WindowScreen
import app.theusual.ui.year.YearScreen
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Every main screen over the demo diary, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }

    @Before fun still() {
        Art.still = true
    }

    private fun home(): Home = runBlocking {
        val app = ApplicationProvider.getApplicationContext<TheUsualApp>()
        val c = app.container
        DemoSeed.seedIfEmpty(app, c)
        val dao = c.db.dao()
        Home(dao.cafes(), dao.visits(), c.settings.now(), System.currentTimeMillis(), app.ru)
    }

    private fun shot(name: String, h: Home, content: @Composable (Home, Go) -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val nav = rememberNavController()
            TheUsualTheme { Box(Modifier.fillMaxSize().background(U.c.bg)) { content(h, Go(nav)) } }
        }
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(500)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun yours(h: Home) = h.cafes.first { h.standing(it) == Standing.REGULAR }

    @Test fun welcome() = home().let { shot("0-welcome", it) { _, _ -> WelcomeScreen {} } }
    @Test fun board() = home().let { shot("1-board", it) { h, go -> BoardScreen(h, go) } }
    @Test fun cafe() = home().let { h -> shot("2-cafe", h) { hh, go -> CafeScreen(yours(hh).id, hh, go) } }
    @Test fun visit() = home().let { h -> shot("3-usual", h) { hh, go -> VisitScreen(yours(hh).id, hh, go) } }
    @Test fun window() = home().let { h -> shot("4-window", h) { hh, go -> WindowScreen(hh.visits.last().id, hh, go) } }
    @Test fun share() = home().let { h -> shot("5-share", h) { hh, go -> ShareScreen(yours(hh).id, hh, go) } }
    @Test fun closed() = home().let { h -> shot("6-closed", h) { hh, go -> CafeScreen(hh.cafes.first { it.closed }.id, hh, go) } }
    @Test fun year() = home().let { shot("7-year", it) { h, go -> YearScreen(h, go) } }
    @Test fun want() = home().let { h -> shot("8-want", h) { hh, go -> CafeScreen(hh.cafes.first { hh.standing(it) == Standing.WANT }.id, hh, go) } }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}
