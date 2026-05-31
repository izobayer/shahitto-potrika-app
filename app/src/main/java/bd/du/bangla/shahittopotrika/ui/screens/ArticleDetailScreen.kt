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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.ui.theme.*
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArticleDetailScreen(
    articleUrl: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onOpenPdf: (pdfUrl: String, title: String) -> Unit = { _, _ -> },
    onNotesClick: (articleId: String, articleTitle: String) -> Unit = { _, _ -> }
) {
    val articleState      by viewModel.articleDetail.collectAsState()
    val isBookmarked      by viewModel.isBookmarked.collectAsState()
    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    // ── TTS state ────────────────────────────────────────────
    var tts       by remember { mutableStateOf<TextToSpeech?>(null) }
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
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { isSpeaking = false }
        })
        onDispose {
            instance.stop()
            instance.shutdown()
            tts = null
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(articleUrl) { viewModel.loadArticleDetail(articleUrl) }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("প্রবন্ধ", maxLines = 1, fontSize = 15.sp, color = OnDarkHigh) },
                navigationIcon = {
                    IconButton(onClick = {
                        tts?.stop()
                        isSpeaking = false
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান", tint = OnDarkHigh)
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
                                tint = if (isBookmarked) TealAccent else OnDarkMed
                            )
                        }
                        // Share
                        IconButton(onClick = {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
                                    }, "শেয়ার করুন"
                                )
                            )
                        }) {
                            Icon(Icons.Default.Share, "শেয়ার", tint = OnDarkMed)
                        }
                        // Overflow menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "আরো বিকল্প", tint = OnDarkMed)
                            }
                            DropdownMenu(
                                expanded         = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor   = DarkSurface
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isSpeaking) "পড়া থামান" else "পড়ে শোনান",
                                            color = OnDarkHigh
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isSpeaking) Icons.Default.VolumeOff
                                            else Icons.Default.VolumeUp,
                                            null,
                                            tint = OnDarkMed
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        if (isSpeaking) {
                                            tts?.stop(); isSpeaking = false
                                        } else {
                                            tts?.speak(
                                                buildSpeechText(article),
                                                TextToSpeech.QUEUE_FLUSH,
                                                null, "sp_utterance"
                                            )
                                            isSpeaking = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("উদ্ধৃতি কপি করুন", color = OnDarkHigh) },
                                    leadingIcon = {
                                        Icon(Icons.Default.ContentCopy, null, tint = OnDarkMed)
                                    },
                                    onClick = {
                                        showMenu = false
                                        val cm = context.getSystemService(
                                            Context.CLIPBOARD_SERVICE
                                        ) as ClipboardManager
                                        cm.setPrimaryClip(
                                            ClipData.newPlainText("Citation", buildCitation(article))
                                        )
                                        scope.launch {
                                            snackbarHostState.showSnackbar("উদ্ধৃতি কপি হয়েছে ✓")
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("নোট লিখুন", color = OnDarkHigh) },
                                    leadingIcon = {
                                        Icon(Icons.Default.EditNote, null, tint = OnDarkMed)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onNotesClick(article.id, article.title)
                                    }
                                )
                                if (article.pdfUrl != null) {
                                    HorizontalDivider(color = DarkOutline)
                                    DropdownMenuItem(
                                        text = { Text("PDF ডাউনলোড করুন", color = OnDarkHigh) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Download, null, tint = OnDarkMed)
                                        },
                                        onClick = {
                                            showMenu = false
                                            downloadPdf(context, article)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("PDF ডাউনলোড শুরু হয়েছে ↓")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = DarkSurface,
                    titleContentColor = OnDarkHigh
                )
            )
        }
    ) { paddingValues ->
        when (val state = articleState) {
            is UiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(DarkBg)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TealAccent)
                        Spacer(Modifier.height(12.dp))
                        Text("প্রবন্ধ লোড হচ্ছে…", color = OnDarkMed)
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(DarkBg)
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
                        .fillMaxSize()
                        .background(DarkBg)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Title
                    Text(
                        article.title,
                        style      = MaterialTheme.typography.titleLarge,
                        color      = OnDarkHigh,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Author pill
                    if (article.authors.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = TealAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                article.authors,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color      = TealAccent,
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                maxLines   = 2
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // DOI
                    if (article.doi != null) {
                        Text(
                            "DOI: ${article.doi}",
                            color    = OnDarkLow,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    HorizontalDivider(color = DarkOutline, modifier = Modifier.padding(vertical = 6.dp))

                    // Abstract
                    if (article.abstract.isNotBlank()) {
                        SectionHeader("সারসংক্ষেপ", modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            article.abstract,
                            fontSize   = 14.sp,
                            lineHeight = 24.sp,
                            color      = OnDarkMed
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // Keywords
                    if (article.keywords.isNotEmpty()) {
                        SectionHeader("মূলশব্দ")
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            article.keywords.forEach { kw ->
                                PillBadge(
                                    text           = kw,
                                    containerColor = TealAccent.copy(alpha = 0.10f),
                                    textColor      = TealAccent
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    HorizontalDivider(color = DarkOutline, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(12.dp))

                    // Primary action buttons
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick  = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(50.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = TealAccent
                            ),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp, TealAccent.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("ওয়েবে পড়ুন", fontWeight = FontWeight.SemiBold)
                        }

                        if (article.pdfUrl != null) {
                            Button(
                                onClick  = { onOpenPdf(article.pdfUrl, article.title) },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(50.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = TealAccent,
                                    contentColor   = Color(0xFF003730)
                                )
                            ) {
                                Text("PDF পড়ুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Secondary action row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick  = { onNotesClick(article.id, article.title) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(50.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnDarkMed
                            ),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp, DarkOutline
                            )
                        ) {
                            Icon(
                                Icons.Default.EditNote, null,
                                modifier = Modifier.size(16.dp),
                                tint     = OnDarkMed
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("নোট")
                        }
                        if (article.pdfUrl != null) {
                            OutlinedButton(
                                onClick  = {
                                    downloadPdf(context, article)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("PDF ডাউনলোড শুরু হয়েছে ↓")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(50.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = OnDarkMed
                                ),
                                border   = androidx.compose.foundation.BorderStroke(
                                    1.dp, DarkOutline
                                )
                            ) {
                                Icon(
                                    Icons.Default.Download, null,
                                    modifier = Modifier.size(16.dp),
                                    tint     = OnDarkMed
                                )
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
    append(article.title); append(". ")
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
    dm.enqueue(
        DownloadManager.Request(Uri.parse(article.pdfUrl))
            .setTitle(article.title)
            .setDescription("সাহিত্য পত্রিকা")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "shahitto_potrika_${article.id}.pdf"
            )
    )
}
