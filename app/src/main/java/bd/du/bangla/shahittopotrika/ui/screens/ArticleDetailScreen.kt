package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleUrl: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val articleState by viewModel.articleDetail.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(articleUrl) {
        viewModel.loadArticleDetail(articleUrl)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("নিবন্ধ", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    if (articleState is UiState.Success) {
                        val article = (articleState as UiState.Success).data
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
                            }
                            context.startActivity(Intent.createChooser(intent, "শেয়ার করুন"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "শেয়ার",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (val state = articleState) {
            is UiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy)
                        Spacer(Modifier.height(12.dp))
                        Text("নিবন্ধ লোড হচ্ছে…")
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadArticleDetail(articleUrl) }
                    )
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
                    // Title
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(10.dp))

                    // Authors
                    if (article.authors.isNotBlank()) {
                        Text(
                            text = article.authors,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    // DOI
                    if (article.doi != null) {
                        Text(
                            text = "DOI: ${article.doi}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Abstract
                    if (article.abstract.isNotBlank()) {
                        Text(
                            text = "সারসংক্ষেপ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Navy
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = article.abstract,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Keywords
                    if (article.keywords.isNotEmpty()) {
                        Text(
                            text = "মূলশব্দ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Navy
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            article.keywords.forEach { kw ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = kw,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open in browser
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ওয়েবে পড়ুন")
                        }

                        // PDF button
                        if (article.pdfUrl != null) {
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(article.pdfUrl))
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy)
                            ) {
                                Text("PDF খুলুন")
                            }
                        }
                    }
                }
            }
        }
    }
}
