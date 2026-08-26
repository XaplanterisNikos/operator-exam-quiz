package com.foxnks.xeiristisexamquiz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val correctCount: Int,
    val totalQuestions: Int,
    val percentage: Double,
    val passed: Boolean,
    val durationSeconds: Int
)
