package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: JournalViewModel,
    onIssueClick: (Issue) -> Unit,
    onBack: () -> Unit
) {
    val archiveState by viewModel.issueArchive.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadIssueArchive()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সংখ্যাসমূহ") },
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
        when (val state = archiveState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Navy)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadIssueArchive() }
                    )
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("কোনো সংখ্যা পাওয়া যায়নি")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data) { issue ->
                            IssueCard(
                                issue = issue,
                                onClick = { onIssueClick(issue) }
                            )
                        }
                    }
                }
            }
        }
    }
}
