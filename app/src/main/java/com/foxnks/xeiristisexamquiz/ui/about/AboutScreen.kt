package com.foxnks.xeiristisexamquiz.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foxnks.xeiristisexamquiz.BuildConfig
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.ui.common.BackTopAppBar

private const val CONTACT_EMAIL = "xaplanterisnikos@gmail.com"
private const val LINKEDIN_URL = "https://www.linkedin.com/in/nick-xaplanteris/"

/**
 * "About" screen: what the app is for, help text for the flag/question-navigator buttons,
 * the creator's own story, and contact links (email, LinkedIn).
 * Οθόνη "Σχετικά": τι κάνει η εφαρμογή, κείμενο βοήθειας για τα κουμπιά flag/question-
 * navigator, η ιστορία του δημιουργού, και σύνδεσμοι επικοινωνίας (email, LinkedIn).
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = { BackTopAppBar(title = stringResource(R.string.about_title), onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AboutSectionCard(
                title = stringResource(R.string.about_purpose_title),
                body = stringResource(R.string.about_purpose_text)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AboutSectionCard(
                title = stringResource(R.string.about_help_title),
                body = stringResource(R.string.about_help_text)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AboutSectionCard(
                title = stringResource(R.string.about_creator_title),
                body = stringResource(R.string.about_creator_story_text)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(R.string.about_contact_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(onClick = { sendEmail(context) }, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about_contact_email_label)) },
                    supportingContent = { Text(CONTACT_EMAIL) },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(onClick = { openLinkedIn(context) }, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about_contact_linkedin_label)) },
                    supportingContent = { Text("nick-xaplanteris") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    }
                )
            }

            Text(
                text = stringResource(R.string.app_version_format, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
    }
}

@Composable
private fun AboutSectionCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Opens the device's email app with the contact address pre-filled, via an explicit
 * ACTION_SENDTO intent (not ACTION_SEND) so only email apps - not any generic
 * share-capable app - are offered as a target. Falls back to a Toast if no email app is
 * installed, instead of crashing with an unhandled ActivityNotFoundException.
 * Ανοίγει την εφαρμογή email της συσκευής με προσυμπληρωμένη τη διεύθυνση επικοινωνίας,
 * μέσω ρητού intent ACTION_SENDTO (όχι ACTION_SEND) ώστε να προτείνονται μόνο εφαρμογές
 * email - όχι οποιαδήποτε γενική εφαρμογή διαμοιρασμού. Επιστρέφει σε Toast αν δεν υπάρχει
 * εγκατεστημένη εφαρμογή email, αντί να κρασάρει με ένα ανεπεξέργαστο
 * ActivityNotFoundException.
 */
private fun sendEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$CONTACT_EMAIL".toUri()).apply {
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.about_email_subject))
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.about_email_app_not_found), Toast.LENGTH_SHORT).show()
    }
}

private fun openLinkedIn(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, LINKEDIN_URL.toUri())
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.about_link_app_not_found),
            Toast.LENGTH_SHORT
        ).show()
    }
}
