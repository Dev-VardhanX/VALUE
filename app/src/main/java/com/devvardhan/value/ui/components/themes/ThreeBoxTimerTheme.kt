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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 3: Three Box (Scoreboard).
 * Three scoreboard panels labelled HRS, MIN, SEC side-by-side.
 * In landscape, panels expand across full width and height with huge odometer rolling digits.
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
        val dims = remember(maxWidth.value, maxHeight.value) {
            AdaptiveLayoutHelper.calculateThreeBoxDimensions(maxWidth.value, maxHeight.value)
        }

        val panelWidth = dims.panelWidth.dp
        val panelHeight = dims.panelHeight.dp
        val cellWidth = ((dims.panelWidth - 28f) / 2f).coerceAtLeast(28f).dp
        val cellHeight = (dims.panelHeight - 44f).coerceAtLeast(40f).dp
        val fontSize = dims.fontSize.sp

        Row(
            horizontalArrangement = Arrangement.spacedBy((maxWidth.value * 0.02f).coerceIn(6f, 18f).dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            ScoreboardPanel(
                label = "HRS",
                tensDigit = digits.hoursTens,
                onesDigit = digits.hoursOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )

            ScoreboardPanel(
                label = "MIN",
                tensDigit = digits.minutesTens,
                onesDigit = digits.minutesOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                fontSize = fontSize
            )

            ScoreboardPanel(
                label = "SEC",
                tensDigit = digits.secondsTens,
                onesDigit = digits.secondsOnes,
                panelWidth = panelWidth,
                panelHeight = panelHeight,
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
    panelWidth: Dp,
    panelHeight: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    fontSize: TextUnit,
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
        Text(
            text = label,
            fontSize = (panelWidth.value * 0.12f).coerceIn(10f, 15f).sp,
            fontWeight = FontWeight.SemiBold,
            color = SubtleGray,
            letterSpacing = 2.sp,
            maxLines = 1,
            softWrap = false
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
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
    val cellShape = RoundedCornerShape((cellWidth.value * 0.16f).coerceIn(6f, 14f).dp)

    Box(
        modifier = modifier
            .size(width = cellWidth, height = cellHeight)
            .clip(cellShape)
            .background(Color(0xFF141414))
            .border(1.dp, BorderSubtle, cellShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    initialOffsetY = { -it }
                ) + fadeIn(animationSpec = tween(280))).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(280))
                )
            },
            label = "ScoreboardRollAnimation"
        ) { targetDigit ->
            Text(
                text = targetDigit.toString(),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                color = PureWhite,
                maxLines = 1,
                softWrap = false
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            thickness = 0.5.dp,
            color = Color(0x33000000)
        )
    }
}

@Preview(name = "ThreeBox Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun ThreeBoxTimerThemePortraitPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3725000L,
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
            remainingMillis = 3725000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "ThreeBox Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun ThreeBoxTimerThemeTabletPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3725000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}
