package app.theusual.ui.onboarding

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theusual.R
import app.theusual.container
import app.theusual.ru
import app.theusual.ui.art.WindowScene
import app.theusual.ui.components.PrimaryButton
import app.theusual.ui.ctx
import app.theusual.ui.theme.Hand
import app.theusual.ui.theme.U
import app.theusual.ui.theme.UType
import java.util.Currency

/** The café front, centred on its lettering, for a tall screen. */
private val Front = Rect(250f, 0f, 870f, 530f)

@Composable
fun WelcomeScreen(done: () -> Unit) {
    val c = ctx()
    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(350.dp)) {
                WindowScene(Modifier.fillMaxSize(), crop = Front, tagline = stringResource(R.string.tagline))
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(80.dp).background(Brush.verticalGradient(listOf(Color.Transparent, U.c.bg))))
            }
            Column(Modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.welcome_title), fontFamily = Hand, fontSize = 34.sp, lineHeight = 36.sp, color = U.c.amber, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.welcome_text), style = UType.Sub, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                PrimaryButton(stringResource(R.string.welcome_go), {
                    val ru = c.ru
                    val currency = if (ru) "₽" else runCatching { Currency.getInstance(c.resources.configuration.locales[0]).symbol }.getOrDefault("")
                    c.container.write { settings.update { it.copy(onboarded = true, currency = it.currency.ifBlank { currency }) } }
                    done()
                }, Modifier.fillMaxWidth())
                Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(24.dp))
            }
        }
    }
}
