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
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.ModeSwitch
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.SecondaryPillButton
import com.devvardhan.value.ui.components.ThemeBottomSheet
import com.devvardhan.value.ui.components.TimerVisual
import com.devvardhan.value.ui.components.rememberCountdownRemainingMillis
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Screen 2: Running Timer screen.
 * Driven by an allocation-free 60fps frame clock decoupled from state emissions.
 * Responsively places visual on left and controls in a vertical column on right in landscape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningTimerScreen(
    status: TimerStatus,
    targetEndTime: Long,
    pausedRemainingMillis: Long,
    totalMillis: Long,
    theme: TimerTheme,
    onThemeChange: (TimerTheme) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    currentMode: AppMode = AppMode.TIMER,
    isStopwatchRunning: Boolean = false,
    onModeSelected: (AppMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPaused = status == TimerStatus.Paused
    var showThemeSheet by remember { mutableStateOf(false) }

    // High performance frame clock for smooth 60fps drawing without recomposition
    val liveRemainingMillis by rememberCountdownRemainingMillis(
        status = status,
        targetEndTime = targetEndTime,
        pausedRemainingMillis = pausedRemainingMillis
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        if (isLandscape) {
            // Landscape layout: visual on the left taking most height/width, controls on the right
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visual container (left/center)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimerVisual(
                        theme = theme,
                        remainingMillis = liveRemainingMillis,
                        totalMillis = totalMillis,
                        status = status
                    )
                }

                // Controls column (right)
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with mode switch and icons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ModeSwitch(
                            currentMode = currentMode,
                            isTimerRunning = status == TimerStatus.Running,
                            isStopwatchRunning = isStopwatchRunning,
                            onModeSelected = onModeSelected,
                            modifier = Modifier.width(200.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showThemeSheet = true },
                                modifier = Modifier
                                    .size(44.dp)
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
                                    .size(44.dp)
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

                    // Action buttons (stacked)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryPillButton(
                            text = stringResource(if (isPaused) R.string.action_resume else R.string.action_pause),
                            onClick = if (isPaused) onResumeClick else onPauseClick,
                            modifier = Modifier.fillMaxWidth()
                        )

                        SecondaryPillButton(
                            text = stringResource(R.string.action_reset),
                            onClick = onResetClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Portrait layout: Header at top, visual in center, action buttons at bottom
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Row 1: Mode Switch centered (identical position to Stopwatch screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ModeSwitch(
                        currentMode = currentMode,
                        isTimerRunning = status == TimerStatus.Running,
                        isStopwatchRunning = isStopwatchRunning,
                        onModeSelected = onModeSelected,
                        modifier = Modifier.width(216.dp)
                    )
                }

                // Row 2: Slim bar right-aligned above visual for Palette & Fullscreen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
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

                    Spacer(modifier = Modifier.width(10.dp))

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

                // Visual container with 60fps frame updates
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimerVisual(
                        theme = theme,
                        remainingMillis = liveRemainingMillis,
                        totalMillis = totalMillis,
                        status = status
                    )
                }

                // Bottom action controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryPillButton(
                        text = stringResource(R.string.action_reset),
                        onClick = onResetClick,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryPillButton(
                        text = stringResource(if (isPaused) R.string.action_resume else R.string.action_pause),
                        onClick = if (isPaused) onResumeClick else onPauseClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showThemeSheet) {
            ThemeBottomSheet(
                selectedTheme = theme,
                onThemeSelected = onThemeChange,
                onDismissRequest = { showThemeSheet = false }
            )
        }
    }
}

@Preview(name = "Running Timer Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun RunningTimerScreenPortraitPreview() {
    ValueTimerTheme {
        RunningTimerScreen(
            status = TimerStatus.Running,
            targetEndTime = System.currentTimeMillis() + 120000L,
            pausedRemainingMillis = 120000L,
            totalMillis = 300000L,
            theme = TimerTheme.CIRCLE,
            onThemeChange = {},
            onPauseClick = {},
            onResumeClick = {},
            onResetClick = {},
            onFullscreenClick = {},
            currentMode = AppMode.TIMER,
            isStopwatchRunning = true
        )
    }
}
