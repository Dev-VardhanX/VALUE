package com.devvardhan.value.ui.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devvardhan.value.model.StopwatchStatus
import kotlinx.coroutines.isActive

/**
 * Decoupled 60fps frame clock for Stopwatch based on SystemClock.elapsedRealtime().
 * Pauses the frame loop while the app is in the background and resyncs immediately on resume.
 */
@Composable
fun rememberStopwatchElapsedMillis(
    status: StopwatchStatus,
    startTime: Long,
    accumulatedMillis: Long
): State<Long> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isAppForeground = remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                isAppForeground.value = true
            } else if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                isAppForeground.value = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val elapsedMillisState = remember {
        mutableLongStateOf(
            when (status) {
                StopwatchStatus.Running -> accumulatedMillis + maxOf(0L, SystemClock.elapsedRealtime() - startTime)
                StopwatchStatus.Paused -> accumulatedMillis
                StopwatchStatus.Idle -> 0L
            }
        )
    }

    LaunchedEffect(status, startTime, accumulatedMillis, isAppForeground.value) {
        when (status) {
            StopwatchStatus.Running -> {
                if (isAppForeground.value) {
                    while (isActive) {
                        withFrameNanos {
                            val now = SystemClock.elapsedRealtime()
                            elapsedMillisState.longValue = accumulatedMillis + maxOf(0L, now - startTime)
                        }
                    }
                } else {
                    val now = SystemClock.elapsedRealtime()
                    elapsedMillisState.longValue = accumulatedMillis + maxOf(0L, now - startTime)
                }
            }

            StopwatchStatus.Paused -> {
                elapsedMillisState.longValue = accumulatedMillis
            }

            StopwatchStatus.Idle -> {
                elapsedMillisState.longValue = 0L
            }
        }
    }

    return elapsedMillisState
}

