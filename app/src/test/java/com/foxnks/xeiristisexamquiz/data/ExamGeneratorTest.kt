package com.foxnks.xeiristisexamquiz.data

import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import com.foxnks.xeiristisexamquiz.data.model.Option
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.data.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExamGeneratorTest {

    private val generator = ExamGenerator()

    private fun chapterQuestions(chapterId: Int, count: Int): List<Question> =
        (1..count).map { n ->
            Question(
                id = "c${chapterId}_q$n",
                chapterId = chapterId,
                type = QuestionType.SINGLE,
                text = "Question $n of chapter $chapterId",
                options = listOf(
                    Option("a", "correct", isCorrect = true),
                    Option("b", "wrong", isCorrect = false)
                )
            )
        }

    @Test
    fun generateExam_picksExactlyTheConfiguredCountPerChapter() {
        val pool = chapterQuestions(1, 10) + chapterQuestions(2, 4)
        val config = ExamConfig(
            totalExamQuestions = 7,
            examTimeLimitMinutes = 90,
            passThresholdPercent = 75,
            questionsPerChapter = mapOf("1" to 5, "2" to 2)
        )

        val exam = generator.generateExam(pool, config, Random(42))

        assertEquals(7, exam.size)
        assertEquals(5, exam.count { it.chapterId == 1 })
        assertEquals(2, exam.count { it.chapterId == 2 })
        assertEquals(exam.size, exam.map { it.id }.toSet().size) // no duplicates
    }

    @Test
    fun generateExam_throwsWithClearMessage_whenChapterPoolIsTooSmall() {
        val pool = chapterQuestions(1, 3)
        val config = ExamConfig(
            totalExamQuestions = 5,
            examTimeLimitMinutes = 90,
            passThresholdPercent = 75,
            questionsPerChapter = mapOf("1" to 5)
        )

        val exception = assertThrows(ExamGenerationException::class.java) {
            generator.generateExam(pool, config, Random(1))
        }
        assertTrue(exception.message!!.contains("1"))
        assertTrue(exception.message!!.contains("5"))
        assertTrue(exception.message!!.contains("3"))
    }

    @Test
    fun generateExam_throwsWhenAllocationSumDoesNotMatchTotal() {
        val pool = chapterQuestions(1, 10)
        val config = ExamConfig(
            totalExamQuestions = 99,
            examTimeLimitMinutes = 90,
            passThresholdPercent = 75,
            questionsPerChapter = mapOf("1" to 5)
        )

        assertThrows(ExamGenerationException::class.java) {
            generator.generateExam(pool, config, Random(1))
        }
    }

    @Test
    fun generateExam_preservesEachQuestionsOwnCorrectOptionsAfterShuffling() {
        val pool = chapterQuestions(1, 20)
        val config = ExamConfig(
            totalExamQuestions = 20,
            examTimeLimitMinutes = 90,
            passThresholdPercent = 75,
            questionsPerChapter = mapOf("1" to 20)
        )

        val exam = generator.generateExam(pool, config, Random(7))

        for (question in exam) {
            assertEquals(1, question.options.count { it.isCorrect })
        }
    }
}
