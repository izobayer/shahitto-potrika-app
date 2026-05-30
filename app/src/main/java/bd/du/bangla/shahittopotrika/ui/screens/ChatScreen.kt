package bd.du.bangla.shahittopotrika.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.du.bangla.shahittopotrika.data.model.ChatMessage
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val messages   by vm.messages.collectAsState()
    val isLoading  by vm.isLoading.collectAsState()
    val listState  = rememberLazyListState()
    var inputText  by remember { mutableStateOf("") }
    val focusReq   = remember { FocusRequester() }

    // Auto-scroll to bottom whenever the message list grows
    LaunchedEffect(messages.size, isLoading) {
        val target = messages.size - 1 + if (isLoading) 1 else 0
        if (target >= 0) listState.animateScrollToItem(target)
    }

    fun doSend() {
        val text = inputText.trim()
        if (text.isBlank() || isLoading) return
        inputText = ""
        vm.sendMessage(text)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সাহিত্য পত্রিকা সহকারী",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Claude AI চালিত চ্যাটবট",
                            fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, "নতুন কথোপকথন", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                value         = inputText,
                onValueChange = { inputText = it },
                onSend        = ::doSend,
                enabled       = !isLoading,
                focusRequester = focusReq
            )
        }
    ) { padding ->
        LazyColumn(
            state         = listState,
            modifier      = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
            if (isLoading) {
                item(key = "typing") { TypingIndicator() }
            }
        }
    }
}

// ── Message bubble ─────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment   = Alignment.Bottom
    ) {
        if (!isUser) {
            BotAvatar()
            Spacer(Modifier.width(6.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart    = if (isUser) 18.dp else 4.dp,
                topEnd      = if (isUser) 4.dp  else 18.dp,
                bottomStart = 18.dp,
                bottomEnd   = 18.dp
            ),
            color    = if (isUser) Navy else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 296.dp),
            shadowElevation = 1.dp
        ) {
            SelectionContainer {
                Text(
                    text     = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color    = if (isUser) Color.White
                               else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isUser) Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun BotAvatar() {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Navy),
        contentAlignment = Alignment.Center
    ) {
        Text("স", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// ── Typing indicator (three bouncing dots) ─────────────────────────────────────

@Composable
private fun TypingIndicator() {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment   = Alignment.Bottom
    ) {
        BotAvatar()
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot$i")
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f, targetValue = -5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(i * 130)
                        ),
                        label = "dot${i}Y"
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .offset(y = offsetY.dp)
                            .clip(CircleShape)
                            .background(Navy.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

// ── Input bar ──────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    focusRequester: FocusRequester
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder   = { Text("আপনার প্রশ্ন লিখুন…", fontSize = 14.sp) },
                shape         = RoundedCornerShape(24.dp),
                maxLines      = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                enabled = enabled,
                colors  = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Navy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick  = onSend,
                enabled  = enabled && value.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Navy,
                    contentColor   = Color.White,
                    disabledContainerColor = Navy.copy(alpha = 0.3f),
                    disabledContentColor   = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "পাঠান",
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}
