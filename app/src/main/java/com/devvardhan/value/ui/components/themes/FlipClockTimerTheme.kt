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
import com.devvardhan.value.core.ScoreboardDigitsHelper
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
    // Optimization: Digits derived from a single snapshot via ScoreboardDigitsHelper
    val digits by remember(isStopwatch, if (isStopwatch) stopwatchElapsedMillisProvider else remainingMillisProvider) {
        derivedStateOf {
            if (isStopwatch) {
                ScoreboardDigitsHelper.extractElapsed(stopwatchElapsedMillisProvider())
            } else {
                ScoreboardDigitsHelper.extract(remainingMillisProvider())
            }
        }
    }
    val isRunning = if (isStopwatch) stopwatchStatus == StopwatchStatus.Running else status == TimerStatus.Running

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
            FlipDigitCard(digit = digits.hoursTens, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)
            FlipDigitCard(digit = digits.hoursOnes, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)

            FlipColon(height = cardHeight, width = dims.colonWidth.dp)

            // Minutes
            FlipDigitCard(digit = digits.minutesTens, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)
            FlipDigitCard(digit = digits.minutesOnes, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)

            FlipColon(height = cardHeight, width = dims.colonWidth.dp)

            // Seconds
            FlipDigitCard(digit = digits.secondsTens, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)
            FlipDigitCard(digit = digits.secondsOnes, width = cardWidth, height = cardHeight, fontSize = fontSize, animate = isRunning)

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
    fontSize: TextUnit,
    animate: Boolean = true
) {
    var currentDigit by remember { mutableStateOf(digit) }
    var outgoingDigit by remember { mutableStateOf(digit) }
    var isFlipping by remember { mutableStateOf(false) }
    val flipAnimation = remember { Animatable(0f) }

    LaunchedEffect(digit, animate) {
        if (digit != currentDigit) {
            if (animate) {
                outgoingDigit = currentDigit
                isFlipping = true
                flipAnimation.snapTo(0f)
                try {
                    flipAnimation.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    )
                    currentDigit = digit
                } finally {
                    flipAnimation.snapTo(0f)
                    isFlipping = false
                    currentDigit = digit
                }
            } else {
                currentDigit = digit
                isFlipping = false
                flipAnimation.snapTo(0f)
            }
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
                rotationX = if (isFlipping) -flipAnimation.value * 180f else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        val charToShow = if (isFlipping) {
            if (flipAnimation.value < 0.5f) outgoingDigit else digit
        } else {
            currentDigit
        }

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
                if (isFlipping && flipAnimation.value >= 0.5f) {
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
