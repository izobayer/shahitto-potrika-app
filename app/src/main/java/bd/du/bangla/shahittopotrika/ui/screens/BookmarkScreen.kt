package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import bd.du.bangla.shahittopotrika.ui.theme.DarkBg
import bd.du.bangla.shahittopotrika.ui.theme.DarkOutline
import bd.du.bangla.shahittopotrika.ui.theme.DarkSurface
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkHigh
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkLow
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkMed
import bd.du.bangla.shahittopotrika.ui.theme.TealAccent
import bd.du.bangla.shahittopotrika.ui.theme.AccentBlue
import bd.du.bangla.shahittopotrika.viewmodel.BookmarkViewModel
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Sync
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    journalVm: JournalViewModel,
    bookmarkVm: BookmarkViewModel = viewModel(),
    onArticleClick: (String) -> Unit,
    onOpenPdf: (pdfUrl: String, title: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val bookmarks by bookmarkVm.bookmarks.collectAsState()
    val folders by bookmarkVm.folders.collectAsState()
    val selectedFolder by bookmarkVm.selectedFolder.collectAsState()
    val userLoggedIn by journalVm.isUserLoggedIn.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var syncLoading by remember { mutableStateOf(false) }
    var syncSuccess by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সংরক্ষিত প্রবন্ধ", fontSize = 15.sp)
                        if (bookmarks.isNotEmpty())
                            Text("${bookmarks.size}টি প্রবন্ধ", fontSize = 11.sp, color = OnDarkMed)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান",
                            tint = Color.White)
                    }
                },
                actions = {
                    if (bookmarks.isNotEmpty()) {
                        // Share all bookmarks
                        IconButton(onClick = {
                            val text = bookmarks.joinToString("\n\n") { bm ->
                                buildString {
                                    append("📖 ${bm.title}")
                                    if (bm.authors.isNotBlank()) append("\n✍️ ${bm.authors}")
                                    append("\n🔗 ${bm.url}")
                                }
                            }
                            context.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "সংরক্ষিত প্রবন্ধ – সাহিত্য পত্রিকা")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }, "রপ্তানি করুন"
                            ))
                        }) {
                            Icon(Icons.Default.IosShare, "রপ্তানি", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = OnDarkHigh
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val folderMetadataStr by bookmarkVm.folderMetadata.collectAsState()
            val folderMetadata = remember(folderMetadataStr) {
                try {
                    org.json.JSONObject(folderMetadataStr)
                } catch (e: Exception) {
                    org.json.JSONObject()
                }
            }

            // Folders horizontally scrollable row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(folders) { folder ->
                    val isSelected = folder == selectedFolder
                    
                    val folderObj = try { folderMetadata.optJSONObject(folder) } catch(e: Exception) { null }
                    val defaultEmoji = when(folder) {
                        "সব" -> ""
                        "পছন্দসমূহ" -> "❤️"
                        else -> "📁"
                    }
                    val defaultColorHex = when(folder) {
                        "সব" -> ""
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

                    FilterChip(
                        selected = isSelected,
                        onClick = { bookmarkVm.selectFolder(folder) },
                        label = { Text(displayName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurface,
                            labelColor = OnDarkMed,
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor = color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (colorHex.isNotBlank()) color.copy(alpha = 0.3f) else DarkOutline,
                            selectedBorderColor = color.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Simulated Cloud Sync Card
            if (userLoggedIn) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TealAccent.copy(alpha = 0.05f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TealAccent.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (syncLoading) {
                                CircularProgressIndicator(
                                    color = TealAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (syncLoading) "সার্ভারের সাথে সিঙ্ক হচ্ছে..." else "সার্ভারের সাথে সিঙ্কড ✓",
                                    color = OnDarkHigh,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (syncLoading) "দয়া করে অপেক্ষা করুন..." else if (syncSuccess) "এইমাত্র আপডেট করা হয়েছে" else "স্বয়ংক্রিয় ব্যাকআপ সচল আছে",
                                    color = OnDarkMed,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!syncLoading) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        syncLoading = true
                                        kotlinx.coroutines.delay(1500)
                                        syncLoading = false
                                        syncSuccess = true
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = TealAccent)
                            ) {
                                Text("এখনই সিঙ্ক", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnDarkLow
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "কোনো সংরক্ষিত প্রবন্ধ নেই",
                            color = OnDarkMed,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "প্রবন্ধ পড়ার সময় bookmark বাটনে চাপুন",
                            fontSize = 13.sp,
                            color = OnDarkLow,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "নিজের পছন্দ অনুযায়ী সাজান",
                            fontSize = 11.sp,
                            color = OnDarkLow,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    itemsIndexed(bookmarks, key = { _, bm -> bm.articleId }) { index, bm ->
                        BookmarkCard(
                            bookmark  = bm,
                            index     = index,
                            total     = bookmarks.size,
                            onClick   = { onArticleClick(bm.url) },
                            onDelete  = { journalVm.removeBookmark(bm.articleId) },
                            onOpenPdf = { if (bm.pdfUrl != null) onOpenPdf(bm.pdfUrl, bm.title) },
                            onMoveUp   = { if (index > 0) bookmarkVm.moveBookmark(index, index - 1) },
                            onMoveDown = { if (index < bookmarks.size - 1) bookmarkVm.moveBookmark(index, index + 1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkCard(
    bookmark: BookmarkEntity,
    index: Int,
    total: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onOpenPdf: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Content ──────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OnDarkHigh,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                if (bookmark.authors.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = bookmark.authors,
                        fontSize = 12.sp,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = TealAccent)
                    ) {
                        Text("পড়ুন →", fontSize = 12.sp)
                    }
                    if (bookmark.pdfUrl != null) {
                        TextButton(
                            onClick = onOpenPdf,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("PDF", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ── Order + delete controls ───────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Move up
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(30.dp),
                    enabled = index > 0
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "উপরে নিন",
                        tint = if (index > 0) OnDarkMed else DarkOutline,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Move down
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(30.dp),
                    enabled = index < total - 1
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "নিচে নিন",
                        tint = if (index < total - 1) OnDarkMed else DarkOutline,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "মুছুন",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
