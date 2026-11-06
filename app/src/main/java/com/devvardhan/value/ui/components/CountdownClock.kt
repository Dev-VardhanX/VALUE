package com.devvardhan.value.ui.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import com.devvardhan.value.model.TimerStatus
import kotlinx.coroutines.isActive

/**
 * Decoupled 60fps UI frame clock based on SystemClock.elapsedRealtime().
 * Immediately computes the correct remaining duration synchronously on frame 0,
 * avoiding any initial 0L delay or late animation starts.
 */
@Composable
fun rememberCountdownRemainingMillis(
    status: TimerStatus,
    targetEndTime: Long,
    pausedRemainingMillis: Long
): State<Long> {
    val initialRemaining = when (status) {
        TimerStatus.Running -> maxOf(0L, targetEndTime - SystemClock.elapsedRealtime())
        TimerStatus.Paused -> pausedRemainingMillis
        else -> 0L
    }

    // Keyed on (status, targetEndTime, pausedRemainingMillis) so state is instantly correct on frame 0
    val remainingMillisState = remember(status, targetEndTime, pausedRemainingMillis) {
        mutableLongStateOf(initialRemaining)
    }

    LaunchedEffect(status, targetEndTime, pausedRemainingMillis) {
        when (status) {
            TimerStatus.Running -> {
                while (isActive) {
                    withFrameNanos {
                        val now = SystemClock.elapsedRealtime()
                        remainingMillisState.longValue = maxOf(0L, targetEndTime - now)
                    }
                }
            }

            TimerStatus.Paused -> {
                remainingMillisState.longValue = pausedRemainingMillis
            }

            else -> {
                remainingMillisState.longValue = 0L
            }
        }
    }

    return remainingMillisState
}
