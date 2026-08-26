package com.foxnks.xeiristisexamquiz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamAttemptDao {

    @Insert
    suspend fun insert(attempt: ExamAttemptEntity)

    @Query("SELECT * FROM exam_attempts ORDER BY timestampMillis DESC")
    fun getAllAttempts(): Flow<List<ExamAttemptEntity>>
}
