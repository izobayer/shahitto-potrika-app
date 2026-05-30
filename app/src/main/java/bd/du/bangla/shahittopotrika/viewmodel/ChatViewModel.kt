package bd.du.bangla.shahittopotrika.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.BuildConfig
import bd.du.bangla.shahittopotrika.data.model.ChatMessage
import bd.du.bangla.shahittopotrika.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val welcomeMessage = ChatMessage(
        role      = "assistant",
        content   = "আস্‌সালামু আলাইকুম! আমি সাহিত্য পত্রিকার সহকারী।\n\nআপনাকে কীভাবে সাহায্য করতে পারি?\n\n• 📖 চলতি সংখ্যার প্রবন্ধ দেখুন\n• 🔍 কোনো বিষয়ে প্রবন্ধ খুঁজুন\n• ✍️ কোনো লেখকের রচনা জানুন\n• 📚 পুরোনো সংখ্যা দেখুন",
        isWelcome = true
    )

    private val _messages = MutableStateFlow(listOf(welcomeMessage))
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val hasApiKey: Boolean get() = BuildConfig.GOOGLE_AI_API_KEY.isNotBlank()

    // ── Public actions ────────────────────────────────────────────────────────

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        if (!hasApiKey) {
            _messages.value = _messages.value + ChatMessage(
                role    = "assistant",
                content = "⚠️ API কী সেট করা নেই। বিল্ডের সময় GOOGLE_AI_API_KEY পরিবেশ চলক সেট করতে হবে।"
            )
            return
        }

        val userMsg = ChatMessage(role = "user", content = text.trim())
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.sendMessage(
                    conversationHistory = _messages.value,
                    apiKey = BuildConfig.GOOGLE_AI_API_KEY
                )
                _messages.value = _messages.value + ChatMessage(
                    role    = "assistant",
                    content = response
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    role    = "assistant",
                    content = "দুঃখিত, একটি সমস্যা হয়েছে:\n${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(welcomeMessage)
    }
}
