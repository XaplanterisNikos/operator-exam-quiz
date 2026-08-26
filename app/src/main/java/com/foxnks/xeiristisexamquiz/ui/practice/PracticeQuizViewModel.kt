package com.foxnks.xeiristisexamquiz.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foxnks.xeiristisexamquiz.data.QuestionRepository
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.data.model.QuestionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PracticeQuizUiState(
    val chapterTitle: String = "",
    val questionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val question: Question? = null,
    val selectedOptionIds: Set<String> = emptySet(),
    val isAnswerChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val isChapterFinished: Boolean = false
)

class PracticeQuizViewModel(
    repository: QuestionRepository,
    chapterId: Int
) : ViewModel() {

    private val chapterTitle = repository.getChapterById(chapterId)?.title.orEmpty()
    private val questions = repository.getQuestionsForChapter(chapterId)

    private val _uiState = MutableStateFlow(buildStateForIndex(0))
    val uiState: StateFlow<PracticeQuizUiState> = _uiState.asStateFlow()

    private fun buildStateForIndex(index: Int): PracticeQuizUiState {
        if (index >= questions.size) {
            return PracticeQuizUiState(
                chapterTitle = chapterTitle,
                questionIndex = index,
                totalQuestions = questions.size,
                isChapterFinished = true
            )
        }
        return PracticeQuizUiState(
            chapterTitle = chapterTitle,
            questionIndex = index,
            totalQuestions = questions.size,
            question = questions[index]
        )
    }

    fun toggleOption(optionId: String) {
        val state = _uiState.value
        val question = state.question ?: return
        if (state.isAnswerChecked) return

        val newSelection = when (question.type) {
            QuestionType.SINGLE -> setOf(optionId)
            QuestionType.MULTIPLE -> if (optionId in state.selectedOptionIds) {
                state.selectedOptionIds - optionId
            } else {
                state.selectedOptionIds + optionId
            }
        }
        _uiState.value = state.copy(selectedOptionIds = newSelection)
    }

    fun checkAnswer() {
        val state = _uiState.value
        val question = state.question ?: return
        if (state.isAnswerChecked || state.selectedOptionIds.isEmpty()) return

        val correctOptionIds = question.options.filter { it.isCorrect }.map { it.id }.toSet()
        val isCorrect = state.selectedOptionIds == correctOptionIds
        _uiState.value = state.copy(isAnswerChecked = true, isCorrect = isCorrect)
    }

    fun goToNextQuestion() {
        val state = _uiState.value
        if (!state.isAnswerChecked) return
        _uiState.value = buildStateForIndex(state.questionIndex + 1)
    }

    companion object {
        fun factory(repository: QuestionRepository, chapterId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PracticeQuizViewModel(repository, chapterId) as T
            }
    }
}
