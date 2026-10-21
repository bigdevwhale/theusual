package app.theusual.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.theusual.R

/**
 * A café on a rainy evening, seen from the street and then from the table by the window: dark
 * wood, amber lamplight, gold lettering on the glass, a cream notebook with coffee rings, kraft
 * tape and rubber stamps, a cork board of sugar packets under string lights. Only the evening look.
 */
@Immutable
data class Palette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    /** Lamplight: for the one thing to do. */
    val amber: Color,
    val amberSoft: Color,
    val onAmber: Color,
    /** The gold leaf of the window lettering. */
    val gold: Color,
    /** The notebook: paper, its ink, a red pencil, a teal stamp. */
    val paper: Color,
    val paperDark: Color,
    val pen: Color,
    val penSoft: Color,
    val red: Color,
    val teal: Color,
    val wood: Color,
    val woodDark: Color,
    val cork: Color,
    /** The sticky note a friend writes on. */
    val sticky: Color,
)

val Evening = Palette(
    bg = Color(0xFF1D1712),
    surface = Color(0xFF2A2019),
    surface2 = Color(0xFF34281F),
    line = Color(0xFF4A3A2E),
    ink = Color(0xFFF3E6CC),
    muted = Color(0xFFCDB894),
    faint = Color(0xFF8F7C63),
    amber = Color(0xFFF2B25C),
    amberSoft = Color(0xFF3E2C1A),
    onAmber = Color(0xFF1D1712),
    gold = Color(0xFFE7BF69),
    paper = Color(0xFFF1E3C4),
    paperDark = Color(0xFFE2CDA4),
    pen = Color(0xFF2E2219),
    penSoft = Color(0xFF6D5B45),
    red = Color(0xFFA8392B),
    teal = Color(0xFF3D7470),
    wood = Color(0xFF5B3B26),
    woodDark = Color(0xFF2C1A0F),
    cork = Color(0xFFB4834F),
    sticky = Color(0xFFFFF3C2),
)

val LocalPalette = staticCompositionLocalOf { Evening }

/** Playfair Display, black italic: the gold leaf on the café window. */
val Sign = FontFamily(Font(R.font.playfair_black_italic, FontWeight.Black, FontStyle.Italic))

/** Manrope: plain and clear, for everything around the notebook. */
val Sans = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/** Caveat: handwriting in the notebook. */
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))

/** PT Mono: receipts, addresses and rubber stamps. */
val Mono = FontFamily(Font(R.font.pt_mono, FontWeight.Normal))

object U {
    val c: Palette @Composable get() = LocalPalette.current

    val RXl = RoundedCornerShape(24.dp)
    val RLg = RoundedCornerShape(18.dp)
    val RMd = RoundedCornerShape(14.dp)
    val RSm = RoundedCornerShape(10.dp)
}

object UType {
    val Hero: TextStyle @Composable get() = TextStyle(fontFamily = Sign, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 40.sp, lineHeight = 46.sp, color = U.c.gold)
    val Display: TextStyle @Composable get() = TextStyle(fontFamily = Sign, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 28.sp, lineHeight = 34.sp, color = U.c.ink)
    val Title: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp, color = U.c.ink)
    val Sub: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 23.sp, color = U.c.muted)
    val Eyebrow: TextStyle @Composable get() = TextStyle(fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.18.em, color = U.c.amber)
    val Strong: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = U.c.ink)
    val Body: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 23.sp, color = U.c.ink)
    val Muted: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 20.sp, color = U.c.muted)
    val Small: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = U.c.faint)
    val Button: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp)

    /** Handwriting on the page. */
    val Hand: TextStyle @Composable get() = TextStyle(fontFamily = app.theusual.ui.theme.Hand, fontSize = 24.sp, lineHeight = 27.sp, color = U.c.pen)

    /** Typewriter on the page: addresses, receipts. */
    val Typed: TextStyle @Composable get() = TextStyle(fontFamily = Mono, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.05.em, color = U.c.pen)

    /** Gold leaf with a dark keyline and a soft drop, as painted on glass. */
    fun gold(size: Float, color: Color = Evening.gold) = TextStyle(
        fontFamily = Sign, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = size.sp, color = color,
        shadow = Shadow(Color(0xCC1A0E05), offset = Offset(2f, 3f), blurRadius = 6f),
    )
}

@Composable
fun TheUsualTheme(content: @Composable () -> Unit) {
    val p = Evening
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = darkColorScheme().copy(
                primary = p.amber,
                onPrimary = p.onAmber,
                primaryContainer = p.amberSoft,
                onPrimaryContainer = p.ink,
                secondary = p.gold,
                background = p.bg,
                surface = p.surface,
                surfaceContainer = p.surface,
                surfaceContainerHigh = p.surface2,
                surfaceContainerHighest = p.surface2,
                surfaceContainerLow = p.surface,
                onBackground = p.ink,
                onSurface = p.ink,
                onSurfaceVariant = p.muted,
                outline = p.line,
                outlineVariant = p.line,
            ),
            typography = MaterialTheme.typography.copy(
                bodyLarge = UType.Body,
                bodyMedium = UType.Body.copy(fontSize = 15.sp),
                labelLarge = UType.Button,
            ),
            content = content,
        )
    }
}
