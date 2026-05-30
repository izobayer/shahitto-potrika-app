package bd.du.bangla.shahittopotrika.data.model

import java.util.UUID

/**
 * Represents a single message in the chatbot conversation.
 * [isWelcome] messages are displayed in the UI but never sent to the API.
 */
data class ChatMessage(
    val id: String        = UUID.randomUUID().toString(),
    val role: String,                     // "user" or "assistant"
    val content: String,
    val isWelcome: Boolean = false        // welcome/greeting — UI only
)
