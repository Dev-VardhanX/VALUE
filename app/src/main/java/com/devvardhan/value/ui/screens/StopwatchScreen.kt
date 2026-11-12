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
import androidx.compose.runtime.remember
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
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.LapList
import com.devvardhan.value.ui.components.MainHeader
import com.devvardhan.value.ui.components.ModeSwitch
import com.devvardhan.value.ui.components.StopwatchControlRow
import com.devvardhan.value.ui.components.StopwatchVisual
import com.devvardhan.value.ui.components.rememberStopwatchElapsedMillis
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.util.debugTestTag
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Stopwatch Screen:
 * Displays real-time count-up visual themes, millisecond-accurate frame updates,
 * unified top header, tactile Reset/Lap/Start/Pause controls, and recorded laps.
 */
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
    onOpenSettings: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Decoupled 60fps frame clock
    val isRunning = status == StopwatchStatus.Running

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        Column(modifier = Modifier.fillMaxSize()) {
            MainHeader(
                currentMode = currentMode,
                isTimerRunning = isTimerRunning,
                isStopwatchRunning = isRunning,
                onOpenSettings = onOpenSettings,
                onFullscreenClick = onFullscreenClick,
                onModeSelected = onModeSelected,
                progressProvider = { 1f },
                fullscreenAlphaProvider = { 1f },
                isLandscape = isLandscape
            )

            StopwatchContent(
                status = status,
                startTime = startTime,
                accumulatedMillis = accumulatedMillis,
                laps = laps,
                theme = theme,
                onStartClick = onStartClick,
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
                onLapClick = onLapClick,
                onResetClick = onResetClick,
                isLandscape = isLandscape,
                isVisible = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Pure body content for Stopwatch: displays theme visual, controls (Start/Pause/Resume, Lap, Reset),
 * and the Lap List. Placed within HorizontalPager below the fixed MainHeader.
 */
@Composable
fun StopwatchContent(
    status: StopwatchStatus,
    startTime: Long,
    accumulatedMillis: Long,
    laps: List<LapItem>,
    theme: TimerTheme,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit,
    isLandscape: Boolean,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val liveElapsedMillis by rememberStopwatchElapsedMillis(
        status = status,
        startTime = startTime,
        accumulatedMillis = accumulatedMillis,
        isVisible = isVisible
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (isLandscape) {
            // Landscape layout: visual on the left (~58%), compact right column with control row and lap list
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left pane: Stopwatch visual (~58% width, full height)
                Box(
                    modifier = Modifier
                        .weight(1.38f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillisProvider = { liveElapsedMillis },
                        status = status
                    )
                }

                // Right pane: control row and lap list filling remainder
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Action controls row (Flag | Start/Pause/Resume | Reset)
                    val landscapeSplitFormatted = if (laps.isEmpty()) {
                        TimeUtils.formatLapTime(liveElapsedMillis)
                    } else {
                        TimeUtils.formatLapTime((liveElapsedMillis - laps.first().totalTimeMillis).coerceAtLeast(0L))
                    }
                    StopwatchControlRow(
                        status = status,
                        lapsCount = laps.size,
                        onStartClick = onStartClick,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onLapClick = onLapClick,
                        onResetClick = onResetClick,
                        modifier = Modifier.fillMaxWidth(),
                        height = 50.dp,
                        latestSplitFormatted = landscapeSplitFormatted,
                        elapsedMillis = liveElapsedMillis,
                        lastLapTotalMillis = laps.firstOrNull()?.totalTimeMillis ?: 0L
                    )

                    // Lap List filling remainder
                    LapList(
                        laps = laps,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .debugTestTag("list_laps")
                    )
                }
            }
        } else {
            // Portrait layout:
            // 1. Visual (~44% height)
            // 2. Action controls (Start / Lap + Pause / Reset + Resume)
            // 3. Lap list filling all remaining height
            val heights = remember(maxHeight.value) {
                AdaptiveLayoutHelper.calculateStopwatchPortraitHeights(maxHeight.value)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual container (~44% of height, fills available box without dead space)
                Box(
                    modifier = Modifier
                        .height(heights.visualHeight.dp)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchVisual(
                        theme = theme,
                        elapsedMillisProvider = { liveElapsedMillis },
                        status = status
                    )
                }

                // Action controls: Clean 3-button row (Flag | Start/Pause/Resume | Reset)
                val portraitSplitFormatted = if (laps.isEmpty()) {
                    TimeUtils.formatLapTime(liveElapsedMillis)
                } else {
                    TimeUtils.formatLapTime((liveElapsedMillis - laps.first().totalTimeMillis).coerceAtLeast(0L))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchControlRow(
                        status = status,
                        lapsCount = laps.size,
                        onStartClick = onStartClick,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onLapClick = onLapClick,
                        onResetClick = onResetClick,
                        modifier = Modifier.fillMaxWidth(),
                        height = 56.dp,
                        latestSplitFormatted = portraitSplitFormatted,
                        elapsedMillis = liveElapsedMillis,
                        lastLapTotalMillis = laps.firstOrNull()?.totalTimeMillis ?: 0L
                    )
                }

                // Lap List: Always visible with at least 2-3 rows, filling remaining height down to bottom
                LapList(
                    laps = laps,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .debugTestTag("list_laps")
                )
            }
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
            onOpenSettings = {},
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
            onOpenSettings = {},
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
            onOpenSettings = {},
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onFullscreenClick = {}
        )
    }
}

@Preview(name = "4. Landscape (5 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewStopwatchLandscapeFiveLaps() {
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
            onOpenSettings = {},
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onFullscreenClick = {}
        )
    }
}
