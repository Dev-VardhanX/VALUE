package com.android.value.core

import android.content.Context
import android.os.SystemClock
import com.android.value.model.TimerStatus
import com.android.value.service.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerSnapshot(
    val status: TimerStatus = TimerStatus.Idle,
    val remainingMillis: Long = 0L,
    val totalMillis: Long = 0L
)

/**
 * Core drift-free timer engine based on SystemClock.elapsedRealtime().
 * Acts as the single source of truth for both ViewModel and Foreground Service.
 */
object TimerEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null

    private var targetEndTime: Long = 0L
    private var totalDurationMillis: Long = 0L
    private var pausedRemainingMillis: Long = 0L

    private val _snapshot = MutableStateFlow(TimerSnapshot())
    val snapshot: StateFlow<TimerSnapshot> = _snapshot.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getContext(fallback: Context? = null): Context? = appContext ?: fallback?.applicationContext

    /**
     * Starts the countdown timer for the specified duration in milliseconds.
     */
    fun start(durationMillis: Long, context: Context? = null) {
        if (durationMillis <= 0L) return
        val ctx = getContext(context)

        totalDurationMillis = durationMillis
        targetEndTime = SystemClock.elapsedRealtime() + durationMillis
        pausedRemainingMillis = 0L

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Running,
            remainingMillis = durationMillis,
            totalMillis = durationMillis
        )

        ctx?.let { TimerService.start(it, durationMillis) }
        startTicker()
    }

    /**
     * Pauses the active countdown.
     */
    fun pause(context: Context? = null) {
        if (_snapshot.value.status != TimerStatus.Running) return
        val ctx = getContext(context)

        val currentRemaining = maxOf(0L, targetEndTime - SystemClock.elapsedRealtime())
        pausedRemainingMillis = currentRemaining
        tickerJob?.cancel()
        tickerJob = null

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Paused,
            remainingMillis = currentRemaining,
            totalMillis = totalDurationMillis
        )

        ctx?.let { TimerService.pause(it) }
    }

    /**
     * Resumes the paused countdown.
     */
    fun resume(context: Context? = null) {
        if (_snapshot.value.status != TimerStatus.Paused) return
        if (pausedRemainingMillis <= 0L) return
        val ctx = getContext(context)

        targetEndTime = SystemClock.elapsedRealtime() + pausedRemainingMillis

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Running,
            remainingMillis = pausedRemainingMillis,
            totalMillis = totalDurationMillis
        )

        ctx?.let { TimerService.resume(it) }
        startTicker()
    }

    /**
     * Resets the timer back to Idle state.
     */
    fun reset(context: Context? = null) {
        val ctx = getContext(context)
        tickerJob?.cancel()
        tickerJob = null

        targetEndTime = 0L
        pausedRemainingMillis = 0L
        totalDurationMillis = 0L

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Idle,
            remainingMillis = 0L,
            totalMillis = 0L
        )

        ctx?.let { TimerService.stop(it) }
    }

    /**
     * Dismisses the finished state and resets to Idle.
     */
    fun dismiss(context: Context? = null) {
        reset(context)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val remaining = targetEndTime - now

                if (remaining <= 0L) {
                    _snapshot.value = TimerSnapshot(
                        status = TimerStatus.Finished,
                        remainingMillis = 0L,
                        totalMillis = totalDurationMillis
                    )
                    // Inform service that time is up so it can fire alarm sound, vibration & notification
                    appContext?.let { TimerService.finish(it) }
                    break
                } else {
                    _snapshot.value = TimerSnapshot(
                        status = TimerStatus.Running,
                        remainingMillis = remaining,
                        totalMillis = totalDurationMillis
                    )
                }
                delay(30L) // 30ms refresh for silky-smooth progress circle and UI response
            }
        }
    }
}

