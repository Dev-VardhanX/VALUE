package com.devvardhan.value.ui.components.themes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 3: Three Box (Scoreboard).
 * Three split stadium scoreboard panels for Hours, Minutes, and Seconds.
 * Centiseconds are decoupled into the dedicated CentisecondsSlot to prevent layout vibration.
 * Uniform tabular monospace digits (tnum) guarantee equal cell widths.
 */
@Composable
fun ThreeBoxTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier
) {
    val digits = remember(remainingMillis, isStopwatch, stopwatchElapsedMillis) {
        if (isStopwatch) {
            ScoreboardDigitsHelper.extractElapsed(stopwatchElapsedMillis)
        } else {
            ScoreboardDigitsHelper.extract(remainingMillis)
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateThreeBoxDimensions(
                availableWidth = maxWidth.value,
                availableHeight = maxHeight.value
            )
        }

        val panelWidth = dims.panelWidth.dp
        val panelHeight = dims.panelHeight.dp
        val cellWidth = ((dims.panelWidth - 24f) / 2f).coerceAtLeast(28f).dp
        val cellHeight = (dims.panelHeight - 40f).coerceAtLeast(40f).dp
        val fontSize = dims.fontSize.sp
        val panelSpacing = (maxWidth.value * 0.02f).coerceIn(4f, 12f).dp

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            // Hours panel
            ScoreboardPanel(
                label = "HRS",
                tensDigit = digits.hoursTens,
                onesDigit = digits.hoursOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize,
                countUp = isStopwatch
            )

            ScoreboardColon(panelHeight = panelHeight, fontSize = fontSize)

            // Minutes panel
            ScoreboardPanel(
                label = "MIN",
                tensDigit = digits.minutesTens,
                onesDigit = digits.minutesOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize,
                countUp = isStopwatch
            )

            ScoreboardColon(panelHeight = panelHeight, fontSize = fontSize)

            // Seconds panel
            ScoreboardPanel(
                label = "SEC",
                tensDigit = digits.secondsTens,
                onesDigit = digits.secondsOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize,
                countUp = isStopwatch
            )
        }
    }
}

@Composable
private fun ScoreboardColon(panelHeight: Dp, fontSize: TextUnit) {
    Box(
        modifier = Modifier
            .height(panelHeight)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ":",
            fontSize = fontSize * 0.65f,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            color = SubtleGray,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun ScoreboardPanel(
    label: String,
    tensDigit: Char,
    onesDigit: Char,
    panelWidth: Dp,
    panelHeight: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
    countUp: Boolean,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape((panelWidth.value * 0.12f).coerceIn(10f, 22f).dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .size(width = panelWidth, height = panelHeight)
            .clip(panelShape)
            .background(Color(0xFF0A0A0A))
            .border(1.dp, BorderSubtle, panelShape)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        // Secondary unit label (small, letter-spaced, ~15-20% of digit size)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = (fontSize.value * 0.20f).coerceIn(10f, 15f).sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 2.sp,
                maxLines = 1,
                softWrap = false
            )
        }

        // Mechanical score digits
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreboardDigitCell(
                digit = tensDigit,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize,
                countUp = countUp
            )
            ScoreboardDigitCell(
                digit = onesDigit,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize,
                countUp = countUp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun ScoreboardDigitCell(
    digit: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
    countUp: Boolean
) {
    val cellShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .size(width = cellWidth, height = cellHeight)
            .clip(cellShape)
            .background(Color(0xFF141414))
            .border(0.5.dp, Color(0xFF282828), cellShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                if (countUp) {
                    (slideInVertically(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn()).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                            targetOffsetY = { fullHeight -> -fullHeight }
                        ) + fadeOut()
                    )
                } else {
                    (slideInVertically(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> -fullHeight }
                    ) + fadeIn()).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                            targetOffsetY = { fullHeight -> fullHeight }
                        ) + fadeOut()
                    )
                }
            },
            label = "DigitRoll"
        ) { currentDigit ->
            Text(
                text = currentDigit.toString(),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                color = PureWhite,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }

        // Mechanical scoreboard center split groove
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFF0A0A0A)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "ThreeBox Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun ThreeBoxTimerThemePortraitPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "ThreeBox Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun ThreeBoxTimerThemeLandscapePreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}
