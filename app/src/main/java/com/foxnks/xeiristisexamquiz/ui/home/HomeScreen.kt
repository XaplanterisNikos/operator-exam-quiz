package com.foxnks.xeiristisexamquiz.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foxnks.xeiristisexamquiz.R

/**
 * The app's home screen: title, the three main action buttons (Practice, Exam, History),
 * an Info icon leading to About, and the watermark illustration in the background.
 * Η αρχική οθόνη της εφαρμογής: τίτλος, τα τρία κύρια κουμπιά ενεργειών (Εξάσκηση, Τεστ,
 * Ιστορικό), ένα εικονίδιο Info που οδηγεί στο About, και η εικόνα watermark στο φόντο.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPracticeClick: () -> Unit,
    onExamClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.home_about_content_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // innerPadding is the space the Scaffold reserves for the TopAppBar - applying it
        // here keeps our content from being drawn underneath it.
        // Το innerPadding είναι ο χώρος που κρατάει η Scaffold για την TopAppBar - το
        // εφαρμόζουμε εδώ ώστε το περιεχόμενό μας να μη ζωγραφίζεται από κάτω της.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Split the screen vertically into two zones: title/buttons on top, the
            // watermark zone below. The top zone does NOT get weight() - it only takes as
            // much height as its content needs (so the buttons never get clipped if the
            // subtitle grows longer on a small screen). weight() goes entirely to the
            // watermark zone below, which then absorbs all the remaining space so no empty
            // gap is left.
            // Χωρίζουμε κάθετα την οθόνη σε δύο ζώνες: από πάνω ο τίτλος/τα κουμπιά, από
            // κάτω η ζώνη του watermark. Η πάνω ζώνη ΔΕΝ παίρνει weight() — καταλαμβάνει
            // μόνο όσο ύψος χρειάζεται το περιεχόμενό της (ώστε ποτέ να μην περικόπτονται
            // τα κουμπιά αν ο υπότιτλος γίνει πιο μακρύς σε μικρή οθόνη). Το weight() πάει
            // ολόκληρο στη ζώνη του watermark από κάτω, η οποία έτσι απορροφά όλο τον
            // υπόλοιπο χώρο και δεν μένει άδειο κενό.
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Short, bold app title.
                    // Σύντομος, έντονος τίτλος της εφαρμογής.
                    Text(
                        text = stringResource(R.string.app_display_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // The official, longer exam title as a lighter-colored subtitle.
                    // Το επίσημο, μεγάλο κείμενο της εξέτασης σαν υπότιτλος, πιο ανοιχτόχρωμο.
                    Text(
                        text = stringResource(R.string.home_official_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(onClick = onPracticeClick, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.home_menu_practice))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onExamClick, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.exam_title))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.history_title))
                    }
                }

                // Watermark zone: the only element with weight() in this Column, so it
                // absorbs all the space left over below the buttons. Same
                // ContentScale/alignment as before (FillWidth + BottomCenter), just inside
                // a smaller container - the image itself doesn't change.
                // Ζώνη του watermark: μοναδικό στοιχείο με weight() στη Column, οπότε
                // απορροφά όλο τον χώρο που περισσεύει κάτω από τα κουμπιά. Το ίδιο
                // ContentScale/alignment με πριν (FillWidth + BottomCenter), απλά μέσα σε
                // μικρότερο container — η εικόνα δεν αλλάζει.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.home_watermark),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.BottomCenter,
                        contentScale = ContentScale.FillWidth,
                        alpha = 0.08f
                    )
                }
            }

            // Signature, fully opaque, pinned to the very bottom, above the watermark.
            // Υπογραφή, πλήρως αδιαφανής, στο απόλυτο κάτω μέρος, πάνω από το watermark.
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
}
