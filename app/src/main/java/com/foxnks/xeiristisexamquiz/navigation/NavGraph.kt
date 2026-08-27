package com.foxnks.xeiristisexamquiz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foxnks.xeiristisexamquiz.data.ExamConfigRepository
import com.foxnks.xeiristisexamquiz.data.ExamStateRepository
import com.foxnks.xeiristisexamquiz.data.ProgressRepository
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.ui.about.AboutScreen
import com.foxnks.xeiristisexamquiz.ui.chapterlist.ChapterListScreen
import com.foxnks.xeiristisexamquiz.ui.chapterlist.ChapterListViewModel
import com.foxnks.xeiristisexamquiz.ui.exam.ExamScreen
import com.foxnks.xeiristisexamquiz.ui.exam.ExamViewModel
import com.foxnks.xeiristisexamquiz.ui.history.HistoryScreen
import com.foxnks.xeiristisexamquiz.ui.history.HistoryViewModel
import com.foxnks.xeiristisexamquiz.ui.home.HomeScreen
import com.foxnks.xeiristisexamquiz.ui.practice.PracticeQuizScreen
import com.foxnks.xeiristisexamquiz.ui.practice.PracticeQuizViewModel
import com.foxnks.xeiristisexamquiz.ui.results.ExamResultsScreen

// ─────────────────────────────────────────────────────────────────────────
// Design note: sharing one ViewModel between ExamScreen and ExamResultsScreen
// ─────────────────────────────────────────────────────────────────────────
// Problem: ExamScreen and ExamResultsScreen are two different routes, but they need
// the EXACT SAME ViewModel instance — the results screen must remember the answers
// the user gave during the exam. Normally, every composable route gets its own,
// separate ViewModel.
//
// Solution: both routes are wrapped in a shared "sub-graph" (navigation(...) with
// route EXAM_GRAPH). Instead of asking for a ViewModel "in their own name", they ask
// for it "in the name of the parent graph" (examGraphEntry, obtained via
// navController.getBackStackEntry(Routes.EXAM_GRAPH)). Android keeps that parent
// backstack entry alive for as long as ANY screen inside the sub-graph is active, so
// both routes receive the exact same ExamViewModel instance — and once we leave
// EXAM_GRAPH entirely (e.g. "Back to home"), the ViewModel is destroyed along with it.
//
// popBackStack / popUpTo / inclusive:
// - popBackStack() alone is the plain "back" button.
// - popUpTo(route) { inclusive = true } clears the stack up to AND INCLUDING that
//   route. Used when moving from the exam to the results screen, so the user can't
//   navigate back INTO the exam after submitting it.
// - popBackStack(Routes.HOME, inclusive = false) means "pop back up to HOME, but keep
//   HOME itself" — this is how we clear the entire EXAM_GRAPH off the stack.
//
// ─────────────────────────────────────────────────────────────────────────
// Σχεδιαστική σημείωση: κοινό ViewModel μεταξύ ExamScreen και ExamResultsScreen
// ─────────────────────────────────────────────────────────────────────────
// Πρόβλημα: το ExamScreen και το ExamResultsScreen είναι δύο διαφορετικά routes, αλλά
// χρειάζονται το ΙΔΙΟ ΑΚΡΙΒΩΣ ViewModel instance — τα αποτελέσματα πρέπει να θυμούνται
// τις απαντήσεις που έδωσε ο χρήστης στο τεστ. Κανονικά, κάθε composable route παίρνει
// το δικό του, ξεχωριστό ViewModel.
//
// Λύση: τα δύο routes «τυλίγονται» μέσα σε ένα κοινό υπο-γράφημα (navigation(...) με
// route EXAM_GRAPH). Αντί να ζητάνε ViewModel «στο όνομά τους», ζητάνε «στο όνομα του
// γονικού γράφου» (examGraphEntry, μέσω navController.getBackStackEntry(Routes.EXAM_GRAPH)).
// Το Android κρατάει ζωντανό αυτό το γονικό backstack entry όσο ΟΠΟΙΑΔΗΠΟΤΕ οθόνη μέσα
// στο υπο-γράφημα είναι ενεργή, άρα και τα δύο routes παίρνουν ακριβώς το ίδιο instance
// ExamViewModel — και όταν φύγουμε εντελώς από το EXAM_GRAPH (π.χ. «Επιστροφή στην
// αρχική»), το ViewModel καταστρέφεται μαζί του.
//
// popBackStack / popUpTo / inclusive:
// - Το popBackStack() μόνο του είναι το απλό κουμπί «πίσω».
// - Το popUpTo(route) { inclusive = true } σβήνει τη στοίβα ΜΕΧΡΙ ΚΑΙ το συγκεκριμένο
//   route. Χρησιμοποιείται όταν πάμε από το τεστ στα αποτελέσματα, ώστε ο χρήστης να
//   μην μπορεί να γυρίσει πίσω ΜΕΣΑ στο τεστ μετά την υποβολή.
// - Το popBackStack(Routes.HOME, inclusive = false) σημαίνει «γύρνα πίσω μέχρι το HOME,
//   αλλά κράτα το HOME» — έτσι καθαρίζεται όλο το EXAM_GRAPH από τη στοίβα.
// ─────────────────────────────────────────────────────────────────────────

/**
 * All the app's navigation routes, gathered in one place so there are no "magic strings"
 * scattered around the codebase.
 * Όλες οι διαδρομές (routes) πλοήγησης της εφαρμογής, μαζεμένες σε ένα σημείο ώστε να
 * μην υπάρχουν "μαγικά strings" σκόρπια στον κώδικα.
 */
object Routes {
    const val HOME = "home"
    const val CHAPTER_LIST = "chapterList"
    const val PRACTICE_QUIZ = "practiceQuiz/{chapterId}"
    const val PRACTICE_QUIZ_ARG = "chapterId"
    const val HISTORY = "history"
    const val ABOUT = "about"

    // Groups ExamScreen + ExamResultsScreen into their own navigation "sub-graph", so they
    // can share the same ExamViewModel - see comment further below.
    // Ομαδοποιεί ExamScreen + ExamResultsScreen σε ένα δικό τους "υπο-γράφημα" πλοήγησης,
    // ώστε να μοιράζονται το ίδιο ExamViewModel - βλέπε σχόλιο πιο κάτω.
    const val EXAM_GRAPH = "examGraph"
    const val EXAM_SCREEN = "examScreen"
    const val EXAM_RESULTS = "examResults"

    // Builds the route "practiceQuiz/5" for the chapter with id 5
    // Χτίζει το route "practiceQuiz/5" για το κεφάλαιο με id 5
    fun practiceQuiz(chapterId: Int) = "practiceQuiz/$chapterId"
}

/**
 * The navigation "map" of the whole app: which screens exist, what route each one has,
 * and what ViewModel/dependencies each one needs to be set up.
 * Ο "χάρτης" πλοήγησης όλης της εφαρμογής: ποιες οθόνες υπάρχουν, ποιο route έχει η
 * καθεμία, και τι ViewModel/εξαρτήσεις χρειάζεται για να στηθεί.
 */
@Composable
fun AppNavGraph(
    questionRepository: QuestionRepository,
    examConfigRepository: ExamConfigRepository,
    progressRepository: ProgressRepository,
    examStateRepository: ExamStateRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onPracticeClick = { navController.navigate(Routes.CHAPTER_LIST) },
                onExamClick = { navController.navigate(Routes.EXAM_GRAPH) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onAboutClick = { navController.navigate(Routes.ABOUT) }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.factory(progressRepository)
            )
            HistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CHAPTER_LIST) {
            val viewModel: ChapterListViewModel = viewModel(
                factory = ChapterListViewModel.factory(questionRepository)
            )
            ChapterListScreen(
                viewModel = viewModel,
                onChapterClick = { chapterId ->
                    navController.navigate(Routes.practiceQuiz(chapterId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PRACTICE_QUIZ,
            arguments = listOf(navArgument(Routes.PRACTICE_QUIZ_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            // Reads the chapterId from the route (e.g. "practiceQuiz/5" -> 5)
            // Διαβάζει το chapterId από το route (π.χ. "practiceQuiz/5" -> 5)
            val chapterId = backStackEntry.arguments?.getInt(Routes.PRACTICE_QUIZ_ARG) ?: return@composable
            val viewModel: PracticeQuizViewModel = viewModel(
                // key: a separate ViewModel per chapter, so it doesn't "carry over" stale
                // data if the user enters a different chapter
                // key: ξεχωριστό ViewModel ανά κεφάλαιο, ώστε να μην "κουβαλάει" παλιά
                // δεδομένα αν ο χρήστης μπει σε άλλο κεφάλαιο
                key = "practiceQuiz_$chapterId",
                factory = PracticeQuizViewModel.factory(questionRepository, chapterId)
            )
            PracticeQuizScreen(
                viewModel = viewModel,
                onFinished = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Nested navigation graph for the final exam: ExamScreen + ExamResultsScreen together
        // under the EXAM_GRAPH route.
        // Υπο-γράφημα (nested navigation graph) για το τελικό τεστ: ExamScreen + ExamResultsScreen
        // μαζί κάτω από το route EXAM_GRAPH.
        navigation(startDestination = Routes.EXAM_SCREEN, route = Routes.EXAM_GRAPH) {
            composable(Routes.EXAM_SCREEN) { backStackEntry ->
                // Gets the backstack entry of the PARENT graph (EXAM_GRAPH), not of the
                // current screen, and uses it as the ViewModel's "owner".
                // Παίρνει το backstack entry του ΓΟΝΙΚΟΥ γράφου (EXAM_GRAPH), όχι της
                // τρέχουσας οθόνης, και το χρησιμοποιεί σαν "ιδιοκτήτη" του ViewModel.
                val examGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.EXAM_GRAPH)
                }
                val viewModel: ExamViewModel = viewModel(
                    viewModelStoreOwner = examGraphEntry,
                    factory = ExamViewModel.factory(
                        questionRepository,
                        examConfigRepository,
                        progressRepository,
                        examStateRepository
                    )
                )
                ExamScreen(
                    viewModel = viewModel,
                    onSubmitted = {
                        navController.navigate(Routes.EXAM_RESULTS) {
                            // Removes EXAM_SCREEN from the back stack, so the "back" button
                            // from the results screen doesn't return to the exam
                            // Αφαιρεί το EXAM_SCREEN από το back stack, ώστε το πλήκτρο
                            // "πίσω" από τα αποτελέσματα να μην ξαναγυρίζει στο τεστ
                            popUpTo(Routes.EXAM_SCREEN) { inclusive = true }
                        }
                    },
                    // Clears the entire EXAM_GRAPH from the back stack, so the user can't
                    // navigate "forward" back into the exam they just cancelled.
                    // Καθαρίζει ολόκληρο το EXAM_GRAPH από το back stack, ώστε ο χρήστης να
                    // μην μπορεί να γυρίσει "μπροστά" ξανά στο τεστ που μόλις ακύρωσε.
                    onExitClick = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }

            // SAME viewModelStoreOwner (examGraphEntry) as ExamScreen above, so viewModel()
            // here returns the EXACT SAME ExamViewModel instance - not a new one. This way
            // the results screen sees exactly the state the exam left behind.
            // ΙΔΙΟ viewModelStoreOwner (examGraphEntry) με το ExamScreen παραπάνω, άρα
            // το viewModel() εδώ επιστρέφει ΤΟ ΙΔΙΟ instance ExamViewModel - όχι νέο.
            // Έτσι τα αποτελέσματα βλέπουν ακριβώς την ίδια κατάσταση που άφησε το τεστ.
            composable(Routes.EXAM_RESULTS) { backStackEntry ->
                val examGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.EXAM_GRAPH)
                }
                val viewModel: ExamViewModel = viewModel(
                    viewModelStoreOwner = examGraphEntry,
                    factory = ExamViewModel.factory(
                        questionRepository,
                        examConfigRepository,
                        progressRepository,
                        examStateRepository
                    )
                )
                ExamResultsScreen(
                    viewModel = viewModel,
                    onBackToHome = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }
        }
    }
}
