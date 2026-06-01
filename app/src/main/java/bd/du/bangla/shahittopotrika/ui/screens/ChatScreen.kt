package bd.du.bangla.shahittopotrika.ui.screens

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bd.du.bangla.shahittopotrika.data.model.ChatMessage
import bd.du.bangla.shahittopotrika.ui.theme.DarkBg
import bd.du.bangla.shahittopotrika.ui.theme.DarkSurface
import bd.du.bangla.shahittopotrika.ui.theme.Navy
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkHigh
import bd.du.bangla.shahittopotrika.ui.theme.OnDarkMed
import bd.du.bangla.shahittopotrika.ui.theme.TealAccent
import bd.du.bangla.shahittopotrika.viewmodel.ChatViewModel
import java.util.Locale

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
    val context    = LocalContext.current

    // ── TTS support for AI messages ──────────────────────────────────────────
    var tts          by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsActiveId  by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                t.setLanguage(Locale("bn", "BD"))
            }
        }
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (ttsActiveId == utteranceId) ttsActiveId = null
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (ttsActiveId == utteranceId) ttsActiveId = null
            }
        })
        tts = t
        onDispose {
            t.stop()
            t.shutdown()
            tts = null
        }
    }

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
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সাহিত্য পত্রিকা সহকারী",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Gemini AI চালিত চ্যাটবট",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        tts?.stop()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "ফিরে যান", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tts?.stop()
                        ttsActiveId = null
                        vm.clearChat()
                    }) {
                        Icon(Icons.Default.DeleteSweep, "নতুন কথোপকথন", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface, titleContentColor = Color.White
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
                .background(DarkBg),
            contentPadding        = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message     = msg,
                    ttsActiveId = ttsActiveId,
                    onTtsToggle = {
                        if (ttsActiveId == msg.id) {
                            tts?.stop()
                            ttsActiveId = null
                        } else {
                            tts?.stop()
                            ttsActiveId = msg.id
                            // Filter out markdown/bullet symbols for cleaner reading
                            val speakText = msg.content
                                .replace("•", "")
                                .replace("*", "")
                            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, msg.id)
                        }
                    }
                )
            }
            if (isLoading) {
                item(key = "typing") { TypingIndicator() }
            }
        }
    }
}

// ── Message bubble ─────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    ttsActiveId: String?,
    onTtsToggle: () -> Unit
) {
    val isUser = message.role == "user"
    val isTtsSpeaking = ttsActiveId == message.id

    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment   = Alignment.Bottom
    ) {
        if (!isUser) {
            BotAvatar()
            Spacer(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(
                    topStart    = if (isUser) 18.dp else 4.dp,
                    topEnd      = if (isUser) 4.dp  else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd   = 18.dp
                ),
                color    = if (isUser) TealAccent else DarkSurface,
                modifier = Modifier.widthIn(max = 280.dp),
                shadowElevation = 1.dp
            ) {
                SelectionContainer {
                    Text(
                        text       = message.content,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                        color      = if (isUser) Color(0xFF003730) else OnDarkHigh
                    )
                }
            }

            // Audio Player icon below Bot message bubbles
            if (!isUser && message.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    IconButton(
                        onClick  = onTtsToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isTtsSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isTtsSpeaking) "শোনা বন্ধ করুন" else "লেখাটি শুনুন",
                            tint = if (isTtsSpeaking) TealAccent else OnDarkMed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (isTtsSpeaking) "পড়া হচ্ছে…" else "শুনুন",
                        fontSize = 11.sp,
                        color = if (isTtsSpeaking) TealAccent else OnDarkMed
                    )
                }
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
            .background(TealAccent),
        contentAlignment = Alignment.Center
    ) {
        Text("স", color = Color(0xFF003730), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            color = DarkSurface
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
                            .background(TealAccent.copy(alpha = 0.6f))
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
        color = DarkSurface,
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
                placeholder   = { Text("আপনার প্রশ্ন লিখুন…", fontSize = 14.sp, color = OnDarkLow) },
                shape         = RoundedCornerShape(24.dp),
                maxLines      = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                enabled = enabled,
                colors  = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealAccent,
                    unfocusedBorderColor = TealAccent.copy(alpha = 0.3f),
                    focusedTextColor     = OnDarkHigh,
                    unfocusedTextColor   = OnDarkHigh
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick  = onSend,
                enabled  = enabled && value.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = TealAccent,
                    contentColor   = Color(0xFF003730),
                    disabledContainerColor = TealAccent.copy(alpha = 0.3f),
                    disabledContentColor   = Color(0xFF003730).copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "পাঠান",
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}
