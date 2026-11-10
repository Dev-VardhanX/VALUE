package com.devvardhan.value.ui.components.themes

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.AdaptiveLayoutHelper
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
 * Wide rounded container in landscape, tall in portrait, whose liquid level exactly equals remaining/total
 * from the very first frame without delays or artificial intro transitions.
 * Features an animated sinusoidal wave driven deterministically from elapsed remaining time.
 *
 * Performance optimizations:
 * - Liquid progress, wave phase, and wave clipping are calculated inside drawWithContent (draw phase).
 * - Digits are derived once per second via derivedStateOf, avoiding per-frame recomposition.
 */
@Composable
fun LiquidFillTimerTheme(
    remainingMillisProvider: () -> Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val isRunning = status == TimerStatus.Running

    // Optimization: Digits derived once per second to prevent unnecessary recomposition
    val displayedSeconds by remember(remainingMillisProvider) {
        derivedStateOf {
            val rem = remainingMillisProvider()
            if (rem <= 0L) 0L else (rem + 999L) / 1000L
        }
    }
    val formattedTime by remember(displayedSeconds) {
        derivedStateOf {
            TimeUtils.formatHmsFromSeconds(displayedSeconds)
        }
    }

    // Preallocated Path to prevent object creation per draw pass
    val liquidPath = remember { Path() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value, layoutMode) {
            AdaptiveLayoutHelper.calculateLiquidFillDimensions(maxWidth.value, maxHeight.value, layoutMode)
        }

        val containerWidth = dims.containerWidth.dp
        val containerHeight = dims.containerHeight.dp
        val cornerRadius = (containerHeight.value * 0.12f).coerceIn(16f, 32f).dp
        val shape = RoundedCornerShape(cornerRadius)
        val fontSize = dims.fontSize.sp

        val textStyle = TextStyle(
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp,
            fontFeatureSettings = "tnum"
        )

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
                    color = PureWhite,
                    maxLines = 1,
                    softWrap = false,
                    style = textStyle
                )
            }

            // Layer 2: White liquid fill with animated sinusoidal wave & inverted black digits
            // Optimization: progress and wave path evaluated in drawWithContent without recomposition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val rem = remainingMillisProvider()
                        val progress = ThemeFractionCalculator.calculateOverallProgress(rem, totalMillis)
                        if (progress > 0.0005f) {
                            val w = size.width
                            val h = size.height
                            val baseSurfaceY = h * (1f - progress)
                            val amplitude = if (isRunning && progress in 0.02f..0.98f) 7.dp.toPx() else 0f
                            val waveLength = w * 0.65f
                            val elapsedSeconds = (totalMillis - rem).coerceAtLeast(0L) / 1000f
                            val wavePhase = if (isRunning) {
                                (elapsedSeconds * (2f * PI.toFloat() / 2.4f)) % (2f * PI.toFloat())
                            } else {
                                0f
                            }

                            liquidPath.reset()
                            liquidPath.moveTo(0f, h)
                            liquidPath.lineTo(0f, baseSurfaceY)

                            // Sample wave surface across container width
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
                                drawRect(color = PureWhite)
                                this@drawWithContent.drawContent()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedTime,
                    color = PureBlack,
                    maxLines = 1,
                    softWrap = false,
                    style = textStyle
                )
            }
        }
    }
}

/**
 * Backward-compatible overload accepting a direct Long value.
 */
@Composable
fun LiquidFillTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = LiquidFillTimerTheme(
    remainingMillisProvider = { remainingMillis },
    totalMillis = totalMillis,
    status = status,
    modifier = modifier,
    layoutMode = layoutMode
)

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "LiquidFill Portrait 50%", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun LiquidFillTimerThemePortraitPreview() {
    ValueTimerTheme {
        LiquidFillTimerTheme(
            remainingMillis = 60000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "LiquidFill Paused", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun LiquidFillTimerThemePausedPreview() {
    ValueTimerTheme {
        LiquidFillTimerTheme(
            remainingMillis = 45000L,
            totalMillis = 120000L,
            status = TimerStatus.Paused
        )
    }
}

@Preview(name = "LiquidFill Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun LiquidFillTimerThemeLandscapePreview() {
    ValueTimerTheme {
        LiquidFillTimerTheme(
            remainingMillis = 80000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "LiquidFill Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun LiquidFillTimerThemeTabletPreview() {
    ValueTimerTheme {
        LiquidFillTimerTheme(
            remainingMillis = 90000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}
