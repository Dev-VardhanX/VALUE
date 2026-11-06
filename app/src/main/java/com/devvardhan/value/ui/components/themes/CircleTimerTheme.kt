package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.CellSizeHelper
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.components.CentisecondsSlot
import com.devvardhan.value.ui.components.MinimalCircularProgress
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 1: Circle.
 * Clean, minimal circular progress ring with centered monospaced countdown or stopwatch digits.
 * In Stopwatch mode: centiseconds sit on the same baseline directly next to the seconds.
 */
@Composable
fun CircleTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val progress = if (isStopwatch) {
        StopwatchFractionCalculator.calculateCircleProgress(stopwatchElapsedMillis)
    } else {
        ThemeFractionCalculator.calculateOverallProgress(remainingMillis, totalMillis)
    }

    val isPaused = if (isStopwatch) {
        stopwatchStatus == StopwatchStatus.Paused
    } else {
        status == TimerStatus.Paused
    }

    val formattedTime = if (isStopwatch) {
        TimeUtils.formatStopwatchAdaptive(stopwatchElapsedMillis)
    } else {
        TimeUtils.formatHms(remainingMillis)
    }

    val subText = if (isStopwatch) {
        if (isPaused) "PAUSED" else "STOPWATCH"
    } else {
        val totalTimeFormatted = TimeUtils.formatHms(totalMillis)
        if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted"
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableDiameter = minOf(maxWidth.value, maxHeight.value)
        val diameter = (availableDiameter - if (layoutMode == ThemeLayoutMode.PORTRAIT) 16f else 28f).coerceAtLeast(90f).dp
        val strokeWidth = (diameter.value * 0.015f).coerceIn(2.5f, 6.5f).dp
        val hasHours = isStopwatch && stopwatchElapsedMillis >= 3_600_000L
        val innerDiameter = diameter.value - (strokeWidth.value * 2f)

        val fontSize = CellSizeHelper.calculateInnerRingFontSize(
            innerDiameter = innerDiameter,
            hasHours = hasHours,
            isStopwatch = isStopwatch
        ).sp

        MinimalCircularProgress(
            progress = progress,
            size = diameter,
            strokeWidth = strokeWidth,
            showLeadingDot = isStopwatch
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isStopwatch) {
                    val mainUnits = TimeUtils.formatStopwatchMainUnits(stopwatchElapsedMillis)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mainUnits,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Light,
                                fontSize = fontSize,
                                letterSpacing = if (hasHours) 0.5.sp else 1.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        CentisecondsSlot(
                            elapsedMillis = stopwatchElapsedMillis,
                            fontSize = (fontSize.value * 0.40f).coerceIn(10f, 26f).sp,
                            modifier = Modifier.padding(bottom = (fontSize.value * 0.06f).dp)
                        )
                    }
                } else {
                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            fontSize = fontSize,
                            letterSpacing = if (hasHours) 0.5.sp else 1.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = PureWhite,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height((diameter.value * 0.025f).coerceIn(4f, 14f).dp))

                Text(
                    text = subText,
                    fontSize = (diameter.value * 0.04f).coerceIn(9f, 15f).sp,
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

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Circle Stopwatch 360dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun CircleStopwatch360Preview() {
    ValueTimerTheme {
        CircleTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 1710L, // 00:00:01.71
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "Circle Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun CircleStopwatchPortraitPreview() {
    ValueTimerTheme {
        CircleTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 145230L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "Circle Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun CircleStopwatchLandscapePreview() {
    ValueTimerTheme {
        CircleTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 4125890L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}
