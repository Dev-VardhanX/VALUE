package com.devvardhan.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.MainHeader
import com.devvardhan.value.ui.components.ModeSwitch
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.WheelTimePicker
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Screen 1: Set Timer screen.
 * Allows user to pick Hours, Minutes, and Seconds via wheel/tappable pickers,
 * displays unified top header, mentions current style, and starts the countdown.
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
    onOpenSettings: () -> Unit,
    onStartClick: () -> Unit,
    currentMode: AppMode = AppMode.TIMER,
    isStopwatchRunning: Boolean = false,
    onModeSelected: (AppMode) -> Unit = {},
    onWheelScrollStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalSeconds = hours * 3600L + minutes * 60L + seconds
    val isStartEnabled = totalSeconds > 0L

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        Column(modifier = Modifier.fillMaxSize()) {
            MainHeader(
                currentMode = currentMode,
                isTimerRunning = false,
                isStopwatchRunning = isStopwatchRunning,
                onOpenSettings = onOpenSettings,
                onFullscreenClick = {},
                onModeSelected = onModeSelected,
                progressProvider = { 0f },
                fullscreenAlphaProvider = { 0f },
                isLandscape = isLandscape
            )

            SetTimerContent(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                onHoursChange = onHoursChange,
                onMinutesChange = onMinutesChange,
                onSecondsChange = onSecondsChange,
                onStartClick = onStartClick,
                isStartEnabled = isStartEnabled,
                isLandscape = isLandscape,
                onWheelScrollStateChange = onWheelScrollStateChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Pure body content for Set Timer: displays the title, the wheel pickers, and the Start button.
 * Placed within the HorizontalPager below the fixed MainHeader.
 */
@Composable
fun SetTimerContent(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onStartClick: () -> Unit,
    isStartEnabled: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    onWheelScrollStateChange: (Boolean) -> Unit = {}
) {
    if (isLandscape) {
        // Landscape layout: Pickers on left side, controls on right side
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 6.dp),
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
                    onSecondsChange = onSecondsChange,
                    onScrollStateChange = onWheelScrollStateChange
                )
            }

            // Right side: Set Timer title, start button
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(R.string.set_timer_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 0.5.sp
                )

                PrimaryPillButton(
                    text = stringResource(R.string.action_start),
                    onClick = onStartClick,
                    enabled = isStartEnabled,
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .debugTestTag("btn_start_timer")
                )
            }
        }
    } else {
        // Portrait layout:
        // 1. Set Timer title
        // 2. Large Pickers
        // 3. Big Start button
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.set_timer_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = PureWhite,
                letterSpacing = 0.5.sp
            )

            // Generously spaced WheelTimePicker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                WheelTimePicker(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange,
                    onSecondsChange = onSecondsChange,
                    onScrollStateChange = onWheelScrollStateChange,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Bottom area: large Start button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                PrimaryPillButton(
                    text = stringResource(R.string.action_start),
                    onClick = onStartClick,
                    enabled = isStartEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugTestTag("btn_start_timer"),
                    height = 56.dp
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

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
            onOpenSettings = {},
            onStartClick = {},
            currentMode = AppMode.TIMER,
            isStopwatchRunning = true
        )
    }
}

@Preview(name = "SetTimer Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun SetTimerScreenLandscapePreview() {
    ValueTimerTheme {
        SetTimerScreen(
            hours = 1,
            minutes = 30,
            seconds = 0,
            selectedTheme = TimerTheme.CONCENTRIC_RINGS,
            onHoursChange = {},
            onMinutesChange = {},
            onSecondsChange = {},
            onOpenSettings = {},
            onStartClick = {},
            currentMode = AppMode.TIMER,
            isStopwatchRunning = false
        )
    }
}
