package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import kotlinx.serialization.json.Json

/**
 * Loads the bundled exam rules from assets/exam_config.json once and keeps them in memory.
 */
class ExamConfigRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val config: ExamConfig = context.assets.open(ASSET_FILE_NAME)
        .bufferedReader()
        .use { it.readText() }
        .let { json.decodeFromString(ExamConfig.serializer(), it) }

    private companion object {
        const val ASSET_FILE_NAME = "exam_config.json"
    }
}
