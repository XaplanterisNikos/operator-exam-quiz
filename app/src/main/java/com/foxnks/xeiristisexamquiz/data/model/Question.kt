package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val chapterId: Int,
    val type: QuestionType,
    val text: String,
    val options: List<Option>,
    val explanation: String? = null
)
