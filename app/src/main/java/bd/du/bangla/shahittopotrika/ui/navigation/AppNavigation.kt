package bd.du.bangla.shahittopotrika.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bd.du.bangla.shahittopotrika.ui.screens.*
import bd.du.bangla.shahittopotrika.viewmodel.BookmarkViewModel
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import bd.du.bangla.shahittopotrika.viewmodel.SearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val HOME           = "home"
    const val ISSUE_LIST     = "issue_list"
    const val ARTICLE_LIST   = "article_list/{issueUrl}"
    const val ARTICLE_DETAIL = "article_detail/{articleUrl}"
    const val SEARCH         = "search"
    const val ABOUT          = "about"
    const val BOOKMARKS      = "bookmarks"
    const val PDF_VIEWER     = "pdf_viewer/{pdfUrl}/{title}"
    const val SETTINGS       = "settings"
    const val READ_HISTORY   = "read_history"
    const val NOTES          = "notes/{articleId}/{articleTitle}"

    fun articleList(issueUrl: String) =
        "article_list/${URLEncoder.encode(issueUrl, "UTF-8")}"
    fun articleDetail(articleUrl: String) =
        "article_detail/${URLEncoder.encode(articleUrl, "UTF-8")}"
    fun pdfViewer(pdfUrl: String, title: String = "PDF") =
        "pdf_viewer/${URLEncoder.encode(pdfUrl, "UTF-8")}/${URLEncoder.encode(title, "UTF-8")}"
    fun notes(articleId: String, articleTitle: String) =
        "notes/${URLEncoder.encode(articleId, "UTF-8")}/${URLEncoder.encode(articleTitle, "UTF-8")}"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    deepLinkUrl: String? = null,
    journalVm:  JournalViewModel  = viewModel(),
    searchVm:   SearchViewModel   = viewModel(),
    bookmarkVm: BookmarkViewModel = viewModel()
) {
    // Handle deep links (e.g. https://journal.bangla.du.ac.bd/index.php/sp/article/view/123)
    LaunchedEffect(deepLinkUrl) {
        if (deepLinkUrl != null && deepLinkUrl.contains("/article/view/")) {
            navController.navigate(Routes.articleDetail(deepLinkUrl))
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel        = journalVm,
                onIssueClick     = { navController.navigate(Routes.articleList(it.url)) },
                onSearchClick    = { navController.navigate(Routes.SEARCH) },
                onIssueListClick = { navController.navigate(Routes.ISSUE_LIST) },
                onAboutClick     = { navController.navigate(Routes.ABOUT) },
                onBookmarksClick = { navController.navigate(Routes.BOOKMARKS) },
                onSettingsClick  = { navController.navigate(Routes.SETTINGS) },
                onHistoryClick   = { navController.navigate(Routes.READ_HISTORY) }
            )
        }

        composable(Routes.ISSUE_LIST) {
            IssueListScreen(
                viewModel    = journalVm,
                onIssueClick = { navController.navigate(Routes.articleList(it.url)) },
                onBack       = { navController.popBackStack() }
            )
        }

        composable(Routes.ARTICLE_LIST) { back ->
            val issueUrl = back.arguments?.getString("issueUrl")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ArticleListScreen(
                issueUrl       = issueUrl,
                viewModel      = journalVm,
                onArticleClick = { navController.navigate(Routes.articleDetail(it.url)) },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(Routes.ARTICLE_DETAIL) { back ->
            val articleUrl = back.arguments?.getString("articleUrl")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ArticleDetailScreen(
                articleUrl   = articleUrl,
                viewModel    = journalVm,
                onBack       = { navController.popBackStack() },
                onOpenPdf    = { pdfUrl, title ->
                    navController.navigate(Routes.pdfViewer(pdfUrl, title))
                },
                onNotesClick = { id, title ->
                    navController.navigate(Routes.notes(id, title))
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel      = searchVm,
                journalVm      = journalVm,
                onArticleClick = { navController.navigate(Routes.articleDetail(it.url)) },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(viewModel = journalVm, onBack = { navController.popBackStack() })
        }

        composable(Routes.BOOKMARKS) {
            BookmarkScreen(
                journalVm      = journalVm,
                bookmarkVm     = bookmarkVm,
                onArticleClick = { navController.navigate(Routes.articleDetail(it)) },
                onOpenPdf      = { pdfUrl, title ->
                    navController.navigate(Routes.pdfViewer(pdfUrl, title))
                },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(Routes.PDF_VIEWER) { back ->
            val pdfUrl = back.arguments?.getString("pdfUrl")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            val title  = back.arguments?.getString("title")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: "PDF"
            PdfViewerScreen(
                pdfUrl = pdfUrl,
                title  = title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.READ_HISTORY) {
            ReadHistoryScreen(
                viewModel        = journalVm,
                onArticleClick   = { navController.navigate(Routes.articleDetail(it)) },
                onBack           = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTES) { back ->
            val articleId    = back.arguments?.getString("articleId")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            val articleTitle = back.arguments?.getString("articleTitle")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ArticleNotesScreen(
                articleId    = articleId,
                articleTitle = articleTitle,
                viewModel    = journalVm,
                onBack       = { navController.popBackStack() }
            )
        }
    }
}
