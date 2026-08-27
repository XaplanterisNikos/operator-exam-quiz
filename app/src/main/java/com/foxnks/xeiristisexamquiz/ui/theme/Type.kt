package com.foxnks.xeiristisexamquiz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
/**
 * Material3 typography styles for the app. We only override bodyLarge (the main body
 * text style) - all other styles (titles, labels, etc.) fall back to Material3 defaults.
 * Στυλ γραμματοσειράς Material3 για την εφαρμογή. Ορίζουμε μόνο το bodyLarge (το κύριο
 * στυλ κειμένου) - όλα τα υπόλοιπα στυλ (τίτλοι, ετικέτες, κλπ) παίρνουν τις προεπιλογές
 * του Material3.
 */

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)