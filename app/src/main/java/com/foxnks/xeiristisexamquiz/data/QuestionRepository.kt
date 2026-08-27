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
 * Φορτώνει μία φορά την ενσωματωμένη τράπεζα ερωτήσεων από το assets/questions.json και
 * την κρατάει στη μνήμη. Το περιεχόμενο είναι μόνο για ανάγνωση κατά την εκτέλεση· για
 * αλλαγές χρειάζεται επεξεργασία του JSON και νέα έκδοση της εφαρμογής.
 */
class QuestionRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // Reads the file from assets, converts it to text and then decodes it into a Kotlin object
    // Διαβάζει το αρχείο από τα assets, το μετατρέπει σε κείμενο και μετά σε αντικείμενο Kotlin
    private val bundle: QuestionsBundle = context.assets.open(ASSET_FILE_NAME)
        .bufferedReader()
        .use { it.readText() }
        .let { json.decodeFromString(QuestionsBundle.serializer(), it) }

    /**
     * All chapters, sorted by their [Chapter.order] field.
     * Όλα τα κεφάλαια, ταξινομημένα με βάση το πεδίο [Chapter.order].
     */
    val chapters: List<Chapter> = bundle.chapters.sortedBy { it.order }

    /**
     * All questions across all chapters, unfiltered.
     * Όλες οι ερωτήσεις όλων των κεφαλαίων, χωρίς φιλτράρισμα.
     */
    val questions: List<Question> = bundle.questions

    /**
     * Returns the chapter with the given id, or null if it doesn't exist.
     * Επιστρέφει το κεφάλαιο με το συγκεκριμένο id, ή null αν δεν υπάρχει.
     */
    fun getChapterById(chapterId: Int): Chapter? =
        chapters.firstOrNull { it.id == chapterId }

    /**
     * Returns all questions that belong to the given chapter.
     * Επιστρέφει όλες τις ερωτήσεις που ανήκουν στο συγκεκριμένο κεφάλαιο.
     */
    fun getQuestionsForChapter(chapterId: Int): List<Question> =
        questions.filter { it.chapterId == chapterId }

    private companion object {
        const val ASSET_FILE_NAME = "questions.json"
    }
}
