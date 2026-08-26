package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 * Mirrors assets/exam_config.json. Keys of [questionsPerChapter] are chapter ids
 * as strings (JSON object keys), each mapped to a fixed (non-proportional) number
 * of questions drawn from that chapter for the final exam.
 */
@Serializable
data class ExamConfig(
    val totalExamQuestions: Int,
    val examTimeLimitMinutes: Int,
    val passThresholdPercent: Int,
    val questionsPerChapter: Map<String, Int>
)
