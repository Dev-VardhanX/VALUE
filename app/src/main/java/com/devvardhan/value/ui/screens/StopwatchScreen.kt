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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.LapList
import com.devvardhan.value.ui.components.ModeSwitch
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.SecondaryPillButton
import com.devvardhan.value.ui.components.StopwatchVisual
import com.devvardhan.value.ui.components.ThemeBottomSheet
import com.devvardhan.value.ui.components.rememberStopwatchElapsedMillis
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Stopwatch Screen:
 * Displays real-time count-up visual themes, millisecond-accurate frame updates,
 * tactile Reset/Lap/Start/Pause controls, and recorded laps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    status: StopwatchStatus,
    startTime: Long,
    accumulatedMillis: Long,
    laps: List<LapItem>,
    theme: TimerTheme,
    currentMode: AppMode,
    isTimerRunning: Boolean,
    onModeSelected: (AppMode) -> Unit,
    onThemeChange: (TimerTheme) -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showThemeSheet by remember { mutableStateOf(false) }

    // Decoupled 60fps frame clock
    val liveElapsedMillis by rememberStopwatchElapsedMillis(
        status = status,
        startTime = startTime,
        accumulatedMillis = accumulatedMillis
    )

    val isRunning = status == StopwatchStatus.Running
    val isPaused = status == StopwatchStatus.Paused
    val isResetEnabled = isPaused || (status == StopwatchStatus.Idle && laps.isNotEmpty()) || (status == StopwatchStatus.Idle && accumulatedMillis > 0L)
    val isLapEnabled = isRunning && laps.size < 99

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        if (isLandscape) {
            // Landscape layout: visual on the left, controls and lap list on the right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left pane: Stopwatch visual taking ~55-60% width
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillis = liveElapsedMillis,
                        status = status
                    )
                }

                // Right pane: Mode switch, header buttons, action controls, and lap list
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top header row: Mode Switch + Palette + Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModeSwitch(
                            currentMode = currentMode,
                            isTimerRunning = isTimerRunning,
                            isStopwatchRunning = isRunning,
                            onModeSelected = onModeSelected,
                            modifier = Modifier.width(180.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showThemeSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .border(1.dp, BorderSubtle, CircleShape)
                                    .semantics { contentDescription = "Choose theme" }
                            ) {
                                Icon(
                                    imageVector = AppIcons.Palette,
                                    contentDescription = stringResource(R.string.action_change_theme),
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onFullscreenClick,
                                modifier = Modifier
                                    .size(42.dp)
                                    .border(1.dp, BorderSubtle, CircleShape)
                                    .semantics { contentDescription = "Enter fullscreen mode" }
                            ) {
                                Icon(
                                    imageVector = AppIcons.Fullscreen,
                                    contentDescription = stringResource(R.string.action_fullscreen),
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecondaryPillButton(
                            text = stringResource(R.string.action_reset),
                            onClick = onResetClick,
                            enabled = isResetEnabled,
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryPillButton(
                            text = stringResource(R.string.action_lap),
                            onClick = onLapClick,
                            enabled = isLapEnabled,
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryPillButton(
                            text = stringResource(
                                when (status) {
                                    StopwatchStatus.Running -> R.string.action_pause
                                    StopwatchStatus.Paused -> R.string.action_resume
                                    StopwatchStatus.Idle -> R.string.action_start
                                }
                            ),
                            onClick = {
                                when (status) {
                                    StopwatchStatus.Running -> onPauseClick()
                                    StopwatchStatus.Paused -> onResumeClick()
                                    StopwatchStatus.Idle -> onStartClick()
                                }
                            },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    // Lap List
                    if (laps.isNotEmpty()) {
                        LapList(
                            laps = laps,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubtleGray,
                        letterSpacing = 4.sp
                    )

                    ModeSwitch(
                        currentMode = currentMode,
                        isTimerRunning = isTimerRunning,
                        isStopwatchRunning = isRunning,
                        onModeSelected = onModeSelected,
                        modifier = Modifier.width(180.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showThemeSheet = true },
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = "Choose theme" }
                        ) {
                            Icon(
                                imageVector = AppIcons.Palette,
                                contentDescription = stringResource(R.string.action_change_theme),
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onFullscreenClick,
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = "Enter fullscreen mode" }
                        ) {
                            Icon(
                                imageVector = AppIcons.Fullscreen,
                                contentDescription = stringResource(R.string.action_fullscreen),
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Middle: Visual container (~55% of height when no laps, adaptive when laps exist)
                Box(
                    modifier = Modifier
                        .weight(if (laps.isNotEmpty()) 1.2f else 2f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillis = liveElapsedMillis,
                        status = status
                    )
                }

                // Action controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryPillButton(
                        text = stringResource(R.string.action_reset),
                        onClick = onResetClick,
                        enabled = isResetEnabled,
                        modifier = Modifier.weight(1f)
                    )

                    SecondaryPillButton(
                        text = stringResource(R.string.action_lap),
                        onClick = onLapClick,
                        enabled = isLapEnabled,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPillButton(
                        text = stringResource(
                            when (status) {
                                StopwatchStatus.Running -> R.string.action_pause
                                StopwatchStatus.Paused -> R.string.action_resume
                                StopwatchStatus.Idle -> R.string.action_start
                            }
                        ),
                        onClick = {
                            when (status) {
                                StopwatchStatus.Running -> onPauseClick()
                                StopwatchStatus.Paused -> onResumeClick()
                                StopwatchStatus.Idle -> onStartClick()
                            }
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // Bottom: Lap list taking remaining space
                if (laps.isNotEmpty()) {
                    LapList(
                        laps = laps,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }

        if (showThemeSheet) {
            ThemeBottomSheet(
                selectedTheme = theme,
                onThemeSelected = onThemeChange,
                onDismissRequest = { showThemeSheet = false },
                availableThemes = TimerTheme.stopwatchThemes
            )
        }
    }
}

@Preview(name = "Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun StopwatchScreenPortraitPreview() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 45000L,
            accumulatedMillis = 45000L,
            laps = listOf(
                LapItem(2, 21450L, 45000L),
                LapItem(1, 23550L, 23550L)
            ),
            theme = TimerTheme.CIRCLE,
            currentMode = AppMode.STOPWATCH,
            isTimerRunning = false,
            onModeSelected = {},
            onThemeChange = {},
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onFullscreenClick = {}
        )
    }
}

@Preview(name = "Stopwatch Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun StopwatchScreenLandscapePreview() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 45000L,
            accumulatedMillis = 45000L,
            laps = listOf(
                LapItem(2, 21450L, 45000L),
                LapItem(1, 23550L, 23550L)
            ),
            theme = TimerTheme.CIRCLE,
            currentMode = AppMode.STOPWATCH,
            isTimerRunning = false,
            onModeSelected = {},
            onThemeChange = {},
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onFullscreenClick = {}
        )
    }
}

