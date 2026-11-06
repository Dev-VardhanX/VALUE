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
import androidx.compose.runtime.getValue
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
import com.devvardhan.value.ui.components.TimerVisual
import com.devvardhan.value.ui.components.rememberCountdownRemainingMillis
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Screen 2: Running Timer screen.
 * Displays countdown animation themes, live remaining digits,
 * unified top header, mode switcher, and Pause/Resume/Reset controls.
 */
@Composable
fun RunningTimerScreen(
    status: TimerStatus,
    targetEndTime: Long,
    pausedRemainingMillis: Long,
    totalMillis: Long,
    theme: TimerTheme,
    onOpenGallery: () -> Unit,
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
            // Landscape layout: visual on the left, controls on the right
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
                        .width(220.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row: VALUE wordmark on left, Style + Fullscreen on right
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenGallery,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, BorderSubtle, CircleShape)
                                    .semantics { contentDescription = "Choose style gallery" }
                            ) {
                                Icon(
                                    imageVector = AppIcons.Palette,
                                    contentDescription = stringResource(R.string.action_style),
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onFullscreenClick,
                                modifier = Modifier
                                    .size(40.dp)
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

                    ModeSwitch(
                        currentMode = currentMode,
                        isTimerRunning = status == TimerStatus.Running,
                        isStopwatchRunning = isStopwatchRunning,
                        onModeSelected = onModeSelected,
                        modifier = Modifier.width(200.dp)
                    )

                    // Action buttons (stacked)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryPillButton(
                            text = stringResource(if (isPaused) R.string.action_resume else R.string.action_pause),
                            onClick = if (isPaused) onResumeClick else onPauseClick,
                            modifier = Modifier.fillMaxWidth(),
                            height = 52.dp
                        )

                        SecondaryPillButton(
                            text = stringResource(R.string.action_reset),
                            onClick = onResetClick,
                            modifier = Modifier.fillMaxWidth(),
                            height = 52.dp
                        )
                    }
                }
            }
        } else {
            // Portrait layout:
            // 1. Top row: VALUE wordmark on left, Style + Fullscreen on right
            // 2. Directly below: ModeSwitch centered
            // 3. Visual
            // 4. Action buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: Wordmark on left, Style & Fullscreen on right
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenGallery,
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = "Choose style gallery" }
                        ) {
                            Icon(
                                imageVector = AppIcons.Palette,
                                contentDescription = stringResource(R.string.action_style),
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

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: ModeSwitch directly below, centered (identical metrics to SetTimer and Stopwatch)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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

                // Visual container with 60fps frame updates
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                        .padding(horizontal = 8.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryPillButton(
                        text = stringResource(R.string.action_reset),
                        onClick = onResetClick,
                        modifier = Modifier.weight(1f),
                        height = 56.dp
                    )

                    PrimaryPillButton(
                        text = stringResource(if (isPaused) R.string.action_resume else R.string.action_pause),
                        onClick = if (isPaused) onResumeClick else onPauseClick,
                        modifier = Modifier.weight(1f),
                        height = 56.dp
                    )
                }
            }
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
            onOpenGallery = {},
            onPauseClick = {},
            onResumeClick = {},
            onResetClick = {},
            onFullscreenClick = {},
            currentMode = AppMode.TIMER,
            isStopwatchRunning = true
        )
    }
}

@Preview(name = "Running Timer Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun RunningTimerScreenLandscapePreview() {
    ValueTimerTheme {
        RunningTimerScreen(
            status = TimerStatus.Running,
            targetEndTime = System.currentTimeMillis() + 120000L,
            pausedRemainingMillis = 120000L,
            totalMillis = 300000L,
            theme = TimerTheme.HOURGLASS,
            onOpenGallery = {},
            onPauseClick = {},
            onResumeClick = {},
            onResetClick = {},
            onFullscreenClick = {},
            currentMode = AppMode.TIMER,
            isStopwatchRunning = false
        )
    }
}
