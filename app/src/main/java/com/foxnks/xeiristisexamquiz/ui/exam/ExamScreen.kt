package com.foxnks.xeiristisexamquiz.ui.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.ui.common.OptionRow
import com.foxnks.xeiristisexamquiz.ui.theme.Purple40

/**
 * The final-exam screen: one question at a time, a countdown timer, flag-for-review, the
 * question-navigator grid, and confirmation dialogs for both submitting and exiting.
 * Η οθόνη τελικού τεστ: μία ερώτηση τη φορά, χρονόμετρο αντίστροφης μέτρησης, σημείωση για
 * επανέλεγχο, το grid πλοήγησης ερωτήσεων, και dialogs επιβεβαίωσης τόσο για υποβολή όσο
 * και για έξοδο.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    onSubmitted: () -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // remember { mutableStateOf(...) }: local UI-only state (which dialog is open) that
    // doesn't need to live in the ViewModel, because it's purely cosmetic and doesn't need
    // to survive process death - it resets to false naturally on a fresh composition.
    // remember { mutableStateOf(...) }: τοπική κατάσταση μόνο-UI (ποιο dialog είναι
    // ανοιχτό) που δεν χρειάζεται να ζει στο ViewModel, γιατί είναι καθαρά αισθητική και
    // δεν χρειάζεται να επιβιώσει σε kill διεργασίας - μηδενίζεται φυσιολογικά σε νέα
    // composition.
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showQuestionGridSheet by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    /// While this screen is open (i.e. while an exam is running) we keep the device screen
    // awake, so it doesn't lock itself while the user is being examined. Resetting to false
    // in onDispose is essential - otherwise keepScreenOn would stay active even after
    // leaving ExamScreen.
    // Όσο είναι ανοιχτή αυτή η οθόνη (δηλαδή όσο τρέχει ένα τεστ) κρατάμε την οθόνη της
    // συσκευής ξύπνια, ώστε να μην κλειδώνει μόνη της ενόσω ο χρήστης εξετάζεται. Η
    // επαναφορά σε false στο onDispose είναι απαραίτητη - διαφορετικά το keepScreenOn θα
    // έμενε ενεργό ακόμα και μετά την έξοδο από το ExamScreen.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Intercepts BOTH the system back gesture/button, not just the TopAppBar's arrow -
    // otherwise the user could bypass the confirmation dialog and accidentally lose the
    // exam with a plain back gesture.
    // Πιάνουμε ΚΑΙ το system back (gesture/κουμπί συσκευής), όχι μόνο το βελάκι της
    // TopAppBar - αλλιώς ο χρήστης θα μπορούσε να προσπεράσει το dialog επιβεβαίωσης και
    // να χάσει το τεστ κατά λάθος με ένα απλό back gesture.
    BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) onSubmitted()
    }

    // Question-bank misconfiguration guard: shows a dedicated error screen instead of
    // crashing or silently showing a broken exam (see ExamGenerationException).
    // Ασφαλιστική δικλείδα για λάθος ρυθμισμένη τράπεζα ερωτήσεων: δείχνει μια δική της
    // οθόνη σφάλματος αντί να κρασάρει ή να δείχνει σιωπηλά ένα χαλασμένο τεστ (βλέπε
    // ExamGenerationException).
    if (state.errorMessage != null) {
        Scaffold(
            modifier = modifier,
            topBar = { TopAppBar(title = { Text(stringResource(R.string.exam_title)) }) }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                Text(
                    text = stringResource(R.string.exam_generation_error_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.errorMessage.orEmpty())
            }
        }
        return
    }

    // Nothing to draw yet (still loading) or already submitted (LaunchedEffect above is
    // about to navigate away) - draw nothing rather than an inconsistent screen.
    // Δεν υπάρχει ακόμα τίποτα να ζωγραφιστεί (φορτώνει ακόμα) ή έχει ήδη υποβληθεί (το
    // LaunchedEffect παραπάνω πάει να πλοηγηθεί μακριά) - μη ζωγραφίζεις τίποτα αντί για
    // ασυνεπή οθόνη.
    if (state.questions.isEmpty() || state.isSubmitted) return

    val question = state.questions[state.currentIndex]
    val answer = state.answers[question.id] ?: ExamAnswerState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exam_title)) },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back_content_description)
                        )
                    }
                },
                actions = {
                    // Countdown timer, turning red in the last minute as a visual warning.
                    // Χρονόμετρο αντίστροφης μέτρησης, γίνεται κόκκινο το τελευταίο λεπτό
                    // σαν οπτική προειδοποίηση.
                    Text(
                        text = formatRemainingTime(state.remainingSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.remainingSeconds <= 60) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.question_progress_format,
                            state.currentIndex + 1,
                            state.questions.size
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    // "Ερωτήσεις" (opens the overview grid) next to "Επανέλεγχος", with the
                    // same button style (FilterChip) so they match visually. No meaningful
                    // "selected" state here - it's simply an action button.
                    // Το "Ερωτήσεις" (άνοιγμα του grid επισκόπησης) δίπλα στο "Επανέλεγχος",
                    // με το ίδιο στυλ κουμπιού (FilterChip) ώστε να ταιριάζουν οπτικά. Δεν
                    // έχει νόημα "selected" state εδώ - είναι απλά ένα κουμπί ενέργειας.
                    FilterChip(
                        selected = false,
                        onClick = { showQuestionGridSheet = true },
                        label = { Text(stringResource(R.string.exam_questions_label)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = answer.isFlaggedForReview,
                        onClick = { viewModel.toggleFlagForReview(question.id) },
                        label = { Text(stringResource(R.string.exam_flag_review_button)) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.currentIndex + 1f) / state.questions.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = question.text, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // isAnswerChecked = false, isCorrectOption = false always here: unlike
                // practice mode, the exam never reveals correctness mid-attempt - OptionRow
                // just acts as a plain selectable row until submission.
                // isAnswerChecked = false, isCorrectOption = false πάντα εδώ: σε αντίθεση
                // με την εξάσκηση, το τεστ ποτέ δεν αποκαλύπτει την ορθότητα κατά τη
                // διάρκεια - το OptionRow λειτουργεί απλά σαν κοινή επιλέξιμη γραμμή μέχρι
                // την υποβολή.
                question.options.forEach { option ->
                    OptionRow(
                        text = option.text,
                        questionType = question.type,
                        isSelected = option.id in answer.selectedOptionIds,
                        isAnswerChecked = false,
                        isCorrectOption = false,
                        onClick = { viewModel.toggleOption(question.id, option.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = viewModel::goToPreviousQuestion,
                    enabled = state.currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.exam_previous_button))
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = viewModel::goToNextQuestion,
                    enabled = state.currentIndex < state.questions.size - 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.exam_next_button))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showSubmitConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.exam_submit_button))
            }
        }
    }

    if (showSubmitConfirmation) {
        val answeredCount = state.answers.values.count { it.selectedOptionIds.isNotEmpty() }
        val unansweredCount = state.questions.size - answeredCount
        val flaggedCount = state.answers.values.count { it.isFlaggedForReview }
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = { Text(stringResource(R.string.exam_submit_dialog_title)) },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.exam_submit_dialog_answered_format,
                            answeredCount,
                            state.questions.size
                        )
                    )
                    if (unansweredCount > 0) {
                        Text(stringResource(R.string.exam_submit_dialog_unanswered_format, unansweredCount))
                    }
                    if (flaggedCount > 0) {
                        Text(stringResource(R.string.exam_submit_dialog_flagged_format, flaggedCount))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.exam_submit_dialog_warning))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitConfirmation = false
                    viewModel.submitExam()
                }) {
                    Text(stringResource(R.string.exam_submit_dialog_confirm))
                }
            },
            dismissButton = {
                Row {
                    // Instead of jumping straight to the first flagged question, opens the
                    // same question grid so the user sees the whole picture of the exam
                    // (answered/unanswered/flagged) and picks where to go themselves.
                    // Αντί να πηδάει κατευθείαν στην πρώτη σημειωμένη ερώτηση, ανοίγει το
                    // ίδιο grid ερωτήσεων ώστε ο χρήστης να βλέπει όλη την εικόνα του τεστ
                    // (απαντημένες/αναπάντητες/σημειωμένες) και να διαλέξει μόνος του πού
                    // θα πάει.
                    TextButton(onClick = {
                        showSubmitConfirmation = false
                        showQuestionGridSheet = true
                    }) {
                        Text(stringResource(R.string.exam_submit_dialog_review_button))
                    }
                    TextButton(onClick = { showSubmitConfirmation = false }) {
                        Text(stringResource(R.string.exam_submit_dialog_cancel))
                    }
                }
            }
        )
    }

    if (showQuestionGridSheet) {
        ModalBottomSheet(onDismissRequest = { showQuestionGridSheet = false }) {
            QuestionNavigationSheetContent(
                questions = state.questions,
                currentIndex = state.currentIndex,
                answers = state.answers,
                visitedQuestionIds = state.visitedQuestionIds,
                onQuestionSelected = { index ->
                    viewModel.goToQuestion(index)
                    showQuestionGridSheet = false
                }
            )
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text(stringResource(R.string.exam_exit_dialog_title)) },
            text = { Text(stringResource(R.string.exam_exit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmDialog = false
                    // Clears the saved "in progress" state and stops the timer BEFORE we
                    // leave the screen, so the cancelled exam doesn't get reloaded next
                    // time and doesn't get a chance to be saved to history as if it had
                    // been submitted normally.
                    // Καθαρίζει την αποθηκευμένη κατάσταση "σε εξέλιξη" και σταματάει το
                    // χρονόμετρο ΠΡΙΝ φύγουμε από την οθόνη, ώστε το ακυρωμένο τεστ να μην
                    // ξαναφορτωθεί την επόμενη φορά και να μην προλάβει να αποθηκευτεί σαν
                    // κανονική υποβολή στο ιστορικό.
                    viewModel.cancelExam()
                    onExitClick()
                }) {
                    Text(stringResource(R.string.exam_exit_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(stringResource(R.string.exam_submit_dialog_cancel))
                }
            }
        )
    }
}

// Instead of jumping straight to the first flagged question, opens the
// same question grid so the user sees the whole picture of the exam
// (answered/unanswered/flagged) and picks where to go themselves.
// Αντί να πηδάει κατευθείαν στην πρώτη σημειωμένη ερώτηση, ανοίγει το
// ίδιο grid ερωτήσεων ώστε ο χρήστης να βλέπει όλη την εικόνα του τεστ
// (απαντημένες/αναπάντητες/σημειωμένες) και να διαλέξει μόνος του πού
// θα πάει.
private enum class QuestionStatus { FLAGGED, ANSWERED, VISITED_UNANSWERED, NOT_VISITED }

private fun questionStatus(
    questionId: String,
    answers: Map<String, ExamAnswerState>,
    visitedQuestionIds: Set<String>
): QuestionStatus {
    val answer = answers[questionId]
    return when {
        answer?.isFlaggedForReview == true -> QuestionStatus.FLAGGED
        answer?.selectedOptionIds?.isNotEmpty() == true -> QuestionStatus.ANSWERED
        questionId in visitedQuestionIds -> QuestionStatus.VISITED_UNANSWERED
        else -> QuestionStatus.NOT_VISITED
    }
}

// Green for "answered" - there's no ready-made semantic green in the M3 color scheme, so
// it's defined here locally. The other colors are drawn from the existing theme (the
// brand's Purple40, error/surfaceVariant from the color scheme).
// Πράσινο για "απαντημένη" - δεν υπάρχει έτοιμο σημασιολογικό πράσινο στο M3 color scheme,
// οπότε ορίζεται εδώ τοπικά. Τα υπόλοιπα χρώματα αντλούνται από το ήδη υπάρχον theme
// (Purple40 του brand, error/surfaceVariant του color scheme).
private val AnsweredGreen = Color(0xFF4CAF50)

@Composable
private fun QuestionStatus.color(): Color = when (this) {
    QuestionStatus.FLAGGED -> Purple40
    QuestionStatus.ANSWERED -> AnsweredGreen
    QuestionStatus.VISITED_UNANSWERED -> MaterialTheme.colorScheme.error
    QuestionStatus.NOT_VISITED -> MaterialTheme.colorScheme.surfaceVariant
}

/**
 * Contents of the ModalBottomSheet: a color legend followed by an 8-column grid, one cell
 * per question, tap to jump straight to it.
 * Το περιεχόμενο του ModalBottomSheet: ένα χρωματικό υπόμνημα και μετά ένα grid 8 στηλών,
 * ένα κουτάκι ανά ερώτηση, πάτημα για άμεση μετάβαση σε αυτήν.
 */
@Composable
private fun QuestionNavigationSheetContent(
    questions: List<Question>,
    currentIndex: Int,
    answers: Map<String, ExamAnswerState>,
    visitedQuestionIds: Set<String>,
    onQuestionSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(text = stringResource(R.string.exam_questions_label), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Color legend, so each cell's status is understandable at a glance.
        // Υπόμνημα χρωμάτων, ώστε η κατάσταση κάθε κουτακιού να είναι κατανοητή με μια ματιά.
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuestionStatusLegendItem(
                    color = MaterialTheme.colorScheme.error,
                    label = stringResource(R.string.exam_legend_unanswered),
                    modifier = Modifier.weight(1f)
                )
                QuestionStatusLegendItem(
                    color = Purple40,
                    label = stringResource(R.string.exam_legend_flagged),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuestionStatusLegendItem(
                    color = AnsweredGreen,
                    label = stringResource(R.string.exam_legend_answered),
                    modifier = Modifier.weight(1f)
                )
                QuestionStatusLegendItem(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    label = stringResource(R.string.exam_legend_not_visited),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fixed(8): always exactly 8 columns regardless of screen width (cells just get
        // narrower/wider) - simpler and more predictable here than an adaptive column
        // count, given the sheet's own width is already constrained by the bottom sheet.
        // heightIn(max = 400.dp) caps the grid's height so it never pushes the legend
        // above it off-screen for exams with many questions - the grid scrolls internally
        // instead.
        // Fixed(8): πάντα ακριβώς 8 στήλες ανεξάρτητα από το πλάτος οθόνης (τα κουτάκια
        // απλά στενεύουν/φαρδαίνουν) - απλούστερο και πιο προβλέψιμο εδώ από προσαρμοστικό
        // αριθμό στηλών, δεδομένου ότι το πλάτος του sheet είναι ήδη περιορισμένο. Το
        // heightIn(max = 400.dp) περιορίζει το ύψος του grid ώστε ποτέ να μη σπρώξει το
        // υπόμνημα από πάνω εκτός οθόνης σε τεστ με πολλές ερωτήσεις - το grid κάνει
        // scroll εσωτερικά αντ' αυτού.
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            itemsIndexed(questions) { index, q ->
                val status = questionStatus(q.id, answers, visitedQuestionIds)
                val isCurrent = index == currentIndex
                val cellColor = status.color()
                val textColor = if (status == QuestionStatus.NOT_VISITED) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    Color.White
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(cellColor)
                        .then(
                            // Thin border around the cell for the question currently open,
                            // so the user can see where they are.
                            // Λεπτό περίγραμμα γύρω από το κουτάκι της ερώτησης που είναι
                            // αυτή τη στιγμή ανοιχτή, ώστε ο χρήστης να βλέπει πού βρίσκεται.
                            if (isCurrent) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onQuestionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionStatusLegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
/**
 * Formats a countdown in seconds as "MM:SS" with zero-padding (e.g. 65 -> "01:05").
 * Μορφοποιεί μια αντίστροφη μέτρηση σε δευτερόλεπτα ως "MM:SS" με μηδενικά γεμίσματος
 * (π.χ. 65 -> "01:05").
 */
private fun formatRemainingTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
