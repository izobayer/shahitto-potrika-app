package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

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
                title = { Text("সম্পর্কে") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            // ── Header card ────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = infoState) {
                        is UiState.Success -> {
                            if (state.data.logoUrl != null) {
                                AsyncImage(
                                    model = state.data.logoUrl,
                                    contentDescription = "লোগো",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            Text(
                                text = state.data.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.data.publisher,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(
                                text = "সাহিত্য পত্রিকা",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Info rows ──────────────────────────────────
            when (val state = infoState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(color = Navy)
                }
                is UiState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadJournalInfo() }
                    )
                }
                is UiState.Success -> {
                    val info = state.data

                    InfoCard {
                        InfoRow(label = "ISSN (Print)", value = info.issn)
                        InfoRow(label = "ISSN (Online)", value = info.eIssn)
                        InfoRow(label = "প্রকাশক", value = info.publisher)
                    }

                    if (info.description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "পরিচিতি",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Navy,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = info.description,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Website button ─────────────────────────────
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://journal.bangla.du.ac.bd/index.php/sp"))
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Icon(Icons.Default.Language, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("ওয়েবসাইট দেখুন")
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "© ১৯৫৭-২০২৬ সাহিত্য পত্রিকা\nবাংলা বিভাগ • ঢাকা বিশ্ববিদ্যালয়",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
