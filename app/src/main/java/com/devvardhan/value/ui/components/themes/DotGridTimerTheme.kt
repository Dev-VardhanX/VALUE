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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 7: Dot Grid.
 * A 10x6 matrix of 60 dots representing the countdown duration.
 * Dots change one-by-one from white to dim gray as time elapses, with large digits below.
 */
@Composable
fun DotGridTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val totalDots = 60
    val columns = 10
    val rows = 6

    val activeDots = ThemeFractionCalculator.calculateDotGridActiveCount(remainingMillis, totalMillis, totalDots)
    val formattedTime = TimeUtils.formatHms(remainingMillis)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val isLandscape = availableWidth > availableHeight * 1.3f

        val gridWidth = minOf(availableWidth * 0.85f, 320f).dp
        val gridHeight = (gridWidth.value * 0.58f).dp
        val fontSize = if (isLandscape) {
            (availableHeight * 0.14f).coerceIn(24f, 44f).sp
        } else {
            (availableWidth * 0.11f).coerceIn(28f, 50f).sp
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-performance Canvas rendering of all 60 dots
            Canvas(
                modifier = Modifier.size(width = gridWidth, height = gridHeight)
            ) {
                val colSpacing = size.width / (columns - 1)
                val rowSpacing = size.height / (rows - 1)
                val dotRadius = (colSpacing * 0.22f).coerceIn(3f, 6.5f)

                var dotIndex = 0
                for (r in 0 until rows) {
                    for (c in 0 until columns) {
                        val cx = c * colSpacing
                        val cy = r * rowSpacing
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

            Spacer(modifier = Modifier.height(32.dp))

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
private fun DotGridTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            DotGridTimerTheme(
                remainingMillis = 150000L,
                totalMillis = 300000L,
                status = TimerStatus.Running
            )
        }
    }
}

