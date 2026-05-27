package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.R
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.components.ShimmerIssueCard
import bd.du.bangla.shahittopotrika.ui.theme.HeaderBg
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

private const val LOGO_URL =
    "https://journal.bangla.du.ac.bd/public/journals/1/pageHeaderLogoImage_en.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onIssueClick: (Issue) -> Unit,
    onSearchClick: () -> Unit,
    onIssueListClick: () -> Unit,
    onAboutClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val currentIssueState by viewModel.currentIssue.collectAsState()
    val isRefreshing      by viewModel.isRefreshingHome.collectAsState()

    Scaffold(
        // ── Top bar: website-style header (light bg + navy strip) ──
        topBar = {
            Column {
                // Site header — light background like the website
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = LOGO_URL,
                            contentDescription = "সাহিত্য পত্রিকা লোগো",
                            modifier = Modifier.height(48.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        Column {
                            Text(
                                "সাহিত্য পত্রিকা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Navy
                            )
                            Text(
                                "বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                                fontSize = 11.sp,
                                color = Navy.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                // Navy nav strip — like website's navigation bar
                TopAppBar(
                    title = { Text("হোম", fontSize = 14.sp) },
                    actions = {
                        IconButton(onClick = onHistoryClick) {
                            Icon(Icons.Default.History, "পঠন ইতিহাস",
                                tint = Color.White)
                        }
                        IconButton(onClick = onBookmarksClick) {
                            Icon(Icons.Default.Bookmark, "Bookmarks",
                                tint = Color.White)
                        }
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, "অনুসন্ধান",
                                tint = Color.White)
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, "সেটিংস",
                                tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy,
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier.height(48.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = true, onClick = { },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("হোম", fontSize = 11.sp) })
                NavigationBarItem(selected = false, onClick = onIssueListClick,
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("সংখ্যা", fontSize = 11.sp) })
                NavigationBarItem(selected = false, onClick = onSearchClick,
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("খুঁজুন", fontSize = 11.sp) })
                NavigationBarItem(selected = false, onClick = onBookmarksClick,
                    icon = { Icon(Icons.Default.Bookmark, null) },
                    label = { Text("সংরক্ষিত", fontSize = 11.sp) })
                NavigationBarItem(selected = false, onClick = onAboutClick,
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("সম্পর্কে", fontSize = 11.sp) })
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
                // ── ISSN info strip ──────────────────────────
                Surface(
                    color = Navy.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "ISSN: 0304-9612  •  eISSN: 2959-5827  •  প্রতিষ্ঠা: ১৯৫৭",
                        fontSize = 11.sp,
                        color = Navy.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Current issue ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "চলতি সংখ্যা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    TextButton(onClick = onIssueListClick) {
                        Text("সকল সংখ্যা →", fontSize = 12.sp, color = Navy)
                    }
                }

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
                        IssueCard(
                            issue = state.data,
                            onClick = { onIssueClick(state.data) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Quick nav ────────────────────────────────
                Text(
                    "দ্রুত নেভিগেশন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickNavCard("আর্কাইভ", "সকল সংখ্যা",
                        Icons.Default.List, Modifier.weight(1f), onIssueListClick)
                    QuickNavCard("সংরক্ষিত", "Bookmarks",
                        Icons.Default.Bookmark, Modifier.weight(1f), onBookmarksClick)
                    QuickNavCard("ইতিহাস", "পড়েছি",
                        Icons.Default.History, Modifier.weight(1f), onHistoryClick)
                    QuickNavCard("সেটিংস", "পছন্দ",
                        Icons.Default.Settings, Modifier.weight(1f), onSettingsClick)
                }

                Spacer(Modifier.height(16.dp))

                // ── Footer strip ─────────────────────────────
                Surface(
                    color = Navy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "© ১৯৫৭–২০২৬  সাহিত্য পত্রিকা  •  বাংলা বিভাগ  •  ঢাকা বিশ্ববিদ্যালয়",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// ── Shared composables ────────────────────────────────────

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (issue.coverImageUrl != null) {
                AsyncImage(
                    model = issue.coverImageUrl,
                    contentDescription = issue.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp, 110.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(issue.title, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp, color = Navy)
                if (issue.volume.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        issue.volume + if (issue.number.isNotBlank()) ", ${issue.number}" else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (issue.year.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(issue.year, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Navy
                ) {
                    Text(
                        "প্রবন্ধ দেখুন →",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Navy, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                textAlign = TextAlign.Center, color = Navy)
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
