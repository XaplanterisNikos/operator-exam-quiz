package com.foxnks.xeiristisexamquiz.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.data.model.QuestionType

/**
 * A single selectable option inside a question. Renders as a RadioButton for [QuestionType.SINGLE]
 * questions or a Checkbox for [QuestionType.MULTIPLE] ones. Once [isAnswerChecked] is true, the
 * row becomes read-only and highlights the correct option in green and any wrong selection in red.
 */
@Composable
fun OptionRow(
    text: String,
    questionType: QuestionType,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    isCorrectOption: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        !isAnswerChecked -> Color.Transparent
        isCorrectOption -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val stateHint = when {
        !isAnswerChecked -> null
        isCorrectOption -> stringResource(R.string.option_row_state_correct)
        isSelected -> stringResource(R.string.option_row_state_wrong_selected)
        else -> null
    }

    val selectionModifier = when (questionType) {
        QuestionType.SINGLE -> Modifier.selectable(
            selected = isSelected,
            enabled = !isAnswerChecked,
            role = Role.RadioButton,
            onClick = onClick
        )
        QuestionType.MULTIPLE -> Modifier.toggleable(
            value = isSelected,
            enabled = !isAnswerChecked,
            role = Role.Checkbox,
            onValueChange = { onClick() }
        )
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .then(selectionModifier)
            .semantics { stateHint?.let { stateDescription = it } }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            when (questionType) {
                QuestionType.SINGLE -> RadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = !isAnswerChecked
                )
                QuestionType.MULTIPLE -> Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    enabled = !isAnswerChecked
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, modifier = Modifier.weight(1f))
        }
    }
}
