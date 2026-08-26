package com.foxnks.xeiristisexamquiz.ui.chapterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.data.model.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChapterListViewModel(repository: QuestionRepository) : ViewModel() {

    private val _chapters = MutableStateFlow(repository.chapters)
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    companion object {
        fun factory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChapterListViewModel(repository) as T
            }
    }
}
