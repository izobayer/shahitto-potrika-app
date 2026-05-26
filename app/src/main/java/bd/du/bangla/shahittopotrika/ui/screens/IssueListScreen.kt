package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun IssueListScreen(
    viewModel: JournalViewModel,
    onIssueClick: (Issue) -> Unit,
    onBack: () -> Unit
) {
    val archiveState by viewModel.issueArchive.collectAsState()
    val isRefreshing by viewModel.isRefreshingArchive.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadIssueArchive(forceRefresh = true) }

    Scaffold(
        topBar = {
            Column {
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
                            contentDescription = "লোগো",
                            modifier = Modifier.height(48.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        Column {
                            Text("সাহিত্য পত্রিকা",
                                fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
                            Text("বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                                fontSize = 11.sp, color = Navy.copy(alpha = 0.7f))
                        }
                    }
                }
                TopAppBar(
                    title = { Text("সংখ্যাসমূহ", fontSize = 14.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) { items(5) { ShimmerIssueCard() } }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center) {
                        ErrorCard(state.message,
                            onRetry = { viewModel.loadIssueArchive(forceRefresh = true) })
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("কোনো সংখ্যা পাওয়া যায়নি")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data) { issue ->
                                IssueCard(issue = issue, onClick = { onIssueClick(issue) })
                            }
                        }
                    }
                }
            }
        }
    }
}
