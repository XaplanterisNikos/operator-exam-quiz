package com.foxnks.xeiristisexamquiz.ui.chapterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.data.model.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the chapter list state for the chapter-list screen. Reads the (already in-memory)
 * chapters from [QuestionRepository] once and exposes them as a [StateFlow] for the UI to
 * observe.
 * Κρατάει την κατάσταση της λίστας κεφαλαίων για την οθόνη λίστας κεφαλαίων. Διαβάζει τα
 * (ήδη φορτωμένα στη μνήμη) κεφάλαια από το [QuestionRepository] μία φορά και τα εκθέτει
 * ως [StateFlow] ώστε να τα παρακολουθεί το UI.
 */
class ChapterListViewModel(repository: QuestionRepository) : ViewModel() {

    // Private, mutable: only this ViewModel is allowed to change the value.
    // Ιδιωτικό, μεταβλητό: μόνο αυτό το ViewModel επιτρέπεται να αλλάξει την τιμή.
    private val _chapters = MutableStateFlow(repository.chapters)

    // Public, read-only: the UI can only read/observe it, never modify it directly.
    // This "private mutable + public read-only" pair is a standard Android pattern.
    // Δημόσιο, μόνο για ανάγνωση: το UI μπορεί μόνο να το διαβάζει/παρακολουθεί, ποτέ να
    // το τροποποιεί απευθείας. Αυτό το ζευγάρι "ιδιωτικό μεταβλητό + δημόσιο μόνο για
    // ανάγνωση" είναι ένα στάνταρ μοτίβο στο Android.
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    companion object {
        /**
         * Builds a [ViewModelProvider.Factory] that knows how to construct a
         * [ChapterListViewModel] with the given [repository]. Needed because this app has
         * no DI framework (Hilt/Koin): the standard [ViewModel] system can only build
         * no-argument ViewModels on its own, so a factory is how we hand it our own
         * dependencies.
         * Χτίζει ένα [ViewModelProvider.Factory] που ξέρει πώς να κατασκευάσει ένα
         * [ChapterListViewModel] με το δοσμένο [repository]. Χρειάζεται γιατί η εφαρμογή
         * δεν έχει framework dependency injection (Hilt/Koin): το στάνταρ σύστημα
         * [ViewModel] μπορεί μόνο του να φτιάξει ViewModels χωρίς παραμέτρους, οπότε το
         * factory είναι ο τρόπος να του δώσουμε τις δικές μας εξαρτήσεις.
         */
        fun factory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChapterListViewModel(repository) as T
            }
    }
}
