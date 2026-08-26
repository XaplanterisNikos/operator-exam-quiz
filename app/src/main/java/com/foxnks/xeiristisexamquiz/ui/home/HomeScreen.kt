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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Χωρίζουμε κάθετα την οθόνη σε δύο ζώνες: από πάνω ο τίτλος/τα κουμπιά,
            // από κάτω η ζώνη του watermark. Η πάνω ζώνη ΔΕΝ παίρνει weight() —
            // καταλαμβάνει μόνο όσο ύψος χρειάζεται το περιεχόμενό της (ώστε ποτέ να
            // μην περικόπτονται τα κουμπιά αν ο υπότιτλος γίνει πιο μακρύς σε μικρή
            // οθόνη). Το weight() πάει ολόκληρο στη ζώνη του watermark από κάτω, η
            // οποία έτσι απορροφά όλο τον υπόλοιπο χώρο και δεν μένει άδειο κενό.
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Σύντομος, έντονος τίτλος της εφαρμογής.
                    Text(
                        text = stringResource(R.string.app_display_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

                // Ζώνη του watermark: μοναδικό στοιχείο με weight() στη Column, οπότε
                // απορροφά όλο τον χώρο που περισσεύει κάτω από τα κουμπιά. Το ίδιο
                // ContentScale/alignment με πριν (FillWidth + BottomCenter), απλά μέσα
                // σε μικρότερο container — η εικόνα δεν αλλάζει.
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
