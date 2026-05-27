package bd.du.bangla.shahittopotrika.ui.screens

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.HeaderBg
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.Locale

private const val LOGO_URL =
    "https://journal.bangla.du.ac.bd/public/journals/1/pageHeaderLogoImage_en.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleUrl: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onOpenPdf: (pdfUrl: String, title: String) -> Unit = { _, _ -> },
    onNotesClick: (articleId: String, articleTitle: String) -> Unit = { _, _ -> }
) {
    val articleState by viewModel.articleDetail.collectAsState()
    val isBookmarked  by viewModel.isBookmarked.collectAsState()
    val context       = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    // ── TTS state ────────────────────────────────────────────
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("bn", "BD")
            }
        }
        tts = instance
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
        onDispose {
            instance.stop()
            instance.shutdown()
            tts = null
        }
    }

    // ── Dropdown menu state ───────────────────────────────────
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(articleUrl) { viewModel.loadArticleDetail(articleUrl) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    title = { Text("প্রবন্ধ", maxLines = 1, fontSize = 14.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            tts?.stop()
                            isSpeaking = false
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                                tint = Color.White)
                        }
                    },
                    actions = {
                        if (articleState is UiState.Success) {
                            val article = (articleState as UiState.Success).data
                            // Bookmark
                            IconButton(onClick = { viewModel.toggleBookmark(article) }) {
                                Icon(
                                    if (isBookmarked) Icons.Default.Bookmark
                                    else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isBookmarked) "Bookmark সরান"
                                                         else "Bookmark করুন",
                                    tint = if (isBookmarked) Color(0xFFFFD700) else Color.White
                                )
                            }
                            // Share
                            IconButton(onClick = {
                                context.startActivity(Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT,
                                            "${article.title}\n${article.url}")
                                    }, "শেয়ার করুন"
                                ))
                            }) {
                                Icon(Icons.Default.Share, "শেয়ার", tint = Color.White)
                            }
                            // Overflow menu
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, "আরো বিকল্প",
                                        tint = Color.White)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    // TTS
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (isSpeaking) "পড়া থামান" else "পড়ে শোনান")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (isSpeaking) Icons.Default.VolumeOff
                                                else Icons.Default.VolumeUp,
                                                null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            if (isSpeaking) {
                                                tts?.stop()
                                                isSpeaking = false
                                            } else {
                                                val text = buildSpeechText(article)
                                                tts?.speak(
                                                    text, TextToSpeech.QUEUE_FLUSH,
                                                    null, "sp_utterance"
                                                )
                                                isSpeaking = true
                                            }
                                        }
                                    )
                                    // Citation
                                    DropdownMenuItem(
                                        text = { Text("উদ্ধৃতি কপি করুন") },
                                        leadingIcon = {
                                            Icon(Icons.Default.ContentCopy, null)
                                        },
                                        onClick = {
                                            showMenu = false
                                            val citation = buildCitation(article)
                                            val cm = context.getSystemService(
                                                Context.CLIPBOARD_SERVICE
                                            ) as ClipboardManager
                                            cm.setPrimaryClip(
                                                ClipData.newPlainText("Citation", citation)
                                            )
                                            scope.launch {
                                                snackbarHostState.showSnackbar("উদ্ধৃতি কপি হয়েছে ✓")
                                            }
                                        }
                                    )
                                    // Notes
                                    DropdownMenuItem(
                                        text = { Text("নোট লিখুন") },
                                        leadingIcon = {
                                            Icon(Icons.Default.EditNote, null)
                                        },
                                        onClick = {
                                            showMenu = false
                                            onNotesClick(article.id, article.title)
                                        }
                                    )
                                    // PDF Download
                                    if (article.pdfUrl != null) {
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("PDF ডাউনলোড করুন") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Download, null)
                                            },
                                            onClick = {
                                                showMenu = false
                                                downloadPdf(context, article)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "PDF ডাউনলোড শুরু হয়েছে ↓"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
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
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy)
                        Spacer(Modifier.height(12.dp))
                        Text("প্রবন্ধ লোড হচ্ছে…")
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
                        state.message,
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
                    Text(
                        article.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp, lineHeight = 28.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(10.dp))

                    if (article.authors.isNotBlank()) {
                        Text(article.authors,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (article.doi != null) {
                        Text("DOI: ${article.doi}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (article.abstract.isNotBlank()) {
                        Text("সারসংক্ষেপ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Navy)
                        Spacer(Modifier.height(6.dp))
                        Text(article.abstract, fontSize = 14.sp, lineHeight = 22.sp)
                        Spacer(Modifier.height(12.dp))
                    }

                    if (article.keywords.isNotEmpty()) {
                        Text("মূলশব্দ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = Navy)
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
                                        kw, fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp, vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(12.dp))

                    // ── Action buttons ────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("ওয়েবে পড়ুন") }

                        if (article.pdfUrl != null) {
                            Button(
                                onClick = { onOpenPdf(article.pdfUrl, article.title) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy)
                            ) { Text("PDF পড়ুন") }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Secondary action row ──────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNotesClick(article.id, article.title) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.EditNote, null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("নোট")
                        }
                        if (article.pdfUrl != null) {
                            OutlinedButton(
                                onClick = {
                                    downloadPdf(context, article)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("PDF ডাউনলোড শুরু হয়েছে ↓")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ডাউনলোড")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildSpeechText(article: Article): String = buildString {
    append(article.title)
    append(". ")
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    if (article.abstract.isNotBlank()) { append(article.abstract) }
}

private fun buildCitation(article: Article): String = buildString {
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    append("\"${article.title}.\"")
    append(" সাহিত্য পত্রিকা, বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়.")
    if (article.doi != null) { append(" DOI: ${article.doi}.") }
    append(" URL: ${article.url}")
}

private fun downloadPdf(context: Context, article: Article) {
    if (article.pdfUrl == null) return
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(article.pdfUrl))
        .setTitle(article.title)
        .setDescription("সাহিত্য পত্রিকা")
        .setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "shahitto_potrika_${article.id}.pdf"
        )
    dm.enqueue(request)
}
