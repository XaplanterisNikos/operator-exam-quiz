package com.foxnks.xeiristisexamquiz.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxnks.xeiristisexamquiz.BuildConfig
import com.foxnks.xeiristisexamquiz.R

/**
 * Splash/loading screen shown while the app's data (question bank, exam config) is being
 * loaded. Layered with Box: watermark image at the back, spinner + app name + version/
 * copyright in the middle, signature pinned to the bottom.
 * Οθόνη εκκίνησης/φόρτωσης που εμφανίζεται όσο φορτώνονται τα δεδομένα της εφαρμογής
 * (τράπεζα ερωτήσεων, ρυθμίσεις τεστ). Επιστρωμένη με Box: watermark εικόνα από πίσω,
 * spinner + όνομα εφαρμογής + έκδοση/copyright στη μέση, υπογραφή καρφωμένη στο κάτω μέρος.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Same watermark layout as HomeScreen (matchParentSize + BottomCenter + FillWidth,
        // so it doesn't get distorted), just with much stronger alpha here since there's no
        // other content whose readability it could hurt.
        // Ίδια διάταξη watermark με το HomeScreen (matchParentSize + BottomCenter + FillWidth,
        // ώστε να μη διαστρεβλώνεται), εδώ απλά με πολύ πιο έντονο alpha αφού δεν υπάρχει
        // άλλο περιεχόμενο να χάσει την αναγνωσιμότητά του.
        Image(
            painter = painterResource(id = R.drawable.home_watermark),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            alignment = Alignment.BottomCenter,
            contentScale = ContentScale.FillWidth,
            alpha = 0.6f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_display_name),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator()

            // Gap between the spinner and the version/copyright text, so they don't
            // visually "stick together".
            // Κενό ανάμεσα στο spinner και το κείμενο έκδοσης/copyright, ώστε να
            // μην "κολλάνε" οπτικά.
            Spacer(modifier = Modifier.height(16.dp))

            // App version + copyright, below the spinner. Stays inside the same centered
            // Column (not pinned to the absolute bottom of the screen) so it always lands
            // on the background, NOT on top of home_watermark.png, which only covers the
            // strip near the screen edge. VERSION_NAME comes from BuildConfig so it updates
            // itself with every new release, with no manual change needed here.
            // Έκδοση εφαρμογής + copyright, κάτω από το spinner. Παραμένει μέσα στην ίδια
            // κεντραρισμένη Column (όχι κολλημένο στο απόλυτο κάτω άκρο της οθόνης) ώστε
            // να πέφτει πάντα στο φόντο, ΟΧΙ πάνω στο home_watermark.png, το οποίο καλύπτει
            // μόνο τη λωρίδα κοντά στην άκρη της οθόνης. Το VERSION_NAME έρχεται από το
            // BuildConfig ώστε να ενημερώνεται μόνο του σε κάθε νέα έκδοση, χωρίς
            // χειροκίνητη αλλαγή εδώ.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.app_copyright_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Same signature, same size/position as HomeScreen, fully opaque.
        // Ίδια υπογραφή, ίδιο μέγεθος/θέση με το HomeScreen, πλήρως αδιαφανής.
        Image(
            painter = painterResource(id = R.drawable.foxnks_signature),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .width(110.dp),
            contentScale = ContentScale.FillWidth
        )
    }
}
