package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    
    val isLoggedIn          by vm.isUserLoggedIn.collectAsState()
    val userName            by vm.userName.collectAsState()
    val userEmail           by vm.userEmail.collectAsState()
    val userPhotoUrl        by vm.userPhotoUrl.collectAsState()

    val context = LocalContext.current
    var showDemoDialog by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                val name = account.displayName ?: "ইউজার"
                val email = account.email ?: ""
                val photo = account.photoUrl?.toString() ?: ""
                vm.loginUser(name, email, photo)
            } else {
                showDemoDialog = true
            }
        } catch (e: Exception) {
            showDemoDialog = true
        }
    }

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
        if (showDemoDialog) {
            AlertDialog(
                onDismissRequest = { showDemoDialog = false },
                title = { Text("কনফিগারেশন অনুপস্থিত", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnDarkHigh) },
                text = { Text("গুগল প্লে সার্ভিস বা ডেভেলপার ক্লায়েন্ট আইডি কনফিগার করা নেই। আপনি কি ডেমো অ্যাকাউন্ট দিয়ে সাইন-ইন করতে চান?", color = OnDarkMed) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.loginUser("বাংলা গবেষক", "researcher@du.ac.bd", "")
                            showDemoDialog = false
                        }
                    ) {
                        Text("হ্যাঁ", color = TealAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDemoDialog = false }) {
                        Text("বাতিল", color = OnDarkLow)
                    }
                },
                containerColor = DarkSurface
            )
        }

        Column(
            modifier = Modifier
                .padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── ইউজার অ্যাকাউন্ট ──────────────────────────────
            SettingsGroupHeader("ইউজার অ্যাকাউন্ট")

            if (isLoggedIn) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (userPhotoUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = userPhotoUrl,
                                    contentDescription = "প্রোফাইল ছবি",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(DarkSurface, CircleShape)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                val initials = getInitials(userName)
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(TealAccent, TealAccent.copy(alpha = 0.7f))
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = OnDarkHigh
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = OnDarkMed
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                googleSignInClient.signOut()
                                vm.logoutUser()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("লগআউট", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "আপনার পছন্দসমূহ সিঙ্ক করতে এবং মন্তব্য করতে লগইন করুন",
                            fontSize = 13.sp,
                            color = OnDarkMed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                launcher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("গুগল অ্যাকাউন্ট দিয়ে লগইন করুন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── চেহারা ───────────────────────────────────
            SettingsGroupHeader("চেহারা")

            val currentThemeMode by vm.themeMode.collectAsState()

            Card(
                shape  = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Palette, null, tint = TealAccent, modifier = Modifier.size(22.dp))
                        Text("অ্যাপ থিম", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeOptionButton(
                            name = "দিন",
                            themeCode = "LIGHT",
                            isSelected = currentThemeMode == "LIGHT",
                            colorBg = Color(0xFFEEF2F6),
                            colorBorder = Color(0xFF2563EB),
                            textColor = Color(0xFF102334),
                            onClick = { vm.setThemeMode("LIGHT") }
                        )
                        ThemeOptionButton(
                            name = "রাত",
                            themeCode = "DARK",
                            isSelected = currentThemeMode == "DARK",
                            colorBg = Color(0xFF0D1F2D),
                            colorBorder = Color(0xFF00D4B1),
                            textColor = Color.White,
                            onClick = { vm.setThemeMode("DARK") }
                        )
                        ThemeOptionButton(
                            name = "সেপিয়া",
                            themeCode = "SEPIA",
                            isSelected = currentThemeMode == "SEPIA",
                            colorBg = Color(0xFFF4ECD8),
                            colorBorder = Color(0xFF8B4513),
                            textColor = Color(0xFF3C2C1E),
                            onClick = { vm.setThemeMode("SEPIA") }
                        )
                        ThemeOptionButton(
                            name = "ব্ল্যাক",
                            themeCode = "OLED",
                            isSelected = currentThemeMode == "OLED",
                            colorBg = Color(0xFF000000),
                            colorBorder = Color(0xFF4ADE80),
                            textColor = Color.White,
                            onClick = { vm.setThemeMode("OLED") }
                        )
                    }
                }
            }

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

private fun getInitials(name: String): String {
    val cleanName = name.replace(Regex("[।.,]"), " ").trim()
    val parts = cleanName.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts[0].take(1)
    return parts[0].take(1) + parts[1].take(1)
}

@Composable
fun ThemeOptionButton(
    name: String,
    themeCode: String,
    isSelected: Boolean,
    colorBg: Color,
    colorBorder: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(colorBg, CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) colorBorder else Color.Gray.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorBorder,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = OnDarkHigh
        )
    }
}
