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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 3: Three Box (Scoreboard).
 * Three stadium/arena scoreboard panels labelled HRS, MIN, SEC.
 * Each panel houses two individual digit cells that perform a vertical odometer roll
 * only when that specific digit changes.
 */
@Composable
fun ThreeBoxTimerTheme(
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier
) {
    val digits = ScoreboardDigitsHelper.extract(remainingMillis)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val isLandscape = availableWidth > availableHeight * 1.3f

        // Sizing for 3 panels side-by-side
        val cellWidth = if (isLandscape) {
            minOf((availableWidth - 140f) / 7.5f, availableHeight * 0.28f, 52f).coerceIn(32f, 56f).dp
        } else {
            minOf((availableWidth - 90f) / 6.8f, availableHeight * 0.22f, 48f).coerceIn(30f, 52f).dp
        }
        val cellHeight = cellWidth * 1.45f
        val fontSize = (cellHeight.value * 0.65f).coerceIn(24f, 48f).sp

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            ScoreboardPanel(
                label = "HRS",
                tensDigit = digits.hoursTens,
                onesDigit = digits.hoursOnes,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )

            ScoreboardPanel(
                label = "MIN",
                tensDigit = digits.minutesTens,
                onesDigit = digits.minutesOnes,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )

            ScoreboardPanel(
                label = "SEC",
                tensDigit = digits.secondsTens,
                onesDigit = digits.secondsOnes,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun ScoreboardPanel(
    label: String,
    tensDigit: Char,
    onesDigit: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(14.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(panelShape)
            .background(Color(0xFF0A0A0A))
            .border(1.dp, BorderSubtle, panelShape)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SubtleGray,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreboardDigitCell(
                digit = tensDigit,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )
            ScoreboardDigitCell(
                digit = onesDigit,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun ScoreboardDigitCell(
    digit: Char,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(width = cellWidth, height = cellHeight)
            .clip(cellShape)
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF262626), cellShape),
        contentAlignment = Alignment.Center
    ) {
        // Vertical odometer roll animation only for changed digits
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { height -> -height } +
                    fadeIn(animationSpec = tween(200))) togetherWith
                (slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { height -> height } +
                    fadeOut(animationSpec = tween(180)))
            },
            label = "ScoreboardDigitRoll"
        ) { activeDigit ->
            Text(
                text = activeDigit.toString(),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                color = PureWhite
            )
        }

        // Horizontal split line across the center of each digit cell
        HorizontalDivider(
            color = Color(0xFF000000),
            thickness = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ThreeBoxTimerThemePreview() {
    ValueTimerTheme {
        Box(modifier = Modifier.size(360.dp)) {
            ThreeBoxTimerTheme(
                remainingMillis = 1845000L,
                totalMillis = 3600000L,
                status = TimerStatus.Running
            )
        }
    }
}
