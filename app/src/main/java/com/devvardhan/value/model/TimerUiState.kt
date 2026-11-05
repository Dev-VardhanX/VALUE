package com.devvardhan.value.model

import androidx.compose.runtime.Immutable

/**
 * Immutable UI State exposed by TimerViewModel for the Compose screens.
 *
 * @property status Current operational state: Idle, Running, Paused, Finished.
 * @property remainingMillis Current countdown remaining time in milliseconds.
 * @property totalMillis Initial duration set for this timer session in milliseconds.
 * @property targetEndTime SystemClock.elapsedRealtime timestamp when the timer will complete.
 * @property pausedRemainingMillis Remaining milliseconds when paused.
 * @property isFullscreen Whether the app is currently in immersive fullscreen mode.
 * @property selectedHours Picker value for hours (0-23).
 * @property selectedMinutes Picker value for minutes (0-59).
 * @property selectedSeconds Picker value for seconds (0-59).
 * @property theme Currently selected visual theme.
 */
@Immutable
data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val remainingMillis: Long = 0L,
    val totalMillis: Long = 0L,
    val targetEndTime: Long = 0L,
    val pausedRemainingMillis: Long = 0L,
    val isFullscreen: Boolean = false,
    val selectedHours: Int = 0,
    val selectedMinutes: Int = 5,
    val selectedSeconds: Int = 0,
    val theme: TimerTheme = TimerTheme.CIRCLE
) {
    val totalPickerMillis: Long
        get() = (selectedHours * 3600L + selectedMinutes * 60L + selectedSeconds) * 1000L

    val isPickerValid: Boolean
        get() = totalPickerMillis > 0L
}
