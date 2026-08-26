package com.foxnks.xeiristisexamquiz.data

import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import com.foxnks.xeiristisexamquiz.data.model.Question
import kotlin.random.Random

/**
 * Thrown when the question bank cannot satisfy [ExamConfig] (missing/insufficient chapter
 * questions, or a chapter allocation total that disagrees with [ExamConfig.totalExamQuestions]).
 * Surfacing this loudly is intentional (see brief section 5): a misconfigured question bank
 * must be caught immediately rather than silently producing a shorter exam.
 */
class ExamGenerationException(message: String) : Exception(message)

/**
 * Builds a final-exam question set using the fixed (non-proportional) per-chapter allocation
 * described in assets/exam_config.json (brief section 5): for every chapter, exactly
 * [ExamConfig.questionsPerChapter] questions are drawn at random without replacement, then the
 * combined set - and each question's own options - are shuffled.
 */
class ExamGenerator {

    fun generateExam(
        allQuestions: List<Question>,
        config: ExamConfig,
        random: Random = Random.Default
    ): List<Question> {
        val questionsByChapter = allQuestions.groupBy { it.chapterId }
        val selected = mutableListOf<Question>()

        for ((chapterIdText, requiredCount) in config.questionsPerChapter) {
            val chapterId = chapterIdText.toIntOrNull()
                ?: throw ExamGenerationException(
                    "Μη έγκυρο chapterId '$chapterIdText' στο exam_config.json."
                )
            val pool = questionsByChapter[chapterId].orEmpty()
            if (pool.size < requiredCount) {
                throw ExamGenerationException(
                    "Το κεφάλαιο $chapterId απαιτεί $requiredCount ερωτήσεις για το τελικό τεστ, " +
                        "αλλά η βάση ερωτήσεων έχει διαθέσιμες μόνο ${pool.size}."
                )
            }
            selected += pool.shuffled(random).take(requiredCount)
        }

        if (selected.size != config.totalExamQuestions) {
            throw ExamGenerationException(
                "Το άθροισμα των questionsPerChapter (${selected.size}) δεν ταιριάζει με το " +
                    "totalExamQuestions (${config.totalExamQuestions}) στο exam_config.json."
            )
        }

        return selected.shuffled(random).map { question ->
            question.copy(options = question.options.shuffled(random))
        }
    }
}
