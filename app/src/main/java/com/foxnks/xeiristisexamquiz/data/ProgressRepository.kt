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
 */
class ProgressRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).examAttemptDao()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val attempts: Flow<List<ExamAttemptEntity>> = dao.getAllAttempts()

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
