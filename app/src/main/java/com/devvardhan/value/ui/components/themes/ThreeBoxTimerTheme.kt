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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
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
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.ui.components.CentisecondsSlot
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Theme 3: Three Box (Scoreboard).
 * Three split stadium scoreboard panels for Hours, Minutes, and Seconds.
 * Tabular monospace digits with exact 1 : 1.6 tall cells and no glyph clipping.
 * In Stopwatch mode: centiseconds sit on the same baseline directly to the right of the seconds panel.
 *
 * Performance optimizations:
 * - Digits extracted once per second via derivedStateOf, avoiding recompositions on every millisecond.
 * - Centiseconds rendered directly in the draw phase via CentisecondsSlot.
 */
@Composable
fun ThreeBoxTimerTheme(
    remainingMillisProvider: () -> Long = { 0L },
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillisProvider: () -> Long = { 0L },
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    // Optimization: Digits derived once per second to prevent unnecessary recomposition
    val digits by remember(isStopwatch, if (isStopwatch) stopwatchElapsedMillisProvider else remainingMillisProvider) {
        derivedStateOf {
            if (isStopwatch) {
                ScoreboardDigitsHelper.extractElapsed(stopwatchElapsedMillisProvider())
            } else {
                ScoreboardDigitsHelper.extract(remainingMillisProvider())
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val dims = remember(maxWidth.value, maxHeight.value, isStopwatch, layoutMode) {
            CellSizeHelper.calculateThreeBoxDimensions(
                availableWidth = maxWidth.value,
                availableHeight = maxHeight.value,
                isStopwatch = isStopwatch,
                layoutMode = layoutMode
            )
        }

        val panelWidth = dims.panelWidth.dp
        val panelHeight = dims.panelHeight.dp
        val cellWidth = dims.cellWidth.dp
        val cellHeight = dims.cellHeight.dp
        val fontSize = dims.fontSize.sp
        val colonWidth = dims.colonWidth.dp
        val gap = dims.gap.dp

        Row(
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
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
                dims = dims,
                countUp = isStopwatch
            )

            ScoreboardColon(panelHeight = panelHeight, fontSize = fontSize, colonWidth = colonWidth)

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
                dims = dims,
                countUp = isStopwatch
            )

            ScoreboardColon(panelHeight = panelHeight, fontSize = fontSize, colonWidth = colonWidth)

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
                dims = dims,
                countUp = isStopwatch
            )

            if (isStopwatch) {
                // Centiseconds slot sits directly after the seconds panel, aligned to digit baseline
                CentisecondsSlot(
                    elapsedMillisProvider = stopwatchElapsedMillisProvider,
                    fontSize = (dims.fontSize * 0.40f).sp,
                    fixedWidthDp = dims.centisSlotWidth.dp,
                    modifier = Modifier.padding(bottom = (dims.cellHeight * 0.08f).dp)
                )
            }
        }
    }
}

/**
 * Backward-compatible overload accepting direct Long values.
 */
@Composable
fun ThreeBoxTimerTheme(
    remainingMillis: Long = 0L,
    totalMillis: Long = 0L,
    status: TimerStatus = TimerStatus.Running,
    isStopwatch: Boolean = false,
    stopwatchElapsedMillis: Long = 0L,
    stopwatchStatus: StopwatchStatus = StopwatchStatus.Running,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = ThreeBoxTimerTheme(
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
private fun ScoreboardColon(panelHeight: Dp, fontSize: TextUnit, colonWidth: Dp) {
    Box(
        modifier = Modifier
            .height(panelHeight)
            .width(colonWidth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ":",
            fontSize = fontSize * 0.70f,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            color = SubtleGray,
            style = TextStyle(
                fontFeatureSettings = "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = fontSize * 0.70f
            )
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
    dims: CellSizeHelper.ThreeBoxCellDimensions,
    countUp: Boolean,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape((panelWidth.value * 0.12f).coerceIn(6f, 16f).dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .size(width = panelWidth, height = panelHeight)
            .background(Color(0xFF0A0A0A), panelShape)
            .border(1.dp, BorderSubtle, panelShape)
            .padding(
                horizontal = dims.panelPaddingHorizontal.dp,
                vertical = dims.panelPaddingVertical.dp
            )
    ) {
        // Label row (small gray HRS/MIN/SEC)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.labelRowHeight.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = (dims.labelRowHeight * 0.70f).sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }

        // Two cells side by side
        Row(
            horizontalArrangement = Arrangement.spacedBy(dims.innerCellGap.dp, Alignment.CenterHorizontally),
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
    val cellShape = RoundedCornerShape((cellWidth.value * 0.15f).coerceIn(4f, 10f).dp)

    Box(
        modifier = Modifier
            .size(width = cellWidth, height = cellHeight)
            .background(Color(0xFF141414), cellShape)
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
            modifier = Modifier.clipToBounds(),
            label = "DigitRoll"
        ) { currentDigit ->
            Box(
                modifier = Modifier.size(width = cellWidth, height = cellHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentDigit.toString(),
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    style = TextStyle(
                        fontFeatureSettings = "tnum",
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = fontSize
                    )
                )
            }
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

@Preview(name = "ThreeBox 120dp Width (Small Preview)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 120, heightDp = 80)
@Composable
private fun ThreeBox120dpPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "ThreeBox 200dp Width (Card Preview)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 200, heightDp = 133)
@Composable
private fun ThreeBox200dpPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "ThreeBox 360dp Width (Phone Portrait)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 240)
@Composable
private fun ThreeBox360dpPreview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            remainingMillis = 3724000L,
            totalMillis = 7200000L,
            status = TimerStatus.Running
        )
    }
}

@Preview(name = "ThreeBox Stopwatch 360dp", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun ThreeBoxStopwatch360Preview() {
    ValueTimerTheme {
        ThreeBoxTimerTheme(
            isStopwatch = true,
            stopwatchElapsedMillis = 1710L, // 00:00:01.71
            stopwatchStatus = StopwatchStatus.Running
        )
    }
}

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
