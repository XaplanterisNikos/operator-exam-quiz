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

/**
 * The user's answer to a single exam question, plus whether it's flagged for review.
 * Η απάντηση του χρήστη σε μία ερώτηση του τεστ, μαζί με το αν είναι σημειωμένη για
 * επανέλεγχο.
 */
data class ExamAnswerState(
    val selectedOptionIds: Set<String> = emptySet(),
    val isFlaggedForReview: Boolean = false
)

/**
 * Everything the exam screen needs to draw itself.
 * Όλα όσα χρειάζεται η οθόνη τεστ για να ζωγραφιστεί.
 */
data class ExamUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, ExamAnswerState> = emptyMap(),
    // Questions the user has seen at least once (navigated to). Needed separately from
    // `answers`, because a question with no entry there could mean either "never seen"
    // (white/neutral in the grid) or "seen but left unanswered" (red) - without this set
    // the two cases can't be told apart.
    // Ερωτήσεις που ο χρήστης έχει δει έστω μία φορά (έγινε πλοήγηση σ' αυτές). Χρειάζεται
    // ξεχωριστά από το `answers`, γιατί μια ερώτηση χωρίς καταχώρηση εκεί μπορεί να
    // σημαίνει είτε "δεν την είδε ποτέ" (λευκή/ουδέτερη στο grid) είτε "την είδε αλλά δεν
    // απάντησε" (κόκκινη) - χωρίς αυτό το σύνολο δεν ξεχωρίζουν.
    val visitedQuestionIds: Set<String> = emptySet(),
    val remainingSeconds: Int = 0,
    val totalTimeSeconds: Int = 0,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Drives the final-exam screen: generates (or restores) the question set, runs the
 * background-resilient timer, tracks answers/flags/visited questions, persists progress on
 * every change, and submits the attempt (manually or on time-out).
 * Οδηγεί την οθόνη τελικού τεστ: δημιουργεί (ή επαναφέρει) το σύνολο ερωτήσεων, τρέχει το
 * χρονόμετρο ανθεκτικό σε background, παρακολουθεί απαντήσεις/flags/επισκεφθείσες
 * ερωτήσεις, αποθηκεύει την πρόοδο σε κάθε αλλαγή, και υποβάλλει την απόπειρα (χειροκίνητα
 * ή αυτόματα με λήξη χρόνου).
 */
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

    // THE SOURCE OF TRUTH for the remaining time. `remainingSeconds` in ExamUiState is
    // just a display value, refreshed every second - it is ALWAYS computed from here,
    // never by decrementing. We use SystemClock.elapsedRealtime() instead of
    // System.currentTimeMillis(), because: (a) it isn't affected if the user changes the
    // device's clock, and (b) it keeps counting normally while the device is asleep/
    // screen-off - exactly the behavior we want for an exam timer.
    // Η ΠΗΓΗ ΑΛΗΘΕΙΑΣ για τον υπόλοιπο χρόνο. Το `remainingSeconds` στο ExamUiState είναι
    // απλά μια τιμή για εμφάνιση, ανανεωμένη κάθε δευτερόλεπτο - υπολογίζεται ΠΑΝΤΑ από
    // εδώ, ποτέ με decrement. Χρησιμοποιούμε SystemClock.elapsedRealtime() κι όχι
    // System.currentTimeMillis(), γιατί: (α) δεν επηρεάζεται αν ο χρήστης αλλάξει την ώρα
    // του ρολογιού της συσκευής, και (β) συνεχίζει κανονικά να μετράει όσο η συσκευή είναι
    // σε sleep/screen-off - ακριβώς η συμπεριφορά που θέλουμε για ένα χρονόμετρο εξέτασης.
    private var examStartElapsedRealtime: Long = 0L

    // init runs once, immediately when the ViewModel is created - the very first thing
    // this screen does is decide: is there an exam already in progress to restore, or do
    // we start a brand-new one?
    // Το init τρέχει μία φορά, αμέσως μόλις δημιουργηθεί το ViewModel - το πρώτο πράγμα
    // που κάνει αυτή η οθόνη είναι να αποφασίσει: υπάρχει ήδη τεστ σε εξέλιξη προς
    // επαναφορά, ή ξεκινάμε ολοκαίνουριο;
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
                // The first question is already visible as soon as the exam opens.
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
     * Rebuilds the exact same exam attempt (same questions/order, answers, flags, visited)
     * after the process was killed or the user simply returns to the app. If time has
     * already run out while the app was backgrounded/killed, immediately auto-submits with
     * whatever answers exist, exactly as if the timer had run out live.
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

    // The actual time-remaining calculation: total time minus however long has elapsed in
    // real device time since the exam started - not decremented, always recomputed from
    // these two fixed points. coerceIn clamps the result between 0 and the total, so it
    // never goes negative or above the original limit.
    // Ο πραγματικός υπολογισμός του υπόλοιπου χρόνου: συνολικός χρόνος μείον όσος
    // πραγματικός χρόνος συσκευής έχει περάσει από την εκκίνηση του τεστ - όχι μείωση με
    // decrement, πάντα επανυπολογισμός από αυτά τα δύο σταθερά σημεία. Το coerceIn κρατάει
    // το αποτέλεσμα ανάμεσα σε 0 και το σύνολο, ώστε ποτέ να μη γίνει αρνητικό ή να
    // ξεπεράσει το αρχικό όριο.
    private fun remainingSecondsSince(startElapsedRealtime: Long, totalTimeSeconds: Int): Int {
        val elapsedSeconds = (SystemClock.elapsedRealtime() - startElapsedRealtime) / 1000
        return (totalTimeSeconds - elapsedSeconds).coerceIn(0, totalTimeSeconds.toLong()).toInt()
    }

    /**
     * Saves the "exam in progress" so it can be rebuilt if the process gets killed. Called
     * on every meaningful change (new answer, flag, question change) and NOT only at the
     * end: the process can be terminated without warning at ANY moment (e.g. Android kills
     * it for memory while the user is in another app), so there is no safe point to wait
     * for other than "immediately after every change".
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

    // The visible "ticking" loop: once a second, recompute the remaining time from
    // elapsedRealtime (never trust a running total) and push it into the UI state. If time
    // runs out, submit automatically and stop the loop. viewModelScope ties this coroutine
    // to the ViewModel's own lifetime - it's cancelled automatically in onCleared() below.
    // Ο ορατός "βηματισμός" (tick): μία φορά το δευτερόλεπτο, επανυπολογίζει τον υπόλοιπο
    // χρόνο από το elapsedRealtime (ποτέ να μην εμπιστεύεσαι ένα "τρέχον σύνολο") και τον
    // στέλνει στο UI state. Αν λήξει ο χρόνος, υποβάλλει αυτόματα και σταματάει τον βρόχο.
    // Το viewModelScope δένει αυτή την coroutine με τη ζωή του ίδιου του ViewModel - ακυρώνεται
    // αυτόματα στο onCleared() παρακάτω.
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
            // Mark the destination question as "visited" the moment the user navigates to
            // it - this way the question grid knows it's no longer "white" even if it ends
            // up unanswered.
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
        // The exam is over (manually or auto-submitted on time-out) - there is no longer an
        // "exam in progress" to restore, so the next time ExamScreen opens it must always
        // start a fresh exam.
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
     * Called when the user confirms exiting an in-progress exam without finishing it. We
     * stop the timer FIRST (before navigating back at all), because otherwise there's a
     * race condition: if the timer's tick happens to fire at the same moment, it would call
     * submitExam() and save to history an exam the user just cancelled. We also clear the
     * saved "in progress" state - exactly like after a normal submission - so the next time
     * ExamScreen opens it does NOT try to resume this cancelled exam. We don't touch
     * ProgressRepository: the exam is treated as if it never happened, and must not appear
     * in the history.
     * Καλείται όταν ο χρήστης επιβεβαιώνει έξοδο από τεστ σε εξέλιξη, χωρίς να το
     * ολοκληρώσει. Σταματάμε ΠΡΩΤΑ το χρονόμετρο (πριν καν γίνει η πλοήγηση προς τα πίσω)
     * γιατί αλλιώς υπάρχει race condition: αν το tick του timer προλάβει να τρέξει την ίδια
     * στιγμή, θα καλούσε submitExam() και θα αποθήκευε στο ιστορικό ένα τεστ που ο χρήστης
     * μόλις ακύρωσε. Καθαρίζουμε επίσης την αποθηκευμένη κατάσταση "σε εξέλιξη" - ακριβώς
     * όπως και μετά από κανονική υποβολή - ώστε η επόμενη είσοδος στο ExamScreen να ΜΗΝ
     * προσπαθήσει να συνεχίσει αυτό το ακυρωμένο τεστ. Δεν αγγίζουμε το ProgressRepository:
     * το τεστ θεωρείται σαν να μην έγινε ποτέ, δεν πρέπει να εμφανιστεί στο ιστορικό.
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

    // Called automatically by the Android lifecycle system when this ViewModel is about to
    // be destroyed (e.g. the whole EXAM_GRAPH is popped off the back stack). Cancelling the
    // timer here prevents it from leaking and continuing to run after nobody needs it.
    // Καλείται αυτόματα από το σύστημα lifecycle του Android όταν αυτό το ViewModel
    // πρόκειται να καταστραφεί (π.χ. αφαιρείται όλο το EXAM_GRAPH από τη στοίβα). Το
    // ακύρωμα του χρονομέτρου εδώ αποτρέπει να "διαρρεύσει" και να συνεχίσει να τρέχει
    // αφού πια κανείς δεν το χρειάζεται.
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
