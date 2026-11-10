package com.devvardhan.value.ui.components.themes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 7: Dot Grid (Timer only).
 * A matrix of 60 dots representing remaining countdown fraction.
 *
 * Performance optimizations:
 * - Active dot count calculation moved into the Canvas draw phase.
 * - Digits derived once per second via derivedStateOf, avoiding per-frame recomposition.
 */
@Composable
fun DotGridTimerTheme(
    remainingMillisProvider: () -> Long = { 0L },
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val totalDots = 60
    val isPaused = status == TimerStatus.Paused

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
    val totalTimeFormatted = remember(totalMillis) {
        TimeUtils.formatHms(totalMillis)
    }
    val subText = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted"

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isCompact = maxHeight < 240.dp
        val isLandscape = when (layoutMode) {
            ThemeLayoutMode.AUTO -> AdaptiveLayoutHelper.isLandscape(maxWidth.value, maxHeight.value, 1.15f)
            ThemeLayoutMode.PORTRAIT -> false
            ThemeLayoutMode.LANDSCAPE -> true
        }

        val matrix = remember(maxWidth.value, maxHeight.value, layoutMode) {
            AdaptiveLayoutHelper.calculateDotGridMatrix(maxWidth.value, maxHeight.value, layoutMode)
        }

        val gridCanvas = @Composable {
            Canvas(
                modifier = Modifier.size(width = matrix.gridWidth.dp, height = matrix.gridHeight.dp)
            ) {
                // Optimization: activeDots evaluated strictly inside draw phase
                val rem = remainingMillisProvider()
                val activeDots = ThemeFractionCalculator.calculateDotGridActiveCount(rem, totalMillis, totalDots)

                val padX = 8.dp.toPx()
                val padY = 8.dp.toPx()
                val usableW = size.width - 2 * padX
                val usableH = size.height - 2 * padY
                val colSpacing = usableW / (matrix.columns - 1)
                val rowSpacing = usableH / (matrix.rows - 1)
                val dotRadius = (colSpacing * 0.22f).coerceIn(3.5f, 9f)

                var dotIndex = 0
                for (r in 0 until matrix.rows) {
                    for (c in 0 until matrix.columns) {
                        val cx = padX + c * colSpacing
                        val cy = padY + r * rowSpacing
                        val isActive = dotIndex < activeDots
                        val dotColor = if (isActive) PureWhite else Color(0xFF222222)
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                        dotIndex++
                    }
                }
            }
        }

        val textMeasurer = rememberTextMeasurer()
        val fontScale = LocalDensity.current.fontScale

        val digitsContent = @Composable {
            val baseFontSize = if (fontScale > 1.0f) {
                (matrix.fontSize / fontScale).sp
            } else {
                matrix.fontSize.sp
            }

            // Defensively ensure digits never exceed available width
            val style = remember(baseFontSize) {
                TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    fontSize = baseFontSize,
                    letterSpacing = 1.sp,
                    fontFeatureSettings = "tnum"
                )
            }
            val measuredWidthDp = with(LocalDensity.current) {
                textMeasurer.measure("00:00:00", style).size.width.toDp()
            }
            val maxAvailableDigitsWidthDp = (maxWidth - 24.dp).coerceAtLeast(60.dp)
            val fittedFontSize = if (measuredWidthDp > maxAvailableDigitsWidthDp) {
                val scale = (maxAvailableDigitsWidthDp.value / measuredWidthDp.value) * 0.95f
                (baseFontSize.value * scale).sp
            } else {
                baseFontSize
            }

            val subFontSize = if (fontScale > 1.0f) {
                ((matrix.fontSize * 0.22f).coerceIn(9f, 13f) / fontScale).sp
            } else {
                (matrix.fontSize * 0.22f).coerceIn(9f, 13f).sp
            }

            val digitsSpacerHeight = if (isCompact) 2.dp else 4.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = fittedFontSize,
                        letterSpacing = 1.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(digitsSpacerHeight))

                Text(
                    text = subText,
                    fontSize = subFontSize,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 1.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        val verticalPadding = if (isCompact) 4.dp else if (isLandscape) 8.dp else 12.dp
        val spacerHeight = if (isCompact) 6.dp else if (isLandscape) 14.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = verticalPadding, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            gridCanvas()
            Spacer(modifier = Modifier.height(spacerHeight))
            digitsContent()
        }
    }
}

/**
 * Backward-compatible overload accepting a direct Long value.
 */
@Composable
fun DotGridTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = DotGridTimerTheme(
    remainingMillisProvider = { remainingMillis },
    totalMillis = totalMillis,
    status = status,
    modifier = modifier,
    layoutMode = layoutMode
)

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "DotGrid Portrait 50%", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun DotGridTimerThemePortraitPreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 60000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "DotGrid Paused", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun DotGridTimerThemePausedPreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 45000L,
            totalMillis = 120000L,
            status = TimerStatus.Paused
        )
    }
}

@Preview(name = "DotGrid Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun DotGridTimerThemeLandscapePreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 80000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "DotGrid Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun DotGridTimerThemeTabletPreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 90000L,
            totalMillis = 120000L,
            status = TimerStatus.Running
        )
    }
}
