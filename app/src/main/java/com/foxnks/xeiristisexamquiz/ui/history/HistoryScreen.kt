package com.foxnks.xeiristisexamquiz.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.data.local.ExamAttemptEntity
import com.foxnks.xeiristisexamquiz.ui.common.BackTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.attempts.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopAppBar(title = stringResource(R.string.history_title), onBackClick = onBackClick)
        }
    ) { innerPadding ->
        if (attempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(attempts, key = ExamAttemptEntity::id) { attempt ->
                    ExamAttemptRow(attempt)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ExamAttemptRow(attempt: ExamAttemptEntity) {
    ListItem(
        headlineContent = { Text(formatAttemptDate(attempt.timestampMillis)) },
        supportingContent = {
            Text(
                stringResource(
                    R.string.history_score_format,
                    attempt.correctCount,
                    attempt.totalQuestions,
                    attempt.percentage
                )
            )
        },
        trailingContent = {
            Text(
                text = if (attempt.passed) {
                    stringResource(R.string.exam_result_passed)
                } else {
                    stringResource(R.string.exam_result_failed)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (attempt.passed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    )
}

private val greekLocale: Locale = Locale.Builder().setLanguage("el").setRegion("GR").build()

private fun formatAttemptDate(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", greekLocale)
    return formatter.format(Date(timestampMillis))
}
