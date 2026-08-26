package com.foxnks.xeiristisexamquiz.ui.results

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.data.model.Question
import com.foxnks.xeiristisexamquiz.ui.exam.ExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    viewModel: ExamViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val total = state.questions.size
    val correctCount = state.questions.count { viewModel.isAnswerCorrect(it) }
    val percent = if (total > 0) correctCount * 100f / total else 0f
    val passed = percent >= viewModel.passThresholdPercent
    val wrongQuestions = state.questions.filterNot { viewModel.isAnswerCorrect(it) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.exam_results_title)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.exam_results_score_format, correctCount, total),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (passed) {
                    stringResource(R.string.exam_result_passed)
                } else {
                    stringResource(R.string.exam_result_failed)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.exam_results_percentage_format,
                    percent,
                    viewModel.passThresholdPercent
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.exam_results_back_home_button))
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (wrongQuestions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.exam_results_wrong_answers_header_format, wrongQuestions.size),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(wrongQuestions, key = Question::id) { question ->
                        WrongAnswerCard(
                            question = question,
                            selectedOptionIds = state.answers[question.id]?.selectedOptionIds
                                ?: emptySet()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WrongAnswerCard(question: Question, selectedOptionIds: Set<String>) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = question.text, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val correctAnswerSuffix = stringResource(R.string.exam_results_correct_answer_suffix)
            val yourAnswerSuffix = stringResource(R.string.exam_results_your_answer_suffix)
            question.options.forEach { option ->
                val wasSelected = option.id in selectedOptionIds
                val suffix = when {
                    option.isCorrect -> correctAnswerSuffix
                    wasSelected -> yourAnswerSuffix
                    else -> ""
                }
                Text(
                    text = option.text + suffix,
                    color = when {
                        option.isCorrect -> MaterialTheme.colorScheme.primary
                        wasSelected -> MaterialTheme.colorScheme.error
                        else -> LocalContentColor.current
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
