package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.Canvas
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
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.InactiveTrack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 4: Concentric Rings.
 * Three thin concentric rings for Hours (outer), Minutes (middle), and Seconds (inner).
 * Adapts with square-fit sizing in portrait, landscape, and tablet, guaranteeing digits
 * always fit inside the inner ring without overlap.
 */
@Composable
fun ConcentricRingsTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val secFraction = ThemeFractionCalculator.calculateSecondsFraction(remainingMillis)
    val minFraction = ThemeFractionCalculator.calculateMinutesFraction(remainingMillis)
    val hrFraction = ThemeFractionCalculator.calculateHoursFraction(remainingMillis, totalMillis)

    val displayedSeconds by remember(remainingMillis) {
        derivedStateOf {
            if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
        }
    }
    val formattedTime by remember(displayedSeconds) {
        derivedStateOf {
            TimeUtils.formatHmsFromSeconds(displayedSeconds)
        }
    }
    val isPaused = status == TimerStatus.Paused

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
        val fontSize = geometry.maxFontSize.sp

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

            // Centered remaining time (positioned inside inner ring)
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
                    letterSpacing = 1.5.sp,
                    maxLines = 1,
                    softWrap = false
                )

                if (isPaused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAUSED",
                        fontSize = (fontSize.value * 0.30f).coerceIn(9f, 14f).sp,
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

@Preview(name = "Concentric Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun ConcentricRingsTimerThemePortraitPreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            remainingMillis = 5420000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Concentric Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun ConcentricRingsTimerThemeLandscapePreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            remainingMillis = 5420000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "Concentric Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun ConcentricRingsTimerThemeTabletPreview() {
    ValueTimerTheme {
        ConcentricRingsTimerTheme(
            remainingMillis = 5420000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}
