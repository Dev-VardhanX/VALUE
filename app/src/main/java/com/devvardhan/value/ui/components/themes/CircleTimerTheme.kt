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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.PlatformTextStyle
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
 *
 * Performance optimizations:
 * - Progress is calculated on-demand via a lambda passed into MinimalCircularProgress,
 *   running strictly in the Canvas draw phase.
 * - Digits are derived once per second via derivedStateOf, eliminating 60fps/100fps recomposition.
 * - Centiseconds rendering is delegated to CentisecondsSlot in the draw phase.
 */
@Composable
fun CircleTimerTheme(
    remainingMillisProvider: () -> Long = { 0L },
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillisProvider: () -> Long = { 0L },
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val isPaused = if (isStopwatch) {
        stopwatchStatus == StopwatchStatus.Paused
    } else {
        status == TimerStatus.Paused
    }

    // Optimization: Progress provider is evaluated in the Canvas draw phase so the composable does not recompose
    val progressProvider: () -> Float = remember(isStopwatch, totalMillis, remainingMillisProvider, stopwatchElapsedMillisProvider) {
        {
            if (isStopwatch) {
                StopwatchFractionCalculator.calculateCircleProgress(stopwatchElapsedMillisProvider())
            } else {
                ThemeFractionCalculator.calculateOverallProgress(remainingMillisProvider(), totalMillis)
            }
        }
    }

    // Optimization: Digits derived once per second to prevent unnecessary recomposition on every frame
    val displayedSeconds by remember(if (isStopwatch) stopwatchElapsedMillisProvider else remainingMillisProvider) {
        derivedStateOf {
            if (isStopwatch) {
                stopwatchElapsedMillisProvider() / 1000L
            } else {
                val rem = remainingMillisProvider()
                if (rem <= 0L) 0L else (rem + 999L) / 1000L
            }
        }
    }

    val formattedTime by remember(displayedSeconds, isStopwatch) {
        derivedStateOf {
            if (isStopwatch) {
                TimeUtils.formatStopwatchAdaptive(displayedSeconds * 1000L)
            } else {
                TimeUtils.formatHmsFromSeconds(displayedSeconds)
            }
        }
    }

    val mainUnits by remember(displayedSeconds) {
        derivedStateOf {
            TimeUtils.formatStopwatchMainUnits(displayedSeconds * 1000L)
        }
    }

    val hasHours by remember(displayedSeconds, isStopwatch) {
        derivedStateOf {
            isStopwatch && displayedSeconds >= 3600L
        }
    }

    val subText = remember(isStopwatch, isPaused, totalMillis) {
        if (isStopwatch) {
            if (isPaused) "PAUSED" else "STOPWATCH"
        } else {
            val totalTimeFormatted = TimeUtils.formatHms(totalMillis)
            if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted"
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableDiameter = minOf(maxWidth.value, maxHeight.value)
        val diameter = (availableDiameter - if (layoutMode == ThemeLayoutMode.PORTRAIT) 16f else 28f).coerceAtLeast(90f).dp
        val strokeWidth = (diameter.value * 0.015f).coerceIn(2.5f, 6.5f).dp
        val innerRadius = (diameter.value - (strokeWidth.value * 2f)) / 2f

        val fontSize = CellSizeHelper.calculateInnerRingSafeFontSize(
            innerRadius = innerRadius,
            hasHours = hasHours,
            isStopwatch = isStopwatch,
            hasLabel = false
        ).sp

        MinimalCircularProgress(
            progressProvider = progressProvider,
            size = diameter,
            strokeWidth = strokeWidth,
            showLeadingDot = isStopwatch
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isStopwatch) {
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
                                fontFeatureSettings = "tnum",
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = fontSize
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        CentisecondsSlot(
                            elapsedMillisProvider = stopwatchElapsedMillisProvider,
                            fontSize = (fontSize.value * 0.40f).sp,
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
                            letterSpacing = 0.5.sp,
                            fontFeatureSettings = "tnum",
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeight = fontSize
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

/**
 * Backward-compatible overload accepting direct Long values.
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
) = CircleTimerTheme(
    remainingMillisProvider = { remainingMillis },
    totalMillis = totalMillis,
    status = status,
    isStopwatch = isStopwatch,
    stopwatchElapsedMillisProvider = { stopwatchElapsedMillis },
    stopwatchStatus = stopwatchStatus,
    modifier = modifier,
    layoutMode = layoutMode
)

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
