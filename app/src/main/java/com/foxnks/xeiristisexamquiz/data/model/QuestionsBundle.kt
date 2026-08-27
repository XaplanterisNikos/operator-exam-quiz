package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 *  Mirrors the root object of assets/questions.json
 *  (one list of chapters + one list of all questions).
 *  Αντιστοιχεί στο ριζικό αντικείμενο του assets/questions.json
 *  (μία λίστα κεφαλαίων + μία λίστα όλων των ερωτήσεων).
 */
@Serializable
data class QuestionsBundle(
    val chapters: List<Chapter>,
    val questions: List<Question>
)
