package com.foxnks.xeiristisexamquiz.data

import com.foxnks.xeiristisexamquiz.data.model.ExamConfig
import com.foxnks.xeiristisexamquiz.data.model.Question
import kotlin.random.Random

/**
 * Thrown when the question bank cannot satisfy [ExamConfig] (missing/insufficient chapter
 * questions, or a chapter allocation total that disagrees with [ExamConfig.totalExamQuestions]).
 * Surfacing this loudly is intentional (see brief section 5): a misconfigured question bank
 * must be caught immediately rather than silently producing a shorter exam.
 * Πετάγεται όταν η τράπεζα ερωτήσεων δεν μπορεί να ικανοποιήσει το [ExamConfig] (λείπουν
 * ή δεν επαρκούν ερωτήσεις κάποιου κεφαλαίου, ή το άθροισμα ανά κεφάλαιο δεν συμφωνεί με
 * το [ExamConfig.totalExamQuestions]). Το να «σκάει» φανερά είναι σκόπιμο: ένα λάθος
 * ρυθμισμένο exam_config.json πρέπει να εντοπίζεται αμέσως, όχι να παράγει σιωπηλά ένα
 * κοντύτερο τεστ.
 */
class ExamGenerationException(message: String) : Exception(message)

/**
 * Builds a final-exam question set using the fixed (non-proportional) per-chapter allocation
 * described in assets/exam_config.json (brief section 5): for every chapter, exactly
 * [ExamConfig.questionsPerChapter] questions are drawn at random without replacement, then the
 * combined set - and each question's own options - are shuffled.
 * Φτιάχνει το σύνολο ερωτήσεων του τελικού τεστ με βάση τη σταθερή (όχι τυχαία/αναλογική)
 * κατανομή ανά κεφάλαιο που ορίζεται στο assets/exam_config.json: για κάθε κεφάλαιο
 * τραβιούνται ακριβώς [ExamConfig.questionsPerChapter] ερωτήσεις τυχαία και χωρίς
 * επανάθεση, και μετά όλο το σύνολο -καθώς και οι επιλογές κάθε ερώτησης- ανακατεύονται.
 */
class ExamGenerator {

    /**
     * @param allQuestions Όλη η τράπεζα ερωτήσεων / The full question bank
     * @param config Οι κανόνες του τελικού τεστ / The final-exam rules
     * @param random Πηγή τυχαιότητας, παραμετροποιήσιμη για tests / Randomness source, overridable for tests
     * @return Λίστα ερωτήσεων έτοιμη για το τεστ, ήδη ανακατεμένη / A shuffled, ready-to-use question list
     * @throws ExamGenerationException αν η τράπεζα ερωτήσεων δεν επαρκεί / if the question bank can't satisfy the config
     */
    fun generateExam(
        allQuestions: List<Question>,
        config: ExamConfig,
        random: Random = Random.Default
    ): List<Question> {
        // Groups all questions by chapter, so we can quickly pull from the right chapter
        // Ομαδοποιεί όλες τις ερωτήσεις ανά κεφάλαιο, ώστε να τραβάμε γρήγορα από το σωστό κεφάλαιο
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
            // Shuffles the chapter's questions and takes the first requiredCount of them
            // Ανακατεύει τις ερωτήσεις του κεφαλαίου και παίρνει τις πρώτες requiredCount
            selected += pool.shuffled(random).take(requiredCount)
        }

        if (selected.size != config.totalExamQuestions) {
            throw ExamGenerationException(
                "Το άθροισμα των questionsPerChapter (${selected.size}) δεν ταιριάζει με το " +
                    "totalExamQuestions (${config.totalExamQuestions}) στο exam_config.json."
            )
        }

        // Final shuffle: the order of questions in the exam, and the order of options within each question
        // Τελικό ανακάτεμα: η σειρά των ερωτήσεων στο τεστ, και η σειρά επιλογών μέσα σε κάθε ερώτηση
        return selected.shuffled(random).map { question ->
            question.copy(options = question.options.shuffled(random))
        }
    }
}
