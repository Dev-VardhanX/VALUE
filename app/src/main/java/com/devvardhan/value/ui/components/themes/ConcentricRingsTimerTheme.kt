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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.CellSizeHelper
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.components.CentisecondsSlot
import com.devvardhan.value.ui.theme.InactiveTrack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 4: Concentric Rings.
 * Outer = Hours, Middle = Minutes, Inner = Seconds.
 * In Stopwatch mode: all rings fill UP clockwise. Zero sweep threshold prevents false round-cap dots at 0.
 * Centiseconds sit on the same baseline directly next to the seconds.
 */
@Composable
fun ConcentricRingsTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
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
        val padding = if (layoutMode == ThemeLayoutMode.PORTRAIT) 16f else 28f
        val geometry = remember(maxWidth.value, maxHeight.value, padding) {
            AdaptiveLayoutHelper.calculateConcentricRingsGeometry(
                width = maxWidth.value,
                height = maxHeight.value,
                padding = padding
            )
        }

        val diameter = geometry.diameter.dp
        val strokeWidth = geometry.strokeWidth.dp
        val ringGap = geometry.ringGap.dp
        val hasHours = isStopwatch && stopwatchElapsedMillis >= 3_600_000L
        val hasLabel = isPaused || (!isStopwatch)
        val clearInnerRadius = geometry.innerDiameter / 2f

        val fontSize = remember(clearInnerRadius, hasHours, isStopwatch, hasLabel) {
            CellSizeHelper.calculateInnerRingSafeFontSize(
                innerRadius = clearInnerRadius,
                hasHours = hasHours,
                isStopwatch = isStopwatch,
                hasLabel = hasLabel
            )
        }.sp

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
                    // Minimum sweep threshold: do not draw round-cap dot at 0
                    if (hrSweep >= 1.0f) {
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
                    // Minimum sweep threshold: do not draw round-cap dot at 0
                    if (minSweep >= 1.0f) {
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
                    // Minimum sweep threshold: do not draw round-cap dot at 0
                    if (secSweep >= 1.0f) {
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
                            elapsedMillis = stopwatchElapsedMillis,
                            fontSize = (fontSize.value * 0.40f).sp,
                            modifier = Modifier.padding(bottom = (fontSize.value * 0.06f).dp)
                        )
                    }

                    if (isPaused) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PAUSED",
                            fontSize = (fontSize.value * 0.28f).coerceIn(9f, 13f).sp,
                            fontWeight = FontWeight.Medium,
                            color = SubtleGray,
                            letterSpacing = 1.5.sp,
                            maxLines = 1,
                            softWrap = false,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
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

                    Spacer(modifier = Modifier.height(2.dp))
                    val subText = if (isPaused) "PAUSED" else "TOTAL ${TimeUtils.formatHms(totalMillis)}"
                    Text(
                        text = subText,
                        fontSize = (fontSize.value * 0.25f).coerceIn(8f, 12f).sp,
                        fontWeight = FontWeight.Medium,
                        color = SubtleGray,
                        letterSpacing = 1.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Concentric Stopwatch 360dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun ConcentricStopwatch360Preview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 1710L, // 00:00:01.71
            stopwatchStatus = StopwatchStatus.Running
        )
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
