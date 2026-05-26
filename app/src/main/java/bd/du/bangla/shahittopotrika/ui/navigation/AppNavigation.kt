package bd.du.bangla.shahittopotrika.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bd.du.bangla.shahittopotrika.ui.screens.AboutScreen
import bd.du.bangla.shahittopotrika.ui.screens.ArticleDetailScreen
import bd.du.bangla.shahittopotrika.ui.screens.ArticleListScreen
import bd.du.bangla.shahittopotrika.ui.screens.HomeScreen
import bd.du.bangla.shahittopotrika.ui.screens.IssueListScreen
import bd.du.bangla.shahittopotrika.ui.screens.SearchScreen
import bd.du.bangla.shahittopotrika.viewmodel.JournalViewModel
import bd.du.bangla.shahittopotrika.viewmodel.SearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder

// ── Route constants ────────────────────────────────────────
object Routes {
    const val HOME            = "home"
    const val ISSUE_LIST      = "issue_list"
    const val ARTICLE_LIST    = "article_list/{issueUrl}"
    const val ARTICLE_DETAIL  = "article_detail/{articleUrl}"
    const val SEARCH          = "search"
    const val ABOUT           = "about"

    fun articleList(issueUrl: String)   = "article_list/${URLEncoder.encode(issueUrl, "UTF-8")}"
    fun articleDetail(articleUrl: String) = "article_detail/${URLEncoder.encode(articleUrl, "UTF-8")}"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    journalVm: JournalViewModel = viewModel(),
    searchVm:  SearchViewModel  = viewModel()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel  = journalVm,
                onIssueClick     = { issue ->
                    navController.navigate(Routes.articleList(issue.url))
                },
                onSearchClick    = { navController.navigate(Routes.SEARCH) },
                onIssueListClick = { navController.navigate(Routes.ISSUE_LIST) },
                onAboutClick     = { navController.navigate(Routes.ABOUT) }
            )
        }

        composable(Routes.ISSUE_LIST) {
            IssueListScreen(
                viewModel = journalVm,
                onIssueClick = { issue ->
                    navController.navigate(Routes.articleList(issue.url))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ARTICLE_LIST) { back ->
            val issueUrl = back.arguments?.getString("issueUrl")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ArticleListScreen(
                issueUrl  = issueUrl,
                viewModel = journalVm,
                onArticleClick = { article ->
                    navController.navigate(Routes.articleDetail(article.url))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ARTICLE_DETAIL) { back ->
            val articleUrl = back.arguments?.getString("articleUrl")
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ArticleDetailScreen(
                articleUrl = articleUrl,
                viewModel  = journalVm,
                onBack     = { navController.popBackStack() }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = searchVm,
                journalVm = journalVm,
                onArticleClick = { article ->
                    navController.navigate(Routes.articleDetail(article.url))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                viewModel = journalVm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
