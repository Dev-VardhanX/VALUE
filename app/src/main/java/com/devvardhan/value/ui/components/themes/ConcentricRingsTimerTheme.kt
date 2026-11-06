package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.InactiveTrack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 4: Concentric Rings.
 * Outer = Hours, Middle = Minutes, Inner = Seconds.
 * In Stopwatch mode: all rings fill UP clockwise. Digits are cleanly scaled to prevent inner ring clipping.
 */
@Composable
fun ConcentricRingsTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier
) {
    val secFraction = if (isStopwatch) {
        StopwatchFractionCalculator.calculateSecondsFraction(stopwatchElapsedMillis)
    } else {
        ThemeFractionCalculator.calculateSecondsFraction(remainingMillis)
    }

    val minFraction = if (isStopwatch) {
        StopwatchFractionCalculator.calculateMinutesFraction(stopwatchElapsedMillis)
    } else {
        ThemeFractionCalculator.calculateMinutesFraction(remainingMillis)
    }

    val hrFraction = if (isStopwatch) {
        StopwatchFractionCalculator.calculateHoursFraction(stopwatchElapsedMillis)
    } else {
        ThemeFractionCalculator.calculateHoursFraction(remainingMillis, totalMillis)
    }

    val displayedSeconds by remember(if (isStopwatch) stopwatchElapsedMillis else remainingMillis) {
        derivedStateOf {
            if (isStopwatch) {
                stopwatchElapsedMillis / 1000L
            } else {
                if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
            }
        }
    }

    val formattedTime by remember(displayedSeconds, isStopwatch, stopwatchElapsedMillis) {
        derivedStateOf {
            if (isStopwatch) {
                TimeUtils.formatStopwatchAdaptive(stopwatchElapsedMillis)
            } else {
                TimeUtils.formatHmsFromSeconds(displayedSeconds)
            }
        }
    }

    val isPaused = if (isStopwatch) {
        stopwatchStatus == StopwatchStatus.Paused
    } else {
        status == TimerStatus.Paused
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val geometry = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateConcentricRingsGeometry(
                width = maxWidth.value,
                height = maxHeight.value,
                padding = 32f
            )
        }

        val diameter = geometry.diameter.dp
        val strokeWidth = geometry.strokeWidth.dp
        val ringGap = geometry.ringGap.dp
        val hasHours = isStopwatch && stopwatchElapsedMillis >= 3_600_000L
        val fontFactor = if (hasHours) 0.16f else 0.20f
        val fontSize = (geometry.innerDiameter * fontFactor).coerceIn(16f, 56f).sp

        Box(
            modifier = Modifier.size(diameter),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokePx = strokeWidth.toPx()
                val gapPx = ringGap.toPx()

                val outerRadius = (size.width / 2f) - strokePx
                val middleRadius = (outerRadius - gapPx).coerceAtLeast(10f)
                val innerRadius = (middleRadius - gapPx).coerceAtLeast(10f)

                // Outer ring (Hours)
                if (outerRadius > 0f) {
                    val outerTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)
                    val outerSize = Size(outerRadius * 2f, outerRadius * 2f)
                    drawArc(
                        color = InactiveTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = outerTopLeft,
                        size = outerSize,
                        style = Stroke(width = strokePx)
                    )
                    val hrSweep = hrFraction * 360f
                    if (hrSweep > 0f) {
                        drawArc(
                            color = PureWhite,
                            startAngle = -90f,
                            sweepAngle = hrSweep,
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = outerSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Middle ring (Minutes)
                if (middleRadius > 0f) {
                    val midTopLeft = Offset(center.x - middleRadius, center.y - middleRadius)
                    val midSize = Size(middleRadius * 2f, middleRadius * 2f)
                    drawArc(
                        color = InactiveTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = midTopLeft,
                        size = midSize,
                        style = Stroke(width = strokePx)
                    )
                    val minSweep = minFraction * 360f
                    if (minSweep > 0f) {
                        drawArc(
                            color = PureWhite,
                            startAngle = -90f,
                            sweepAngle = minSweep,
                            useCenter = false,
                            topLeft = midTopLeft,
                            size = midSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Inner ring (Seconds)
                if (innerRadius > 0f) {
                    val inTopLeft = Offset(center.x - innerRadius, center.y - innerRadius)
                    val inSize = Size(innerRadius * 2f, innerRadius * 2f)
                    drawArc(
                        color = InactiveTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = inTopLeft,
                        size = inSize,
                        style = Stroke(width = strokePx)
                    )
                    val secSweep = secFraction * 360f
                    if (secSweep > 0f) {
                        drawArc(
                            color = PureWhite,
                            startAngle = -90f,
                            sweepAngle = secSweep,
                            useCenter = false,
                            topLeft = inTopLeft,
                            size = inSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Centered digits
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isStopwatch) {
                    val mainUnits = TimeUtils.formatStopwatchMainUnits(stopwatchElapsedMillis)
                    val centisStr = ".${TimeUtils.formatStopwatchCentiseconds(stopwatchElapsedMillis)}"

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mainUnits,
                            fontSize = fontSize,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            color = PureWhite,
                            letterSpacing = if (hasHours) 0.5.sp else 1.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = centisStr,
                            fontSize = (fontSize.value * 0.40f).coerceIn(12f, 24f).sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            color = SubtleGray,
                            modifier = Modifier.padding(bottom = (fontSize.value * 0.08f).dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Text(
                        text = formattedTime,
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = if (hasHours) 1.sp else 1.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (isPaused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAUSED",
                        fontSize = (fontSize.value * 0.28f).coerceIn(9f, 13f).sp,
                        fontWeight = FontWeight.Medium,
                        color = SubtleGray,
                        letterSpacing = 2.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Preview(name = "Concentric Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun ConcentricStopwatchPortraitPreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 145230L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "Concentric Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun ConcentricStopwatchLandscapePreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 4125890L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}
