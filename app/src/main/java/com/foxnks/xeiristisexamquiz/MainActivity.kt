package com.foxnks.xeiristisexamquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.foxnks.xeiristisexamquiz.data.ExamConfigRepository
import com.foxnks.xeiristisexamquiz.data.ExamStateRepository
import com.foxnks.xeiristisexamquiz.data.ProgressRepository
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.navigation.AppNavGraph
import com.foxnks.xeiristisexamquiz.ui.common.ErrorScreen
import com.foxnks.xeiristisexamquiz.ui.common.LoadingScreen
import com.foxnks.xeiristisexamquiz.ui.theme.XeiristisExamQuizTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * The app's single Activity - everything else is Compose screens navigated within it (a
 * standard modern-Android setup, no separate Activity per screen).
 * Η μοναδική Activity της εφαρμογής - όλα τα υπόλοιπα είναι Compose οθόνες που
 * πλοηγούνται μέσα σε αυτήν (στάνταρ σύγχρονη ρύθμιση Android, όχι ξεχωριστή Activity ανά
 * οθόνη).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XeiristisExamQuizApp()
        }
    }
}

// Minimum display time for LoadingScreen, so it doesn't "flash" briefly when the question
// bank loads faster than this.
// Ελάχιστος χρόνος εμφάνισης του LoadingScreen, ώστε να μη "τρεμοπαίζει" σαν flash όταν το
// question bank φορτώνει πιο γρήγορα απ' αυτό.
private const val MIN_LOADING_SCREEN_DURATION_MS = 2500L

/**
 * Loading/parsing the bundled question bank and opening the local database are both
 * boundary operations (file I/O, JSON parsing) that can fail on a corrupt or missing asset
 * - hence the explicit [Error] state instead of letting an exception crash the app at
 * startup.
 * Το φόρτωμα/parsing της ενσωματωμένης τράπεζας ερωτήσεων και το άνοιγμα της τοπικής
 * βάσης είναι και τα δύο λειτουργίες ορίου (file I/O, JSON parsing) που μπορούν να
 * αποτύχουν σε αλλοιωμένο ή απόν asset - εξ ου και η ρητή κατάσταση [Error] αντί να αφήσουμε
 * ένα exception να κρασάρει την εφαρμογή στην εκκίνηση.
 */
private sealed interface AppInitState {
    data object Loading : AppInitState
    data class Error(val message: String) : AppInitState
    data class Ready(
        val questionRepository: QuestionRepository,
        val examConfigRepository: ExamConfigRepository,
        val progressRepository: ProgressRepository,
        val examStateRepository: ExamStateRepository
    ) : AppInitState
}

@Composable
private fun XeiristisExamQuizApp() {
    val context = LocalContext.current.applicationContext
    var initState by remember { mutableStateOf<AppInitState>(AppInitState.Loading) }
    val unknownErrorMessage = stringResource(R.string.error_unknown_data_load)

    LaunchedEffect(Unit) {
        coroutineScope {
            // Starts at the same time as the actual loading; we wait at the end for
            // whichever of the two finishes last, so a fast load doesn't make the
            // LoadingScreen flash, but a slow load isn't delayed any further.
            // Ξεκινάει ταυτόχρονα με το πραγματικό load· περιμένουμε στο τέλος όποιο από
            // τα δύο τελειώσει τελευταίο, ώστε ένα γρήγορο load να μην κάνει flash το
            // LoadingScreen, αλλά ένα αργό load να μην καθυστερήσει επιπλέον.
            val minDurationJob = async { delay(MIN_LOADING_SCREEN_DURATION_MS) }

            val result = try {
                withContext(Dispatchers.IO) {
                    AppInitState.Ready(
                        questionRepository = QuestionRepository(context),
                        examConfigRepository = ExamConfigRepository(context),
                        progressRepository = ProgressRepository(context),
                        examStateRepository = ExamStateRepository(context)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppInitState.Error(e.message ?: unknownErrorMessage)
            }

            minDurationJob.await()
            initState = result
        }
    }

    XeiristisExamQuizTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val state = initState) {
                is AppInitState.Loading -> LoadingScreen()
                is AppInitState.Error -> ErrorScreen(message = state.message)
                is AppInitState.Ready -> AppNavGraph(
                    questionRepository = state.questionRepository,
                    examConfigRepository = state.examConfigRepository,
                    progressRepository = state.progressRepository,
                    examStateRepository = state.examStateRepository
                )
            }
        }
    }
}
