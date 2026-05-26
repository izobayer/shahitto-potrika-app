package bd.du.bangla.shahittopotrika.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.viewinterop.AndroidView
import bd.du.bangla.shahittopotrika.ui.theme.HeaderBg
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import coil.compose.AsyncImage

private const val LOGO_URL =
    "https://journal.bangla.du.ac.bd/public/journals/1/pageHeaderLogoImage_en.png"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    title: String = "PDF",
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Google Docs embedded viewer — works without any extra library
    val viewerUrl = remember(pdfUrl) {
        "https://docs.google.com/gview?embedded=true&url=${Uri.encode(pdfUrl)}"
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasError  by remember { mutableStateOf(false) }
    var progress  by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Scaffold(
        topBar = {
            Column {
                // ── Site header ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeaderBg)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = LOGO_URL,
                            contentDescription = "লোগো",
                            modifier = Modifier.height(40.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        Column {
                            Text("সাহিত্য পত্রিকা",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                            Text("বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
                                fontSize = 10.sp, color = Navy.copy(alpha = 0.7f))
                        }
                    }
                }
                // ── Nav bar ──────────────────────────────────
                TopAppBar(
                    title = {
                        Text(
                            text = title.take(40) + if (title.length > 40) "…" else "",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                                tint = Color.White)
                        }
                    },
                    actions = {
                        // Reload button
                        IconButton(onClick = {
                            hasError = false
                            isLoading = true
                            webViewRef?.loadUrl(viewerUrl)
                        }) {
                            Icon(Icons.Default.Refresh, "রিলোড", tint = Color.White)
                        }
                        // Open in external browser / download
                        IconButton(onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                            )
                        }) {
                            Icon(Icons.Default.Download, "ডাউনলোড", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy,
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier.height(48.dp)
                )
                // ── Progress bar ──────────────────────────────
                if (isLoading && !hasError) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2563EB),
                        trackColor = Navy.copy(alpha = 0.2f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (hasError) {
                // Error state — offer to open externally
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("PDF লোড করা যায়নি", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = {
                            hasError = false
                            isLoading = true
                            webViewRef?.loadUrl(viewerUrl)
                        }) {
                            Text("আবার চেষ্টা")
                        }
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            Text("বাইরে খুলুন")
                        }
                    }
                }
            } else {
                // WebView
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).also { wv ->
                            webViewRef = wv
                            wv.settings.apply {
                                javaScriptEnabled  = true
                                domStorageEnabled  = true
                                loadWithOverviewMode = true
                                useWideViewPort    = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportZoom(true)
                            }
                            wv.webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress
                                    if (newProgress == 100) isLoading = false
                                }
                            }
                            wv.webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) hasError = true
                                }
                            }
                            wv.loadUrl(viewerUrl)
                        }
                    }
                )

                // Loading spinner overlay (initial load)
                if (isLoading && progress < 15) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Navy)
                            Spacer(Modifier.height(12.dp))
                            Text("PDF লোড হচ্ছে…", color = Navy)
                        }
                    }
                }
            }
        }
    }
}
