package bd.du.bangla.shahittopotrika.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Brand colours ─────────────────────────────────────────
val Navy        = Color(0xFF101E5A)   // primary / splash bg
val NavyLight   = Color(0xFF2B3E9E)
val NavyDark    = Color(0xFF080F2E)
val HeaderBg    = Color(0xFFEEF2F6)   // surface / header bg
val AccentBlue  = Color(0xFF2563EB)   // links / active
val TextPrimary = Color(0xFF1B2C3D)
val TextSub     = Color(0xFF5A6A7A)

private val LightColors = lightColorScheme(
    primary            = Navy,
    onPrimary          = Color.White,
    primaryContainer   = NavyLight,
    onPrimaryContainer = Color.White,
    secondary          = AccentBlue,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001846),
    background         = HeaderBg,
    onBackground       = TextPrimary,
    surface            = Color.White,
    onSurface          = TextPrimary,
    surfaceVariant     = Color(0xFFE8EDF2),
    onSurfaceVariant   = TextSub,
    outline            = Color(0xFFB0BCC8),
    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
)

private val DarkColors = darkColorScheme(
    primary            = NavyLight,
    onPrimary          = Color.White,
    primaryContainer   = NavyDark,
    onPrimaryContainer = HeaderBg,
    secondary          = AccentBlue,
    onSecondary        = Color.White,
    background         = Color(0xFF0D1117),
    onBackground       = Color(0xFFE0E6F0),
    surface            = Color(0xFF161C2A),
    onSurface          = Color(0xFFE0E6F0),
    surfaceVariant     = Color(0xFF1E2738),
    onSurfaceVariant   = Color(0xFFAAB8C8),
    outline            = Color(0xFF3A4A5A),
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
)

@Composable
fun ShahittoPotrikaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // keep brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Navy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}
