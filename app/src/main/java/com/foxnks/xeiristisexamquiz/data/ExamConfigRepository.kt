package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import kotlinx.serialization.json.Json

/**
 * Loads the bundled exam rules from assets/exam_config.json once and keeps them in memory.
 * Φορτώνει μία φορά τους ενσωματωμένους κανόνες τελικού τεστ από το assets/exam_config.json
 * και τους κρατάει στη μνήμη.
 */
class ExamConfigRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * The final-exam rules (question count, time limit, pass threshold, per-chapter allocation).
     * Οι κανόνες του τελικού τεστ (αριθμός ερωτήσεων, χρονικό όριο, όριο επιτυχίας, κατανομή ανά κεφάλαιο).
     */
    val config: ExamConfig = context.assets.open(ASSET_FILE_NAME)
        .bufferedReader()
        .use { it.readText() }
        .let { json.decodeFromString(ExamConfig.serializer(), it) }

    private companion object {
        const val ASSET_FILE_NAME = "exam_config.json"
    }
}
