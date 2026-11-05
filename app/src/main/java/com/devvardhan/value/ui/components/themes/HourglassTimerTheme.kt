package com.devvardhan.value.ui.components.themes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 2: Hourglass.
 * Line-drawn glass container with sand draining from the top chamber to the bottom chamber.
 * A continuous falling stream runs through the neck while running, freezing/disappearing when paused.
 * Preallocates all Path objects outside the draw loop for allocation-free 60fps rendering.
 */
@Composable
fun HourglassTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val progress = ThemeFractionCalculator.calculateOverallProgress(remainingMillis, totalMillis)
    val isRunning = status == TimerStatus.Running && remainingMillis > 0L

    // Digits update only once per second via derived state
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

    // Preallocated Path objects to prevent per-frame garbage collection
    val glassPath = remember { Path() }
    val topSandPath = remember { Path() }
    val bottomSandPath = remember { Path() }

    // Stream animation
    val streamAnim = if (isRunning) {
        val infiniteTransition = rememberInfiniteTransition(label = "sandStream")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sandAlpha"
        )
        alpha
    } else {
        1.0f
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight.value
        val availableWidth = maxWidth.value
        val isLandscape = availableWidth > availableHeight * 1.3f

        val hourglassHeight = if (isLandscape) {
            (availableHeight * 0.55f).coerceIn(120f, 220f)
        } else {
            (availableHeight * 0.40f).coerceIn(140f, 240f)
        }
        val hourglassWidth = hourglassHeight * 0.62f
        val fontSize = if (isLandscape) {
            (availableHeight * 0.13f).coerceIn(24f, 44f).sp
        } else {
            (availableWidth * 0.10f).coerceIn(28f, 48f).sp
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier.size(width = hourglassWidth.dp, height = hourglassHeight.dp)
            ) {
                val w = size.width
                val h = size.height
                val neckWidth = w * 0.12f
                val neckHeight = h * 0.05f
                val bulbHeight = (h - neckHeight) / 2f
                val plateExtra = w * 0.08f
                val strokeThickness = 1.5.dp.toPx()

                // 1. Top and bottom plates
                drawLine(
                    color = SubtleGray,
                    start = Offset(-plateExtra, 0f),
                    end = Offset(w + plateExtra, 0f),
                    strokeWidth = strokeThickness * 1.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = SubtleGray,
                    start = Offset(-plateExtra, h),
                    end = Offset(w + plateExtra, h),
                    strokeWidth = strokeThickness * 1.5f,
                    cap = StrokeCap.Round
                )

                // 2. Glass outline (reuse glassPath without allocating new objects)
                glassPath.reset()
                glassPath.moveTo(0f, 0f)
                glassPath.cubicTo(
                    w * 0.05f, bulbHeight * 0.6f,
                    (w - neckWidth) / 2f - 2f, bulbHeight * 0.9f,
                    (w - neckWidth) / 2f, bulbHeight
                )
                glassPath.lineTo((w - neckWidth) / 2f, bulbHeight + neckHeight)
                glassPath.cubicTo(
                    (w - neckWidth) / 2f - 2f, bulbHeight + neckHeight + bulbHeight * 0.1f,
                    w * 0.05f, bulbHeight + neckHeight + bulbHeight * 0.4f,
                    0f, h
                )

                glassPath.moveTo(w, h)
                glassPath.cubicTo(
                    w - w * 0.05f, bulbHeight + neckHeight + bulbHeight * 0.4f,
                    (w + neckWidth) / 2f + 2f, bulbHeight + neckHeight + bulbHeight * 0.1f,
                    (w + neckWidth) / 2f, bulbHeight + neckHeight
                )
                glassPath.lineTo((w + neckWidth) / 2f, bulbHeight)
                glassPath.cubicTo(
                    (w + neckWidth) / 2f + 2f, bulbHeight * 0.9f,
                    w - w * 0.05f, bulbHeight * 0.6f,
                    w, 0f
                )
                drawPath(glassPath, color = BorderSubtle, style = Stroke(width = strokeThickness))

                // 3. Sand in top chamber
                if (progress > 0.005f) {
                    val sandHeightTop = bulbHeight * progress
                    val sandTopY = bulbHeight - sandHeightTop
                    val fractionAtY = 1f - (sandTopY / bulbHeight)
                    val halfWidthAtTop = (neckWidth / 2f) + (w / 2f - neckWidth / 2f) * fractionAtY
                    val leftX = (w / 2f) - halfWidthAtTop
                    val rightX = (w / 2f) + halfWidthAtTop

                    topSandPath.reset()
                    topSandPath.moveTo(leftX, sandTopY)
                    topSandPath.lineTo(rightX, sandTopY)
                    topSandPath.lineTo((w + neckWidth) / 2f, bulbHeight)
                    topSandPath.lineTo((w - neckWidth) / 2f, bulbHeight)
                    topSandPath.close()

                    drawPath(topSandPath, color = PureWhite)
                }

                // 4. Sand in bottom chamber
                val elapsedFraction = (1f - progress).coerceIn(0f, 1f)
                if (elapsedFraction > 0.005f) {
                    val sandHeightBottom = bulbHeight * elapsedFraction
                    val sandBottomTopY = h - sandHeightBottom
                    val fractionFromBottom = sandHeightBottom / bulbHeight
                    val halfWidthAtSandSurface = (w / 2f) - (w / 2f - neckWidth / 2f) * (1f - fractionFromBottom)
                    val leftX = (w / 2f) - halfWidthAtSandSurface.coerceAtLeast(neckWidth / 2f)
                    val rightX = (w / 2f) + halfWidthAtSandSurface.coerceAtLeast(neckWidth / 2f)

                    bottomSandPath.reset()
                    bottomSandPath.moveTo(0f, h)
                    bottomSandPath.lineTo(w, h)
                    bottomSandPath.lineTo(rightX, sandBottomTopY)
                    bottomSandPath.quadraticTo(w / 2f, sandBottomTopY - 4f, leftX, sandBottomTopY)
                    bottomSandPath.close()

                    drawPath(bottomSandPath, color = PureWhite)
                }

                // 5. Falling sand stream through the neck
                if (isRunning && progress > 0.01f) {
                    val sandBottomTopY = h - (bulbHeight * (1f - progress))
                    drawLine(
                        color = PureWhite.copy(alpha = streamAnim),
                        start = Offset(w / 2f, bulbHeight),
                        end = Offset(w / 2f, sandBottomTopY.coerceAtMost(h)),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Centered remaining digits (recomposes once per second)
            Text(
                text = formattedTime,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                color = PureWhite,
                letterSpacing = 2.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HourglassTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            HourglassTimerTheme(
                remainingMillis = 180000L,
                totalMillis = 360000L,
                status = TimerStatus.Running
            )
        }
    }
}
