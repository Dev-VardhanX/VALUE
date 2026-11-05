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
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.DarkGray
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 5: Flip Clock.
 * Classic split-flap cards for Hours, Minutes, and Seconds with a thin center divider line
 * and smooth 3D rotation flip animations when digits change.
 */
@Composable
fun FlipClockTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val totalSeconds = if (remainingMillis <= 0L) 0L else (remainingMillis + 999) / 1000
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
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value

        // Card width sizing: 6 digit cards + 2 colons + margins
        val cardWidth = minOf(
            (availableWidth - 80f) / 7.2f,
            availableHeight * 0.28f,
            58f
        ).coerceIn(34f, 62f).dp

        val cardHeight = cardWidth * 1.45f
        val fontSize = (cardHeight.value * 0.60f).coerceIn(24f, 54f).sp

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
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
            displayedDigit = digit
            flipAnimation.snapTo(0f)
            flipAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
    }

    val progress = flipAnimation.value
    val isFirstHalf = progress < 0.5f
    val rotationX = if (isFirstHalf) {
        progress * 180f
    } else {
        (progress - 1f) * 180f
    }
    val activeChar = if (isFirstHalf) previousDigit else displayedDigit

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .graphicsLayer {
                this.rotationX = rotationX
                cameraDistance = 16f * density
            }
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = activeChar.toString(),
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = PureWhite
        )

        // Split-flap center divider line
        HorizontalDivider(
            color = Color(0xFF000000),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FlipColon(height: Dp) {
    Box(
        modifier = Modifier
            .height(height)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SubtleGray)
            )
            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SubtleGray)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FlipClockTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            FlipClockTimerTheme(
                remainingMillis = 1845000L,
                totalMillis = 3600000L,
                status = TimerStatus.Running
            )
        }
    }
}

