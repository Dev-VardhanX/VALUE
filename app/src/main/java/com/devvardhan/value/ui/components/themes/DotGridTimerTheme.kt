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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
 */
@Composable
fun DotGridTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    val totalDots = 60
    val activeDots = ThemeFractionCalculator.calculateDotGridActiveCount(remainingMillis, totalMillis, totalDots)
    val formattedTime = TimeUtils.formatHms(remainingMillis)
    val isPaused = status == TimerStatus.Paused
    val totalTimeFormatted = TimeUtils.formatHms(totalMillis)
    val subText = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted"

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val matrix = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateDotGridMatrix(maxWidth.value, maxHeight.value)
        }

        val isRowLayout = when (layoutMode) {
            ThemeLayoutMode.AUTO -> matrix.isRowLayout
            ThemeLayoutMode.PORTRAIT -> false
            ThemeLayoutMode.LANDSCAPE -> true
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
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        fontSize = matrix.fontSize.sp,
                        letterSpacing = 1.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = PureWhite,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subText,
                    fontSize = (matrix.fontSize * 0.22f).coerceIn(9f, 13f).sp,
                    color = SubtleGray,
                    letterSpacing = 1.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        if (isRowLayout) {
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
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                gridCanvas()
                Spacer(modifier = Modifier.height(20.dp))
                digitsContent()
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "DotGrid Timer Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun DotGridTimerPortraitPreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 1800000L,
            totalMillis = 3600000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "DotGrid Timer Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun DotGridTimerLandscapePreview() {
    ValueTimerTheme {
        DotGridTimerTheme(
            remainingMillis = 1800000L,
            totalMillis = 3600000L,
            status = TimerStatus.Running
        )
    }
}
