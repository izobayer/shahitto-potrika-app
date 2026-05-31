package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.du.bangla.shahittopotrika.BuildConfig
import bd.du.bangla.shahittopotrika.ui.theme.DarkSurface
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkHigh
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkLow
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkMed
import bd.du.bangla.shahittopotrika.ui.theme.TealAccent
import bd.du.bangla.shahittopotrika.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val isDark              by vm.isDarkMode.collectAsState()
    val fontScale           by vm.fontScale.collectAsState()
    val notifOn             by vm.notificationsEnabled.collectAsState()
    val notifUpdateOn       by vm.notificationsAppUpdate.collectAsState()
    val offlineCache        by vm.offlineCacheEnabled.collectAsState()
    val historyOn           by vm.historyTrackingEnabled.collectAsState()
    val pdfExternal         by vm.openPdfExternal.collectAsState()
    val showAbstract        by vm.showAbstractInList.collectAsState()
    val context = LocalContext.current

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
                    containerColor = DarkSurface, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── চেহারা ───────────────────────────────────
            SettingsGroupHeader("চেহারা")

            SettingsToggleRow(
                icon     = Icons.Default.DarkMode,
                title    = "ডার্ক মোড",
                subtitle = "রাতে পড়ার জন্য অন্ধকার থিম",
                checked  = isDark,
                onToggle = { vm.toggleDarkMode() }
            )

            // Font scale slider
            Card(
                shape  = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.TextFields, null, tint = TealAccent,
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
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${(fontScale * 100).toInt()}%",
                            fontSize   = 12.sp,
                            color      = TealAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value         = fontScale,
                        onValueChange = { vm.setFontScale(it) },
                        valueRange    = 0.85f..1.30f,
                        steps         = 2,
                        colors        = SliderDefaults.colors(
                            thumbColor       = TealAccent,
                            activeTrackColor = TealAccent
                        )
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

            SettingsToggleRow(
                icon     = Icons.Default.Article,
                title    = "তালিকায় সারসংক্ষেপ দেখান",
                subtitle = "প্রবন্ধ তালিকায় সংক্ষিপ্ত বিবরণ",
                checked  = showAbstract,
                onToggle = { vm.toggleShowAbstract() }
            )

            // ── পড়া ─────────────────────────────────────
            SettingsGroupHeader("পড়া ও প্রবন্ধ")

            SettingsToggleRow(
                icon     = Icons.Default.PictureAsPdf,
                title    = "বাইরের অ্যাপে PDF খুলুন",
                subtitle = "নিজের PDF রিডারে খুলতে চাইলে চালু করুন",
                checked  = pdfExternal,
                onToggle = { vm.toggleOpenPdfExternal() }
            )

            SettingsToggleRow(
                icon     = Icons.Default.History,
                title    = "পঠন ইতিহাস সংরক্ষণ",
                subtitle = "কোন প্রবন্ধ পড়েছেন তার তালিকা রাখুন",
                checked  = historyOn,
                onToggle = { vm.toggleHistoryTracking() }
            )

            // ── ডেটা ────────────────────────────────────
            SettingsGroupHeader("ডেটা ও ক্যাশ")

            SettingsToggleRow(
                icon     = Icons.Default.Download,
                title    = "অফলাইন ক্যাশ",
                subtitle = "ইন্টারনেট ছাড়াও আগের তথ্য দেখা যাবে",
                checked  = offlineCache,
                onToggle = { vm.toggleOfflineCache() }
            )

            // ── বিজ্ঞপ্তি ──────────────────────────────
            SettingsGroupHeader("বিজ্ঞপ্তি")

            SettingsToggleRow(
                icon     = Icons.Default.Notifications,
                title    = "নতুন সংখ্যার বিজ্ঞপ্তি",
                subtitle = "নতুন সংখ্যা প্রকাশ হলে জানাবে",
                checked  = notifOn,
                onToggle = { vm.toggleNotifications() }
            )

            SettingsToggleRow(
                icon     = Icons.Default.SystemUpdate,
                title    = "আ্যাপ আপডেট বিজ্ঞপ্তি",
                subtitle = "নতুন সংস্করণ পাওয়া গেলে জানাবে",
                checked  = notifUpdateOn,
                onToggle = { vm.toggleNotifUpdate() }
            )

            Text(
                "বিজ্ঞপ্তির জন্য ইন্টারনেট সংযোগ প্রয়োজন। অ্যাপ বন্ধ থাকলেও প্রতিদিন একবার চেক করা হবে।",
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // ── সম্পর্কে ────────────────────────────────
            SettingsGroupHeader("অ্যাপ সম্পর্কে")

            // Website link card
            Card(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://journal.bangla.du.ac.bd/index.php/sp")))
                },
                shape  = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Language, null, tint = TealAccent,
                        modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ওয়েবসাইট", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("journal.bangla.du.ac.bd", fontSize = 12.sp, color = TealAccent)
                    }
                    Icon(Icons.Default.OpenInNew, null,
                        tint = OnDarkLow, modifier = Modifier.size(16.dp))
                }
            }

            // App version footer
            Spacer(Modifier.height(4.dp))
            Text(
                "সংস্করণ ${BuildConfig.VERSION_NAME}  •  build ${BuildConfig.VERSION_CODE}",
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize   = 12.sp,
        color      = TealAccent,
        modifier   = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = TealAccent, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked         = checked,
                onCheckedChange = { onToggle() },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor  = Color.White,
                    checkedTrackColor  = TealAccent
                )
            )
        }
    }
}
