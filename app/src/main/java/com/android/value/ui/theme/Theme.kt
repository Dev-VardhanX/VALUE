package com.android.value.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = PureWhite,
    onPrimaryContainer = PureBlack,
    secondary = SubtleGray,
    onSecondary = PureWhite,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = SubtleGray,
    outline = SubtleGray,
    outlineVariant = BorderSubtle
)

/**
 * Minimalist monochrome theme for VALUE Timer.
 * Dark theme is permanently enforced and dynamic color is disabled.
 */
@Composable
fun ValueTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        typography = Typography,
        content = content
    )
}
