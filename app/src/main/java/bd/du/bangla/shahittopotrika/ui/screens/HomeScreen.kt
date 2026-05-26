package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import bd.du.bangla.shahittopotrika.ui.components.ShimmerIssueCard
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
    onAboutClick: () -> Unit,
    onBookmarksClick: () -> Unit
) {
    val currentIssueState by viewModel.currentIssue.collectAsState()
    val isRefreshing      by viewModel.isRefreshingHome.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সাহিত্য পত্রিকা", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("বাংলা বিভাগ • ঢাকা বিশ্ববিদ্যালয়",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
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
                NavigationBarItem(selected = true,  onClick = { },
                    icon = { Icon(Icons.Default.List, null) }, label = { Text("হোম") })
                NavigationBarItem(selected = false, onClick = onIssueListClick,
                    icon = { Icon(Icons.Default.List, null) }, label = { Text("সংখ্যাসমূহ") })
                NavigationBarItem(selected = false, onClick = onSearchClick,
                    icon = { Icon(Icons.Default.Search, null) }, label = { Text("অনুসন্ধান") })
                NavigationBarItem(selected = false, onClick = onAboutClick,
                    icon = { Icon(Icons.Default.Info, null) }, label = { Text("সম্পর্কে") })
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = { viewModel.loadCurrentIssue(forceRefresh = true) },
            modifier     = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // ── Hero ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Navy, Navy.copy(alpha = 0.85f))))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("সাহিত্য পত্রিকা",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 28.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("ISSN: 0304-9612 • eISSN: 2959-5827",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.70f),
                            fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("চলতি সংখ্যা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                when (val state = currentIssueState) {
                    is UiState.Loading -> {
                        Box(Modifier.padding(horizontal = 16.dp)) { ShimmerIssueCard() }
                    }
                    is UiState.Error -> {
                        ErrorCard(state.message,
                            onRetry = { viewModel.loadCurrentIssue() },
                            modifier = Modifier.padding(16.dp))
                    }
                    is UiState.Success -> {
                        IssueCard(issue = state.data,
                            onClick = { onIssueClick(state.data) },
                            modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("দ্রুত নেভিগেশন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickNavCard("সংখ্যা আর্কাইভ", "সকল সংখ্যা দেখুন",
                        Icons.Default.List, Modifier.weight(1f), onIssueListClick)
                    QuickNavCard("Bookmarks", "সংরক্ষিত নিবন্ধ",
                        Icons.Default.Bookmark, Modifier.weight(1f), onBookmarksClick)
                    QuickNavCard("সম্পর্কে", "পত্রিকার তথ্য",
                        Icons.Default.Info, Modifier.weight(1f), onAboutClick)
                }

                Spacer(Modifier.height(24.dp))
            }
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
                    modifier = Modifier.size(90.dp, 120.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(issue.title, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (issue.volume.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(issue.volume + if (issue.number.isNotBlank()) ", ${issue.number}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (issue.year.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(issue.year, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Text("নিবন্ধ পড়ুন →", color = Navy,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun QuickNavCard(
    title: String, subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Navy, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("আবার চেষ্টা করুন") }
        }
    }
}
