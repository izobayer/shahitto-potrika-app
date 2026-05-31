package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val currentIssueState by viewModel.currentIssue.collectAsState()
    val isRefreshing      by viewModel.isRefreshingHome.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            "সাহিত্য পত্রিকা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            "বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, "পঠন ইতিহাস", tint = Color.White)
                    }
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, "Bookmarks", tint = Color.White)
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "অনুসন্ধান", tint = Color.White)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "সেটিংস", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            val itemColors = NavigationBarItemDefaults.colors(
                indicatorColor        = Navy.copy(alpha = 0.12f),
                selectedIconColor     = Navy,
                selectedTextColor     = Navy,
                unselectedIconColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor   = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true, onClick = { },
                    icon    = { Icon(Icons.Default.Home, null) },
                    label   = { Text("হোম", fontSize = 11.sp) },
                    colors  = itemColors
                )
                NavigationBarItem(
                    selected = false, onClick = onIssueListClick,
                    icon    = { Icon(Icons.Default.List, null) },
                    label   = { Text("সংখ্যা", fontSize = 11.sp) },
                    colors  = itemColors
                )
                NavigationBarItem(
                    selected = false, onClick = onSearchClick,
                    icon    = { Icon(Icons.Default.Search, null) },
                    label   = { Text("খুঁজুন", fontSize = 11.sp) },
                    colors  = itemColors
                )
                NavigationBarItem(
                    selected = false, onClick = onBookmarksClick,
                    icon    = { Icon(Icons.Default.Bookmark, null) },
                    label   = { Text("সংরক্ষিত", fontSize = 11.sp) },
                    colors  = itemColors
                )
                NavigationBarItem(
                    selected = false, onClick = onAboutClick,
                    icon    = { Icon(Icons.Default.Info, null) },
                    label   = { Text("সম্পর্কে", fontSize = 11.sp) },
                    colors  = itemColors
                )
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

                Spacer(Modifier.height(20.dp))

                // ── Current issue ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader("চলতি সংখ্যা")
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

                Spacer(Modifier.height(24.dp))

                // ── Quick nav ────────────────────────────────
                SectionHeader(
                    "দ্রুত নেভিগেশন",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickNavCard(
                        title   = "আর্কাইভ",
                        icon    = Icons.Default.List,
                        modifier = Modifier.weight(1f),
                        onClick  = onIssueListClick
                    )
                    QuickNavCard(
                        title   = "সংরক্ষিত",
                        icon    = Icons.Default.Bookmark,
                        modifier = Modifier.weight(1f),
                        onClick  = onBookmarksClick
                    )
                    QuickNavCard(
                        title   = "ইতিহাস",
                        icon    = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick  = onHistoryClick
                    )
                    QuickNavCard(
                        title   = "সেটিংস",
                        icon    = Icons.Default.Settings,
                        modifier = Modifier.weight(1f),
                        onClick  = onSettingsClick
                    )
                }

                Spacer(Modifier.height(24.dp))

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

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
fun PillBadge(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(50.dp),
        color    = containerColor,
        modifier = modifier
    ) {
        Text(
            text,
            fontSize     = 11.sp,
            fontWeight   = FontWeight.Medium,
            color        = textColor,
            modifier     = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier,
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Navy)
        )
        Text(
            title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Navy
        )
    }
}

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            if (issue.coverImageUrl != null) {
                AsyncImage(
                    model            = issue.coverImageUrl,
                    contentDescription = issue.title,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier
                        .size(80.dp, 110.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    issue.title,
                    fontWeight  = FontWeight.Bold,
                    fontSize    = 15.sp,
                    maxLines    = 2,
                    overflow    = TextOverflow.Ellipsis,
                    lineHeight  = 22.sp,
                    color       = Navy
                )
                Spacer(Modifier.height(8.dp))
                // Volume / Year badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (issue.volume.isNotBlank()) {
                        PillBadge(
                            text           = issue.volume + if (issue.number.isNotBlank()) " · ${issue.number}" else "",
                            containerColor = Navy.copy(alpha = 0.08f),
                            textColor      = Navy
                        )
                    }
                    if (issue.year.isNotBlank()) {
                        PillBadge(
                            text           = issue.year,
                            containerColor = Color(0xFFFFF3E0),
                            textColor      = Color(0xFFE65100)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                // Pill action button
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Navy
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "প্রবন্ধ দেখুন",
                            color      = Color.White,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNavCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier              = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier          = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Navy.copy(alpha = 0.08f)),
                contentAlignment  = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = Navy,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Text(
                title,
                fontWeight  = FontWeight.SemiBold,
                fontSize    = 11.sp,
                textAlign   = TextAlign.Center,
                color       = Navy,
                maxLines    = 1
            )
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message,
                color     = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("আবার চেষ্টা করুন") }
        }
    }
}
