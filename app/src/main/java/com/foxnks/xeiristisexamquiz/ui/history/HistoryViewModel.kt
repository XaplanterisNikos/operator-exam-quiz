package com.foxnks.xeiristisexamquiz.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foxnks.xeiristisexamquiz.data.ProgressRepository
import com.foxnks.xeiristisexamquiz.data.local.ExamAttemptEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Holds the exam attempt history for the history screen, sourced live from Room via
 * [ProgressRepository].
 * Κρατάει το ιστορικό απόπειρων τεστ για την οθόνη ιστορικού, ζωντανά από το Room μέσω του
 * [ProgressRepository].
 */
class HistoryViewModel(progressRepository: ProgressRepository) : ViewModel() {

    // Unlike the other ViewModels' "private MutableStateFlow + public StateFlow" pattern,
    // here there's nothing for this ViewModel to mutate itself - the data already arrives
    // as a live Flow straight from Room. stateIn() just adapts that Flow into a StateFlow
    // (so the UI always has an immediate current value, with no "waiting for first emission").
    // SharingStarted.WhileSubscribed(5000) keeps the underlying database query active only
    // while at least one screen is observing it, with a 5-second grace period after the
    // last observer disappears (e.g. a brief screen rotation) before actually stopping it -
    // avoiding a wasteful restart of the query for such a short gap.
    // Σε αντίθεση με το μοτίβο "ιδιωτικό MutableStateFlow + δημόσιο StateFlow" των άλλων
    // ViewModels, εδώ δεν υπάρχει τίποτα που να πρέπει να μεταβάλλει μόνο του αυτό το
    // ViewModel - τα δεδομένα έρχονται ήδη ως ζωντανό Flow απευθείας από το Room. Το
    // stateIn() απλά προσαρμόζει αυτό το Flow σε StateFlow (ώστε το UI να έχει πάντα άμεσα
    // διαθέσιμη μια τρέχουσα τιμή, χωρίς "αναμονή για την πρώτη τιμή"). Το
    // SharingStarted.WhileSubscribed(5000) κρατάει το ερώτημα της βάσης ενεργό μόνο όσο
    // τουλάχιστον μία οθόνη το παρακολουθεί, με περίοδο χάριτος 5 δευτερολέπτων αφού
    // εξαφανιστεί ο τελευταίος παρατηρητής (π.χ. σύντομη περιστροφή οθόνης) πριν το
    // σταματήσει πραγματικά - αποφεύγοντας άσκοπη επανεκκίνηση του ερωτήματος για τόσο
    // μικρό κενό.
    val attempts: StateFlow<List<ExamAttemptEntity>> = progressRepository.attempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(progressRepository: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HistoryViewModel(progressRepository) as T
            }
    }
}
