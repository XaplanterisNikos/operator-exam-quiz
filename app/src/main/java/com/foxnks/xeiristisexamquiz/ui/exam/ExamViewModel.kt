package com.foxnks.xeiristisexamquiz.ui.exam

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foxnks.xeiristisexamquiz.data.ExamConfigRepository
import com.foxnks.xeiristisexamquiz.data.ExamGenerationException
import com.foxnks.xeiristisexamquiz.data.ExamGenerator
import com.foxnks.xeiristisexamquiz.data.ExamStateRepository
import com.foxnks.xeiristisexamquiz.data.PersistedExamAnswer
import com.foxnks.xeiristisexamquiz.data.PersistedExamState
import com.foxnks.xeiristisexamquiz.data.ProgressRepository
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.data.model.QuestionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamAnswerState(
    val selectedOptionIds: Set<String> = emptySet(),
    val isFlaggedForReview: Boolean = false
)

data class ExamUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, ExamAnswerState> = emptyMap(),
    // Ερωτήσεις που ο χρήστης έχει δει έστω μία φορά (έγινε πλοήγηση σ' αυτές).
    // Χρειάζεται ξεχωριστά από το `answers`, γιατί μια ερώτηση χωρίς καταχώρηση εκεί
    // μπορεί να σημαίνει είτε "δεν την είδε ποτέ" (λευκή/ουδέτερη στο grid) είτε
    // "την είδε αλλά δεν απάντησε" (κόκκινη) - χωρίς αυτό το σύνολο δεν ξεχωρίζουν.
    val visitedQuestionIds: Set<String> = emptySet(),
    val remainingSeconds: Int = 0,
    val totalTimeSeconds: Int = 0,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class ExamViewModel(
    private val questionRepository: QuestionRepository,
    private val examConfigRepository: ExamConfigRepository,
    private val progressRepository: ProgressRepository,
    private val examStateRepository: ExamStateRepository,
    private val examGenerator: ExamGenerator = ExamGenerator()
) : ViewModel() {

    val passThresholdPercent: Int get() = examConfigRepository.config.passThresholdPercent

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Η ΠΗΓΗ ΑΛΗΘΕΙΑΣ για τον υπόλοιπο χρόνο. Το `remainingSeconds` στο ExamUiState είναι
    // απλά μια τιμή για εμφάνιση, ανανεωμένη κάθε δευτερόλεπτο - υπολογίζεται ΠΑΝΤΑ από
    // εδώ, ποτέ με decrement. Χρησιμοποιούμε SystemClock.elapsedRealtime() κι όχι
    // System.currentTimeMillis(), γιατί: (α) δεν επηρεάζεται αν ο χρήστης αλλάξει την ώρα
    // του ρολογιού της συσκευής, και (β) συνεχίζει κανονικά να μετράει όσο η συσκευή είναι
    // σε sleep/screen-off - ακριβώς η συμπεριφορά που θέλουμε για ένα χρονόμετρο εξέτασης.
    private var examStartElapsedRealtime: Long = 0L

    init {
        val persisted = examStateRepository.load()
        if (persisted != null) {
            restoreFromPersistedState(persisted)
        } else {
            startNewExam()
        }
    }

    private fun startNewExam() {
        try {
            val config = examConfigRepository.config
            val questions = examGenerator.generateExam(questionRepository.questions, config)
            val totalTimeSeconds = config.examTimeLimitMinutes * 60
            examStartElapsedRealtime = SystemClock.elapsedRealtime()
            _uiState.value = ExamUiState(
                questions = questions,
                remainingSeconds = totalTimeSeconds,
                totalTimeSeconds = totalTimeSeconds,
                // Η πρώτη ερώτηση είναι ήδη ορατή μόλις ανοίξει το τεστ.
                visitedQuestionIds = questions.firstOrNull()?.let { setOf(it.id) } ?: emptySet()
            )
            persistState()
            startTimer()
        } catch (e: ExamGenerationException) {
            _uiState.value = ExamUiState(errorMessage = e.message)
        }
    }

    /**
     * Ξαναφτιάχνει ακριβώς την ίδια απόπειρα τεστ (ίδιες ερωτήσεις/σειρά, απαντήσεις,
     * flags, visited) μετά από kill της διεργασίας ή απλή επιστροφή στην εφαρμογή. Αν ο
     * χρόνος έχει ήδη λήξει όσο η εφαρμογή ήταν στο background/killed, κάνει αμέσως
     * αυτόματη υποβολή με ό,τι απαντήσεις υπάρχουν, ακριβώς σαν να έληγε ο χρόνος ζωντανά.
     */
    private fun restoreFromPersistedState(persisted: PersistedExamState) {
        examStartElapsedRealtime = persisted.examStartElapsedRealtime
        val remaining = remainingSecondsSince(examStartElapsedRealtime, persisted.totalTimeSeconds)

        _uiState.value = ExamUiState(
            questions = persisted.questions,
            currentIndex = persisted.currentIndex,
            answers = persisted.answers.mapValues { (_, a) ->
                ExamAnswerState(a.selectedOptionIds, a.isFlaggedForReview)
            },
            visitedQuestionIds = persisted.visitedQuestionIds,
            remainingSeconds = remaining,
            totalTimeSeconds = persisted.totalTimeSeconds
        )

        if (remaining <= 0) {
            submitExam()
        } else {
            startTimer()
        }
    }

    private fun remainingSecondsSince(startElapsedRealtime: Long, totalTimeSeconds: Int): Int {
        val elapsedSeconds = (SystemClock.elapsedRealtime() - startElapsedRealtime) / 1000
        return (totalTimeSeconds - elapsedSeconds).coerceIn(0, totalTimeSeconds.toLong()).toInt()
    }

    /**
     * Αποθηκεύει το "τεστ σε εξέλιξη" ώστε να μπορεί να ξαναφτιαχτεί αν σκοτωθεί η
     * διεργασία. Καλείται σε κάθε σημαντική αλλαγή (νέα απάντηση, flag, αλλαγή ερώτησης)
     * κι ΟΧΙ μόνο στο τέλος: η διεργασία μπορεί να τερματιστεί απροειδοποίητα ΟΠΟΙΑΔΗΠΟΤΕ
     * στιγμή (π.χ. το Android τη σκοτώνει για μνήμη ενώ ο χρήστης είναι σε άλλη εφαρμογή),
     * οπότε δεν υπάρχει ασφαλές σημείο να περιμένουμε παρά μόνο "αμέσως μετά από κάθε αλλαγή".
     */
    private fun persistState() {
        val state = _uiState.value
        if (state.questions.isEmpty() || state.isSubmitted) return
        examStateRepository.save(
            PersistedExamState(
                examStartElapsedRealtime = examStartElapsedRealtime,
                totalTimeSeconds = state.totalTimeSeconds,
                questions = state.questions,
                currentIndex = state.currentIndex,
                answers = state.answers.mapValues { (_, a) ->
                    PersistedExamAnswer(a.selectedOptionIds, a.isFlaggedForReview)
                },
                visitedQuestionIds = state.visitedQuestionIds
            )
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = remainingSecondsSince(examStartElapsedRealtime, _uiState.value.totalTimeSeconds)
                _uiState.value = _uiState.value.copy(remainingSeconds = remaining)
                if (remaining <= 0) {
                    submitExam()
                    break
                }
                delay(1000)
            }
        }
    }

    fun goToQuestion(index: Int) {
        val state = _uiState.value
        if (index in state.questions.indices) {
            // Μαρκάρουμε την ερώτηση-προορισμό ως "visited" τη στιγμή που ο χρήστης
            // πλοηγείται σ' αυτήν - έτσι το grid ερωτήσεων ξέρει ότι δεν είναι πια
            // "λευκή" ακόμα κι αν μείνει τελικά αναπάντητη.
            val visitedId = state.questions[index].id
            _uiState.value = state.copy(
                currentIndex = index,
                visitedQuestionIds = state.visitedQuestionIds + visitedId
            )
            persistState()
        }
    }

    fun goToNextQuestion() = goToQuestion(_uiState.value.currentIndex + 1)

    fun goToPreviousQuestion() = goToQuestion(_uiState.value.currentIndex - 1)

    fun toggleOption(questionId: String, optionId: String) {
        val state = _uiState.value
        if (state.isSubmitted) return
        val question = state.questions.firstOrNull { it.id == questionId } ?: return
        val currentAnswer = state.answers[questionId] ?: ExamAnswerState()

        val newSelection = when (question.type) {
            QuestionType.SINGLE -> setOf(optionId)
            QuestionType.MULTIPLE -> if (optionId in currentAnswer.selectedOptionIds) {
                currentAnswer.selectedOptionIds - optionId
            } else {
                currentAnswer.selectedOptionIds + optionId
            }
        }
        _uiState.value = state.copy(
            answers = state.answers + (questionId to currentAnswer.copy(selectedOptionIds = newSelection))
        )
        persistState()
    }

    fun toggleFlagForReview(questionId: String) {
        val state = _uiState.value
        if (state.isSubmitted) return
        val currentAnswer = state.answers[questionId] ?: ExamAnswerState()
        _uiState.value = state.copy(
            answers = state.answers + (
                questionId to currentAnswer.copy(isFlaggedForReview = !currentAnswer.isFlaggedForReview)
                )
        )
        persistState()
    }

    fun submitExam() {
        val state = _uiState.value
        if (state.isSubmitted) return
        timerJob?.cancel()
        _uiState.value = state.copy(isSubmitted = true)
        // Το τεστ ολοκληρώθηκε (χειροκίνητα ή αυτόματα από λήξη χρόνου) - δεν υπάρχει πια
        // "τεστ σε εξέλιξη" προς επαναφορά, οπότε η επόμενη είσοδος στο ExamScreen πρέπει
        // να ξεκινάει πάντα φρέσκο τεστ.
        examStateRepository.clear()

        val correctCount = state.questions.count { isAnswerCorrect(it) }
        val total = state.questions.size
        val percentage = if (total > 0) correctCount * 100.0 / total else 0.0
        val passed = percentage >= passThresholdPercent
        val durationSeconds = state.totalTimeSeconds - state.remainingSeconds

        progressRepository.saveAttempt(
            correctCount = correctCount,
            totalQuestions = total,
            percentage = percentage,
            passed = passed,
            durationSeconds = durationSeconds
        )
    }

    /**
     * Καλείται όταν ο χρήστης επιβεβαιώνει έξοδο από τεστ σε εξέλιξη, χωρίς να το
     * ολοκληρώσει. Σταματάμε ΠΡΩΤΑ το χρονόμετρο (πριν καν γίνει η πλοήγηση προς τα
     * πίσω) γιατί αλλιώς υπάρχει race condition: αν το tick του timer προλάβει να
     * τρέξει την ίδια στιγμή, θα καλούσε submitExam() και θα αποθήκευε στο ιστορικό
     * ένα τεστ που ο χρήστης μόλις ακύρωσε. Καθαρίζουμε επίσης την αποθηκευμένη
     * κατάσταση "σε εξέλιξη" - ακριβώς όπως και μετά από κανονική υποβολή - ώστε η
     * επόμενη είσοδος στο ExamScreen να ΜΗΝ προσπαθήσει να συνεχίσει αυτό το
     * ακυρωμένο τεστ. Δεν αγγίζουμε το ProgressRepository: το τεστ θεωρείται σαν να
     * μην έγινε ποτέ, δεν πρέπει να εμφανιστεί στο ιστορικό.
     */
    fun cancelExam() {
        timerJob?.cancel()
        examStateRepository.clear()
    }

    fun isAnswerCorrect(question: Question): Boolean {
        val correctOptionIds = question.options.filter { it.isCorrect }.map { it.id }.toSet()
        val selected = _uiState.value.answers[question.id]?.selectedOptionIds ?: emptySet()
        return selected == correctOptionIds
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun factory(
            questionRepository: QuestionRepository,
            examConfigRepository: ExamConfigRepository,
            progressRepository: ProgressRepository,
            examStateRepository: ExamStateRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExamViewModel(
                        questionRepository,
                        examConfigRepository,
                        progressRepository,
                        examStateRepository
                    ) as T
            }
    }
}
