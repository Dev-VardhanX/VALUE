package com.android.value.ui.components.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.core.ThemeFractionCalculator
import com.android.value.core.TimeUtils
import com.android.value.model.TimerStatus
import com.android.value.ui.theme.BorderSubtle
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray
import com.android.value.ui.theme.ValueTimerTheme

/**
 * Theme 3: Three Box.
 * Three rounded square boxes labelled H, M, S that drain like liquid fill levels.
 * Text dynamically inverts (white on black, black on white) where the fill level overlaps it.
 */
@Composable
fun ThreeBoxTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val totalSeconds = if (remainingMillis <= 0L) 0L else (remainingMillis + 999) / 1000
    val hours = (totalSeconds / 3600).toInt()
    val minutes = ((totalSeconds % 3600) / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()

    val secFraction = ThemeFractionCalculator.calculateSecondsFraction(remainingMillis)
    val minFraction = ThemeFractionCalculator.calculateMinutesFraction(remainingMillis)
    val hrFraction = ThemeFractionCalculator.calculateHoursFraction(remainingMillis, totalMillis)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value

        // Fit 3 boxes horizontally with spacing
        val boxSize = minOf(
            (availableWidth - 64) / 3f,
            availableHeight * 0.45f,
            120f
        ).coerceIn(70f, 130f).dp

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            DrainingBox(
                label = "H",
                value = TimeUtils.formatTwoDigits(hours),
                fraction = hrFraction,
                boxSize = boxSize
            )

            DrainingBox(
                label = "M",
                value = TimeUtils.formatTwoDigits(minutes),
                fraction = minFraction,
                boxSize = boxSize
            )

            DrainingBox(
                label = "S",
                value = TimeUtils.formatTwoDigits(seconds),
                fraction = secFraction,
                boxSize = boxSize
            )
        }
    }
}

@Composable
private fun DrainingBox(
    label: String,
    value: String,
    fraction: Float,
    boxSize: Dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 14.dp
    val shape = RoundedCornerShape(cornerRadius)
    val fontSize = (boxSize.value * 0.40f).coerceIn(24f, 48f).sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SubtleGray,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(shape)
                .background(PureBlack)
                .border(1.dp, BorderSubtle, shape)
        ) {
            // Layer 1: Base layer - White text on PureBlack background
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite
                )
            }

            // Layer 2: White fill from bottom with clipping & black inverted text
            val clampedFraction = fraction.coerceIn(0f, 1f)
            if (clampedFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val fillTop = size.height * (1f - clampedFraction)
                            clipRect(
                                left = 0f,
                                top = fillTop,
                                right = size.width,
                                bottom = size.height
                            ) {
                                // Draw solid white fill in the clipped region
                                drawRect(
                                    color = PureWhite,
                                    topLeft = Offset(0f, fillTop),
                                    size = Size(size.width, size.height - fillTop)
                                )
                                // Draw content (black text) clipped to this fill
                                this@drawWithContent.drawContent()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = PureBlack
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ThreeBoxTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            ThreeBoxTimerTheme(
                remainingMillis = 3725000L,
                totalMillis = 7200000L,
                status = TimerStatus.Running
            )
        }
    }
}

