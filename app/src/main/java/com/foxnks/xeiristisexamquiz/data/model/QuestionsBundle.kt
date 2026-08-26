package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 * Mirrors the root object of assets/questions.json.
 */
@Serializable
data class QuestionsBundle(
    val chapters: List<Chapter>,
    val questions: List<Question>
)
