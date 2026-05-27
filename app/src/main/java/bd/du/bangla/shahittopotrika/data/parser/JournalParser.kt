package bd.du.bangla.shahittopotrika.data.parser

import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.JournalInfo
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

    // ── Issues ──────────────────────────────────────────────
    fun fetchIssueArchive(): List<Issue> {
        val doc = fetch("$BASE_URL/issue/archive")
        return doc.select(".obj_issue_summary").mapIndexed { idx, el ->
            val titleEl = el.selectFirst(".title a") ?: el.selectFirst("h2 a")
            val url = titleEl?.absUrl("href") ?: ""
            val id = url.substringAfterLast("/").ifBlank { idx.toString() }
            val cover = el.selectFirst(".cover img")?.absUrl("src")
            val seriesEl = el.selectFirst(".series, .volume, .pkp_vol_no")
            val title = titleEl?.text() ?: "সংখ্যা ${idx + 1}"
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
        val title = titleEl?.text() ?: "চলতি সংখ্যা"
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
        val authors = cleanAuthors(
            doc.select(".authors .name").joinToString(", ") { it.text() }
                .ifBlank { doc.selectFirst(".authors")?.text() ?: "" }
        )
        val abstract = doc.selectFirst(".abstract p, .abstract")?.text() ?: ""
        val pdfLink = doc.select("a.obj_galley_link.pdf").firstOrNull()?.absUrl("href")
            ?: doc.select("a.obj_galley_link").firstOrNull()?.absUrl("href")
        val keywords = doc.select(".keywords .value a, .keywords span").map { it.text() }
        val doi = doc.selectFirst(".doi a")?.text()
        val id = articleUrl.substringAfterLast("/")
        return Article(
            id = id, title = title, authors = authors, abstract = abstract,
            url = articleUrl, pdfUrl = pdfLink, keywords = keywords, doi = doi
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
                authors = el.selectFirst(".authors")?.text() ?: "",
                abstract = el.selectFirst(".abstract")?.text() ?: "",
                url = articleUrl, pdfUrl = null
            )
        }.filterNotNull()
    }

    // ── Author name cleaning ───────────────────────────────────
    /**
     * Removes stray non-letter prefix characters that OJS sometimes emits
     * before author names (e.g. "►", "•", Unicode directional marks, NBSP).
     */
    private fun cleanAuthors(raw: String): String {
        if (raw.isBlank()) return raw
        // Split by comma, clean each name, rejoin
        return raw.split(",").joinToString(", ") { part ->
            part.trim()
                // Drop any leading characters that are NOT letters, digits, or spaces
                .trimStart { c -> !c.isLetterOrDigit() && c != ' ' }
                .trim()
        }.trim(',', ' ')
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
}
