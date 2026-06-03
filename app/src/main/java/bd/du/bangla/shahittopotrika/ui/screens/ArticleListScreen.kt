package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.components.ShimmerArticleCard
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    issueUrl: String,
    viewModel: JournalViewModel,
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit
) {
    val articlesState by viewModel.articles.collectAsState()
    val isRefreshing  by viewModel.isRefreshingArticles.collectAsState()

    LaunchedEffect(issueUrl) { viewModel.loadArticlesForIssue(issueUrl) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("প্রবন্ধসমূহ", fontSize = 15.sp, color = OnDarkHigh) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = OnDarkHigh
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = DarkSurface,
                    titleContentColor = OnDarkHigh,
                    navigationIconContentColor = OnDarkHigh
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = { viewModel.loadArticlesForIssue(issueUrl, forceRefresh = true) },
            modifier     = Modifier
                .padding(paddingValues)
                .background(DarkBg)
        ) {
            when (val state = articlesState) {
                is UiState.Loading -> {
                    LazyColumn(
                        modifier            = Modifier.background(DarkBg),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(6) { ShimmerArticleCard() }
                    }
                }
                is UiState.Error -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(DarkBg)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCard(
                            message = state.message,
                            onRetry = { viewModel.loadArticlesForIssue(issueUrl) }
                        )
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(DarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "প্রবন্ধ/সংখ্যা লোড হচ্ছে। অনুগ্রহ করে অপেক্ষা করুন।",
                                color = OnDarkMed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier            = Modifier.background(DarkBg),
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                article.title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                maxLines   = 3,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 21.sp,
                color      = OnDarkHigh
            )
            if (article.authors.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    article.authors,
                    fontSize   = 12.sp,
                    color      = AccentBlue,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
            if (article.abstract.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    article.abstract,
                    fontSize   = 12.sp,
                    color      = OnDarkMed,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "বিস্তারিত পড়ুন →",
                    color      = TealAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 12.sp
                )
                if (article.pdfUrl != null) {
                    PillBadge(
                        text           = "PDF",
                        containerColor = TealAccent.copy(alpha = 0.15f),
                        textColor      = TealAccent
                    )
                }
            }
        }
    }
}
