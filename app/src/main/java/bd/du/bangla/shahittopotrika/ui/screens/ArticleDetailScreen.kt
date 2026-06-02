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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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
    onNotesClick: (articleId: String, articleTitle: String) -> Unit = { _, _ -> },
    onAuthorClick: (String) -> Unit = {}
) {
    val articleState      by viewModel.articleDetail.collectAsState()
    val isBookmarked      by viewModel.isBookmarked.collectAsState()
    val currentFolders    by viewModel.currentArticleFolders.collectAsState()
    val allFolders        by viewModel.allFolderNames.collectAsState()
    val folderMetadataStr by viewModel.folderMetadata.collectAsState()
    val comments          by viewModel.currentArticleComments.collectAsState()
    val userLoggedIn      by viewModel.isUserLoggedIn.collectAsState()
    val currentUserName   by viewModel.userName.collectAsState()
    val currentUserEmail  by viewModel.userEmail.collectAsState()
    
    val context           = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var aiSummary by remember { mutableStateOf<String?>(null) }
    var summaryLoading by remember { mutableStateOf(false) }
    val chatMessages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    var userQuery by remember { mutableStateOf("") }
    var responseLoading by remember { mutableStateOf(false) }
    var showCitationDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val historyItem by viewModel.currentArticleHistory.collectAsState()
    var hasRestoredScroll by remember(articleUrl) { mutableStateOf(false) }

    LaunchedEffect(articleState, historyItem) {
        if (articleState is UiState.Success && historyItem != null && !hasRestoredScroll) {
            val offset = historyItem?.scrollOffset ?: 0
            if (offset > 0) {
                kotlinx.coroutines.delay(150)
                scrollState.scrollTo(offset)
            }
            hasRestoredScroll = true
        }
    }

    DisposableEffect(articleUrl, articleState) {
        onDispose {
            if (articleState is UiState.Success) {
                val article = (articleState as UiState.Success).data
                val maxScroll = scrollState.maxValue
                if (maxScroll > 0) {
                    val progress = scrollState.value.toFloat() / maxScroll.toFloat()
                    viewModel.updateReadingProgress(article.id, progress, scrollState.value)
                }
            }
        }
    }

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
                        IconButton(onClick = { showBookmarkDialog = true }) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark
                                else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Bookmark করুন"
                                                     else "Bookmark করুন",
                                tint = if (isBookmarked) TealAccent else OnDarkMed
                            )
                        }
                        // AI Assistant
                        IconButton(onClick = { showAiSheet = true }) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI সারসংক্ষেপক",
                                tint = TealAccent
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
                                    text = { Text("উদ্ধৃতি কপি করুন", color = OnDarkHigh) },
                                    leadingIcon = {
                                        Icon(Icons.Default.ContentCopy, null, tint = OnDarkMed)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showCitationDialog = true
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
                        .verticalScroll(scrollState)
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

                    // Author profile card
                    if (article.authors.isNotBlank()) {
                        val authorName = article.authors.trim()
                        val initials = getInitials(authorName)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealAccent.copy(alpha = 0.07f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAuthorClick(authorName) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(TealAccent, TealAccent.copy(alpha = 0.7f))
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "লেখক",
                                        fontSize = 11.sp,
                                        color = OnDarkLow
                                    )
                                    Text(
                                        text = authorName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealAccent,
                                        maxLines = 2
                                    )
                                }
                            }
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
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TealAccent)) {
                                    append("সারসংক্ষেপ: ")
                                }
                                append(article.abstract.trim())
                            },
                            fontSize   = 13.sp,
                            lineHeight = 18.sp,
                            color      = OnDarkMed,
                            textAlign  = TextAlign.Justify,
                            modifier   = Modifier.padding(top = 4.dp)
                        )
                        Spacer(Modifier.height(16.dp))
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

                    // ── AI Audio Narration Button ───────────────────────────────
                    Button(
                        onClick = {
                            if (isSpeaking) {
                                tts?.stop()
                                isSpeaking = false
                            } else {
                                tts?.speak(
                                    buildSpeechText(article),
                                    TextToSpeech.QUEUE_FLUSH,
                                    null, "sp_utterance"
                                )
                                isSpeaking = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(50.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.12f) else Color(0xFFEEF2F6),
                            contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.5f) else Color(0xFF78C4FF)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint     = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isSpeaking) "পড়া থামান" else "প্রবন্ধটি শুনুন (AI অডিও)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

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
                                containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.12f) else Color(0xFFEEF2F6),
                                contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                            ),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.5f) else Color(0xFF78C4FF)
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
                                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF102334),
                                    contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF003730) else Color.White
                                )
                            ) {
                                Text("PDF পড়ুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

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
                                containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.12f) else Color(0xFFEEF2F6),
                                contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                            ),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.5f) else Color(0xFF78C4FF)
                            )
                        ) {
                            Icon(
                                Icons.Default.EditNote, null,
                                modifier = Modifier.size(18.dp),
                                tint     = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
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
                                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.12f) else Color(0xFFEEF2F6),
                                    contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                                ),
                                border   = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.5f) else Color(0xFF78C4FF)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Download, null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("ডাউনলোড")
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = DarkOutline)
                    Spacer(Modifier.height(16.dp))

                    // Comments Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আলোচনা ও মন্তব্য (${comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnDarkHigh
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Write Comment
                    if (userLoggedIn) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("আপনার মন্তব্য লিখুন...", color = OnDarkLow, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = OnDarkHigh,
                                    unfocusedTextColor = OnDarkHigh,
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg,
                                    focusedIndicatorColor = TealAccent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.addComment(
                                            articleId = article.id,
                                            userName = currentUserName.ifBlank { "ব্যবহারকারী" },
                                            userEmail = currentUserEmail,
                                            commentText = commentText.trim()
                                        )
                                        commentText = ""
                                    }
                                },
                                enabled = commentText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("মন্তব্য করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        // User not logged in notice
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TealAccent.copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, null, tint = TealAccent, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "মন্তব্য পোস্ট করতে লগইন করুন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OnDarkHigh
                                    )
                                    Text(
                                        text = "সেটিংস থেকে আপনার গুগল অ্যাকাউন্ট ব্যবহার করে লগইন করতে পারবেন।",
                                        fontSize = 11.sp,
                                        color = OnDarkMed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Comments List
                    if (comments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এখনো কোনো মন্তব্য নেই। আলোচনা শুরু করতে প্রথম মন্তব্যটি লিখুন!",
                                color = OnDarkLow,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            comments.forEach { comment ->
                                CommentCard(
                                    comment = comment,
                                    onDelete = { viewModel.deleteComment(comment.id) }
                                )
                            }
                        }
                    }

                    if (showBookmarkDialog) {
                        BookmarkFoldersDialog(
                            article = article,
                            allFolders = allFolders,
                            currentFolders = currentFolders,
                            folderMetadataStr = folderMetadataStr,
                            onDismiss = { showBookmarkDialog = false },
                            onSave = { checked, unchecked ->
                                viewModel.updateArticleBookmarks(article, checked, unchecked)
                                showBookmarkDialog = false
                            },
                            onAddFolderMetadata = { folder, emoji, colorHex ->
                                viewModel.updateFolderMetadata(folder, emoji, colorHex)
                            }
                        )
                    }

                    if (showCitationDialog) {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        CitationDialog(
                            article = article,
                            onDismiss = { showCitationDialog = false },
                            onCopy = { citationText ->
                                cm.setPrimaryClip(ClipData.newPlainText("Citation", citationText))
                                scope.launch {
                                    snackbarHostState.showSnackbar("উদ্ধৃতি কপি হয়েছে ✓")
                                }
                                showCitationDialog = false
                            }
                        )
                    }

                    if (showAiSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showAiSheet = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            containerColor = DarkSurface,
                            dragHandle = { BottomSheetDefaults.DragHandle(color = DarkOutline) }
                        ) {
                            AiAssistantContent(
                                article = article,
                                aiSummary = aiSummary,
                                summaryLoading = summaryLoading,
                                chatMessages = chatMessages,
                                userQuery = userQuery,
                                responseLoading = responseLoading,
                                onQueryChange = { userQuery = it },
                                onLoadSummary = {
                                    scope.launch {
                                        summaryLoading = true
                                        val prompt = "You are Shahitto Potrika's academic assistant. Please read the following article title, author, and abstract, and provide a concise, high-quality, academic summary in Bengali (5-6 sentences maximum). Highlight key research questions or findings if mentioned.\n\nTitle: ${article.title}\nAuthor: ${article.authors}\nAbstract: ${article.abstract}"
                                        aiSummary = bd.du.bangla.shahittopotrika.data.ai.GeminiHelper.generateContent(prompt)
                                        summaryLoading = false
                                    }
                                },
                                onSendMessage = {
                                    if (userQuery.isNotBlank()) {
                                        val query = userQuery.trim()
                                        chatMessages.add(Pair(query, true))
                                        userQuery = ""
                                        scope.launch {
                                            responseLoading = true
                                            val prompt = "You are Shahitto Potrika's academic assistant. Answer the user's question about the article in Bengali. Be helpful, polite, and scholarly. Keep the answer concise.\n\nArticle Title: ${article.title}\nArticle Abstract: ${article.abstract}\nUser Question: $query"
                                            val reply = bd.du.bangla.shahittopotrika.data.ai.GeminiHelper.generateContent(prompt)
                                            chatMessages.add(Pair(reply, false))
                                            responseLoading = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkFoldersDialog(
    article: Article,
    allFolders: List<String>,
    currentFolders: List<String>,
    folderMetadataStr: String,
    onDismiss: () -> Unit,
    onSave: (checked: List<String>, unchecked: List<String>) -> Unit,
    onAddFolderMetadata: (String, String, String) -> Unit
) {
    val foldersList = remember(allFolders) {
        val list = mutableListOf("পছন্দসমূহ")
        allFolders.forEach {
            if (it != "পছন্দসমূহ" && it.isNotBlank() && it !in list) {
                list.add(it)
            }
        }
        list
    }

    val checkedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            foldersList.forEach { folder ->
                put(folder, folder in currentFolders)
            }
        }
    }

    var newFolderName by remember { mutableStateOf("") }
    val localFolders = remember { mutableStateListOf<String>() }

    var selectedEmoji by remember { mutableStateOf("📚") }
    var selectedColorHex by remember { mutableStateOf("#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ফোল্ডার নির্বাচন করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnDarkHigh) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // List of folders
                (foldersList + localFolders).distinct().forEach { folder ->
                    val json = remember(folderMetadataStr) {
                        try { org.json.JSONObject(folderMetadataStr) } catch(e: Exception) { org.json.JSONObject() }
                    }
                    val folderObj = try { json.optJSONObject(folder) } catch(e: Exception) { null }
                    val defaultEmoji = when(folder) {
                        "পছন্দসমূহ" -> "❤️"
                        else -> "📁"
                    }
                    val defaultColorHex = when(folder) {
                        "পছন্দসমূহ" -> "#BA1A1A"
                        else -> ""
                    }
                    val emoji = folderObj?.optString("emoji") ?: defaultEmoji
                    val colorHex = folderObj?.optString("colorHex") ?: defaultColorHex
                    val color = if (colorHex.isNotBlank()) {
                        try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { TealAccent }
                    } else {
                        TealAccent
                    }
                    val displayName = if (emoji.isNotBlank()) "$emoji $folder" else folder

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val currentVal = checkedStates[folder] ?: false
                                checkedStates[folder] = !currentVal
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedStates[folder] ?: false,
                            onCheckedChange = { checked ->
                                checkedStates[folder] = checked
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = color,
                                uncheckedColor = OnDarkMed,
                                checkmarkColor = Color.Black
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(displayName, color = OnDarkHigh, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Text field to add new folder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("নতুন ফোল্ডার...", color = OnDarkLow, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = OnDarkHigh,
                            unfocusedTextColor = OnDarkHigh,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedIndicatorColor = TealAccent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                val folder = newFolderName.trim()
                                if (folder !in foldersList && folder !in localFolders) {
                                    localFolders.add(folder)
                                    checkedStates[folder] = true
                                    onAddFolderMetadata(folder, selectedEmoji, selectedColorHex)
                                }
                                newFolderName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, "ফোল্ডার যোগ করুন", tint = TealAccent)
                    }
                }

                if (newFolderName.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("আইকন নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnDarkMed)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("📚", "✍️", "🎯", "💡", "📁").forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) TealAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) TealAccent else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("রঙ নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnDarkMed)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val colors = listOf(
                            "#2563EB" to Color(0xFF2563EB),
                            "#00D4B1" to Color(0xFF00D4B1),
                            "#BA1A1A" to Color(0xFFBA1A1A),
                            "#E67E22" to Color(0xFFE67E22),
                            "#8E44AD" to Color(0xFF8E44AD)
                        )
                        colors.forEach { (hex, colorVal) ->
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(colorVal, shape = RoundedCornerShape(50.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalChecked = (foldersList + localFolders).distinct().filter { checkedStates[it] == true }
                    val finalUnchecked = currentFolders.filter { it !in finalChecked }
                    onSave(finalChecked, finalUnchecked)
                }
            ) {
                Text("সংরক্ষণ", color = TealAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = OnDarkMed)
            }
        },
        containerColor = DarkSurface
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun getInitials(name: String): String {
    val cleanName = name.replace(Regex("[।.,]"), " ").trim()
    val parts = cleanName.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts[0].take(1)
    return parts[0].take(1) + parts[1].take(1)
}

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

private fun buildApaCitation(article: Article): String = buildString {
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    append("\"${article.title}.\"")
    append(" সাহিত্য পত্রিকা, ঢাকা বিশ্ববিদ্যালয়.")
    if (article.doi != null) { append(" DOI: ${article.doi}.") }
    append(" URL: ${article.url}")
}

private fun buildMlaCitation(article: Article): String = buildString {
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    append("\"${article.title}.\"")
    append(" সাহিত্য পত্রিকা, ঢাকা বিশ্ববিদ্যালয়, URL: ${article.url}.")
}

private fun buildChicagoCitation(article: Article): String = buildString {
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    append("\"${article.title}.\"")
    append(" সাহিত্য পত্রিকা, ঢাকা বিশ্ববিদ্যালয়, URL: ${article.url}.")
}

private fun buildHarvardCitation(article: Article): String = buildString {
    if (article.authors.isNotBlank()) { append(article.authors); append(". ") }
    append("\"${article.title}\",")
    append(" সাহিত্য পত্রিকা, ঢাকা বিশ্ববিদ্যালয়, Available at: ${article.url}.")
}

private fun buildBibtexCitation(article: Article): String = buildString {
    append("@article{shahitto_potrika_${article.id},\n")
    append("  author = {${article.authors.ifBlank { "বাংলা বিভাগ" }}},\n")
    append("  title = {${article.title}},\n")
    append("  journal = {সাহিত্য পত্রিকা},\n")
    append("  publisher = {বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়},\n")
    if (article.doi != null) { append("  doi = {${article.doi}},\n") }
    append("  url = {${article.url}}\n")
    append("}")
}

private fun buildRisCitation(article: Article): String = buildString {
    append("TY  - JOUR\n")
    append("AU  - ${article.authors.ifBlank { "বাংলা বিভাগ" }}\n")
    append("TI  - ${article.title}\n")
    append("JO  - সাহিত্য পত্রিকা\n")
    append("PB  - বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়\n")
    if (article.doi != null) { append("DO  - ${article.doi}\n") }
    append("UR  - ${article.url}\n")
    append("ER  - ")
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

@Composable
fun CommentCard(
    comment: bd.du.bangla.shahittopotrika.data.local.entity.ArticleCommentEntity,
    onDelete: () -> Unit
) {
    val initials = getInitials(comment.userName)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(TealAccent, TealAccent.copy(alpha = 0.7f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifBlank { "U" },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = comment.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OnDarkHigh
                        )
                        Text(
                            text = comment.userEmail,
                            fontSize = 10.sp,
                            color = OnDarkLow
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "মন্তব্য মুছুন",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = comment.commentText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = OnDarkHigh
                )
            }
        }
    }
}

import androidx.compose.ui.platform.LocalContext
import android.content.Intent

@Composable
fun CitationDialog(
    article: Article,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    val context = LocalContext.current
    val apa = buildApaCitation(article)
    val mla = buildMlaCitation(article)
    val chicago = buildChicagoCitation(article)
    val harvard = buildHarvardCitation(article)
    val bibtex = buildBibtexCitation(article)
    val ris = buildRisCitation(article)

    val onShare = { text: String ->
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }, "শেয়ার করুন"
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "উদ্ধৃতি কপি ও শেয়ার করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnDarkHigh
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CitationRow(title = "APA Format", citation = apa, onCopy = onCopy, onShare = onShare)
                CitationRow(title = "MLA Format", citation = mla, onCopy = onCopy, onShare = onShare)
                CitationRow(title = "Chicago Format", citation = chicago, onCopy = onCopy, onShare = onShare)
                CitationRow(title = "Harvard Format", citation = harvard, onCopy = onCopy, onShare = onShare)
                CitationRow(title = "BibTeX Format (Mendeley/Zotero)", citation = bibtex, onCopy = onCopy, onShare = onShare)
                CitationRow(title = "RIS Format (EndNote/RefMan)", citation = ris, onCopy = onCopy, onShare = onShare)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TealAccent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun CitationRow(
    title: String,
    citation: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg, shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TealAccent,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { onCopy(citation) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "কপি করুন",
                        tint = OnDarkMed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { onShare(citation) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "শেয়ার করুন",
                        tint = OnDarkMed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = citation,
            fontSize = 12.sp,
            color = OnDarkHigh,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun AiAssistantContent(
    article: Article,
    aiSummary: String?,
    summaryLoading: Boolean,
    chatMessages: List<Pair<String, Boolean>>,
    userQuery: String,
    responseLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onLoadSummary: () -> Unit,
    onSendMessage: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (aiSummary == null) {
            onLoadSummary()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = TealAccent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "AI একাডেমিক সহকারী",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnDarkHigh
            )
        }

        HorizontalDivider(color = DarkOutline, modifier = Modifier.padding(bottom = 12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (summaryLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TealAccent, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("সারসংক্ষেপ তৈরি হচ্ছে...", color = OnDarkMed, fontSize = 12.sp)
                    }
                }
            } else if (aiSummary != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TealAccent.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "প্রবন্ধের সারসংক্ষেপ (AI):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TealAccent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = aiSummary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = OnDarkHigh
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (aiSummary != null) {
                Text(
                    text = "প্রবন্ধ সম্পর্কে জিজ্ঞেস করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OnDarkMed,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chatMessages.isEmpty()) {
                        Text(
                            text = "কোনো প্রশ্ন আছে? নিচে লিখুন এবং পাঠান।",
                            color = OnDarkLow,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        chatMessages.forEach { (text, isUser) ->
                            ChatBubble(text = text, isUser = isUser)
                        }
                    }

                    if (responseLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            CircularProgressIndicator(color = TealAccent, modifier = Modifier.size(14.dp))
                            Text("AI ভাবছে...", color = OnDarkLow, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (aiSummary != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("প্রশ্ন লিখুন...", color = OnDarkLow, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = OnDarkHigh,
                        unfocusedTextColor = OnDarkHigh,
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg,
                        focusedIndicatorColor = TealAccent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onSendMessage,
                    enabled = userQuery.isNotBlank() && !responseLoading,
                    modifier = Modifier
                        .background(if (userQuery.isNotBlank() && !responseLoading) TealAccent else DarkOutline, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "পাঠান",
                        tint = if (userQuery.isNotBlank() && !responseLoading) Color.Black else OnDarkLow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 12.dp
            ),
            color = if (isUser) TealAccent.copy(alpha = 0.15f) else DarkBg,
            border = if (isUser) androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.3f)) else null,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = OnDarkHigh,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
