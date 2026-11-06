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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
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
 * For Stopwatch: shows flap animation counting up with baseline-aligned centiseconds.
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
        val showHours = !isStopwatch || hours > 0

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (showHours) {
                if (hours >= 10 || !isStopwatch) {
                    FlipDigitCard(digit = hStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
                }
                FlipDigitCard(digit = hStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)
                FlipColon(height = cardHeight)
            }

            // Minutes
            FlipDigitCard(digit = mStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
            FlipDigitCard(digit = mStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

            FlipColon(height = cardHeight)

            // Seconds
            FlipDigitCard(digit = sStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
            FlipDigitCard(digit = sStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)
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

    val cardShape = RoundedCornerShape((width.value * 0.14f).coerceIn(6f, 14f).dp)

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(cardShape)
            .background(Color(0xFF0F0F0F))
            .border(1.dp, Color(0xFF262626), cardShape)
            .graphicsLayer {
                cameraDistance = 16f * density
                rotationX = -flipAnimation.value * 180f
            },
        contentAlignment = Alignment.Center
    ) {
        val charToShow = if (flipAnimation.value < 0.5f) previousDigit else displayedDigit

        Text(
            text = charToShow.toString(),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = fontSize,
                fontFeatureSettings = "tnum"
            ),
            color = PureWhite,
            modifier = Modifier.graphicsLayer {
                if (flipAnimation.value >= 0.5f) {
                    rotationX = 180f
                }
            }
        )

        // Center split seam
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFF050505)
        )
    }
}

@Composable
private fun FlipColon(height: Dp) {
    Column(
        modifier = Modifier
            .height(height)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
    }
}

@Preview(name = "Flip Clock Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun FlipClockStopwatchPortraitPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 145230L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "Flip Clock Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun FlipClockStopwatchLandscapePreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 4125890L,
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}
