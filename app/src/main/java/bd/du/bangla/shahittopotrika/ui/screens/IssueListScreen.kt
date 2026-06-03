package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.components.ShimmerIssueCard
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: JournalViewModel,
    onIssueClick: (Issue) -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onSearchClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val archiveState by viewModel.issueArchive.collectAsState()
    val isRefreshing by viewModel.isRefreshingArchive.collectAsState()

    // Year filter
    val yearFilters = listOf("সব বছর", "২০২০–২০২৪", "২০১০–২০১৯", "২০০০–২০০৯", "১৯৫৭–১৯৯৯")
    var selectedFilter by remember { mutableStateOf("সব বছর") }

    LaunchedEffect(Unit) { viewModel.loadIssueArchive(forceRefresh = true) }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            BlinkistBottomNav(
                currentRoute     = "issue_list",
                onHomeClick      = onHomeClick,
                onExploreClick   = onSearchClick,
                onArchiveClick   = {},
                onBookmarksClick = onBookmarksClick,
                onAboutClick     = onAboutClick
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = { viewModel.loadIssueArchive(forceRefresh = true) },
            modifier     = Modifier.padding(paddingValues)
        ) {
            when (val state = archiveState) {
                is UiState.Loading -> {
                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxSize()
                            .background(DarkBg),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { ArchiveHeader() }
                        items(5) {
                            ShimmerIssueCard()
                        }
                    }
                }
                is UiState.Error -> {
                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxSize()
                            .background(DarkBg),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item { ArchiveHeader() }
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ErrorCard(
                                    message = state.message,
                                    onRetry = { viewModel.loadIssueArchive(forceRefresh = true) }
                                )
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    // Group issues by year range
                    val groupedIssues = remember(state.data) {
                        val g20_24 = state.data.filter { it.year.toIntOrNull() in 2020..2024 }
                        val g10_19 = state.data.filter { it.year.toIntOrNull() in 2010..2019 }
                        val g00_09 = state.data.filter { it.year.toIntOrNull() in 2000..2009 }
                        val g57_99 = state.data.filter { it.year.toIntOrNull() in 1957..1999 }
                        val others = state.data.filter {
                            val y = it.year.toIntOrNull()
                            y == null || (y !in 2020..2024 && y !in 2010..2019 && y !in 2000..2009 && y !in 1957..1999)
                        }

                        listOf(
                            GroupData("২০২০–২০২৪", g20_24),
                            GroupData("২০১০–২০১৯", g10_19),
                            GroupData("২০০০–২০০৯", g00_09),
                            GroupData("১৯৫৭–১৯৯৯", g57_99),
                            GroupData("অন্যান্য বছর", others)
                        ).filter { it.issues.isNotEmpty() }
                    }

                    val displayGroups = remember(groupedIssues, selectedFilter) {
                        if (selectedFilter == "সব বছর") {
                            groupedIssues
                        } else {
                            groupedIssues.filter { it.title == selectedFilter }
                        }
                    }

                    val totalCount = remember(displayGroups) {
                        displayGroups.sumOf { it.issues.size }
                    }

                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxSize()
                            .background(DarkBg),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header + year filters
                        item {
                            ArchiveHeader()
                            Spacer(Modifier.height(12.dp))
                            // Year filter chips
                            LazyRow(
                                contentPadding        = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(yearFilters) { filter ->
                                    val isSelected = selectedFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick  = { selectedFilter = filter },
                                        label    = { Text(filter, fontSize = 12.sp) },
                                        shape    = RoundedCornerShape(50.dp),
                                        colors   = FilterChipDefaults.filterChipColors(
                                            containerColor         = DarkSurface,
                                            labelColor             = OnDarkMed,
                                            selectedContainerColor = TealAccent.copy(alpha = 0.15f),
                                            selectedLabelColor     = TealAccent
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled             = true,
                                            selected            = isSelected,
                                            borderColor         = DarkOutline,
                                            selectedBorderColor = TealAccent.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${totalCount}টি সংখ্যা",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = OnDarkLow,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }

                        if (displayGroups.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("এই বছর পরিসরে কোনো সংখ্যা নেই", color = OnDarkMed)
                                }
                            }
                        } else {
                            displayGroups.forEach { group ->
                                item(key = group.title) {
                                    GroupHeader(title = group.title, count = group.issues.size)
                                }
                                items(group.issues, key = { it.id }) { issue ->
                                    IssueCard(
                                        issue    = issue,
                                        onClick  = { onIssueClick(issue) },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class GroupData(val title: String, val issues: List<Issue>)

@Composable
private fun GroupHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TealAccent)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnDarkHigh
            )
        }
        Text(
            text = "${count}টি সংখ্যা",
            style = MaterialTheme.typography.bodySmall,
            color = OnDarkLow
        )
    }
}

@Composable
private fun ArchiveHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            "আর্কাইভ",
            style = MaterialTheme.typography.headlineMedium,
            color = OnDarkHigh
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TealAccent)
        )
    }
}
