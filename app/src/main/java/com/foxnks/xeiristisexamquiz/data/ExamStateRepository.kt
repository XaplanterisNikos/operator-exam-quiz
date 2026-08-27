package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import androidx.core.content.edit
import com.foxnks.xeiristisexamquiz.data.model.Question
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The answer to a single question, as stored in the "exam in progress" state. A separate,
 * lightweight copy of ExamAnswerState (ui/exam) so that the data layer doesn't depend on
 * ui-layer types; the conversion happens in ExamViewModel when saving/restoring.
 * Απάντηση σε μία ερώτηση, όπως αποθηκεύεται στο "τεστ σε εξέλιξη". Ξεχωριστό, ελαφρύ
 * αντίγραφο του ExamAnswerState (ui/exam) ώστε ο data layer να μην εξαρτάται από τύπους
 * του ui layer· η μετατροπή γίνεται στο ExamViewModel κατά την αποθήκευση/επαναφορά.
 */
@Serializable
data class PersistedExamAnswer(
    val selectedOptionIds: Set<String> = emptySet(),
    val isFlaggedForReview: Boolean = false
)

/**
 * Everything needed to rebuild the EXACT same final-exam attempt after the Android process
 * gets killed: the same questions in the same display order ([Question.options] already
 * holds the option order as shuffled for this attempt - no need to re-draw them), the
 * answers, the flags for review, which questions have been visited, and the start moment in
 * elapsedRealtime so the remaining time is calculated correctly no matter how long the exam
 * stayed "frozen".
 * Ό,τι χρειάζεται για να ξαναφτιαχτεί ΑΚΡΙΒΩΣ η ίδια απόπειρα τελικού τεστ μετά από
 * kill της διεργασίας από το Android: οι ίδιες ερωτήσεις με την ίδια σειρά εμφάνισης
 * (το [Question.options] περιέχει ήδη τη σειρά επιλογών όπως ανακατεύτηκε γι' αυτή την
 * απόπειρα - δεν χρειάζεται να ξανατραβηχτούν), οι απαντήσεις, τα flags για επανέλεγχο,
 * ποιες ερωτήσεις έχουν εμφανιστεί, και η στιγμή εκκίνησης σε elapsedRealtime ώστε να
 * υπολογίζεται σωστά ο υπόλοιπος χρόνος ανεξάρτητα από πόσο έμεινε "παγωμένο" το τεστ.
 */
@Serializable
data class PersistedExamState(
    val examStartElapsedRealtime: Long,
    val totalTimeSeconds: Int,
    val questions: List<Question>,
    val currentIndex: Int,
    val answers: Map<String, PersistedExamAnswer>,
    val visitedQuestionIds: Set<String>
)

/**
 * Persists (as a single JSON string in SharedPreferences) the state of a final exam that is
 * currently in progress, so it survives screen-off / app switching / even the OS killing the
 * process due to low memory. Plain SharedPreferences (not Room) because this is a single
 * state "blob", not relational data - no migrations/queries needed, so a whole new database
 * wouldn't be worth the extra setup.
 * Αποθηκεύει σε SharedPreferences (ως ένα JSON string) την κατάσταση ενός τελικού τεστ
 * που βρίσκεται σε εξέλιξη, ώστε να επιβιώνει σε screen off / εναλλαγή εφαρμογής / ακόμα
 * και kill της διεργασίας από το λειτουργικό λόγω μνήμης. Απλό SharedPreferences (όχι
 * Room) γιατί πρόκειται για ένα και μοναδικό "blob" κατάστασης, όχι σχεσιακά δεδομένα -
 * δεν χρειάζεται migration/queries, άρα δεν αξίζει το επιπλέον setup μιας νέας βάσης.
 */
class ExamStateRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Saves (or overwrites) the current in-progress exam state.
     * Αποθηκεύει (ή αντικαθιστά) την τρέχουσα κατάσταση τεστ σε εξέλιξη.
     */
    fun save(state: PersistedExamState) {
        prefs.edit {
            putString(KEY_STATE, json.encodeToString(PersistedExamState.serializer(), state))
        }
    }

    /**
     * Loads the saved exam state, or null if none exists or it's corrupted.
     * Φορτώνει την αποθηκευμένη κατάσταση τεστ, ή null αν δεν υπάρχει ή είναι αλλοιωμένη.
     */
    fun load(): PersistedExamState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return try {
            json.decodeFromString(PersistedExamState.serializer(), raw)
        } catch (e: Exception) {
            // Αλλοιωμένα ή ασύμβατα δεδομένα (π.χ. άλλαξε το schema σε νέα έκδοση της
            // εφαρμογής) - καλύτερα να ξεκινήσει φρέσκο τεστ παρά να κρασάρει η οθόνη.
            null
        }
    }

    /**
     * Clears the saved exam state (called after submitting or cancelling an exam).
     * Διαγράφει την αποθηκευμένη κατάσταση τεστ (καλείται μετά από υποβολή ή ακύρωση τεστ).
     */
    fun clear() {
        prefs.edit { remove(KEY_STATE) }
    }

    private companion object {
        const val PREFS_NAME = "exam_in_progress"
        const val KEY_STATE = "state"
    }
}
