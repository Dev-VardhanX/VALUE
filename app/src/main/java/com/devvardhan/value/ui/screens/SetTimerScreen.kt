package com.devvardhan.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.ThemeSelectorRow
import com.devvardhan.value.ui.components.WheelTimePicker
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Screen 1: Set Timer screen.
 * Allows user to pick Hours, Minutes, and Seconds via wheel/tappable pickers,
 * choose visual countdown themes, and start the countdown.
 * Automatically adapts between single-column portrait and dual-column landscape.
 */
@Composable
fun SetTimerScreen(
    hours: Int,
    minutes: Int,
    seconds: Int,
    selectedTheme: TimerTheme,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onThemeChange: (TimerTheme) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = hours * 3600L + minutes * 60L + seconds
    val isStartEnabled = totalSeconds > 0L

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        if (isLandscape) {
            // Landscape layout: Pickers on left side, Theme Selector & Start button on right side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: WheelTimePicker centered
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    WheelTimePicker(
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        onHoursChange = onHoursChange,
                        onMinutesChange = onMinutesChange,
                        onSecondsChange = onSecondsChange
                    )
                }

                // Right side: Header, theme selector, start button
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubtleGray,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.set_timer_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = PureWhite,
                            letterSpacing = 0.5.sp
                        )
                    }

                    ThemeSelectorRow(
                        selectedTheme = selectedTheme,
                        onThemeSelected = onThemeChange,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    PrimaryPillButton(
                        text = stringResource(R.string.action_start),
                        onClick = onStartClick,
                        enabled = isStartEnabled,
                        modifier = Modifier.fillMaxWidth(0.90f)
                    )
                }
            }
        } else {
            // Portrait layout: Header at top, pickers in center, themes and start at bottom
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubtleGray,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.set_timer_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                WheelTimePicker(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange,
                    onSecondsChange = onSecondsChange,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeSelectorRow(
                        selectedTheme = selectedTheme,
                        onThemeSelected = onThemeChange,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    PrimaryPillButton(
                        text = stringResource(R.string.action_start),
                        onClick = onStartClick,
                        enabled = isStartEnabled,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(name = "SetTimer Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun SetTimerScreenPortraitPreview() {
    ValueTimerTheme {
        SetTimerScreen(
            hours = 0,
            minutes = 25,
            seconds = 0,
            selectedTheme = TimerTheme.CIRCLE,
            onHoursChange = {},
            onMinutesChange = {},
            onSecondsChange = {},
            onThemeChange = {},
            onStartClick = {}
        )
    }
}

@Preview(name = "SetTimer Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun SetTimerScreenLandscapePreview() {
    ValueTimerTheme {
        SetTimerScreen(
            hours = 0,
            minutes = 25,
            seconds = 0,
            selectedTheme = TimerTheme.CIRCLE,
            onHoursChange = {},
            onMinutesChange = {},
            onSecondsChange = {},
            onThemeChange = {},
            onStartClick = {}
        )
    }
}

@Preview(name = "SetTimer Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1280, heightDp = 800)
@Composable
private fun SetTimerScreenTabletPreview() {
    ValueTimerTheme {
        SetTimerScreen(
            hours = 1,
            minutes = 30,
            seconds = 0,
            selectedTheme = TimerTheme.CIRCLE,
            onHoursChange = {},
            onMinutesChange = {},
            onSecondsChange = {},
            onThemeChange = {},
            onStartClick = {}
        )
    }
}
