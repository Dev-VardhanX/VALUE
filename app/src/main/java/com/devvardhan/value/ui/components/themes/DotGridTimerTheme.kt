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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 7: Dot Grid.
 * A matrix of 60 dots.
 * For Stopwatch: one more dot lights up each second, resets every minute, with minutes in digits.
 */
@Composable
fun DotGridTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier
) {
    val totalDots = 60
    val activeDots = if (isStopwatch) {
        StopwatchFractionCalculator.calculateActiveDots(stopwatchElapsedMillis)
    } else {
        ThemeFractionCalculator.calculateDotGridActiveCount(remainingMillis, totalMillis, totalDots)
    }

    val formattedTime = if (isStopwatch) {
        TimeUtils.formatStopwatchAdaptive(stopwatchElapsedMillis)
    } else {
        TimeUtils.formatHms(remainingMillis)
    }

    val isPaused = if (isStopwatch) {
        stopwatchStatus == StopwatchStatus.Paused
    } else {
        status == TimerStatus.Paused
    }

    val subText = if (isStopwatch) {
        if (isPaused) "PAUSED" else "STOPWATCH"
    } else {
        val totalTimeFormatted = TimeUtils.formatHms(totalMillis)
        if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted"
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val matrix = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateDotGridMatrix(maxWidth.value, maxHeight.value)
        }

        val gridCanvas = @Composable {
            Canvas(
                modifier = Modifier.size(width = matrix.gridWidth.dp, height = matrix.gridHeight.dp)
            ) {
                val colSpacing = size.width / (matrix.columns - 1)
                val rowSpacing = size.height / (matrix.rows - 1)
                val dotRadius = (colSpacing * 0.22f).coerceIn(3.5f, 10f)

                var dotIndex = 0
                for (r in 0 until matrix.rows) {
                    for (c in 0 until matrix.columns) {
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
        }

        val digitsContent = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    fontSize = matrix.fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subText,
                    fontSize = (matrix.fontSize * 0.26f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (matrix.isRowLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                gridCanvas()
                digitsContent()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                gridCanvas()
                Spacer(modifier = Modifier.height(28.dp))
                digitsContent()
            }
        }
    }
}

@Preview(name = "DotGrid Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun DotGridStopwatchPortraitPreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 35000L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "DotGrid Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun DotGridStopwatchLandscapePreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 95200L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}
