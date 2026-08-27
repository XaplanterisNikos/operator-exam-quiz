package com.foxnks.xeiristisexamquiz.ui.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.ui.common.BackTopAppBar
import com.foxnks.xeiristisexamquiz.ui.common.OptionRow

/**
 * Practice mode for a single chapter: one question at a time, with immediate feedback and
 * explanation after checking an answer.
 * Εξάσκηση για ένα κεφάλαιο: μία ερώτηση τη φορά, με άμεση ανατροφοδότηση και επεξήγηση
 * μόλις ελεγχθεί μια απάντηση.
 */
@Composable
fun PracticeQuizScreen(
    viewModel: PracticeQuizViewModel,
    onFinished: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // LaunchedEffect runs a side effect (here: navigating away) in response to a state
    // change, rather than during composition itself - composables must stay "pure"
    // (side-effect free) except inside effect blocks like this one. Keyed on
    // isChapterFinished, so it only re-runs when THAT specific value changes.
    // Το LaunchedEffect εκτελεί μια παρενέργεια (εδώ: αποχώρηση από την οθόνη) ως
    // απάντηση σε αλλαγή κατάστασης, αντί κατά τη σύνθεση (composition) του UI - τα
    // composables πρέπει να παραμένουν "καθαρά" (χωρίς παρενέργειες) εκτός από μέσα σε
    // blocks όπως αυτό. Κλειδωμένο στο isChapterFinished, άρα ξανατρέχει μόνο όταν
    // αλλάξει ΑΥΤΗ η συγκεκριμένη τιμή.
    LaunchedEffect(state.isChapterFinished) {
        if (state.isChapterFinished) onFinished()
    }

    // Early return: if there's no current question (e.g. right at the "finished" moment,
    // before LaunchedEffect has navigated away yet), draw nothing instead of crashing.
    // Πρόωρη επιστροφή: αν δεν υπάρχει τρέχουσα ερώτηση (π.χ. ακριβώς τη στιγμή του
    // "ολοκληρώθηκε", πριν προλάβει το LaunchedEffect να πλοηγηθεί μακριά), μη ζωγραφίζεις
    // τίποτα αντί να κρασάρεις.
    val question = state.question ?: return

    Scaffold(
        modifier = modifier,
        topBar = { BackTopAppBar(title = state.chapterTitle, onBackClick = onBackClick) }
    ) { innerPadding ->
        // Scrollable zone (question text + options + explanation) takes all space
        // except what the bottom button needs - same weight() trick as HomeScreen.
        // Η κυλιόμενη ζώνη (κείμενο ερώτησης + επιλογές + επεξήγηση) παίρνει όλο τον
        // χώρο εκτός από όσο χρειάζεται το κάτω κουμπί - ίδιο κόλπο weight() με το
        // HomeScreen.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(
                        R.string.question_progress_format,
                        state.questionIndex + 1,
                        state.totalQuestions
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.questionIndex + 1f) / state.totalQuestions },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = question.text, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                question.options.forEach { option ->
                    OptionRow(
                        text = option.text,
                        questionType = question.type,
                        isSelected = option.id in state.selectedOptionIds,
                        isAnswerChecked = state.isAnswerChecked,
                        isCorrectOption = option.isCorrect,
                        onClick = { viewModel.toggleOption(option.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.isAnswerChecked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.isCorrect) {
                            stringResource(R.string.practice_answer_correct)
                        } else {
                            stringResource(R.string.practice_answer_wrong)
                        },
                        color = if (state.isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    question.explanation?.let { explanation ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = explanation, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom button swaps role depending on state: "check answer" before checking,
            // then "next question" (or "finish chapter" on the last one) after.
            // Το κάτω κουμπί αλλάζει ρόλο ανάλογα με την κατάσταση: "έλεγχος απάντησης"
            // πριν τον έλεγχο, μετά "επόμενη ερώτηση" (ή "ολοκλήρωση κεφαλαίου" στην
            // τελευταία).
            if (!state.isAnswerChecked) {
                Button(
                    onClick = viewModel::checkAnswer,
                    enabled = state.selectedOptionIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.practice_check_answer_button))
                }
            } else {
                Button(
                    onClick = viewModel::goToNextQuestion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.questionIndex + 1 < state.totalQuestions) {
                            stringResource(R.string.practice_next_question_button)
                        } else {
                            stringResource(R.string.practice_finish_chapter_button)
                        }
                    )
                }
            }
        }
    }
}
