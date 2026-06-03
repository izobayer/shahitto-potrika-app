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
            val borderSize = (iconWidth * 1.15f).toInt()

            val borderView = object : View(context) {
                val paint = Paint().apply {
                    color = Color.parseColor("#00D4B1") // Brand TealAccent
                    style = Paint.Style.STROKE
                    strokeWidth = 10f // Premium thinner border
                    isAntiAlias = true
                    strokeCap = Paint.Cap.ROUND
                }
                
                override fun onDraw(canvas: Canvas) {
                    super.onDraw(canvas)
                    val size = width.coerceAtMost(height).toFloat()
                    val padding = paint.strokeWidth / 2f + 4f
                    val rect = RectF(padding, padding, size - padding, size - padding)
                    // Draw a 260 degrees arc for a very premium look
                    canvas.drawArc(rect, -90f, 260f, false, paint)
                }
            }

            val layoutParams = FrameLayout.LayoutParams(borderSize, borderSize).apply {
                gravity = Gravity.CENTER
            }
            borderView.layoutParams = layoutParams

            val root = provider.view as? ViewGroup
            root?.addView(borderView)

            // Setup initial states for the "creamy" scale animation
            icon.scaleX = 0.5f
            icon.scaleY = 0.5f
            borderView.scaleX = 0.5f
            borderView.scaleY = 0.5f
            borderView.alpha = 0f

            // Buttery-smooth decelerate interpolator
            val cubicInterpolator = android.view.animation.PathInterpolator(0.2f, 0.8f, 0.2f, 1.0f)

            // 1. Scale icon from 0.5f to 0.65f (making it smaller as requested)
            val iconScaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 0.5f, 0.65f).apply {
                duration = 1000
                interpolator = cubicInterpolator
            }
            val iconScaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0.5f, 0.65f).apply {
                duration = 1000
                interpolator = cubicInterpolator
            }

            // 2. Scale border from 0.5f to 0.68f (to match the smaller logo size)
            val borderScaleX = ObjectAnimator.ofFloat(borderView, View.SCALE_X, 0.5f, 0.68f).apply {
                duration = 1100
                interpolator = cubicInterpolator
            }
            val borderScaleY = ObjectAnimator.ofFloat(borderView, View.SCALE_Y, 0.5f, 0.68f).apply {
                duration = 1100
                interpolator = cubicInterpolator
            }
            val borderAlpha = ObjectAnimator.ofFloat(borderView, View.ALPHA, 0f, 1f).apply {
                duration = 400
                interpolator = cubicInterpolator
            }

            // 3. Spin the border slowly (360 degrees) in 1200ms
            val borderRotate = ObjectAnimator.ofFloat(borderView, View.ROTATION, 0f, 360f).apply {
                duration = 1200
                interpolator = cubicInterpolator
            }

            // 4. Smoothly fade out the entire splash screen
            val fadeOut = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                startDelay = 800
                duration = 500
                interpolator = android.view.animation.AccelerateInterpolator()
            }

            val animSet = android.animation.AnimatorSet()
            animSet.playTogether(
                iconScaleX, iconScaleY,
                borderScaleX, borderScaleY, borderAlpha,
                borderRotate, fadeOut
            )

            animSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(a: Animator) {
                    provider.remove()
                }
                override fun onAnimationStart(a: Animator) {}
                override fun onAnimationCancel(a: Animator) {
                    provider.remove()
                }
                override fun onAnimationRepeat(a: Animator) {}
            })

            animSet.start()
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
