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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devvardhan.value.R
import com.devvardhan.value.core.AdaptiveLayoutHelper
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
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Stopwatch Screen:
 * Displays real-time count-up visual themes, millisecond-accurate frame updates,
 * tactile Reset/Lap/Start/Pause controls, and recorded laps.
 * Designed with dedicated 360dp phone ergonomics: uncrowded header, non-wrapping pill buttons,
 * and a permanently visible lap list.
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left pane: Stopwatch visual taking ~58% width
                Box(
                    modifier = Modifier
                        .weight(1.3f)
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
                        .weight(1.0f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row: Mode Switch + Palette + Fullscreen
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
                            modifier = Modifier.width(200.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons (clean 1 or 2 button states)
                    when (status) {
                        StopwatchStatus.Idle -> {
                            PrimaryPillButton(
                                text = stringResource(R.string.action_start),
                                onClick = onStartClick,
                                modifier = Modifier.fillMaxWidth(),
                                height = 52.dp
                            )
                        }
                        StopwatchStatus.Running -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SecondaryPillButton(
                                    text = stringResource(R.string.action_lap),
                                    onClick = onLapClick,
                                    enabled = isLapEnabled,
                                    modifier = Modifier.weight(1f),
                                    height = 52.dp
                                )
                                PrimaryPillButton(
                                    text = stringResource(R.string.action_pause),
                                    onClick = onPauseClick,
                                    modifier = Modifier.weight(1f),
                                    height = 52.dp
                                )
                            }
                        }
                        StopwatchStatus.Paused -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SecondaryPillButton(
                                    text = stringResource(R.string.action_reset),
                                    onClick = onResetClick,
                                    modifier = Modifier.weight(1f),
                                    height = 52.dp
                                )
                                PrimaryPillButton(
                                    text = stringResource(R.string.action_resume),
                                    onClick = onResumeClick,
                                    modifier = Modifier.weight(1f),
                                    height = 52.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lap List filling remainder
                    LapList(
                        laps = laps,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        } else {
            // Portrait layout: Header -> Slim Actions -> Visual (42%) -> Controls -> Lap list (remaining)
            val heights = remember(maxHeight.value) {
                AdaptiveLayoutHelper.calculateStopwatchPortraitHeights(maxHeight.value)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: Mode Switch centered
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ModeSwitch(
                        currentMode = currentMode,
                        isTimerRunning = isTimerRunning,
                        isStopwatchRunning = isRunning,
                        onModeSelected = onModeSelected,
                        modifier = Modifier.width(216.dp)
                    )
                }

                // Row 2: Slim bar right-aligned above visual for Palette & Fullscreen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
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

                // Visual container (~40-42% of height, never floating in empty space)
                Box(
                    modifier = Modifier
                        .height(heights.visualHeight.dp)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillis = liveElapsedMillis,
                        status = status
                    )
                }

                // Action controls: Clean states (1 big start button in Idle, 2 equal buttons in Running/Paused)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (status) {
                        StopwatchStatus.Idle -> {
                            PrimaryPillButton(
                                text = stringResource(R.string.action_start),
                                onClick = onStartClick,
                                modifier = Modifier.fillMaxWidth(),
                                height = 56.dp
                            )
                        }
                        StopwatchStatus.Running -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SecondaryPillButton(
                                    text = stringResource(R.string.action_lap),
                                    onClick = onLapClick,
                                    enabled = isLapEnabled,
                                    modifier = Modifier.weight(1f),
                                    height = 56.dp
                                )
                                PrimaryPillButton(
                                    text = stringResource(R.string.action_pause),
                                    onClick = onPauseClick,
                                    modifier = Modifier.weight(1f),
                                    height = 56.dp
                                )
                            }
                        }
                        StopwatchStatus.Paused -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SecondaryPillButton(
                                    text = stringResource(R.string.action_reset),
                                    onClick = onResetClick,
                                    modifier = Modifier.weight(1f),
                                    height = 56.dp
                                )
                                PrimaryPillButton(
                                    text = stringResource(R.string.action_resume),
                                    onClick = onResumeClick,
                                    modifier = Modifier.weight(1f),
                                    height = 56.dp
                                )
                            }
                        }
                    }
                }

                // Lap List: Always visible with at least 2-3 rows, filling remaining height down to bottom
                LapList(
                    laps = laps,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
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

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

private val sampleLaps5 = listOf(
    LapItem(5, 12450L, 78900L),
    LapItem(4, 18200L, 66450L),
    LapItem(3, 9100L, 48250L),
    LapItem(2, 21450L, 39150L),
    LapItem(1, 17700L, 17700L)
)

@Preview(name = "1. Portrait 360x640 (0 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun PreviewStopwatch360x640ZeroLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Idle,
            startTime = 0L,
            accumulatedMillis = 0L,
            laps = emptyList(),
            theme = TimerTheme.THREE_BOX,
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

@Preview(name = "2. Portrait 360x640 (5 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun PreviewStopwatch360x640FiveLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 78900L,
            accumulatedMillis = 78900L,
            laps = sampleLaps5,
            theme = TimerTheme.THREE_BOX,
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

@Preview(name = "3. Portrait 412x915 Font 1.3 (0 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915, fontScale = 1.3f)
@Composable
private fun PreviewStopwatch412x915Font13ZeroLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Idle,
            startTime = 0L,
            accumulatedMillis = 0L,
            laps = emptyList(),
            theme = TimerTheme.CONCENTRIC_RINGS,
            currentMode = AppMode.STOPWATCH,
            isTimerRunning = true,
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

@Preview(name = "4. Portrait 412x915 Font 1.3 (5 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915, fontScale = 1.3f)
@Composable
private fun PreviewStopwatch412x915Font13FiveLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Paused,
            startTime = 0L,
            accumulatedMillis = 78900L,
            laps = sampleLaps5,
            theme = TimerTheme.FLIP_CLOCK,
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

@Preview(name = "5. Landscape (0 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewStopwatchLandscapeZeroLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Idle,
            startTime = 0L,
            accumulatedMillis = 0L,
            laps = emptyList(),
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

@Preview(name = "6. Landscape (5 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewStopwatchLandscapeFiveLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 78900L,
            accumulatedMillis = 78900L,
            laps = sampleLaps5,
            theme = TimerTheme.DOT_GRID,
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

@Preview(name = "7. Tablet (0 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewStopwatchTabletZeroLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Idle,
            startTime = 0L,
            accumulatedMillis = 0L,
            laps = emptyList(),
            theme = TimerTheme.THREE_BOX,
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

@Preview(name = "8. Tablet (5 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewStopwatchTabletFiveLaps() {
    ValueTimerTheme {
        StopwatchScreen(
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 78900L,
            accumulatedMillis = 78900L,
            laps = sampleLaps5,
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
