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
 * Sends user messages to Claude API (claude-3-5-haiku) and handles the
 * tool-use agentic loop that searches the journal website in real time.
 */
class ChatRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL   = "claude-3-5-haiku-20241022"
        private const val API_URL = "https://api.anthropic.com/v1/messages"

        private const val SYSTEM_PROMPT = """তুমি সাহিত্য পত্রিকার একজন সহকারী চ্যাটবট। সাহিত্য পত্রিকা হলো ঢাকা বিশ্ববিদ্যালয়ের বাংলা বিভাগের একটি একাডেমিক জার্নাল, ১৯৫৭ সাল থেকে প্রকাশিত। ISSN: 0304-9612 | eISSN: 2959-5827।

তুমি:
• সবসময় বাংলায় কথা বলবে
• প্রবন্ধ বা সংখ্যার তথ্য জানতে চাইলে সরবরাহকৃত tool ব্যবহার করবে
• ব্যবহারকারীদের প্রয়োজনীয় প্রবন্ধ ও সংখ্যা খুঁজে পেতে সাহায্য করবে
• বাংলা সাহিত্য, ভাষা ও একাডেমিক বিষয়ে আলোচনা করতে পারবে
• উত্তর সংক্ষিপ্ত, তথ্যবহুল ও সহজবোধ্য রাখবে
• প্রবন্ধের তথ্য দেওয়ার সময় শিরোনাম ও লেখকের নাম স্পষ্টভাবে উল্লেখ করবে"""
    }

    // ── Tool definitions ──────────────────────────────────────────────────────

    private val tools = JSONArray().apply {
        put(buildTool(
            name = "search_articles",
            description = "সাহিত্য পত্রিকায় বিষয়, শিরোনাম বা লেখকের নাম দিয়ে প্রবন্ধ খোঁজে।",
            properties = mapOf(
                "query" to Pair("string", "সার্চ করার জন্য কীওয়ার্ড (বাংলা বা ইংরেজি)")
            ),
            required = listOf("query")
        ))
        put(buildTool(
            name = "get_current_issue",
            description = "সাহিত্য পত্রিকার সর্বশেষ/চলতি সংখ্যার তথ্য ও প্রবন্ধের তালিকা দেয়।",
            properties = emptyMap(),
            required = emptyList()
        ))
        put(buildTool(
            name = "list_issues",
            description = "সাহিত্য পত্রিকার সকল প্রকাশিত সংখ্যার তালিকা দেয়।",
            properties = emptyMap(),
            required = emptyList()
        ))
        put(buildTool(
            name = "get_issue_articles",
            description = "একটি নির্দিষ্ট সংখ্যার সকল প্রবন্ধের তালিকা দেয়।",
            properties = mapOf(
                "issue_url" to Pair("string", "সংখ্যার URL (list_issues থেকে পাওয়া)")
            ),
            required = listOf("issue_url")
        ))
    }

    private fun buildTool(
        name: String,
        description: String,
        properties: Map<String, Pair<String, String>>,  // name → (type, description)
        required: List<String>
    ) = JSONObject().apply {
        put("name", name)
        put("description", description)
        put("input_schema", JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().also { props ->
                properties.forEach { (k, v) ->
                    props.put(k, JSONObject().apply {
                        put("type", v.first)
                        put("description", v.second)
                    })
                }
            })
            put("required", JSONArray().also { arr -> required.forEach(arr::put) })
        })
    }

    // ── Main entry point ──────────────────────────────────────────────────────

    /**
     * Sends the conversation to Claude and returns the final assistant text,
     * executing any tool calls in between (up to 6 iterations).
     */
    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {

        // Build initial messages JSON (skip welcome/system UI messages)
        var messages = buildMessagesArray(conversationHistory)

        var finalText = ""

        repeat(6) iteration@{ _ ->
            val requestBody = JSONObject().apply {
                put("model", MODEL)
                put("max_tokens", 2048)
                put("system", SYSTEM_PROMPT)
                put("messages", messages)
                put("tools", tools)
            }

            val response   = callApi(requestBody, apiKey)
            val stopReason = response.optString("stop_reason", "end_turn")
            val content    = response.getJSONArray("content")

            when (stopReason) {
                "end_turn", "max_tokens" -> {
                    finalText = extractText(content)
                    return@withContext finalText
                }
                "tool_use" -> {
                    // Append assistant response (which includes tool_use blocks)
                    messages.put(JSONObject().apply {
                        put("role", "assistant")
                        put("content", content)
                    })

                    // Execute each tool call and collect results
                    val toolResults = JSONArray()
                    for (i in 0 until content.length()) {
                        val block = content.getJSONObject(i)
                        if (block.getString("type") == "tool_use") {
                            val toolId   = block.getString("id")
                            val toolName = block.getString("name")
                            val toolInput = block.getJSONObject("input")
                            val result   = executeTool(toolName, toolInput)
                            toolResults.put(JSONObject().apply {
                                put("type", "tool_result")
                                put("tool_use_id", toolId)
                                put("content", result)
                            })
                        }
                    }

                    // Append tool results as a user turn
                    messages.put(JSONObject().apply {
                        put("role", "user")
                        put("content", toolResults)
                    })
                }
                else -> return@withContext extractText(content).ifBlank {
                    "দুঃখিত, উত্তর দেওয়া সম্ভব হয়নি।"
                }
            }
        }

        finalText.ifBlank { "দুঃখিত, উত্তর দেওয়া সম্ভব হয়নি।" }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildMessagesArray(history: List<ChatMessage>): JSONArray {
        val arr = JSONArray()
        history
            .filter { !it.isWelcome }
            .takeLast(30)           // cap context window at last 30 messages
            .forEach { msg ->
                arr.put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
        return arr
    }

    private fun callApi(body: JSONObject, apiKey: String): JSONObject {
        val request = Request.Builder()
            .url(API_URL)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string() ?: throw Exception("সার্ভার থেকে কোনো রেসপন্স নেই")
            if (!response.isSuccessful) {
                val errMsg = runCatching {
                    JSONObject(bodyStr).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: bodyStr.take(200)
                throw Exception("API সমস্যা (HTTP ${response.code}): $errMsg")
            }
            return JSONObject(bodyStr)
        }
    }

    private fun extractText(content: JSONArray): String {
        val sb = StringBuilder()
        for (i in 0 until content.length()) {
            val block = content.getJSONObject(i)
            if (block.getString("type") == "text") {
                sb.append(block.getString("text"))
            }
        }
        return sb.toString().trim()
    }

    // ── Tool executor ─────────────────────────────────────────────────────────

    private fun executeTool(name: String, input: JSONObject): String {
        return try {
            when (name) {
                "search_articles" -> {
                    val query   = input.getString("query")
                    val results = JournalParser.search(query)
                    if (results.isEmpty()) {
                        "\"$query\" সম্পর্কে কোনো প্রবন্ধ পাওয়া যায়নি।"
                    } else {
                        JSONArray().also { arr ->
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
                }

                "get_current_issue" -> {
                    val issue = JournalParser.fetchCurrentIssue()
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
                    if (issues.isEmpty()) {
                        "সংখ্যার তালিকা পাওয়া যায়নি।"
                    } else {
                        JSONArray().also { arr ->
                            issues.take(30).forEach { issue ->
                                arr.put(JSONObject().apply {
                                    put("শিরোনাম", issue.title)
                                    put("খণ্ড", issue.volume)
                                    put("সংখ্যা_নম্বর", issue.number)
                                    put("বছর", issue.year)
                                    put("url", issue.url)
                                })
                            }
                        }.toString()
                    }
                }

                "get_issue_articles" -> {
                    val issueUrl = input.getString("issue_url")
                    val articles = JournalParser.fetchArticlesForIssue(issueUrl)
                    if (articles.isEmpty()) {
                        "এই সংখ্যায় কোনো প্রবন্ধ পাওয়া যায়নি।"
                    } else {
                        JSONArray().also { arr ->
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
                }

                else -> "{\"ত্রুটি\": \"অজানা tool: $name\"}"
            }
        } catch (e: Exception) {
            "{\"ত্রুটি\": \"তথ্য সংগ্রহে সমস্যা হয়েছে: ${e.message}\"}"
        }
    }
}
