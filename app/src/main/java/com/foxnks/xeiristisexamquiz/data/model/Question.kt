package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 * A full question: which chapter it belongs to, its type, its text
 *  and its available options. explanation is an optional clarification text.
 *  Μία πλήρης ερώτηση: σε ποιο κεφάλαιο ανήκει, τι τύπου είναι, το κείμενό της
 *  και οι διαθέσιμες επιλογές. Το explanation είναι προαιρετική επεξήγηση.
 */
@Serializable
data class Question(
    val id: String,
    val chapterId: Int,
    val type: QuestionType,
    val text: String,
    val options: List<Option>,
    val explanation: String? = null
)
