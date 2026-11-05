package com.android.value.ui.components.themes

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
import androidx.compose.runtime.getValue
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
import com.android.value.core.ThemeFractionCalculator
import com.android.value.core.TimeUtils
import com.android.value.model.TimerStatus
import com.android.value.ui.theme.BorderSubtle
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray
import com.android.value.ui.theme.ValueTimerTheme

/**
 * Theme 2: Hourglass.
 * Line-drawn glass container with sand draining from the top chamber to the bottom chamber.
 * A continuous falling stream runs through the neck while running, freezing/disappearing when paused.
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
    val formattedTime = TimeUtils.formatHms(remainingMillis)

    // Subtle grain particle offset for stream motion while running
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
                modifier = Modifier
                    .size(width = hourglassWidth.dp, height = hourglassHeight.dp)
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

                // 2. Hourglass glass outline
                val glassPath = Path().apply {
                    // Left side: top to neck, neck to bottom
                    moveTo(0f, 0f)
                    cubicTo(
                        w * 0.05f, bulbHeight * 0.6f,
                        (w - neckWidth) / 2f - 2f, bulbHeight * 0.9f,
                        (w - neckWidth) / 2f, bulbHeight
                    )
                    lineTo((w - neckWidth) / 2f, bulbHeight + neckHeight)
                    cubicTo(
                        (w - neckWidth) / 2f - 2f, bulbHeight + neckHeight + bulbHeight * 0.1f,
                        w * 0.05f, bulbHeight + neckHeight + bulbHeight * 0.4f,
                        0f, h
                    )

                    // Right side: bottom to neck, neck to top
                    moveTo(w, h)
                    cubicTo(
                        w - w * 0.05f, bulbHeight + neckHeight + bulbHeight * 0.4f,
                        (w + neckWidth) / 2f + 2f, bulbHeight + neckHeight + bulbHeight * 0.1f,
                        (w + neckWidth) / 2f, bulbHeight + neckHeight
                    )
                    lineTo((w + neckWidth) / 2f, bulbHeight)
                    cubicTo(
                        (w + neckWidth) / 2f + 2f, bulbHeight * 0.9f,
                        w - w * 0.05f, bulbHeight * 0.6f,
                        w, 0f
                    )
                }
                drawPath(glassPath, color = BorderSubtle, style = Stroke(width = strokeThickness))

                // 3. Sand in top chamber (decreases from top down to neck as progress drops)
                if (progress > 0.005f) {
                    val sandHeightTop = bulbHeight * progress
                    val sandTopY = bulbHeight - sandHeightTop
                    val topSandPath = Path().apply {
                        val fractionAtY = 1f - (sandTopY / bulbHeight)
                        val halfWidthAtTop = (neckWidth / 2f) + (w / 2f - neckWidth / 2f) * fractionAtY
                        val leftX = (w / 2f) - halfWidthAtTop
                        val rightX = (w / 2f) + halfWidthAtTop

                        moveTo(leftX, sandTopY)
                        lineTo(rightX, sandTopY)
                        lineTo((w + neckWidth) / 2f, bulbHeight)
                        lineTo((w - neckWidth) / 2f, bulbHeight)
                        close()
                    }
                    drawPath(topSandPath, color = PureWhite)
                }

                // 4. Sand in bottom chamber (accumulates from bottom up)
                val elapsedFraction = (1f - progress).coerceIn(0f, 1f)
                if (elapsedFraction > 0.005f) {
                    val sandHeightBottom = bulbHeight * elapsedFraction
                    val sandBottomTopY = h - sandHeightBottom
                    val bottomSandPath = Path().apply {
                        val fractionFromBottom = sandHeightBottom / bulbHeight
                        val halfWidthAtSandSurface = (w / 2f) - (w / 2f - neckWidth / 2f) * (1f - fractionFromBottom)
                        val leftX = (w / 2f) - halfWidthAtSandSurface.coerceAtLeast(neckWidth / 2f)
                        val rightX = (w / 2f) + halfWidthAtSandSurface.coerceAtLeast(neckWidth / 2f)

                        moveTo(0f, h)
                        lineTo(w, h)
                        lineTo(rightX, sandBottomTopY)
                        // Slight mound curve at the center of the pile
                        quadraticTo(w / 2f, sandBottomTopY - 4f, leftX, sandBottomTopY)
                        close()
                    }
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

            // Centered remaining digits
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

