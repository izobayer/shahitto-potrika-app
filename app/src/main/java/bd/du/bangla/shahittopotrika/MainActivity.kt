package bd.du.bangla.shahittopotrika

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import bd.du.bangla.shahittopotrika.ui.navigation.AppNavigation
import bd.du.bangla.shahittopotrika.ui.theme.ShahittoPotrikaTheme
import bd.du.bangla.shahittopotrika.viewmodel.SettingsViewModel

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.ViewGroup
import android.view.Gravity
import android.widget.FrameLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // ── Splash screen: show logo and rotate a light green border around it ──
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { provider ->
            val icon = provider.iconView
            val context = icon.context
            
            val iconWidth = if (icon.width > 0) icon.width else (108 * context.resources.displayMetrics.density).toInt()
            val iconHeight = if (icon.height > 0) icon.height else (108 * context.resources.displayMetrics.density).toInt()
            val borderSize = (iconWidth * 1.25f).toInt()

            val borderView = object : View(context) {
                val paint = Paint().apply {
                    color = Color.parseColor("#4ADE80") // Bold light green
                    style = Paint.Style.STROKE
                    strokeWidth = 14f // Bold border
                    isAntiAlias = true
                    strokeCap = Paint.Cap.ROUND
                }
                
                override fun onDraw(canvas: Canvas) {
                    super.onDraw(canvas)
                    val size = width.coerceAtMost(height).toFloat()
                    val padding = paint.strokeWidth / 2f + 6f
                    val rect = RectF(padding, padding, size - padding, size - padding)
                    // Draw a circular segment (280 degrees arc) for spinning effect
                    canvas.drawArc(rect, 0f, 280f, false, paint)
                }
            }

            val layoutParams = FrameLayout.LayoutParams(borderSize, borderSize).apply {
                gravity = Gravity.CENTER
            }
            borderView.layoutParams = layoutParams

            val root = provider.view as? ViewGroup
            root?.addView(borderView)

            // Spin the border twice (720 degrees) in 800ms
            val rotateAnimator = ObjectAnimator.ofFloat(borderView, View.ROTATION, 0f, 720f).apply {
                duration = 800
                interpolator = LinearInterpolator()
            }

            // Smoothly fade out the entire splash screen
            val alphaAnimator = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                duration = 800
            }

            rotateAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(a: Animator) {
                    provider.remove()
                }
                override fun onAnimationStart(a: Animator) {}
                override fun onAnimationCancel(a: Animator) {
                    provider.remove()
                }
                override fun onAnimationRepeat(a: Animator) {}
            })

            rotateAnimator.start()
            alphaAnimator.start()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkUrl = intent?.data?.toString()

        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val themeMode by settingsVm.themeMode.collectAsState()
            val fontScale by settingsVm.fontScale.collectAsState()

            ShahittoPotrikaTheme(themeMode = themeMode, fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        deepLinkUrl   = deepLinkUrl
                    )
                }
            }
        }
    }
}
