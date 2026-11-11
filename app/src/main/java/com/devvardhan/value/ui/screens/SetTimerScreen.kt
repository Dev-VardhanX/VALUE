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
    val settingsDesc = stringResource(R.string.action_settings)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        if (isLandscape) {
            // Landscape layout: Pickers on left side, controls on right side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
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

                // Right side: Header row, Mode switch, Style label, start button
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header row: VALUE wordmark on left, Settings button on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubtleGray,
                            letterSpacing = 3.sp
                        )

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = settingsDesc }
                                .debugTestTag("btn_settings")
                        ) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = settingsDesc,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    ModeSwitch(
                        currentMode = currentMode,
                        isTimerRunning = false,
                        isStopwatchRunning = isStopwatchRunning,
                        onModeSelected = onModeSelected,
                        modifier = Modifier.width(200.dp)
                    )

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
            // 1. Top row: VALUE wordmark on left, Style button on right
            // 2. Directly below: ModeSwitch centered
            // 3. Set Timer title
            // 4. Large Pickers
            // 5. Gap + Style label + Big Start button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Wordmark on left, Settings button on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubtleGray,
                            letterSpacing = 3.sp
                        )

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = settingsDesc }
                                .debugTestTag("btn_settings")
                        ) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = settingsDesc,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: ModeSwitch directly below, centered
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModeSwitch(
                            currentMode = currentMode,
                            isTimerRunning = false,
                            isStopwatchRunning = isStopwatchRunning,
                            onModeSelected = onModeSelected,
                            modifier = Modifier.width(216.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.set_timer_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 0.5.sp
                    )
                }

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

                // Bottom area: large Start button (no style or sound controls)
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
