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
 * Avoids emitting StateFlow 60 times a second while guaranteeing perfectly fluid visual motion.
 */
@Composable
fun rememberCountdownRemainingMillis(
    status: TimerStatus,
    targetEndTime: Long,
    pausedRemainingMillis: Long
): State<Long> {
    val remainingMillisState = remember {
        mutableLongStateOf(
            when (status) {
                TimerStatus.Running -> maxOf(0L, targetEndTime - SystemClock.elapsedRealtime())
                TimerStatus.Paused -> pausedRemainingMillis
                else -> 0L
            }
        )
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

