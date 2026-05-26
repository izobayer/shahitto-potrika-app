package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleNotesScreen(
    articleId: String,
    articleTitle: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    // Ensure the ViewModel knows which article's note we're editing
    LaunchedEffect(articleId) { viewModel.setCurrentArticle(articleId) }

    val noteEntity by viewModel.currentNote.collectAsState()
    var noteText by remember(noteEntity) { mutableStateOf(noteEntity?.noteText ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("নোট মুছবেন?") },
            text  = { Text("এই নিবন্ধের নোট মুছে যাবে।") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(articleId)
                    noteText = ""
                    showDeleteDialog = false
                }) { Text("মুছুন", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("বাতিল") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("নোট", fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান",
                            tint = Color.White)
                    }
                },
                actions = {
                    if (noteEntity != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "মুছুন", tint = Color.White)
                        }
                    }
                    IconButton(onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.saveNote(articleId, articleTitle, noteText)
                            scope.launch { snackbarHostState.showSnackbar("নোট সংরক্ষিত হয়েছে") }
                        }
                    }) {
                        Icon(Icons.Default.Save, "সংরক্ষণ করুন", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = articleTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = {
                    Text(
                        "এখানে আপনার নোট লিখুন…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(10.dp),
                label = { Text("নোট") }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.saveNote(articleId, articleTitle, noteText)
                            scope.launch { snackbarHostState.showSnackbar("নোট সংরক্ষিত হয়েছে") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("সংরক্ষণ করুন")
                }
            }
        }
    }
}
