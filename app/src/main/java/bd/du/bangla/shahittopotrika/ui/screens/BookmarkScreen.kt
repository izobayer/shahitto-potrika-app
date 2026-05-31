package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import bd.du.bangla.shahittopotrika.viewmodel.BookmarkViewModel
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel

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
    val context = LocalContext.current

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
        if (bookmarks.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
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
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
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
                        color = TealAccent,
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
