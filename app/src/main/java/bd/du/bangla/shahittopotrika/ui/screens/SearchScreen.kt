package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.components.ShimmerBox
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import bd.du.bangla.shahittopotrika.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    journalVm: JournalViewModel,
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onIssueListClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val query        by viewModel.query.collectAsState()
    val results      by viewModel.results.collectAsState()
    val archiveState by journalVm.issueArchive.collectAsState()

    // Category chip state
    val categories = listOf("প্রবন্ধ", "কবিতা", "গল্প", "নাটক", "গবেষণা", "সমালোচনা", "অনুবাদ", "উপন্যাস")
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { journalVm.loadIssueArchive() }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            BlinkistBottomNav(
                currentRoute     = "search",
                onHomeClick      = onHomeClick,
                onExploreClick   = {},
                onArchiveClick   = onIssueListClick,
                onBookmarksClick = onBookmarksClick,
                onAboutClick     = onAboutClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Big heading ─────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        "এক্সপ্লোর",
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

            // ── Pill search bar ──────────────────────────────
            item {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    placeholder   = { Text("প্রবন্ধ, লেখক বা বিষয় খুঁজুন…", color = OnDarkLow) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(50.dp),
                    leadingIcon   = {
                        Icon(Icons.Default.Search, null, tint = OnDarkMed)
                    },
                    trailingIcon  = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.clear() }) {
                                Icon(Icons.Default.Clear, "মুছুন", tint = OnDarkMed)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = TealAccent,
                        unfocusedBorderColor = DarkOutline,
                        cursorColor          = TealAccent,
                        focusedTextColor     = OnDarkHigh,
                        unfocusedTextColor   = OnDarkHigh,
                        focusedContainerColor   = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick  = { viewModel.search() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape   = RoundedCornerShape(50.dp),
                    enabled = query.isNotBlank(),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor         = TealAccent,
                        contentColor           = Color(0xFF003730),
                        disabledContainerColor = DarkOutline,
                        disabledContentColor   = OnDarkLow
                    )
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("অনুসন্ধান করুন", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Category chips ───────────────────────────────
            if (results == null) {
                item {
                    Text(
                        "বিভাগ",
                        style    = MaterialTheme.typography.titleSmall,
                        color    = OnDarkMed,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick  = {
                                    selectedCategory = if (isSelected) null else cat
                                    if (!isSelected) {
                                        viewModel.onQueryChange(cat)
                                        viewModel.search()
                                    }
                                },
                                label    = { Text(cat, fontSize = 12.sp) },
                                shape    = RoundedCornerShape(50.dp),
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor         = DarkSurface,
                                    labelColor             = OnDarkMed,
                                    selectedContainerColor = TealAccent.copy(alpha = 0.15f),
                                    selectedLabelColor     = TealAccent
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled          = true,
                                    selected         = isSelected,
                                    borderColor      = DarkOutline,
                                    selectedBorderColor = TealAccent.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Recent issues horizontal strip ──────────
                item {
                    Text(
                        "সংখ্যাসমূহ",
                        style    = MaterialTheme.typography.titleSmall,
                        color    = OnDarkMed,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    when (val state = archiveState) {
                        is UiState.Loading -> {
                            LazyRow(
                                contentPadding        = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(4) {
                                    Box(
                                        Modifier
                                            .size(110.dp, 140.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        ShimmerBox(Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                        is UiState.Success -> {
                            LazyRow(
                                contentPadding        = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.data.take(8)) { issue ->
                                    RecentIssueCard(
                                        issue   = issue,
                                        onClick = { /* navigate to issue articles */ }
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── Empty state hint ─────────────────────────
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "ওপরের অনুসন্ধান বাক্সে লিখে প্রবন্ধ খুঁজুন",
                            color    = OnDarkLow,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Search results ───────────────────────────────
            when (val state = results) {
                is UiState.Loading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TealAccent)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorCard(
                            message  = state.message,
                            onRetry  = { viewModel.search() },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    item {
                        Text(
                            "${state.data.size}টি ফলাফল",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = OnDarkMed,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    if (state.data.isEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "প্রবন্ধ/সংখ্যা লোড হচ্ছে। অনুগ্রহ করে অপেক্ষা করুন।",
                                    color = OnDarkMed,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(state.data) { article ->
                            ArticleCard(
                                article  = article,
                                onClick  = { onArticleClick(article) },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                null -> {}
            }
        }
    }
}
