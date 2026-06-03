package bd.du.bangla.shahittopotrika.data.parser

import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.JournalInfo
import bd.du.bangla.shahittopotrika.data.model.Author
import bd.du.bangla.shahittopotrika.data.model.AuthorDetails
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object JournalParser {

    private const val BASE_URL = "https://journal.bangla.du.ac.bd/index.php/sp"

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private fun fetch(url: String): Document {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: ""
        return Jsoup.parse(html, url)
    }

    private fun cleanIssueTitle(raw: String): String {
        return raw.replace("সাহিত্য পত্রিকা:", "")
                  .replace("সাহিত্য পত্রিকা :", "")
                  .trim()
    }

    // ── Issues ──────────────────────────────────────────────
    fun fetchIssueArchive(): List<Issue> {
        val doc = fetch("$BASE_URL/issue/archive")
        return doc.select(".obj_issue_summary").mapIndexed { idx, el ->
            val titleEl = el.selectFirst(".title a") ?: el.selectFirst("h2 a")
            val url = titleEl?.absUrl("href") ?: ""
            val id = url.substringAfterLast("/").ifBlank { idx.toString() }
            val cover = el.selectFirst(".cover img")?.absUrl("src")
            val seriesEl = el.selectFirst(".series, .volume, .pkp_vol_no")
            val title = cleanIssueTitle(titleEl?.text() ?: "সংখ্যা ${idx + 1}")
            val volumeText = seriesEl?.text() ?: ""
            val year = el.selectFirst(".date, .published")?.text()?.take(4) ?: ""
            Issue(
                id = id, title = title,
                volume = volumeText.substringBefore(",").trim(),
                number = volumeText.substringAfter(",").trim(),
                year = year, coverImageUrl = cover, url = url
            )
        }
    }

    fun fetchCurrentIssue(): Issue? {
        val doc = fetch("$BASE_URL/issue/current")
        val titleEl = doc.selectFirst(".obj_issue_toc .heading h2, h1.title")
        val cover = doc.selectFirst(".cover img, .pkp_structure_main img.cover")?.absUrl("src")
        val url = "$BASE_URL/issue/current"
        val title = cleanIssueTitle(titleEl?.text() ?: "চলতি সংখ্যা")
        val id = doc.selectFirst("link[rel=canonical]")?.attr("href")
            ?.substringAfterLast("/") ?: "current"
        return Issue(id = id, title = title, volume = "", number = "", year = "", coverImageUrl = cover, url = url)
    }

    // ── Articles ─────────────────────────────────────────────
    fun fetchArticlesForIssue(issueUrl: String): List<Article> {
        val doc = fetch(issueUrl)
        val articles = mutableListOf<Article>()
        doc.select(".obj_article_summary").forEach { el ->
            val titleEl = el.selectFirst(".title a") ?: return@forEach
            val articleUrl = titleEl.absUrl("href")
            val id = articleUrl.substringAfterLast("/").ifBlank { articleUrl.hashCode().toString() }
            val authors = cleanAuthors(el.select(".authors").text())
            val abstract = el.selectFirst(".abstract")?.text() ?: ""
            val pdfEl = el.selectFirst("a.obj_galley_link.pdf")
            articles.add(
                Article(
                    id = id, title = titleEl.text(), authors = authors,
                    abstract = abstract, url = articleUrl,
                    pdfUrl = pdfEl?.absUrl("href")
                )
            )
        }
        return articles
    }

    fun fetchArticleDetail(articleUrl: String): Article {
        val doc = fetch(articleUrl)
        val title = doc.selectFirst("h1.title, .page_article h1")?.text() ?: ""

        // Authors block — try detailed author elements first
        val authorElements = doc.select(".authors .author")
        val authors = if (authorElements.isNotEmpty()) {
            cleanAuthors(
                authorElements.joinToString(", ") { el ->
                    el.selectFirst(".name")?.text() ?: el.text()
                }
            )
        } else {
            cleanAuthors(
                doc.select(".authors .name").joinToString(", ") { it.text() }
                    .ifBlank { doc.selectFirst(".authors")?.text() ?: "" }
            )
        }

        // Author photo — OJS puts it in .author .photo img or figure.photo img
        val firstAuthorEl = doc.selectFirst(".authors .author")
        val authorPhotoUrl = firstAuthorEl?.selectFirst(
            "img.photo, .photo img, figure.photo img, .author-photo img"
        )?.absUrl("src")?.takeIf { it.isNotBlank() }

        // Author affiliation
        val authorAffiliation = firstAuthorEl?.selectFirst(
            ".affiliation, span.affiliation"
        )?.text()?.takeIf { it.isNotBlank() }

        val abstract = doc.selectFirst(".abstract p, .abstract")?.text() ?: ""
        val pdfLink = doc.select("a.obj_galley_link.pdf").firstOrNull()?.absUrl("href")
            ?: doc.select("a.obj_galley_link").firstOrNull()?.absUrl("href")
        val keywords = doc.select(".keywords .value a, .keywords span").map { it.text() }
        val doi = doc.selectFirst(".doi a")?.text()
        val id = articleUrl.substringAfterLast("/")
        return Article(
            id = id, title = title, authors = authors, abstract = abstract,
            url = articleUrl, pdfUrl = pdfLink, keywords = keywords, doi = doi,
            authorPhotoUrl = authorPhotoUrl, authorAffiliation = authorAffiliation
        )
    }

    // ── Search ───────────────────────────────────────────────
    fun search(query: String): List<Article> {
        val url = "$BASE_URL/search/search?query=${query.replace(" ", "+")}"
        val doc = fetch(url)
        return doc.select(".obj_article_summary").map { el ->
            val titleEl = el.selectFirst(".title a") ?: return@map null
            val articleUrl = titleEl.absUrl("href")
            val id = articleUrl.substringAfterLast("/")
            Article(
                id = id, title = titleEl.text(),
                authors = cleanAuthors(el.selectFirst(".authors")?.text() ?: ""),
                abstract = el.selectFirst(".abstract")?.text() ?: "",
                url = articleUrl, pdfUrl = null
            )
        }.filterNotNull()
    }

    // ── Author name cleaning ───────────────────────────────────
    /**
     * Removes stray non-letter prefix characters that OJS sometimes emits
     * before author names (e.g. "►", "•", Unicode directional marks, NBSP).
     * Uses \p{L} / \p{N} so Bengali (and all Unicode) letters are recognised.
     */
    private val leadingJunkRe = Regex("^[^\\p{L}\\p{N}]+")

    private fun cleanAuthors(raw: String): String {
        if (raw.isBlank()) return raw
        return raw.split(Regex("[,;]+"))
            .map { part -> part.trim().replace(leadingJunkRe, "").trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    // ── Journal Info ──────────────────────────────────────────
    fun fetchJournalInfo(): JournalInfo {
        val doc = fetch("$BASE_URL/about")
        val desc = doc.selectFirst(".pkp_page_about .description, .pkp_page_about p")?.text() ?: ""
        val logo = doc.selectFirst(".pkp_site_name img, .header_banner_img")?.absUrl("src")
        return JournalInfo(
            name = "সাহিত্য পত্রিকা",
            description = desc,
            issn = "0304-9612",
            eIssn = "2959-5827",
            publisher = "বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়",
            logoUrl = logo
        )
    }

    // ── Manual User Registration on Website ──────────────────────
    fun registerUserOnWebsite(
        name: String,
        email: String,
        username: String,
        password: String,
        affiliation: String = "App Client"
    ): Result<Unit> = kotlin.runCatching {
        val registerUrl = "$BASE_URL/user/register"
        val getRequest = Request.Builder()
            .url(registerUrl)
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
            .header("Accept", "text/html,application/xhtml+xml")
            .build()
            
        val getResponse = client.newCall(getRequest).execute()
        val cookies = getResponse.headers("Set-Cookie")
        val html = getResponse.body?.string() ?: ""
        val doc = Jsoup.parse(html, registerUrl)
        
        val csrfToken = doc.selectFirst("input[name=csrfToken]")?.attr("value")
            ?: throw Exception("CSRF token not found on registration page")
            
        val nameParts = name.trim().split(" ", limit = 2)
        val givenName = nameParts.firstOrNull() ?: name
        val familyName = if (nameParts.size > 1) nameParts[1] else ""
        
        val formBuilder = okhttp3.FormBody.Builder()
            .add("csrfToken", csrfToken)
            .add("givenName", givenName)
            .add("familyName", familyName)
            .add("email", email)
            .add("username", username)
            .add("password", password)
            .add("passwordAgain", password)
            .add("country", "BD")
            .add("affiliation", affiliation)
            .add("privacyConsent", "1")
            .add("emailConsent", "1")
            
        val postRequest = Request.Builder()
            .url(registerUrl)
            .post(formBuilder.build())
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
            .header("Accept", "text/html,application/xhtml+xml")
            
        cookies.forEach { cookie ->
            val cookieValue = cookie.substringBefore(";")
            postRequest.addHeader("Cookie", cookieValue)
        }
        
        val postResponse = client.newCall(postRequest.build()).execute()
        val postHtml = postResponse.body?.string() ?: ""
        
        if (postResponse.code == 200 && (postHtml.contains("error") || postHtml.contains("required") || postHtml.contains("invalid"))) {
            val errDoc = Jsoup.parse(postHtml)
            val errorMsg = errDoc.select(".pkp_form_error, .error, .notification-error").text().trim()
            if (errorMsg.isNotBlank()) {
                throw Exception(errorMsg)
            }
            throw Exception("রেজিস্ট্রেশন ব্যর্থ হয়েছে। দয়া করে সব তথ্য পুনরায় যাচাই করুন।")
        }
        
        if (postResponse.code >= 400) {
            throw Exception("সার্ভার রেসপন্স কোড: ${postResponse.code}")
        }
    }

    // ── Authors List & Details Scraper ──────────────────────────
    fun fetchAuthorList(): List<Author> {
        val doc = fetch("https://journal.bangla.du.ac.bd/author")
        return doc.select(".premium-author-card").map { el ->
            val name = el.attr("data-name")
            val portraitImg = el.selectFirst(".author-portrait img")?.absUrl("src")
            val link = el.selectFirst("a.card-top")?.absUrl("href") ?: ""
            val id = java.net.URLDecoder.decode(link.substringAfter("/author/view/"), "UTF-8").ifBlank { name }
            val firstLetter = el.attr("data-first-letter").ifBlank { name.take(1) }
            Author(name = name, id = id, photoUrl = portraitImg, firstLetter = firstLetter)
        }
    }

    fun fetchAuthorDetails(authorId: String): AuthorDetails {
        val authorUrl = "https://journal.bangla.du.ac.bd/author/view/${java.net.URLEncoder.encode(authorId, "UTF-8").replace("+", "%20")}"
        val doc = fetch(authorUrl)
        val headerEl = doc.selectFirst(".author_header")
        val name = headerEl?.selectFirst("h1")?.text() ?: authorId
        val photoUrl = headerEl?.selectFirst("img")?.absUrl("src")
        val title = headerEl?.select("div")?.getOrNull(1)?.text()

        val articles = mutableListOf<Article>()
        doc.select(".obj_article_summary").forEach { el ->
            val titleEl = el.selectFirst(".title a") ?: return@forEach
            val articleUrl = titleEl.absUrl("href")
            val id = articleUrl.substringAfterLast("/").ifBlank { articleUrl.hashCode().toString() }
            val authors = cleanAuthors(el.select(".authors").text())
            val abstract = el.selectFirst(".abstract")?.text() ?: ""
            val pdfEl = el.selectFirst("a.obj_galley_link.pdf")
            articles.add(
                Article(
                    id = id, title = titleEl.text(), authors = authors,
                    abstract = abstract, url = articleUrl,
                    pdfUrl = pdfEl?.absUrl("href")
                )
            )
        }
        return AuthorDetails(name = name, photoUrl = photoUrl, title = title, articles = articles)
    }
}
