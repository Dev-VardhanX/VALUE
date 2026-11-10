package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
 *
 * Performance optimizations:
 * - Ring fraction and sweep calculations moved strictly inside the Canvas draw phase.
 * - Digits derived once per second via derivedStateOf, eliminating 60fps recomposition.
 * - Centiseconds rendering handled in draw phase by CentisecondsSlot.
 */
@Composable
fun ConcentricRingsTimerTheme(
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

    // Optimization: Digits derived once per second to prevent unnecessary recomposition
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
        val rawInnerRadius = geometry.innerDiameter / 2f
        val clearInnerRadius = (rawInnerRadius - (geometry.strokeWidth / 2f)).coerceAtLeast(10f)

        val showSubText = isPaused || (!isStopwatch && clearInnerRadius >= 52f)
        val hasLabel = showSubText

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
                // Optimization: Fraction calculations executed in the draw phase, avoiding recomposition
                val ms = if (isStopwatch) stopwatchElapsedMillisProvider() else remainingMillisProvider()
                val hrFraction = if (isStopwatch) {
                    StopwatchFractionCalculator.calculateHoursFraction(ms)
                } else {
                    ThemeFractionCalculator.calculateHoursFraction(ms, totalMillis)
                }
                val minFraction = if (isStopwatch) {
                    StopwatchFractionCalculator.calculateMinutesFraction(ms)
                } else {
                    ThemeFractionCalculator.calculateMinutesFraction(ms)
                }
                val secFraction = if (isStopwatch) {
                    StopwatchFractionCalculator.calculateSecondsFraction(ms)
                } else {
                    ThemeFractionCalculator.calculateSecondsFraction(ms)
                }

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

                if (showSubText) {
                    val subText = if (isPaused) "PAUSED" else "TOTAL ${TimeUtils.formatHms(totalMillis)}"
                    Text(
                        text = subText,
                        fontSize = (fontSize.value * 0.22f).coerceIn(8f, 13f).sp,
                        fontWeight = FontWeight.Medium,
                        color = SubtleGray,
                        letterSpacing = 1.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(top = (fontSize.value * 0.08f).dp)
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible overload accepting direct Long values.
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
) = ConcentricRingsTimerTheme(
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

@Preview(name = "Concentric Rings Paused", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun ConcentricRingsPausedPreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            remainingMillis = 45000L,
            totalMillis = 120000L,
            status = TimerStatus.Paused
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
