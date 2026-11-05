package com.android.value.ui.components.themes

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
import com.android.value.core.ThemeFractionCalculator
import com.android.value.core.TimeUtils
import com.android.value.model.TimerStatus
import com.android.value.ui.theme.InactiveTrack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray
import com.android.value.ui.theme.ValueTimerTheme

/**
 * Theme 4: Concentric Rings.
 * Three thin concentric rings for Hours (outer), Minutes (middle), and Seconds (inner).
 * Each ring sweeps smoothly down as its unit counts down, with HH:MM:SS centered inside.
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

    val formattedTime = TimeUtils.formatHms(remainingMillis)
    val isPaused = status == TimerStatus.Paused

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val diameter = minOf(maxWidth * 0.88f, maxHeight * 0.72f, 320.dp)
        val strokeWidth = (diameter.value * 0.015f).coerceIn(2.5f, 4.5f).dp
        val ringGap = (diameter.value * 0.055f).coerceIn(12f, 18f).dp
        val fontSize = (diameter.value * 0.125f).coerceIn(22f, 42f).sp

        Box(
            modifier = Modifier.size(diameter),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokePx = strokeWidth.toPx()
                val gapPx = ringGap.toPx()

                val outerRadius = (size.width / 2f) - strokePx
                val middleRadius = outerRadius - gapPx
                val innerRadius = middleRadius - gapPx

                val rings = listOf(
                    Triple(outerRadius, hrFraction, "Hours"),
                    Triple(middleRadius, minFraction, "Minutes"),
                    Triple(innerRadius, secFraction, "Seconds")
                )

                for ((radius, fraction, _) in rings) {
                    if (radius <= 0f) continue
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val ringSize = Size(radius * 2f, radius * 2f)

                    // Inactive background ring
                    drawArc(
                        color = InactiveTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = ringSize,
                        style = Stroke(width = strokePx)
                    )

                    // Active sweep arc
                    val sweep = fraction.coerceIn(0f, 1f) * 360f
                    if (sweep > 0f) {
                        drawArc(
                            color = PureWhite,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Centered remaining time
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
                    letterSpacing = 1.5.sp
                )

                if (isPaused) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PAUSED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubtleGray,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ConcentricRingsTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            ConcentricRingsTimerTheme(
                remainingMillis = 5420000L,
                totalMillis = 7200000L,
                status = TimerStatus.Running
            )
        }
    }
}

