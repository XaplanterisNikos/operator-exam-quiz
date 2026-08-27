package com.foxnks.xeiristisexamquiz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Color scheme for dark mode, based on the "80" tones.
 * Σχήμα χρωμάτων για σκούρο θέμα (dark mode), βασισμένο στις "80" αποχρώσεις.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Color scheme for light mode, based on the "40" tones.
 * Σχήμα χρωμάτων για ανοιχτό θέμα (light mode), βασισμένο στις "40" αποχρώσεις.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Wraps the whole app and gives it Material3 colors/typography. Called once at the top,
 * in MainActivity.
 *
 * Color scheme selection:
 * 1. If dynamicColor=true AND the device is Android 12+ (S): uses colors derived from the
 *    user's wallpaper (Material You) - different per device/user.
 * 2. Otherwise: uses the fixed DarkColorScheme or LightColorScheme above, depending on
 *    whether the device is in dark mode.
 * "Τυλίγει" όλη την εφαρμογή και της δίνει χρώματα/γραμματοσειρές Material3. Καλείται μία
 * φορά στην κορυφή, στο MainActivity.
 *
 * Επιλογή σχήματος χρωμάτων:
 * 1. Αν dynamicColor=true ΚΑΙ η συσκευή είναι Android 12+ (S): χρησιμοποιεί τα χρώματα του
 *    wallpaper του χρήστη (Material You) - διαφορετικό ανά συσκευή/χρήστη.
 * 2. Αλλιώς: χρησιμοποιεί το σταθερό DarkColorScheme ή LightColorScheme παραπάνω, ανάλογα
 *    με το αν η συσκευή είναι σε dark mode.
 *
 */
@Composable
fun XeiristisExamQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}