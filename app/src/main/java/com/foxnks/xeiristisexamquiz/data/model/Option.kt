package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 *  A single answer option within a question (text + whether it's correct)
 *  Μία επιλογή απάντησης μέσα σε μια ερώτηση (κείμενο + αν είναι σωστή)
 */
@Serializable
data class Option(
    val id: String, // μοναδικό id της επιλογής / unique id of the option
    val text: String,   // το κείμενο που βλέπει ο χρήστης / the text shown to the user
    val isCorrect: Boolean  // true αν είναι η σωστή απάντηση / true if this is the correct answer
)
