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

@Composable
fun ChapterListScreen(
    viewModel: ChapterListViewModel,
    onChapterClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
