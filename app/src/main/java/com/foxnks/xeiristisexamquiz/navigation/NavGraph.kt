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

object Routes {
    const val HOME = "home"
    const val CHAPTER_LIST = "chapterList"
    const val PRACTICE_QUIZ = "practiceQuiz/{chapterId}"
    const val PRACTICE_QUIZ_ARG = "chapterId"
    const val HISTORY = "history"
    const val ABOUT = "about"

    const val EXAM_GRAPH = "examGraph"
    const val EXAM_SCREEN = "examScreen"
    const val EXAM_RESULTS = "examResults"

    fun practiceQuiz(chapterId: Int) = "practiceQuiz/$chapterId"
}

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
            val chapterId = backStackEntry.arguments?.getInt(Routes.PRACTICE_QUIZ_ARG) ?: return@composable
            val viewModel: PracticeQuizViewModel = viewModel(
                key = "practiceQuiz_$chapterId",
                factory = PracticeQuizViewModel.factory(questionRepository, chapterId)
            )
            PracticeQuizScreen(
                viewModel = viewModel,
                onFinished = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        navigation(startDestination = Routes.EXAM_SCREEN, route = Routes.EXAM_GRAPH) {
            composable(Routes.EXAM_SCREEN) { backStackEntry ->
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
                            popUpTo(Routes.EXAM_SCREEN) { inclusive = true }
                        }
                    },
                    // Καθαρίζει ολόκληρο το EXAM_GRAPH από το back stack, ώστε ο χρήστης να
                    // μην μπορεί να γυρίσει "μπροστά" ξανά στο τεστ που μόλις ακύρωσε.
                    onExitClick = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }

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
