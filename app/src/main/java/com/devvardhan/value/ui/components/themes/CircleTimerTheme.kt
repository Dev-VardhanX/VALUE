package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.components.MinimalCircularProgress
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 1: Circle.
 * Clean, minimal circular progress ring with centered monospaced countdown digits.
 * Scales dynamically to min(width, height) - padding in portrait, landscape, and tablet.
 */
@Composable
fun CircleTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val progress = ThemeFractionCalculator.calculateOverallProgress(remainingMillis, totalMillis)
    val isPaused = status == TimerStatus.Paused
    val formattedTime = TimeUtils.formatHms(remainingMillis)
    val totalTimeFormatted = TimeUtils.formatHms(totalMillis)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val diameter = (minOf(maxWidth, maxHeight) - 32.dp).coerceAtLeast(120.dp)
        val strokeWidth = (diameter.value * 0.014f).coerceIn(2.5f, 6.5f).dp
        val fontSize = (diameter.value * 0.16f).coerceIn(24f, 84f).sp

        MinimalCircularProgress(
            progress = progress,
            size = diameter,
            strokeWidth = strokeWidth
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height((diameter.value * 0.025f).coerceIn(4f, 16f).dp))

                Text(
                    text = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted",
                    fontSize = (diameter.value * 0.04f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(if (isPaused) 1.0f else 0.7f),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Preview(name = "Circle Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun CircleTimerThemePortraitPreview() {
    ValueTimerTheme {
        CircleTimerTheme(
            remainingMillis = 125000L,
            totalMillis = 300000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Circle Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun CircleTimerThemeLandscapePreview() {
    ValueTimerTheme {
        CircleTimerTheme(
            remainingMillis = 125000L,
            totalMillis = 300000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Circle Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun CircleTimerThemeTabletPreview() {
    ValueTimerTheme {
        CircleTimerTheme(
            remainingMillis = 125000L,
            totalMillis = 300000L,
            status = TimerStatus.Running
        )
    }
}
