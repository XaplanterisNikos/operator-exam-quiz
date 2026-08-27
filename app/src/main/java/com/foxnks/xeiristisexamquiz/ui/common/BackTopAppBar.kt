package com.foxnks.xeiristisexamquiz.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.foxnks.xeiristisexamquiz.R

/**
 * A [TopAppBar] with a back arrow, used by every non-root screen so there is always an
 * obvious way back besides the system back gesture/button.
 * Μια [TopAppBar] με βέλος επιστροφής, που χρησιμοποιείται σε κάθε μη-αρχική οθόνη ώστε
 * να υπάρχει πάντα εμφανής τρόπος επιστροφής, πέρα από τη χειρονομία/κουμπί "πίσω" του
 * συστήματος.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back_content_description)
                )
            }
        }
    )
}
