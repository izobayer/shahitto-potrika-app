package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import bd.du.bangla.shahittopotrika.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    journalVm: JournalViewModel,
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit
) {
    val query   by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("অনুসন্ধান") },
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
                .fillMaxSize()
        ) {
            // ── Search bar ─────────────────────────────────
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("নিবন্ধ খুঁজুন…") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { viewModel.clear() }) {
                            Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions  = KeyboardActions(onSearch = { viewModel.search() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    cursorColor = Navy
                )
            )

            Button(
                onClick = { viewModel.search() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = query.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Icon(Icons.Default.Search, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("অনুসন্ধান করুন")
            }

            Spacer(Modifier.height(8.dp))

            // ── Results ────────────────────────────────────
            when (val state = results) {
                null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "নিবন্ধ খুঁজতে উপরের বাক্সে টাইপ করুন",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy)
                    }
                }
                is UiState.Error -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCard(
                            message = state.message,
                            onRetry = { viewModel.search() }
                        )
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("কোনো ফলাফল পাওয়া যায়নি")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "${state.data.size}টি ফলাফল পাওয়া গেছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(state.data) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onArticleClick(article) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
