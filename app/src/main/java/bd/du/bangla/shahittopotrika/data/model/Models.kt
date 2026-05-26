package bd.du.bangla.shahittopotrika.data.model

data class Issue(
    val id: String,
    val title: String,
    val volume: String,
    val number: String,
    val year: String,
    val coverImageUrl: String?,
    val url: String,
    val articleCount: Int = 0
)

data class Article(
    val id: String,
    val title: String,
    val authors: String,
    val abstract: String,
    val url: String,
    val pdfUrl: String?,
    val keywords: List<String> = emptyList(),
    val doi: String? = null
)

data class JournalInfo(
    val name: String,
    val description: String,
    val issn: String,
    val eIssn: String,
    val publisher: String,
    val logoUrl: String?
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
