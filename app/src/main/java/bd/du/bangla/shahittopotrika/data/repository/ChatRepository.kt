package bd.du.bangla.shahittopotrika.data.repository

import bd.du.bangla.shahittopotrika.data.model.ChatMessage
import bd.du.bangla.shahittopotrika.data.parser.JournalParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Sends user messages to Google Gemini 1.5 Flash and handles the
 * function-calling agentic loop that searches the journal in real time.
 *
 * API key must be provided from BuildConfig.GOOGLE_AI_API_KEY.
 * Get a free key at: https://aistudio.google.com/apikey
 */
class ChatRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL   = "gemini-1.5-flash-latest"
        private const val API_URL =
            "https://generativelanguage.googleapis.com/v1/models/$MODEL:generateContent"

        private const val SYSTEM_PROMPT =
            "তুমি সাহিত্য পত্রিকার একজন সহকারী চ্যাটবট। সাহিত্য পত্রিকা হলো ঢাকা বিশ্ববিদ্যালয়ের " +
            "বাংলা বিভাগের একটি একাডেমিক জার্নাল, ১৯৫৭ সাল থেকে প্রকাশিত। " +
            "ISSN: 0304-9612 | eISSN: 2959-5827।\n\n" +
            "তুমি:\n" +
            "• সবসময় বাংলায় কথা বলবে\n" +
            "• প্রবন্ধ বা সংখ্যার তথ্য জানতে চাইলে সরবরাহকৃত function ব্যবহার করবে\n" +
            "• ব্যবহারকারীদের প্রয়োজনীয় প্রবন্ধ ও সংখ্যা খুঁজে পেতে সাহায্য করবে\n" +
            "• বাংলা সাহিত্য, ভাষা ও একাডেমিক বিষয়ে আলোচনা করতে পারবে\n" +
            "• উত্তর সংক্ষিপ্ত, তথ্যবহুল ও সহজবোধ্য রাখবে\n" +
            "• প্রবন্ধের তথ্য দেওয়ার সময় শিরোনাম ও লেখকের নাম স্পষ্টভাবে উল্লেখ করবে"
    }

    // ── Function declarations (Gemini tool-use format) ────────────────────────

    private val tools = JSONObject().apply {
        put("function_declarations", JSONArray().apply {
            put(funcDecl(
                name = "search_articles",
                desc = "সাহিত্য পত্রিকায় বিষয়, শিরোনাম বা লেখকের নাম দিয়ে প্রবন্ধ খোঁজে।",
                props = mapOf("query" to "সার্চ করার জন্য কীওয়ার্ড (বাংলা বা ইংরেজি)"),
                required = listOf("query")
            ))
            put(funcDecl(
                name = "get_current_issue",
                desc = "সাহিত্য পত্রিকার সর্বশেষ/চলতি সংখ্যার তথ্য ও প্রবন্ধের তালিকা দেয়।",
                props = emptyMap(), required = emptyList()
            ))
            put(funcDecl(
                name = "list_issues",
                desc = "সাহিত্য পত্রিকার সকল প্রকাশিত সংখ্যার তালিকা দেয়।",
                props = emptyMap(), required = emptyList()
            ))
            put(funcDecl(
                name = "get_issue_articles",
                desc = "একটি নির্দিষ্ট সংখ্যার সকল প্রবন্ধের তালিকা দেয়।",
                props = mapOf("issue_url" to "সংখ্যার URL (list_issues থেকে পাওয়া)"),
                required = listOf("issue_url")
            ))
        })
    }

    private fun funcDecl(
        name: String,
        desc: String,
        props: Map<String, String>,   // paramName → description
        required: List<String>
    ) = JSONObject().apply {
        put("name", name)
        put("description", desc)
        put("parameters", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().also { p ->
                props.forEach { (k, d) ->
                    p.put(k, JSONObject().apply { put("type", "STRING"); put("description", d) })
                }
            })
            if (required.isNotEmpty()) {
                put("required", JSONArray().also { a -> required.forEach(a::put) })
            }
        })
    }

    // ── Main entry point ──────────────────────────────────────────────────────

    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {

        var contents = buildContents(conversationHistory)

        repeat(6) { _ ->
            val body = JSONObject().apply {
                put("contents", contents)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
                })
                put("tools", JSONArray().put(tools))
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 2048)
                    put("temperature", 0.7)
                })
            }

            val response   = callApi(body, apiKey)
            val candidates = response.optJSONArray("candidates")
                ?: throw Exception("API থেকে কোনো candidates আসেনি। Response: ${response.toString().take(300)}")

            if (candidates.length() == 0) throw Exception("API থেকে খালি response এসেছে।")

            val candidate = candidates.getJSONObject(0)

            // Blocked by safety filters?
            val finishReason = candidate.optString("finishReason")
            if (finishReason == "SAFETY") throw Exception("নিরাপত্তা ফিল্টার দ্বারা বাধাপ্রাপ্ত।")

            val content = candidate.optJSONObject("content")
                ?: return@withContext "দুঃখিত, উত্তর তৈরি করা সম্ভব হয়নি।"

            val parts = content.getJSONArray("parts")

            // Separate text parts from function-call parts
            val sb    = StringBuilder()
            val calls = mutableListOf<JSONObject>()

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                when {
                    part.has("functionCall") -> calls.add(part.getJSONObject("functionCall"))
                    part.has("text")         -> sb.append(part.getString("text"))
                }
            }

            // No function calls → return the text
            if (calls.isEmpty()) {
                return@withContext sb.toString().trim()
                    .ifBlank { "দুঃখিত, উত্তর তৈরি করা সম্ভব হয়নি।" }
            }

            // Append model's function-call turn
            contents.put(JSONObject().apply {
                put("role", "model")
                put("parts", parts)
            })

            // Execute each function and collect responses
            val responseParts = JSONArray()
            calls.forEach { call ->
                val funcName = call.getString("name")
                val funcArgs = call.optJSONObject("args") ?: JSONObject()
                val result   = executeTool(funcName, funcArgs)
                responseParts.put(JSONObject().apply {
                    put("functionResponse", JSONObject().apply {
                        put("name", funcName)
                        put("response", JSONObject().put("content", result))
                    })
                })
            }

            // Append function responses as next user turn
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", responseParts)
            })
        }

        "দুঃখিত, উত্তর দেওয়া সম্ভব হয়নি।"
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Converts internal ChatMessage list to Gemini `contents` array. */
    private fun buildContents(history: List<ChatMessage>): JSONArray {
        val arr = JSONArray()
        history
            .filter { !it.isWelcome }
            .takeLast(30)
            .forEach { msg ->
                // Gemini uses "model" where Claude uses "assistant"
                val role = if (msg.role == "assistant") "model" else "user"
                arr.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                })
            }
        return arr
    }

    private fun callApi(body: JSONObject, apiKey: String): JSONObject {
        val url = "$API_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .header("content-type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string()
                ?: throw Exception("সার্ভার থেকে কোনো রেসপন্স নেই")
            if (!response.isSuccessful) {
                val errMsg = runCatching {
                    JSONObject(bodyStr).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: bodyStr.take(300)
                throw Exception("Gemini API সমস্যা (HTTP ${response.code}): $errMsg")
            }
            return JSONObject(bodyStr)
        }
    }

    // ── Tool executor (same as before) ────────────────────────────────────────

    private fun executeTool(name: String, input: JSONObject): String {
        return try {
            when (name) {
                "search_articles" -> {
                    val query   = input.getString("query")
                    val results = JournalParser.search(query)
                    if (results.isEmpty()) "\"$query\" সম্পর্কে কোনো প্রবন্ধ পাওয়া যায়নি।"
                    else JSONArray().also { arr ->
                        results.take(6).forEach { a ->
                            arr.put(JSONObject().apply {
                                put("শিরোনাম", a.title)
                                put("লেখক", a.authors)
                                put("সারসংক্ষেপ", a.abstract.take(200))
                                put("url", a.url)
                                if (a.pdfUrl != null) put("pdf", a.pdfUrl)
                            })
                        }
                    }.toString()
                }

                "get_current_issue" -> {
                    val issue    = JournalParser.fetchCurrentIssue()
                        ?: return "চলতি সংখ্যার তথ্য পাওয়া যায়নি।"
                    val articles = JournalParser.fetchArticlesForIssue(issue.url)
                    JSONObject().apply {
                        put("শিরোনাম", issue.title)
                        put("প্রকাশবর্ষ", issue.year)
                        put("url", issue.url)
                        put("মোট_প্রবন্ধ", articles.size)
                        put("প্রবন্ধসমূহ", JSONArray().also { arr ->
                            articles.take(15).forEach { a ->
                                arr.put(JSONObject().apply {
                                    put("শিরোনাম", a.title)
                                    put("লেখক", a.authors)
                                    put("url", a.url)
                                    if (a.pdfUrl != null) put("pdf", a.pdfUrl)
                                })
                            }
                        })
                    }.toString()
                }

                "list_issues" -> {
                    val issues = JournalParser.fetchIssueArchive()
                    if (issues.isEmpty()) "সংখ্যার তালিকা পাওয়া যায়নি।"
                    else JSONArray().also { arr ->
                        issues.take(30).forEach { issue ->
                            arr.put(JSONObject().apply {
                                put("শিরোনাম", issue.title)
                                put("খণ্ড", issue.volume)
                                put("বছর", issue.year)
                                put("url", issue.url)
                            })
                        }
                    }.toString()
                }

                "get_issue_articles" -> {
                    val issueUrl = input.getString("issue_url")
                    val articles = JournalParser.fetchArticlesForIssue(issueUrl)
                    if (articles.isEmpty()) "এই সংখ্যায় কোনো প্রবন্ধ পাওয়া যায়নি।"
                    else JSONArray().also { arr ->
                        articles.forEach { a ->
                            arr.put(JSONObject().apply {
                                put("শিরোনাম", a.title)
                                put("লেখক", a.authors)
                                put("url", a.url)
                                if (a.pdfUrl != null) put("pdf", a.pdfUrl)
                            })
                        }
                    }.toString()
                }

                else -> "{\"ত্রুটি\": \"অজানা function: $name\"}"
            }
        } catch (e: Exception) {
            "{\"ত্রুটি\": \"তথ্য সংগ্রহে সমস্যা: ${e.message}\"}"
        }
    }
}
