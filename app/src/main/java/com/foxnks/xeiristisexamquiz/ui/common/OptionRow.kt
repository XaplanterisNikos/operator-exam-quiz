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
 * A single selectable option inside a question. Renders as a RadioButton for
 * [QuestionType.SINGLE] questions or a Checkbox for [QuestionType.MULTIPLE] ones. Once
 * [isAnswerChecked] is true, the row becomes read-only and highlights the correct option
 * in green and any wrong selection in red.
 * Μία επιλέξιμη επιλογή μέσα σε μια ερώτηση. Εμφανίζεται ως RadioButton για ερωτήσεις
 * [QuestionType.SINGLE] ή ως Checkbox για [QuestionType.MULTIPLE]. Μόλις γίνει true το
 * [isAnswerChecked], η γραμμή γίνεται μόνο-για-ανάγνωση και τονίζει τη σωστή επιλογή με
 * πράσινο και οποιαδήποτε λάθος επιλογή με κόκκινο.
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
    // Background color of the row, depending on state: neutral while answering, green if
    // this is the correct option (once checked), red if it was wrongly selected.
    // Χρώμα φόντου της γραμμής, ανάλογα με την κατάσταση: ουδέτερο ενώ απαντάς, πράσινο αν
    // είναι η σωστή επιλογή (μόλις ελεγχθεί), κόκκινο αν επιλέχθηκε λάθος.
    val containerColor = when {
        !isAnswerChecked -> Color.Transparent
        isCorrectOption -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    // Accessibility text announced by screen readers (e.g. TalkBack) once the answer is
    // checked, so the color-coding isn't the only way to tell correct/wrong apart.
    // Κείμενο προσβασιμότητας που ανακοινώνεται από screen readers (π.χ. TalkBack) μόλις
    // ελεγχθεί η απάντηση, ώστε ο χρωματικός κώδικας να μην είναι ο μοναδικός τρόπος να
    // ξεχωρίσει κανείς το σωστό από το λάθος.
    val stateHint = when {
        !isAnswerChecked -> null
        isCorrectOption -> stringResource(R.string.option_row_state_correct)
        isSelected -> stringResource(R.string.option_row_state_wrong_selected)
        else -> null
    }

    // SINGLE questions behave like a radio group (selectable, Role.RadioButton); MULTIPLE
    // questions behave like independent checkboxes (toggleable, Role.Checkbox). Disabled
    // (enabled = !isAnswerChecked) once the answer has been checked, so it can't change.
    // Οι ερωτήσεις SINGLE συμπεριφέρονται σαν ομάδα radio (selectable, Role.RadioButton),
    // οι MULTIPLE σαν ανεξάρτητα checkbox (toggleable, Role.Checkbox). Απενεργοποιείται
    // (enabled = !isAnswerChecked) μόλις ελεγχθεί η απάντηση, ώστε να μην αλλάζει άλλο.
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
                // onClick/onCheckedChange = null here on purpose: the click is already
                // handled by the parent Surface's selectionModifier above, so the whole
                // row (not just the small radio/checkbox circle) is tappable.
                // Το onClick/onCheckedChange = null εδώ είναι σκόπιμο: το κλικ ήδη το
                // χειρίζεται το selectionModifier του γονικού Surface παραπάνω, ώστε να
                // πατιέται ολόκληρη η γραμμή (όχι μόνο ο μικρός κύκλος/τετράγωνο).
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
