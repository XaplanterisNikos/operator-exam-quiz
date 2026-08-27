package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 *  A syllabus chapter. order determines the display order in the chapter list.
 *  Ένα κεφάλαιο ύλης. Το order καθορίζει τη σειρά εμφάνισης στη λίστα κεφαλαίων.
 */
@Serializable
data class Chapter(
    val id: Int,
    val title: String,
    val order: Int
)
