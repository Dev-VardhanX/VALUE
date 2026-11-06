package com.devvardhan.value.model

import androidx.compose.runtime.Immutable

/**
 * Immutable UI state for the Stopwatch screen.
 */
@Immutable
data class StopwatchUiState(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val startTime: Long = 0L,
    val accumulatedMillis: Long = 0L,
    val laps: List<LapItem> = emptyList(),
    val theme: TimerTheme = TimerTheme.CIRCLE,
    val isFullscreen: Boolean = false
)

