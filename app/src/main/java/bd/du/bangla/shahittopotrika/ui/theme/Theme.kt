package bd.du.bangla.shahittopotrika.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import bd.du.bangla.shahittopotrika.R

// ── Qayyum Book — used for "সাহিত্য পত্রিকা" branding ────────────────────────
val QayyumBookFamily = FontFamily(
    Font(R.font.qayyum_book, FontWeight.Normal),
    Font(R.font.qayyum_book, FontWeight.Medium),
    Font(R.font.qayyum_book, FontWeight.SemiBold),
    Font(R.font.qayyum_book, FontWeight.Bold),
)

// ── Kalpurush font family ─────────────────────────────────────────────────────
val KalpurushFamily = FontFamily(
    Font(R.font.kalpurush, FontWeight.Thin),
    Font(R.font.kalpurush, FontWeight.ExtraLight),
    Font(R.font.kalpurush, FontWeight.Light),
    Font(R.font.kalpurush, FontWeight.Normal),
    Font(R.font.kalpurush, FontWeight.Medium),
    Font(R.font.kalpurush, FontWeight.SemiBold),
    Font(R.font.kalpurush, FontWeight.Bold),
    Font(R.font.kalpurush, FontWeight.ExtraBold),
    Font(R.font.kalpurush, FontWeight.Black),
)

// ── Dark palette (Blinkist-style Navy-Teal) ───────────────────────────────────
val StaticDarkBg        = Color(0xFF0D1F2D)
val StaticDarkSurface   = Color(0xFF112535)
val StaticDarkSurface2  = Color(0xFF0A1820)
val TealAccent          = Color(0xFF00D4B1)
val TealAccentDim       = Color(0xFF009E85)
val StaticOnDarkHigh    = Color(0xFFFFFFFF)
val StaticOnDarkMed     = Color(0xFFB0C8D4)
val StaticOnDarkLow     = Color(0xFF5A8090)
val StaticDarkOutline   = Color(0xFF1E3A4A)

// ── Dynamic Color Lookups (Theme-Aware) ───────────────────────────────────────
val DarkBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val DarkSurface2: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val OnDarkHigh: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val OnDarkMed: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val OnDarkLow: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

val DarkOutline: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

// ── Light palette (kept for settings toggle) ──────────────────────────────────
val Navy        = Color(0xFF101E5A)
val NavyLight   = Color(0xFF2B3E9E)
val NavyDark    = Color(0xFF080F2E)
val HeaderBg    = Color(0xFFEEF2F6)
val AccentBlue  = Color(0xFF2563EB)
val TextPrimary = Color(0xFF1B2C3D)
val TextSub     = Color(0xFF5A6A7A)

// ── Typography (Kalpurush everywhere) ────────────────────────────────────────
val KalpurushTypography = Typography(
    displayLarge = TextStyle(
        fontFamily   = KalpurushFamily,
        fontWeight   = FontWeight.Black,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = KalpurushFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = KalpurushFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 30.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = KalpurushFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = KalpurushFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = KalpurushFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 13.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = KalpurushFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp
    ),
)

// ── Color schemes ─────────────────────────────────────────────────────────────
private val BlinkistDarkColors = darkColorScheme(
    primary              = TealAccent,
    onPrimary            = Color(0xFF003730),
    primaryContainer     = TealAccentDim,
    onPrimaryContainer   = StaticOnDarkHigh,
    secondary            = TealAccentDim,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF0A2A25),
    onSecondaryContainer = TealAccent,
    background           = StaticDarkBg,
    onBackground         = StaticOnDarkHigh,
    surface              = StaticDarkSurface,
    onSurface            = StaticOnDarkHigh,
    surfaceVariant       = StaticDarkSurface2,
    onSurfaceVariant     = StaticOnDarkMed,
    outline              = StaticDarkOutline,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF2A1A1A),
    onErrorContainer     = Color(0xFFFF8A80),
)

private val LightColors = lightColorScheme(
    primary              = Color(0xFF102334),
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFEEF2F6),
    onPrimaryContainer   = Color(0xFF102334),
    secondary            = Color(0xFF2563EB),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFEEF2F6),
    onSecondaryContainer = Color(0xFF102334),
    background           = Color(0xFFEEF2F6),
    onBackground         = Color(0xFF102334),
    surface              = Color.White,
    onSurface            = Color(0xFF102334),
    surfaceVariant       = Color(0xFFF6F8FB),
    onSurfaceVariant     = Color(0xFF4F6574),
    outline              = Color(0xFFCEDAE2),
    error                = Color(0xFFBA1A1A),
    onError              = Color.White,
)

@Composable
fun ShahittoPotrikaTheme(
    darkTheme: Boolean = false,
    fontScale: Float   = 1.15f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BlinkistDarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val baseDensity = LocalDensity.current
    val scaledDensity = Density(
        density   = baseDensity.density,
        fontScale = fontScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = KalpurushTypography,
            content     = content
        )
    }
}
