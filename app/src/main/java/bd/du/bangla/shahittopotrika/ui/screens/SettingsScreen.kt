package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val isDark    by vm.isDarkMode.collectAsState()
    val fontScale by vm.fontScale.collectAsState()
    val notifOn   by vm.notificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস", fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Appearance ───────────────────────────────
            SettingsGroupHeader("চেহারা")

            SettingsToggleRow(
                icon  = Icons.Default.DarkMode,
                title = "ডার্ক মোড",
                subtitle = "রাতে পড়ার জন্য অন্ধকার থিম",
                checked = isDark,
                onToggle = { vm.toggleDarkMode() }
            )

            // Font scale slider
            Card(shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.TextFields, null, tint = Navy,
                            modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("টেক্সট আকার", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(
                                when {
                                    fontScale <= 0.85f -> "ছোট"
                                    fontScale <= 1.0f  -> "স্বাভাবিক"
                                    fontScale <= 1.15f -> "বড়"
                                    else               -> "অনেক বড়"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("${(fontScale * 100).toInt()}%",
                            fontSize = 12.sp, color = Navy, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { vm.setFontScale(it) },
                        valueRange = 0.85f..1.30f,
                        steps = 2,   // 0.85, 1.0, 1.15, 1.30
                        colors = SliderDefaults.colors(
                            thumbColor = Navy, activeTrackColor = Navy)
                    )
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ক", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("ক", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("ক", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("ক", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Notifications ────────────────────────────
            SettingsGroupHeader("বিজ্ঞপ্তি")

            SettingsToggleRow(
                icon  = Icons.Default.Notifications,
                title = "নতুন সংখ্যার বিজ্ঞপ্তি",
                subtitle = "নতুন সংখ্যা প্রকাশ হলে জানাবে",
                checked = notifOn,
                onToggle = { vm.toggleNotifications() }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "বিজ্ঞপ্তির জন্য ইন্টারনেট সংযোগ প্রয়োজন। অ্যাপ বন্ধ থাকলেও প্রতিদিন একবার চেক করা হবে।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        color = Navy, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: () -> Unit
) {
    Card(shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = Navy, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Navy))
        }
    }
}
