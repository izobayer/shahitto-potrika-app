package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.AuthorDetails
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorDetailScreen(
    authorId: String,
    viewModel: JournalViewModel,
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(authorId) {
        viewModel.loadAuthorDetails(authorId)
    }

    val authorDetailsState by viewModel.authorDetails.collectAsState()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("লেখকের প্রোফাইল", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnDarkHigh) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান", tint = OnDarkHigh)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = OnDarkHigh
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(paddingValues)
        ) {
            when (val state = authorDetailsState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TealAccent)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        ErrorCard(
                            message = state.message,
                            onRetry = { viewModel.loadAuthorDetails(authorId) }
                        )
                    }
                }
                is UiState.Success -> {
                    val details = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AuthorProfileHeader(details = details)
                        }

                        item {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "প্রকাশিত প্রবন্ধসমূহ (${details.articles.size}টি)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnDarkHigh,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                        }

                        if (details.articles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "কোনো প্রবন্ধ পাওয়া যায়নি",
                                        color = OnDarkLow,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(details.articles) { article ->
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
fun AuthorProfileHeader(details: AuthorDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!details.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = details.photoUrl,
                    contentDescription = details.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .border(2.dp, TealAccent, CircleShape)
                )
            } else {
                val initials = details.name.split(" ").filter { it.isNotBlank() }.map { it.first() }.joinToString("").take(2)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(TealAccent, TealAccent.copy(alpha = 0.6f))
                            )
                        )
                        .border(2.dp, TealAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifBlank { details.name.take(1) },
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = details.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnDarkHigh,
                textAlign = TextAlign.Center
            )

            if (!details.title.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = details.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkMed,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
