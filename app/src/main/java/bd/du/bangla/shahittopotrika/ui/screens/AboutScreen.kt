package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.R
import bd.du.bangla.shahittopotrika.data.model.UiState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import bd.du.bangla.shahittopotrika.ui.theme.DarkSurface
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkHigh
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkMed
import bd.du.bangla.shahittopotrika.ui.theme.QayyumBookFamily
import bd.du.bangla.shahittopotrika.ui.theme.TealAccent
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val infoState by viewModel.journalInfo.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সম্পর্কে", fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = OnDarkHigh
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Journal info ───────────────────────────────
            when (val state = infoState) {
                is UiState.Loading -> CircularProgressIndicator(color = TealAccent)
                is UiState.Error -> ErrorCard(state.message,
                    onRetry = { viewModel.loadJournalInfo() })
                is UiState.Success -> {
                    val info = state.data
                    InfoCard {
                        InfoRow(
                            label           = "পত্রিকার নাম",
                            value           = info.name,
                            valueFontFamily = QayyumBookFamily,
                            valueFontSize   = 15.sp
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("ISSN (মুদ্রণ)", info.issn)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("ISSN (অনলাইন)", info.eIssn)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("প্রকাশক", info.publisher)
                    }

                    if (info.description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("পরিচিতি", fontWeight = FontWeight.SemiBold,
                                    color = TealAccent, fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(info.description, fontSize = 14.sp, lineHeight = 22.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Website button ─────────────────────────────
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://journal.bangla.du.ac.bd/index.php/sp")))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ওয়েবসাইট দেখুন")
            }

            Spacer(Modifier.height(24.dp))

            // ── Developer section ──────────────────────────
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TealAccent.copy(alpha = 0.06f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealAccent,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Code, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column {
                        Text(
                            stringResource(R.string.developer_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.developer_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TealAccent
                        )
                        Text(
                            "অ্যান্ড্রয়েড অ্যাপ্লিকেশন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = androidx.compose.ui.Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:zobayer@du.ac.bd")
                                })
                            }
                        ) {
                            Icon(Icons.Default.Email, null,
                                tint = TealAccent, modifier = androidx.compose.ui.Modifier.size(14.dp))
                            Text(
                                "zobayer@du.ac.bd",
                                fontSize = 12.sp,
                                color = TealAccent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    append("© ১৯৫৭–২০২৬ ")
                    withStyle(
                        SpanStyle(
                            fontFamily = QayyumBookFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp
                        )
                    ) {
                        append("সাহিত্য পত্রিকা")
                    }
                    append("\nবাংলা বিভাগ • ঢাকা বিশ্ববিদ্যালয়\nসর্বস্বত্ব সংরক্ষিত")
                },
                fontSize  = 11.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueFontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize   = valueFontSize,
            fontWeight = FontWeight.Medium,
            fontFamily = valueFontFamily,
            modifier   = Modifier.weight(1.2f),
            textAlign  = TextAlign.End,
            color      = TealAccent
        )
    }
}
