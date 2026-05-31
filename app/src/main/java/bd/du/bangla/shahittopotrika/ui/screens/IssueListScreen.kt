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
                    // Filter issues by year range
                    val filtered = remember(state.data, selectedFilter) {
                        when (selectedFilter) {
                            "২০২০–২০২৪" -> state.data.filter { it.year.toIntOrNull() in 2020..2024 }
                            "২০১০–২০১৯" -> state.data.filter { it.year.toIntOrNull() in 2010..2019 }
                            "২০০০–২০০৯" -> state.data.filter { it.year.toIntOrNull() in 2000..2009 }
                            "১৯৫৭–১৯৯৯" -> state.data.filter {
                                it.year.toIntOrNull()?.let { y -> y in 1957..1999 } == true
                            }
                            else -> state.data
                        }
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
                                "${filtered.size}টি সংখ্যা",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = OnDarkLow,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }

                        if (filtered.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("এই বছর পরিসরে কোনো সংখ্যা নেই", color = OnDarkMed)
                                }
                            }
                        } else {
                            items(filtered) { issue ->
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

@Composable
private fun ArchiveHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            "আর্কাইভ",
            style = MaterialTheme.typography.headlineLarge,
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
