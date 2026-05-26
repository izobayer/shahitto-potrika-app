package bd.du.bangla.shahittopotrika

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Capture deep-link URL from the intent (may be null for normal launches)
        val deepLinkUrl = intent?.data?.toString()

        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val isDark     by settingsVm.isDarkMode.collectAsState()
            val fontScale  by settingsVm.fontScale.collectAsState()

            ShahittoPotrikaTheme(darkTheme = isDark, fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
