package com.foxnks.xeiristisexamquiz.ui.chapterlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxnks.xeiristisexamquiz.R
import com.foxnks.xeiristisexamquiz.data.model.Chapter
import com.foxnks.xeiristisexamquiz.ui.common.BackTopAppBar

/**
 * Shows the list of syllabus chapters; tapping one opens practice mode for that chapter.
 * Δείχνει τη λίστα των κεφαλαίων ύλης· πατώντας ένα ανοίγει η εξάσκηση γι' αυτό το κεφάλαιο.
 */
@Composable
fun ChapterListScreen(
    viewModel: ChapterListViewModel,
    onChapterClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subscribes to the ViewModel's StateFlow. "by" (property delegation) means we can use
    // "chapters" directly below as a plain List, instead of "chapters.value" every time.
    // "WithLifecycle" additionally pauses the subscription automatically when the screen
    // isn't visible, to avoid wasted work.
    // Γίνεται συνδρομητής στο StateFlow του ViewModel. Το "by" (property delegation) μας
    // επιτρέπει να χρησιμοποιούμε το "chapters" απευθείας παρακάτω σαν απλή Λίστα, αντί
    // για "chapters.value" κάθε φορά. Το "WithLifecycle" επιπλέον παγώνει αυτόματα τη
    // συνδρομή όταν η οθόνη δεν είναι ορατή, για να μη γίνεται άσκοπη δουλειά.
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.chapter_list_title),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        // LazyColumn only creates/draws the rows currently visible on screen (plus a small
        // buffer), instead of all of them at once - essential for lists that could grow
        // large. The "key = Chapter::id" tells Compose how to tell rows apart across
        // recompositions, so it can correctly reuse/reorder them instead of redrawing
        // everything from scratch.
        // Η LazyColumn δημιουργεί/ζωγραφίζει μόνο τις γραμμές που είναι αυτή τη στιγμή
        // ορατές στην οθόνη (συν ένα μικρό απόθεμα), αντί για όλες μαζί - απαραίτητο για
        // λίστες που θα μπορούσαν να μεγαλώσουν. Το "key = Chapter::id" λέει στο Compose
        // πώς να ξεχωρίζει τις γραμμές μεταξύ recompositions, ώστε να μπορεί σωστά να τις
        // επαναχρησιμοποιεί/αναδιατάσσει αντί να ξαναζωγραφίζει τα πάντα από την αρχή.
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            items(chapters, key = Chapter::id) { chapter ->
                ListItem(
                    headlineContent = { Text(chapter.title) },
                    modifier = Modifier.clickable { onChapterClick(chapter.id) }
                )
                HorizontalDivider()
            }
        }
    }
}
