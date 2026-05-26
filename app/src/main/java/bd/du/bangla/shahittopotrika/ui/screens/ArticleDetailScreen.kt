package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.HeaderBg
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

private const val LOGO_URL =
    "https://journal.bangla.du.ac.bd/public/journals/1/pageHeaderLogoImage_en.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleUrl: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val articleState by viewModel.articleDetail.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(articleUrl) { viewModel.loadArticleDetail(articleUrl) }

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
                    title = { Text("নিবন্ধ", maxLines = 1, fontSize = 14.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                                tint = Color.White)
                        }
                    },
                    actions = {
                        if (articleState is UiState.Success) {
                            val article = (articleState as UiState.Success).data
                            IconButton(onClick = { viewModel.toggleBookmark(article) }) {
                                Icon(
                                    if (isBookmarked) Icons.Default.Bookmark
                                    else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isBookmarked) "Bookmark সরান" else "Bookmark করুন",
                                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.White
                                )
                            }
                            IconButton(onClick = {
                                context.startActivity(Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
                                    }, "শেয়ার করুন"
                                ))
                            }) {
                                Icon(Icons.Default.Share, "শেয়ার", tint = Color.White)
                            }
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
        when (val state = articleState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy)
                        Spacer(Modifier.height(12.dp))
                        Text("নিবন্ধ লোড হচ্ছে…")
                    }
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center) {
                    ErrorCard(state.message, onRetry = { viewModel.loadArticleDetail(articleUrl) })
                }
            }
            is UiState.Success -> {
                val article = state.data
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(article.title, fontWeight = FontWeight.Bold,
                        fontSize = 20.sp, lineHeight = 28.sp,
                        style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))

                    if (article.authors.isNotBlank()) {
                        Text(article.authors,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (article.doi != null) {
                        Text("DOI: ${article.doi}",
                            color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (article.abstract.isNotBlank()) {
                        Text("সারসংক্ষেপ", fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Navy)
                        Spacer(Modifier.height(6.dp))
                        Text(article.abstract, fontSize = 14.sp, lineHeight = 22.sp)
                        Spacer(Modifier.height(12.dp))
                    }

                    if (article.keywords.isNotEmpty()) {
                        Text("মূলশব্দ", fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Navy)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            article.keywords.forEach { kw ->
                                Surface(shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(kw, fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(article.url))) },
                            modifier = Modifier.weight(1f)
                        ) { Text("ওয়েবে পড়ুন") }

                        if (article.pdfUrl != null) {
                            Button(
                                onClick = { context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(article.pdfUrl))) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy)
                            ) { Text("PDF খুলুন") }
                        }
                    }
                }
            }
        }
    }
}
