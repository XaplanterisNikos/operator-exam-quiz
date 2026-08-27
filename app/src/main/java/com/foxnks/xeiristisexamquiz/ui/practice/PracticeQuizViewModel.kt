package com.foxnks.xeiristisexamquiz.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.data.model.QuestionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Everything the practice screen needs to draw itself, bundled into one immutable snapshot.
 * Whenever anything changes, a brand-new copy of this object is produced (see [ViewModel]'s
 * usage of [PracticeQuizUiState.copy] below) - the UI never mutates fields directly.
 * Όλα όσα χρειάζεται η οθόνη εξάσκησης για να ζωγραφιστεί, μαζεμένα σε ένα αμετάβλητο
 * στιγμιότυπο. Όποτε αλλάζει κάτι, παράγεται ένα ολοκαίνουριο αντίγραφο αυτού του
 * αντικειμένου (δες τη χρήση του [PracticeQuizUiState.copy] παρακάτω στο [ViewModel]) - το
 * UI ποτέ δεν τροποποιεί πεδία απευθείας.
 */
data class PracticeQuizUiState(
    val chapterTitle: String = "",
    val questionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val question: Question? = null,
    val selectedOptionIds: Set<String> = emptySet(),
    val isAnswerChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val isChapterFinished: Boolean = false
)

/**
 * Drives the practice-mode screen for a single chapter: tracks which question we're on,
 * the user's current selection, and whether the answer has been checked yet.
 * Οδηγεί την οθόνη εξάσκησης για ένα κεφάλαιο: παρακολουθεί σε ποια ερώτηση βρισκόμαστε,
 * την τρέχουσα επιλογή του χρήστη, και αν έχει ήδη ελεγχθεί η απάντηση.
 */
class PracticeQuizViewModel(
    repository: QuestionRepository,
    chapterId: Int
) : ViewModel() {

    private val chapterTitle = repository.getChapterById(chapterId)?.title.orEmpty()
    private val questions = repository.getQuestionsForChapter(chapterId)

    private val _uiState = MutableStateFlow(buildStateForIndex(0))
    val uiState: StateFlow<PracticeQuizUiState> = _uiState.asStateFlow()

    // Builds the full state snapshot for a given question index. Once the index runs past
    // the last question, returns a "chapter finished" state instead of crashing on an
    // out-of-bounds access.
    // Χτίζει το πλήρες στιγμιότυπο κατάστασης για έναν δεδομένο δείκτη ερώτησης. Μόλις ο
    // δείκτης ξεπεράσει την τελευταία ερώτηση, επιστρέφει κατάσταση "κεφάλαιο
    // ολοκληρώθηκε" αντί να κρασάρει σε πρόσβαση εκτός ορίων.
    private fun buildStateForIndex(index: Int): PracticeQuizUiState {
        if (index >= questions.size) {
            return PracticeQuizUiState(
                chapterTitle = chapterTitle,
                questionIndex = index,
                totalQuestions = questions.size,
                isChapterFinished = true
            )
        }
        return PracticeQuizUiState(
            chapterTitle = chapterTitle,
            questionIndex = index,
            totalQuestions = questions.size,
            question = questions[index]
        )
    }

    /**
     * Called when the user taps an option. SINGLE questions replace the whole selection
     * (only one choice possible); MULTIPLE questions add/remove that one option from the
     * set. Ignored once the answer has already been checked.
     * Καλείται όταν ο χρήστης πατήσει μια επιλογή. Οι ερωτήσεις SINGLE αντικαθιστούν όλη
     * την επιλογή (μόνο μία δυνατή επιλογή)· οι MULTIPLE προσθέτουν/αφαιρούν αυτή τη μία
     * επιλογή από το σύνολο. Αγνοείται μόλις έχει ήδη ελεγχθεί η απάντηση.
     */
    fun toggleOption(optionId: String) {
        val state = _uiState.value
        val question = state.question ?: return
        if (state.isAnswerChecked) return

        val newSelection = when (question.type) {
            QuestionType.SINGLE -> setOf(optionId)
            QuestionType.MULTIPLE -> if (optionId in state.selectedOptionIds) {
                state.selectedOptionIds - optionId
            } else {
                state.selectedOptionIds + optionId
            }
        }
        _uiState.value = state.copy(selectedOptionIds = newSelection)
    }

    /**
     * Compares the user's selection against the question's correct options and locks the
     * answer in as checked. No-op if nothing is selected yet or it's already checked.
     * Συγκρίνει την επιλογή του χρήστη με τις σωστές επιλογές της ερώτησης και "κλειδώνει"
     * την απάντηση ως ελεγμένη. Δεν κάνει τίποτα αν δεν έχει επιλεγεί ακόμα κάτι ή αν έχει
     * ήδη ελεγχθεί.
     */
    fun checkAnswer() {
        val state = _uiState.value
        val question = state.question ?: return
        if (state.isAnswerChecked || state.selectedOptionIds.isEmpty()) return

        val correctOptionIds = question.options.filter { it.isCorrect }.map { it.id }.toSet()
        val isCorrect = state.selectedOptionIds == correctOptionIds
        _uiState.value = state.copy(isAnswerChecked = true, isCorrect = isCorrect)
    }

    /**
     * Advances to the next question. Only allowed after the current answer has been
     * checked, so the user can't skip past an unanswered question.
     * Προχωράει στην επόμενη ερώτηση. Επιτρέπεται μόνο αφού έχει ελεγχθεί η τρέχουσα
     * απάντηση, ώστε ο χρήστης να μην μπορεί να προσπεράσει μια αναπάντητη ερώτηση.
     */
    fun goToNextQuestion() {
        val state = _uiState.value
        if (!state.isAnswerChecked) return
        _uiState.value = buildStateForIndex(state.questionIndex + 1)
    }

    companion object {
        fun factory(repository: QuestionRepository, chapterId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PracticeQuizViewModel(repository, chapterId) as T
            }
    }
}
