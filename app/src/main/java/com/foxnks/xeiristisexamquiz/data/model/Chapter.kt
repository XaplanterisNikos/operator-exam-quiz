package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: Int,
    val title: String,
    val order: Int
)
