package com.devvardhan.value.ui.components.themes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import kotlin.math.PI
import kotlin.math.sin

/**
 * Theme 6: Liquid Fill.
 * A rounded vessel container whose white liquid level lowers as time counts down,
 * with a subtle sinusoidal wave on the surface.
 * Digits dynamically invert colors on overlap with the liquid.
 * Preallocates the wave Path object outside the draw loop for 60fps allocation-free rendering.
 */
@Composable
fun LiquidFillTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val progress = ThemeFractionCalculator.calculateOverallProgress(remainingMillis, totalMillis)
    val isRunning = status == TimerStatus.Running && remainingMillis > 0L

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

    // Preallocated Path to prevent object creation per draw pass
    val liquidPath = remember { Path() }

    // Wave phase animation (active while running, frozen when paused)
    val wavePhase = if (isRunning) {
        val transition = rememberInfiniteTransition(label = "waveTransition")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavePhase"
        )
        phase
    } else {
        0f
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = minOf(maxWidth * 0.85f, 320.dp)
        val containerHeight = minOf(maxHeight * 0.55f, 320.dp, containerWidth * 1.15f)
        val cornerRadius = 24.dp
        val shape = RoundedCornerShape(cornerRadius)
        val fontSize = (containerWidth.value * 0.14f).coerceIn(26f, 48f).sp

        Box(
            modifier = Modifier
                .size(width = containerWidth, height = containerHeight)
                .clip(shape)
                .background(PureBlack)
                .border(1.5.dp, BorderSubtle, shape)
        ) {
            // Layer 1: Base layer - White digits on PureBlack background
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedTime,
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 2.sp
                )
            }

            // Layer 2: White liquid fill with animated sinusoidal wave & inverted black digits
            if (progress > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val w = size.width
                            val h = size.height
                            val baseSurfaceY = h * (1f - progress)
                            val amplitude = if (progress in 0.02f..0.98f) 7.dp.toPx() else 0f
                            val waveLength = w * 0.8f

                            liquidPath.reset()
                            liquidPath.moveTo(0f, h)
                            liquidPath.lineTo(0f, baseSurfaceY)

                            // Sample wave surface across width
                            val stepPx = 6f
                            var currentX = 0f
                            while (currentX <= w) {
                                val waveY = baseSurfaceY + (sin((currentX / waveLength) * 2 * PI.toFloat() + wavePhase) * amplitude)
                                liquidPath.lineTo(currentX, waveY)
                                currentX += stepPx
                            }

                            liquidPath.lineTo(w, h)
                            liquidPath.close()

                            clipPath(liquidPath) {
                                drawPath(liquidPath, color = PureWhite)
                                this@drawWithContent.drawContent()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = PureBlack,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LiquidFillTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            LiquidFillTimerTheme(
                remainingMillis = 145000L,
                totalMillis = 300000L,
                status = TimerStatus.Running
            )
        }
    }
}
