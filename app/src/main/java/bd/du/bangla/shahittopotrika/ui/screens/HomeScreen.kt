package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import bd.du.bangla.shahittopotrika.ui.components.ShimmerBox
import bd.du.bangla.shahittopotrika.ui.components.ShimmerIssueCard
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

// ── HomeScreen ────────────────────────────────────────────────────────────────

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
    onHistoryClick: () -> Unit = {},
    onAuthorsClick: () -> Unit = {}
) {
    val currentIssueState by viewModel.currentIssue.collectAsState()
    val archiveState       by viewModel.issueArchive.collectAsState()
    val isRefreshing       by viewModel.isRefreshingHome.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadIssueArchive() }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            BlinkistBottomNav(
                currentRoute     = "home",
                onHomeClick      = {},
                onExploreClick   = onSearchClick,
                onArchiveClick   = onIssueListClick,
                onBookmarksClick = onBookmarksClick,
                onAboutClick     = onAboutClick
            )
        }
    ) { paddingValuesValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = { viewModel.loadCurrentIssue(forceRefresh = true) },
            modifier     = Modifier.padding(paddingValuesValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Top icon row (brand + icons) ─────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "সাহিত্য পত্রিকা",
                            fontFamily = QayyumBookFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 22.sp,
                            color      = TealAccent
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            "বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                            fontSize   = 10.sp,
                            color      = OnDarkLow,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, "ইতিহাস", tint = OnDarkMed)
                    }
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, "সংরক্ষিত", tint = OnDarkMed)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "সেটিংস", tint = OnDarkMed)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Hero current issue ────────────────────────
                when (val state = currentIssueState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            ShimmerBox(Modifier.fillMaxSize())
                        }
                    }
                    is UiState.Error -> {
                        ErrorCard(
                            message  = state.message,
                            onRetry  = { viewModel.loadCurrentIssue() },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    is UiState.Success -> {
                        HeroIssueCard(
                            issue    = state.data,
                            onClick  = { onIssueClick(state.data) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Recent issues header ──────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "সাম্প্রতিক সংখ্যা",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnDarkHigh
                    )
                    TextButton(onClick = onIssueListClick) {
                        Text("সব দেখুন", color = TealAccent, fontSize = 12.sp)
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint     = TealAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Recent issues vertical list ─────────────────
                when (val state = archiveState) {
                    is UiState.Loading -> {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(3) {
                                ShimmerIssueCard()
                            }
                        }
                    }
                    is UiState.Success -> {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.data.take(5).forEach { issue ->
                                IssueCard(issue = issue, onClick = { onIssueClick(issue) })
                            }
                        }
                    }
                    else -> {}
                }

                Spacer(Modifier.height(28.dp))

                // ── Quick nav ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickNavCard(
                            title    = "পঠন ইতিহাস",
                            icon     = Icons.Default.History,
                            modifier = Modifier.weight(1f),
                            onClick  = onHistoryClick
                        )
                        QuickNavCard(
                            title    = "সংরক্ষিত",
                            icon     = Icons.Default.Bookmark,
                            modifier = Modifier.weight(1f),
                            onClick  = onBookmarksClick
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickNavCard(
                            title    = "লেখক তালিকা",
                            icon     = Icons.Default.People,
                            modifier = Modifier.weight(1f),
                            onClick  = onAuthorsClick
                        )
                        QuickNavCard(
                            title    = "সেটিংস",
                            icon     = Icons.Default.Settings,
                            modifier = Modifier.weight(1f),
                            onClick  = onSettingsClick
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Footer strip ──────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        "সাহিত্য পত্রিকা",
                        fontFamily = QayyumBookFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = OnDarkMed,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "ISSN: 0304-9612  •  eISSN: 2959-5827  •  প্রতিষ্ঠা: ১৯৫৭",
                        fontSize  = 11.sp,
                        color     = OnDarkLow,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── HeroIssueCard ─────────────────────────────────────────────────────────────

@Composable
fun HeroIssueCard(issue: Issue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, DarkOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (issue.coverImageUrl != null) {
                // Cover image on the left, portrait aspect ratio fully shown
                AsyncImage(
                    model              = issue.coverImageUrl,
                    contentDescription = issue.title,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .size(120.dp, 170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                )
                Spacer(Modifier.width(16.dp))
            } else {
                // Placeholder gradient
                Box(
                    modifier = Modifier
                        .size(120.dp, 170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TealAccent.copy(alpha = 0.18f),
                                    DarkSurface
                                )
                            )
                        )
                )
                Spacer(Modifier.width(16.dp))
            }

            // Details on the right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // "চলতি সংখ্যা" pill badge
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = TealAccent
                ) {
                    Text(
                        "চলতি সংখ্যা",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF003730),
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    issue.title,
                    style    = MaterialTheme.typography.titleLarge,
                    color    = OnDarkHigh,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (issue.volume.isNotBlank()) {
                        PillBadge(
                            text           = issue.volume +
                                if (issue.number.isNotBlank()) " · ${issue.number}" else "",
                            containerColor = OnDarkHigh.copy(alpha = 0.12f),
                            textColor      = OnDarkMed
                        )
                    }
                    if (issue.year.isNotBlank()) {
                        PillBadge(
                            text           = issue.year,
                            containerColor = TealAccent.copy(alpha = 0.15f),
                            textColor      = TealAccent
                        )
                    }
                }
            }
        }
    }
}

// ── RecentIssueCard (horizontal scroll item) ──────────────────────────────────

@Composable
fun RecentIssueCard(issue: Issue, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier  = Modifier.width(130.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, DarkOutline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                if (issue.coverImageUrl != null) {
                    AsyncImage(
                        model              = issue.coverImageUrl,
                        contentDescription = issue.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    // Subtle bottom gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        DarkSurface.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurface2)
                    )
                }
                // Year badge
                if (issue.year.isNotBlank()) {
                    Surface(
                        shape    = RoundedCornerShape(topEnd = 8.dp),
                        color    = TealAccent,
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            issue.year,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF003730),
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                issue.title,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = OnDarkHigh,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

// ── BlinkistBottomNav ─────────────────────────────────────────────────────────

@Composable
fun BlinkistBottomNav(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor      = TealAccent.copy(alpha = 0.15f),
        selectedIconColor   = TealAccent,
        selectedTextColor   = TealAccent,
        unselectedIconColor = OnDarkLow,
        unselectedTextColor = OnDarkLow
    )
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick  = onHomeClick,
            icon     = { Icon(Icons.Default.Home, null) },
            label    = { Text("হোম", fontSize = 9.sp, maxLines = 1, softWrap = false) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "search",
            onClick  = onExploreClick,
            icon     = { Icon(Icons.Default.Search, null) },
            label    = { Text("অনুসন্ধান", fontSize = 9.sp, maxLines = 1, softWrap = false) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "issue_list",
            onClick  = onArchiveClick,
            icon     = { Icon(Icons.Default.List, null) },
            label    = { Text("আর্কাইভ", fontSize = 9.sp, maxLines = 1, softWrap = false) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "bookmarks",
            onClick  = onBookmarksClick,
            icon     = { Icon(Icons.Default.Bookmark, null) },
            label    = { Text("সংরক্ষিত", fontSize = 9.sp, maxLines = 1, softWrap = false) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "about",
            onClick  = onAboutClick,
            icon     = { Icon(Icons.Default.Info, null) },
            label    = { Text("সম্পর্কে", fontSize = 9.sp, maxLines = 1, softWrap = false) },
            colors   = itemColors
        )
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
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            color      = textColor,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TealAccent)
        )
        Text(
            title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = OnDarkHigh
        )
    }
}

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, DarkOutline)
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            if (issue.coverImageUrl != null) {
                AsyncImage(
                    model              = issue.coverImageUrl,
                    contentDescription = issue.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(80.dp, 110.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    issue.title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                    color      = OnDarkHigh
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (issue.volume.isNotBlank()) {
                        PillBadge(
                            text           = issue.volume +
                                if (issue.number.isNotBlank()) " · ${issue.number}" else "",
                            containerColor = OnDarkHigh.copy(alpha = 0.08f),
                            textColor      = OnDarkMed
                        )
                    }
                    if (issue.year.isNotBlank()) {
                        PillBadge(
                            text           = issue.year,
                            containerColor = TealAccent.copy(alpha = 0.15f),
                            textColor      = TealAccent
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = TealAccent
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "প্রবন্ধ দেখুন",
                            color      = Color(0xFF003730),
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint     = Color(0xFF003730),
                            modifier = Modifier.size(14.dp)
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
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, DarkOutline)
    ) {
        Row(
            modifier              = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TealAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint     = TealAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = OnDarkHigh,
                maxLines   = 1
            )
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A)),
        border   = BorderStroke(1.dp, Color(0xFF3A2020))
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message,
                color     = Color(0xFFFF8A80),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("আবার চেষ্টা করুন", color = TealAccent)
            }
        }
    }
}
