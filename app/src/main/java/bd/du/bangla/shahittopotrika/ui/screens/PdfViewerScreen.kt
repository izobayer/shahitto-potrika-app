package bd.du.bangla.shahittopotrika.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.ui.theme.DarkSurface
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkHigh
import bd.du.bangla.shahittopotrika.ui.theme.TealAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    title: String = "PDF",
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var pdfFile   by remember { mutableStateOf<File?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var loading   by remember { mutableStateOf(true) }
    var errorMsg  by remember { mutableStateOf<String?>(null) }
    var retryKey  by remember { mutableIntStateOf(0) }

    // Download the PDF on first compose (or retry)
    LaunchedEffect(pdfUrl, retryKey) {
        loading  = true
        errorMsg = null
        withContext(Dispatchers.IO) {
            try {
                val file = downloadPdf(context, pdfUrl)
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        pageCount = renderer.pageCount
                    }
                }
                if (pageCount == 0) throw Exception("PDF-এ কোনো পৃষ্ঠা নেই")
                pdfFile  = file
                loading  = false
            } catch (e: Exception) {
                errorMsg = e.message ?: "PDF লোড করা যায়নি"
                loading  = false
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            title.take(38) + if (title.length > 38) "…" else "",
                            fontSize = 13.sp, maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                                tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { retryKey++ }) {
                            Icon(Icons.Default.Refresh, "আবার লোড", tint = Color.White)
                        }
                        IconButton(onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                            )
                        }) {
                            Icon(Icons.Default.Download, "বাইরে খুলুন", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface, titleContentColor = Color.White
                    )
                )
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2563EB),
                        trackColor = TealAccent.copy(alpha = 0.3f)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealAccent)
                            Spacer(Modifier.height(12.dp))
                            Text("PDF ডাউনলোড হচ্ছে…", color = TealAccent)
                        }
                    }
                }
                errorMsg != null -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("❌ $errorMsg",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { retryKey++ },
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.12f) else Color(0xFFEEF2F6),
                                    contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF2563EB)
                                ),
                                border   = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent.copy(alpha = 0.5f) else Color(0xFF78C4FF)
                                )
                            ) {
                                Text("আবার চেষ্টা")
                            }
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) TealAccent else Color(0xFF102334),
                                    contentColor   = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF003730) else Color.White
                                )
                            ) { Text("ব্রাউজারে খুলুন") }
                        }
                    }
                }
                pdfFile != null && pageCount > 0 -> {
                    PdfPagesView(file = pdfFile!!, pageCount = pageCount)
                }
            }
        }
    }
}

// ── PDF pages viewer ──────────────────────────────────────────────────────────

@Composable
fun PdfPagesView(file: File, pageCount: Int) {
    val config  = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.roundToPx() }

    LazyColumn(
        state  = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF555555))
    ) {
        items(count = pageCount, key = { it }) { pageIndex ->
            PdfPage(
                file         = file,
                pageIndex    = pageIndex,
                screenWidth  = screenWidthPx,
                modifier     = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun PdfPage(
    file: File,
    pageIndex: Int,
    screenWidth: Int,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(pageIndex, screenWidth) {
        withContext(Dispatchers.IO) {
            try {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        renderer.openPage(pageIndex).use { page ->
                            val scale  = screenWidth.toFloat() / page.width.toFloat()
                            val width  = screenWidth
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bmp.eraseColor(AndroidColor.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap = bmp
                        }
                    }
                }
            } catch (_: Exception) { /* leave bitmap null */ }
        }
    }

    DisposableEffect(pageIndex) {
        onDispose { bitmap?.recycle() }
    }

    if (bitmap != null) {
        val colorFilter = if (isDark) {
            val matrix = floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
            ColorFilter.colorMatrix(ColorMatrix(matrix))
        } else {
            null
        }

        Image(
            bitmap          = bitmap!!.asImageBitmap(),
            contentDescription = "পৃষ্ঠা ${pageIndex + 1}",
            contentScale    = ContentScale.FillWidth,
            colorFilter     = colorFilter,
            modifier        = modifier
        )
    } else {
        // Placeholder while rendering
        Box(
            modifier = modifier
                .aspectRatio(1f / 1.414f)       // A4 ratio placeholder
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color       = TealAccent,
                strokeWidth = 2.dp,
                modifier    = Modifier.size(28.dp)
            )
        }
    }
}

// ── PDF download helper ───────────────────────────────────────────────────────

private val httpClient by lazy {
    OkHttpClient.Builder()
        .followRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}

/**
 * Downloads [url] to the app's cache directory.
 * Converts OJS galley-view URLs to direct download URLs automatically.
 * Returns the cached [File] immediately if already downloaded.
 */
private suspend fun downloadPdf(context: android.content.Context, rawUrl: String): File =
    withContext(Dispatchers.IO) {
        // OJS: /article/view/{id}/{galleryId} → /article/download/{id}/{galleryId}
        val url = rawUrl.replace("/article/view/", "/article/download/")

        val cacheFile = File(context.cacheDir, "pdf_${url.hashCode()}.pdf")
        if (cacheFile.exists() && cacheFile.length() > 512L) return@withContext cacheFile

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/pdf, */*")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw Exception("সার্ভার সাড়া দেয়নি (${response.code})")

            val bytes = response.body?.bytes()
                ?: throw Exception("PDF ডেটা খালি")

            // Verify PDF magic bytes: %PDF
            if (bytes.size < 4 ||
                bytes[0] != 0x25.toByte() ||   // %
                bytes[1] != 0x50.toByte() ||   // P
                bytes[2] != 0x44.toByte() ||   // D
                bytes[3] != 0x46.toByte()       // F
            ) {
                throw Exception("এই লিংকটি সরাসরি PDF নয়। ব্রাউজারে খুলুন।")
            }

            cacheFile.writeBytes(bytes)
        }

        cacheFile
    }
