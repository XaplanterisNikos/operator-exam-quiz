package com.foxnks.xeiristisexamquiz.data.model

import kotlinx.serialization.Serializable

/**
 *  Defines whether a question has one (SINGLE) or more than one (MULTIPLE) correct answers
 *  Καθορίζει αν μια ερώτηση έχει μία (SINGLE) ή περισσότερες (MULTIPLE) σωστές απαντήσεις
 */
@Serializable
enum class QuestionType { SINGLE, MULTIPLE }
