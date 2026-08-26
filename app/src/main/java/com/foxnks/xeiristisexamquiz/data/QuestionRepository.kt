package com.foxnks.xeiristisexamquiz.data

import android.content.Context
import com.foxnks.xeiristisexamquiz.data.model.Chapter
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.data.model.QuestionsBundle
import kotlinx.serialization.json.Json

/**
 * Loads the bundled question bank from assets/questions.json once and keeps it in memory.
 * The content is read-only at runtime; updates require editing the JSON and shipping a
 * new app version.
 */
class QuestionRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val bundle: QuestionsBundle = context.assets.open(ASSET_FILE_NAME)
        .bufferedReader()
        .use { it.readText() }
        .let { json.decodeFromString(QuestionsBundle.serializer(), it) }

    val chapters: List<Chapter> = bundle.chapters.sortedBy { it.order }

    val questions: List<Question> = bundle.questions

    fun getChapterById(chapterId: Int): Chapter? =
        chapters.firstOrNull { it.id == chapterId }

    fun getQuestionsForChapter(chapterId: Int): List<Question> =
        questions.filter { it.chapterId == chapterId }

    private companion object {
        const val ASSET_FILE_NAME = "questions.json"
    }
}
