package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import com.foxnks.xeiristisexamquiz.data.local.AppDatabase
import com.foxnks.xeiristisexamquiz.data.local.ExamAttemptEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Persists and reads back the user's final-exam attempt history (Room, local to the device).
 *
 * [saveAttempt] runs on a scope owned by this repository - not the caller's (e.g. a ViewModel's
 * `viewModelScope`) - so the write survives navigating away immediately after submitting an exam,
 * which would otherwise cancel it mid-flight together with the screen's ViewModel.
 * Αποθηκεύει και διαβάζει το ιστορικό απόπειρων τελικού τεστ του χρήστη (Room, τοπικά
 * στη συσκευή).
 *
 * Το [saveAttempt] τρέχει σε scope που ανήκει σε αυτό το repository - όχι στο scope του
 * καλούντος (π.χ. το viewModelScope ενός ViewModel) - ώστε η εγγραφή να επιβιώνει αν ο
 * χρήστης φύγει από την οθόνη αμέσως μετά την υποβολή του τεστ, κάτι που αλλιώς θα την
 * ακύρωνε στη μέση μαζί με το ViewModel της οθόνης.
 */
class ProgressRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).examAttemptDao()

    // Our own coroutine scope, independent of whichever screen calls this
    // Δικό μας coroutine scope, ανεξάρτητο από όποια οθόνη το καλεί
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * All exam attempts, as a live stream (auto-updates on every new entry).
     * Όλες οι απόπειρες τεστ, ως ζωντανό ρεύμα (ενημερώνεται αυτόματα σε κάθε νέα εγγραφή).
     */
    val attempts: Flow<List<ExamAttemptEntity>> = dao.getAllAttempts()

    /**
     * Saves a completed exam attempt to the history.
     * Αποθηκεύει μία ολοκληρωμένη απόπειρα τεστ στο ιστορικό.
     */
    fun saveAttempt(
        correctCount: Int,
        totalQuestions: Int,
        percentage: Double,
        passed: Boolean,
        durationSeconds: Int
    ) {
        repositoryScope.launch {
            dao.insert(
                ExamAttemptEntity(
                    timestampMillis = System.currentTimeMillis(),
                    correctCount = correctCount,
                    totalQuestions = totalQuestions,
                    percentage = percentage,
                    passed = passed,
                    durationSeconds = durationSeconds
                )
            )
        }
    }
}
