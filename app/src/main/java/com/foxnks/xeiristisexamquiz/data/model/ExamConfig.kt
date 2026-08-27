package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 * Mirrors assets/exam_config.json. Keys of questionsPerChapter are chapter ids
 *  as strings (JSON object keys), each mapped to a fixed (non-proportional) number
 *  of questions drawn from that chapter for the final exam.
 *  Αντιστοιχεί στο assets/exam_config.json. Τα κλειδιά του questionsPerChapter είναι
 *  τα id των κεφαλαίων ως string (επειδή έτσι τα γράφει η JSON), και η τιμή είναι ο
 *  σταθερός (όχι τυχαίος/αναλογικός) αριθμός ερωτήσεων που παίρνουμε από κάθε κεφάλαιο.
 */
@Serializable
data class ExamConfig(
    val totalExamQuestions: Int,
    val examTimeLimitMinutes: Int,
    val passThresholdPercent: Int,
    val questionsPerChapter: Map<String, Int>
)
