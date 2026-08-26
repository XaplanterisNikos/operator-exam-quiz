package com.foxnks.xeiristisexamquiz.ui.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.ui.common.BackTopAppBar
import com.foxnks.xeiristisexamquiz.ui.common.OptionRow

@Composable
fun PracticeQuizScreen(
    viewModel: PracticeQuizViewModel,
    onFinished: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isChapterFinished) {
        if (state.isChapterFinished) onFinished()
    }

    val question = state.question ?: return

    Scaffold(
        modifier = modifier,
        topBar = { BackTopAppBar(title = state.chapterTitle, onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(
                        R.string.question_progress_format,
                        state.questionIndex + 1,
                        state.totalQuestions
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.questionIndex + 1f) / state.totalQuestions },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = question.text, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                question.options.forEach { option ->
                    OptionRow(
                        text = option.text,
                        questionType = question.type,
                        isSelected = option.id in state.selectedOptionIds,
                        isAnswerChecked = state.isAnswerChecked,
                        isCorrectOption = option.isCorrect,
                        onClick = { viewModel.toggleOption(option.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.isAnswerChecked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.isCorrect) {
                            stringResource(R.string.practice_answer_correct)
                        } else {
                            stringResource(R.string.practice_answer_wrong)
                        },
                        color = if (state.isCorrect) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    question.explanation?.let { explanation ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = explanation, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!state.isAnswerChecked) {
                Button(
                    onClick = viewModel::checkAnswer,
                    enabled = state.selectedOptionIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.practice_check_answer_button))
                }
            } else {
                Button(
                    onClick = viewModel::goToNextQuestion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.questionIndex + 1 < state.totalQuestions) {
                            stringResource(R.string.practice_next_question_button)
                        } else {
                            stringResource(R.string.practice_finish_chapter_button)
                        }
                    )
                }
            }
        }
    }
}
