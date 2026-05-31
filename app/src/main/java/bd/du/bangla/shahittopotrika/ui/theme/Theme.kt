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
val DarkBg        = Color(0xFF0D1F2D)
val DarkSurface   = Color(0xFF112535)
val DarkSurface2  = Color(0xFF0A1820)
val TealAccent    = Color(0xFF00D4B1)
val TealAccentDim = Color(0xFF009E85)
val OnDarkHigh    = Color(0xFFFFFFFF)
val OnDarkMed     = Color(0xFFB0C8D4)
val OnDarkLow     = Color(0xFF5A8090)
val DarkOutline   = Color(0xFF1E3A4A)

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
    onPrimaryContainer   = OnDarkHigh,
    secondary            = TealAccentDim,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF0A2A25),
    onSecondaryContainer = TealAccent,
    background           = DarkBg,
    onBackground         = OnDarkHigh,
    surface              = DarkSurface,
    onSurface            = OnDarkHigh,
    surfaceVariant       = DarkSurface2,
    onSurfaceVariant     = OnDarkMed,
    outline              = DarkOutline,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF2A1A1A),
    onErrorContainer     = Color(0xFFFF8A80),
)

private val LightColors = lightColorScheme(
    primary              = Navy,
    onPrimary            = Color.White,
    primaryContainer     = NavyLight,
    onPrimaryContainer   = Color.White,
    secondary            = AccentBlue,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001846),
    background           = Color(0xFFF6F8FC),
    onBackground         = TextPrimary,
    surface              = Color.White,
    onSurface            = TextPrimary,
    surfaceVariant       = Color(0xFFE8EDF2),
    onSurfaceVariant     = TextSub,
    outline              = Color(0xFFB0BCC8),
    error                = Color(0xFFBA1A1A),
    onError              = Color.White,
)

@Composable
fun ShahittoPotrikaTheme(
    darkTheme: Boolean = true,
    fontScale: Float   = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BlinkistDarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
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
