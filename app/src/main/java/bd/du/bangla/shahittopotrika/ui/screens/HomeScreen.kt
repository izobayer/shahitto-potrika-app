package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onIssueClick: (Issue) -> Unit,
    onSearchClick: () -> Unit,
    onIssueListClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val currentIssueState by viewModel.currentIssue.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "সাহিত্য পত্রিকা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "বাংলা বিভাগ • ঢাকা বিশ্ববিদ্যালয়",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "অনুসন্ধান",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("হোম") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onIssueListClick,
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("সংখ্যাসমূহ") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSearchClick,
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("অনুসন্ধান") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onAboutClick,
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("সম্পর্কে") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            // ── Hero banner ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Navy, Navy.copy(alpha = 0.85f))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "সাহিত্য পত্রিকা",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ISSN: 0304-9612 • eISSN: 2959-5827",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Current issue ──────────────────────────────
            Text(
                text = "চলতি সংখ্যা",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val state = currentIssueState) {
                is UiState.Loading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Navy)
                    }
                }
                is UiState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadCurrentIssue() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is UiState.Success -> {
                    IssueCard(
                        issue = state.data,
                        onClick = { onIssueClick(state.data) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Quick nav ──────────────────────────────────
            Text(
                text = "দ্রুত নেভিগেশন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    title = "সংখ্যা আর্কাইভ",
                    subtitle = "সকল সংখ্যা দেখুন",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f),
                    onClick = onIssueListClick
                )
                QuickNavCard(
                    title = "সম্পর্কে",
                    subtitle = "পত্রিকার তথ্য",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f),
                    onClick = onAboutClick
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (issue.coverImageUrl != null) {
                AsyncImage(
                    model = issue.coverImageUrl,
                    contentDescription = issue.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp, 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (issue.volume.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = issue.volume + if (issue.number.isNotBlank()) ", ${issue.number}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (issue.year.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = issue.year,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "নিবন্ধ পড়ুন →",
                    color = Navy,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Navy, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("আবার চেষ্টা করুন")
            }
        }
    }
}
