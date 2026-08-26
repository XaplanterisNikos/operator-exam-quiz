package com.foxnks.xeiristisexamquiz.data

import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import com.foxnks.xeiristisexamquiz.data.model.QuestionType
import com.foxnks.xeiristisexamquiz.data.model.QuestionsBundle
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parses the actual bundled assets on the JVM (no Android Context needed) to catch schema
 * drift between the data models and assets/questions.json / assets/exam_config.json.
 */
class AssetJsonParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun assetFile(name: String) = File("src/main/assets/$name")

    @Test
    fun questionsJson_parsesAndEveryOptionListHasExactlyOneOrMoreCorrectAnswers() {
        val text = assetFile("questions.json").readText()
        val bundle = json.decodeFromString(QuestionsBundle.serializer(), text)

        assertTrue(bundle.chapters.isNotEmpty())
        assertTrue(bundle.questions.isNotEmpty())

        for (question in bundle.questions) {
            val correctCount = question.options.count { it.isCorrect }
            when (question.type) {
                QuestionType.SINGLE -> assertEquals(
                    "Question ${question.id} should have exactly 1 correct option",
                    1,
                    correctCount
                )
                QuestionType.MULTIPLE -> assertTrue(
                    "Question ${question.id} should have at least 1 correct option",
                    correctCount >= 1
                )
            }
        }
    }

    @Test
    fun examConfigJson_parsesAndChapterAllocationsSumToTotal() {
        val text = assetFile("exam_config.json").readText()
        val config = json.decodeFromString(ExamConfig.serializer(), text)

        val sum = config.questionsPerChapter.values.sum()
        assertEquals(config.totalExamQuestions, sum)
    }

    @Test
    fun questionsJson_hasEnoughQuestionsPerChapterToGenerateAnExam() {
        val bundle = json.decodeFromString(
            QuestionsBundle.serializer(),
            assetFile("questions.json").readText()
        )
        val config = json.decodeFromString(
            ExamConfig.serializer(),
            assetFile("exam_config.json").readText()
        )

        val countByChapter = bundle.questions.groupingBy { it.chapterId }.eachCount()

        for ((chapterIdText, required) in config.questionsPerChapter) {
            val chapterId = chapterIdText.toInt()
            val available = countByChapter[chapterId] ?: 0
            assertTrue(
                "Chapter $chapterId needs $required questions for the exam but only " +
                    "$available are available in questions.json",
                available >= required
            )
        }
    }
}
