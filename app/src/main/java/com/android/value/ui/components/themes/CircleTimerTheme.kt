package com.android.value.ui.components.themes

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
import com.android.value.core.ThemeFractionCalculator
import com.android.value.core.TimeUtils
import com.android.value.model.TimerStatus
import com.android.value.ui.components.MinimalCircularProgress
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray
import com.android.value.ui.theme.ValueTimerTheme

/**
 * Theme 1: Circle (existing).
 * Clean, minimal circular progress ring with centered monospaced countdown digits.
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
        val diameter = minOf(maxWidth * 0.85f, maxHeight * 0.70f, 320.dp)
        val strokeWidth = (diameter.value * 0.012f).coerceIn(2.5f, 4.5f).dp
        val fontSize = (diameter.value * 0.145f).coerceIn(28f, 52f).sp

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
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(if (isPaused) 1.0f else 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CircleTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            CircleTimerTheme(
                remainingMillis = 125000L,
                totalMillis = 300000L,
                status = TimerStatus.Running
            )
        }
    }
}

