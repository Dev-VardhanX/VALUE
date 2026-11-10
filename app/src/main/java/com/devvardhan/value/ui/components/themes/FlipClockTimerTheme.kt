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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.CellSizeHelper
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.components.CentisecondsSlot
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 5: Flip Clock.
 * Classic split-flap cards for Hours, Minutes, and Seconds with a center divider line.
 * Always shows all six cards (HH:MM:SS) in both Timer and Stopwatch modes.
 * Tall flip cards with 1 : 1.4 aspect ratio.
 * In Stopwatch mode: centiseconds sit on the same baseline directly to the right of the seconds cards.
 * Capped at 99:59:59 (holds there, no overflow).
 *
 * Performance optimizations:
 * - Time strings derived once per second via derivedStateOf, avoiding card recompositions on every millisecond.
 * - Centiseconds rendered directly in the draw phase via CentisecondsSlot.
 */
@Composable
fun FlipClockTimerTheme(
    remainingMillisProvider: () -> Long = { 0L },
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillisProvider: () -> Long = { 0L },
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    // Optimization: Digits derived once per second to prevent unnecessary recomposition on every millisecond
    val timeStrings by remember(isStopwatch, if (isStopwatch) stopwatchElapsedMillisProvider else remainingMillisProvider) {
        derivedStateOf {
            val raw = if (isStopwatch) {
                maxOf(0L, stopwatchElapsedMillisProvider()) / 1000L
            } else {
                val rem = remainingMillisProvider()
                if (rem <= 0L) 0L else (rem + 999L) / 1000L
            }
            val maxSeconds = 99 * 3600L + 59 * 60L + 59L
            val displayedSeconds = raw.coerceAtMost(maxSeconds)
            val hours = (displayedSeconds / 3600).toInt()
            val minutes = ((displayedSeconds % 3600) / 60).toInt()
            val seconds = (displayedSeconds % 60).toInt()
            Triple(
                TimeUtils.formatTwoDigits(hours),
                TimeUtils.formatTwoDigits(minutes),
                TimeUtils.formatTwoDigits(seconds)
            )
        }
    }
    val (hStr, mStr, sStr) = timeStrings

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value, isStopwatch, layoutMode) {
            CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = maxWidth.value,
                availableHeight = maxHeight.value,
                cardCount = 6,
                isStopwatch = isStopwatch,
                layoutMode = layoutMode
            )
        }

        val cardWidth = dims.cardWidth.dp
        val cardHeight = dims.cardHeight.dp
        val fontSize = dims.fontSize.sp
        val spacing = dims.gap.dp

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            // Hours
            FlipDigitCard(digit = hStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
            FlipDigitCard(digit = hStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

            FlipColon(height = cardHeight, width = dims.colonWidth.dp)

            // Minutes
            FlipDigitCard(digit = mStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
            FlipDigitCard(digit = mStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

            FlipColon(height = cardHeight, width = dims.colonWidth.dp)

            // Seconds
            FlipDigitCard(digit = sStr[0], width = cardWidth, height = cardHeight, fontSize = fontSize)
            FlipDigitCard(digit = sStr[1], width = cardWidth, height = cardHeight, fontSize = fontSize)

            if (isStopwatch) {
                CentisecondsSlot(
                    elapsedMillisProvider = stopwatchElapsedMillisProvider,
                    fontSize = (dims.fontSize * 0.40f).sp,
                    fixedWidthDp = dims.centisSlotWidth.dp,
                    modifier = Modifier.padding(bottom = (dims.cardHeight * 0.08f).dp)
                )
            }
        }
    }
}

/**
 * Backward-compatible overload accepting direct Long values.
 */
@Composable
fun FlipClockTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = FlipClockTimerTheme(
    remainingMillisProvider = { remainingMillis },
    totalMillis = totalMillis,
    status = status,
    isStopwatch = isStopwatch,
    stopwatchElapsedMillisProvider = { stopwatchElapsedMillis },
    stopwatchStatus = stopwatchStatus,
    modifier = modifier,
    layoutMode = layoutMode
)

@Composable
private fun FlipDigitCard(
    digit: Char,
    width: Dp,
    height: Dp,
    fontSize: TextUnit
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

    val cardShape = RoundedCornerShape((width.value * 0.15f).coerceIn(4f, 10f).dp)

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .background(Color(0xFF0F0F0F), cardShape)
            .border(0.5.dp, Color(0xFF262626), cardShape)
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
                fontFeatureSettings = "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = fontSize
            ),
            color = PureWhite,
            modifier = Modifier.graphicsLayer {
                if (flipAnimation.value >= 0.5f) {
                    rotationX = 180f
                }
            }
        )

        // Split-flap center groove
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFF050505)
        )
    }
}

@Composable
private fun FlipColon(height: Dp, width: Dp = 8.dp) {
    Column(
        modifier = Modifier
            .height(height)
            .width(width),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size((width.value * 0.5f).coerceIn(2.5f, 5f).dp)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height((height.value * 0.12f).coerceIn(4f, 10f).dp))
        Box(
            modifier = Modifier
                .size((width.value * 0.5f).coerceIn(2.5f, 5f).dp)
                .background(SubtleGray, RoundedCornerShape(2.dp))
        )
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "FlipClock Small Preview 120dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 120, heightDp = 80)
@Composable
private fun FlipClock120dpPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "FlipClock Card Preview 200dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 200, heightDp = 133)
@Composable
private fun FlipClock200dpPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "FlipClock Phone Portrait 360dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 240)
@Composable
private fun FlipClock360dpPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "FlipClock Stopwatch 360dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun FlipClockStopwatch360Preview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 1710L, // 00:00:01.71
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

@Preview(name = "FlipClock Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun FlipClockTimerThemePortraitPreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "FlipClock Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun FlipClockTimerThemeLandscapePreview() {
    ValueTimerTheme {
        FlipClockTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}
