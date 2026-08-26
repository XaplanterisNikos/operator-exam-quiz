package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import androidx.core.content.edit
import com.foxnks.xeiristisexamquiz.data.model.Question
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
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

    fun save(state: PersistedExamState) {
        prefs.edit {
            putString(KEY_STATE, json.encodeToString(PersistedExamState.serializer(), state))
        }
    }

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

    fun clear() {
        prefs.edit { remove(KEY_STATE) }
    }

    private companion object {
        const val PREFS_NAME = "exam_in_progress"
        const val KEY_STATE = "state"
    }
}
