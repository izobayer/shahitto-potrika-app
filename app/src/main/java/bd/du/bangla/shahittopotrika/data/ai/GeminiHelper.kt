package bd.du.bangla.shahittopotrika.data.ai

import bd.du.bangla.shahittopotrika.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GOOGLE_AI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext "ত্রুটি: Gemini API Key কনফিগার করা নেই। অনুগ্রহ করে local.properties চেক করুন।"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        
        // Escape prompt for JSON body
        val escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val jsonBody = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "$escapedPrompt"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(mediaType))
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext "ত্রুটি: সার্ভার সাড়া দেয়নি (${response.code})"
                }
                val text = extractTextFromJson(bodyString)
                if (text.isBlank()) {
                    "দুঃখিত, কোনো উত্তর পাওয়া যায়নি।"
                } else {
                    text
                }
            }
        } catch (e: Exception) {
            "ত্রুটি: নেটওয়ার্ক সংযোগ সমস্যা (${e.message})"
        }
    }

    private fun extractTextFromJson(json: String): String {
        try {
            val partsSplit = json.split("\"parts\"")
            if (partsSplit.size < 2) return ""
            val textSplit = partsSplit[1].split("\"text\"")
            if (textSplit.size < 2) return ""
            val rawText = textSplit[1].substringAfter(":").trim()
            if (!rawText.startsWith("\"")) return ""
            
            var i = 1
            val sb = java.lang.StringBuilder()
            while (i < rawText.length) {
                val char = rawText[i]
                if (char == '\\' && i + 1 < rawText.length) {
                    val next = rawText[i + 1]
                    when (next) {
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        'r' -> sb.append('\r')
                        '\\' -> sb.append('\\')
                        '"' -> sb.append('"')
                        else -> sb.append(next)
                    }
                    i += 2
                } else if (char == '"') {
                    break
                } else {
                    sb.append(char)
                    i++
                }
            }
            return sb.toString()
        } catch (e: Exception) {
            return ""
        }
    }
}
