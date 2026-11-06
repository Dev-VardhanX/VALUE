package com.devvardhan.value.ui.components.themes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.DarkGray
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 5: Flip Clock.
 * Classic split-flap cards for Hours, Minutes, and Seconds with a center divider line
 * and smooth 3D rotation flip animations when digits change.
 * For Stopwatch: shows flap animation counting up with centiseconds as a static line.
 */
@Composable
fun FlipClockTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier
) {
    val totalSeconds = if (isStopwatch) {
        maxOf(0L, stopwatchElapsedMillis) / 1000L
    } else {
        if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
    }

    val hours = (totalSeconds / 3600).toInt()
    val minutes = ((totalSeconds % 3600) / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()

    val hStr = TimeUtils.formatTwoDigits(hours)
    val mStr = TimeUtils.formatTwoDigits(minutes)
    val sStr = TimeUtils.formatTwoDigits(seconds)

    val centisStr = if (isStopwatch) {
        String.format(".%02d", (stopwatchElapsedMillis % 1000L) / 10L)
    } else null

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateFlipClockDimensions(maxWidth.value, maxHeight.value)
        }

        val cardWidth = dims.cardWidth.dp
        val cardHeight = dims.cardHeight.dp
        val fontSize = dims.fontSize.sp
        val spacing = (cardWidth.value * 0.08f).coerceIn(3f, 8f).dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Hours
                FlipDigitCard(digit = hStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
                FlipDigitCard(digit = hStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

                FlipColon(height = cardHeight)

                // Minutes
                FlipDigitCard(digit = mStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
                FlipDigitCard(digit = mStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

                FlipColon(height = cardHeight)

                // Seconds
                FlipDigitCard(digit = sStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
                FlipDigitCard(digit = sStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)
            }

            if (centisStr != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = centisStr,
                    fontSize = (fontSize.value * 0.45f).coerceIn(14f, 32f).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = SubtleGray,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun FlipDigitCard(
    digit: Char,
    width: Dp,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    var previousDigit by remember { mutableStateOf(digit) }
    var displayedDigit by remember { mutableStateOf(digit) }
    val flipAnimation = remember { Animatable(0f) }

    LaunchedEffect(digit) {
        if (digit != displayedDigit) {
            previousDigit = displayedDigit
            flipAnimation.snapTo(0f)
            flipAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
            displayedDigit = digit
            flipAnimation.snapTo(0f)
        }
    }

    val cardShape = RoundedCornerShape((width.value * 0.14f).coerceIn(6f, 16f).dp)
    val rotation = flipAnimation.value * 180f
    val currentShowing = if (rotation < 90f) previousDigit else digit

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .graphicsLayer {
                rotationX = if (rotation < 90f) -rotation else (180f - rotation)
                cameraDistance = 16f * density
            }
            .clip(cardShape)
            .background(Color(0xFF0F0F0F))
            .border(1.dp, Color(0xFF262626), cardShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = currentShowing.toString(),
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = PureWhite,
            maxLines = 1,
            softWrap = false
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 1.dp,
            color = Color(0xFF000000)
        )
    }
}

@Composable
private fun FlipColon(height: Dp) {
    val colonSize = (height.value * 0.055f).coerceIn(4f, 10f).dp
    Column(
        modifier = Modifier
            .height(height)
            .padding(horizontal = (colonSize.value * 0.5f).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(colonSize)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .height((height.value * 0.18f).coerceIn(12f, 28f).dp)
        )
        Box(
            modifier = Modifier
                .size(colonSize)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
    }
}

@Preview(name = "FlipClock Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun FlipClockStopwatchPortraitPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 59820L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "FlipClock Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun FlipClockStopwatchLandscapePreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 3600000L + 125000L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}
