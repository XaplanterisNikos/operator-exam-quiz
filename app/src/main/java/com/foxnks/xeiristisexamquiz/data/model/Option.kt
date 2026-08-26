package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Option(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)
