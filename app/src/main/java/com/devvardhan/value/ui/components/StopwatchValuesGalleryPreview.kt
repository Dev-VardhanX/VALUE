package com.devvardhan.value.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Test values required for verifying all stopwatch themes:
 * 1. 00:00:00.00 (Zero / Idle)
 * 2. 00:00:01.71 (Short / Active Centis)
 * 3. 00:59:59.99 (Boundary before 1 hour, max 2-unit digits)
 * 4. 01:23:45.67 (3-unit with hours)
 * 5. 12:34:56.78 (Double digit hours)
 */
object StopwatchTestValues {
    const val ZERO = 0L
    const val ONE_SEC_71 = 1_710L
    const val FIFTY_NINE_MIN = 3_599_990L
    const val ONE_HOUR_23 = 5_025_670L
    const val TWELVE_HOUR_34 = 45_296_780L

    val ALL = listOf(
        "00:00:00.00" to ZERO,
        "00:00:01.71" to ONE_SEC_71,
        "00:59:59.99" to FIFTY_NINE_MIN,
        "01:23:45.67" to ONE_HOUR_23,
        "12:34:56.78" to TWELVE_HOUR_34
    )
}

@Composable
fun StopwatchThemeTestGallery(theme: TimerTheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "${theme.displayName} — Test Values Gallery",
            color = PureWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        StopwatchTestValues.ALL.forEach { (label, millis) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target: $label",
                        color = SubtleGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${millis}ms",
                        color = SubtleGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(PureBlack, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillis = millis,
                        status = StopwatchStatus.Running,
                        layoutMode = ThemeLayoutMode.PORTRAIT
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS FOR EACH THEME ACROSS ALL 5 TEST VALUES
// -------------------------------------------------------------------------------------------------

@Preview(name = "Three Box - All Values (Portrait 360dp)", widthDp = 360, heightDp = 1200)
@Composable
private fun PreviewThreeBoxAllValues() {
    ValueTimerTheme {
        StopwatchThemeTestGallery(TimerTheme.THREE_BOX)
    }
}

@Preview(name = "Flip Clock - All Values (Portrait 360dp)", widthDp = 360, heightDp = 1200)
@Composable
private fun PreviewFlipClockAllValues() {
    ValueTimerTheme {
        StopwatchThemeTestGallery(TimerTheme.FLIP_CLOCK)
    }
}

@Preview(name = "Circle - All Values (Portrait 360dp)", widthDp = 360, heightDp = 1200)
@Composable
private fun PreviewCircleAllValues() {
    ValueTimerTheme {
        StopwatchThemeTestGallery(TimerTheme.CIRCLE)
    }
}

@Preview(name = "Concentric Rings - All Values (Portrait 360dp)", widthDp = 360, heightDp = 1200)
@Composable
private fun PreviewConcentricRingsAllValues() {
    ValueTimerTheme {
        StopwatchThemeTestGallery(TimerTheme.CONCENTRIC_RINGS)
    }
}

@Preview(name = "Dot Grid - All Values (Portrait 360dp)", widthDp = 360, heightDp = 1200)
@Composable
private fun PreviewDotGridAllValues() {
    ValueTimerTheme {
        StopwatchThemeTestGallery(TimerTheme.DOT_GRID)
    }
}

// Individual Spot Checks for Landscape and Fullscreen
@Preview(name = "Three Box 12:34:56.78 Landscape", widthDp = 740, heightDp = 360)
@Composable
private fun PreviewThreeBoxMaxLandscape() {
    ValueTimerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack),
            contentAlignment = Alignment.Center
        ) {
            StopwatchVisual(
                theme = TimerTheme.THREE_BOX,
                elapsedMillis = StopwatchTestValues.TWELVE_HOUR_34,
                status = StopwatchStatus.Running,
                layoutMode = ThemeLayoutMode.LANDSCAPE
            )
        }
    }
}

@Preview(name = "Concentric Rings Zero Paused (No Arc, Dot Suppressed)", widthDp = 360, heightDp = 360)
@Composable
private fun PreviewConcentricRingsZeroPaused() {
    ValueTimerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack),
            contentAlignment = Alignment.Center
        ) {
            StopwatchVisual(
                theme = TimerTheme.CONCENTRIC_RINGS,
                elapsedMillis = StopwatchTestValues.ZERO,
                status = StopwatchStatus.Paused,
                layoutMode = ThemeLayoutMode.PORTRAIT
            )
        }
    }
}

