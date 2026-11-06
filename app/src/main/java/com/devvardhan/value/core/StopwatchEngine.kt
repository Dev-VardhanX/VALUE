package com.devvardhan.value.core

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Immutable
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.service.StopwatchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of the stopwatch state.
 */
@Immutable
data class StopwatchSnapshot(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val startTime: Long = 0L,
    val accumulatedMillis: Long = 0L,
    val wallClockStartTime: Long = 0L,
    val laps: List<LapItem> = emptyList()
) {
    /**
     * Calculates the exact elapsed milliseconds using elapsedRealtime to prevent drift.
     */
    fun computeElapsedMillis(now: Long = SystemClock.elapsedRealtime()): Long {
        return when (status) {
            StopwatchStatus.Running -> accumulatedMillis + maxOf(0L, now - startTime)
            StopwatchStatus.Paused -> accumulatedMillis
            StopwatchStatus.Idle -> 0L
        }
    }
}

/**
 * Thread-safe single source of truth for the stopwatch.
 * Implements a strict, deterministic state machine: Idle -> Running -> Paused -> Running -> Idle.
 * Eliminates state leaks, cancels old coroutine jobs, and coordinates with StopwatchService.
 */
object StopwatchEngine {

    private const val TAG = "StopwatchEngine"
    const val MAX_LAPS = 99

    // Time providers (customizable for unit tests without mocking android.os.SystemClock)
    var timeProvider: () -> Long = { SystemClock.elapsedRealtime() }
    var wallClockProvider: () -> Long = { System.currentTimeMillis() }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var activeJob: Job? = null

    private val _snapshot = MutableStateFlow(StopwatchSnapshot())
    val snapshot: StateFlow<StopwatchSnapshot> = _snapshot.asStateFlow()

    private var appContext: Context? = null
    private var preferencesRepo: StopwatchPreferencesRepository? = null

    fun initialize(context: Context) {
        val app = context.applicationContext
        appContext = app
        preferencesRepo = StopwatchPreferencesRepository(app)
        Log.d(TAG, "StopwatchEngine initialized")

        // Restore active session if process was terminated while stopwatch was active
        scope.launch {
            try {
                preferencesRepo?.activeSessionFlow?.collect { session ->
                    restoreSession(session)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore stopwatch session", e)
            }
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun restoreSession(session: PersistedStopwatchSession) {
        if (_snapshot.value.status != StopwatchStatus.Idle) {
            // Already initialized or running in-memory
            return
        }

        val now = timeProvider()
        val nowWall = wallClockProvider()

        when (session.status) {
            StopwatchStatus.Running -> {
                if (now >= session.startTime && session.startTime > 0L) {
                    // Normal process death recovery while running
                    Log.d(TAG, "Restoring active running stopwatch session")
                    _snapshot.value = StopwatchSnapshot(
                        status = StopwatchStatus.Running,
                        startTime = session.startTime,
                        accumulatedMillis = session.accumulatedMillis,
                        wallClockStartTime = session.wallClockStartTime,
                        laps = session.laps
                    )
                    appContext?.let {
                        val elapsed = session.accumulatedMillis + (now - session.startTime)
                        StopwatchService.commandStart(it, elapsed)
                    }
                } else {
                    // Device rebooted: elapsedRealtime was reset
                    Log.d(TAG, "Reboot detected during stopwatch recovery; applying wall-clock fallback")
                    val wallElapsed = if (session.wallClockStartTime > 0L && nowWall > session.wallClockStartTime) {
                        nowWall - session.wallClockStartTime
                    } else {
                        0L
                    }
                    val totalAccumulated = session.accumulatedMillis + wallElapsed
                    _snapshot.value = StopwatchSnapshot(
                        status = StopwatchStatus.Paused,
                        startTime = 0L,
                        accumulatedMillis = totalAccumulated,
                        wallClockStartTime = 0L,
                        laps = session.laps
                    )
                }
            }

            StopwatchStatus.Paused -> {
                Log.d(TAG, "Restoring paused stopwatch session")
                _snapshot.value = StopwatchSnapshot(
                    status = StopwatchStatus.Paused,
                    startTime = 0L,
                    accumulatedMillis = session.accumulatedMillis,
                    wallClockStartTime = 0L,
                    laps = session.laps
                )
            }

            StopwatchStatus.Idle -> {
                // Keep idle
            }
        }
    }

    /**
     * Starts the stopwatch from Idle or Resumes from Paused.
     */
    fun start() {
        activeJob?.cancel()
        val current = _snapshot.value
        if (current.status == StopwatchStatus.Running) return

        val now = timeProvider()
        val nowWall = wallClockProvider()

        val newAccumulated = if (current.status == StopwatchStatus.Idle) 0L else current.accumulatedMillis
        val newLaps = if (current.status == StopwatchStatus.Idle) emptyList() else current.laps

        _snapshot.value = StopwatchSnapshot(
            status = StopwatchStatus.Running,
            startTime = now,
            accumulatedMillis = newAccumulated,
            wallClockStartTime = nowWall,
            laps = newLaps
        )
        Log.d(TAG, "transition -> Running (startTime=$now, accumulated=$newAccumulated)")

        persistCurrentState()
        appContext?.let { StopwatchService.commandStart(it, newAccumulated) }
    }

    /**
     * Pauses a running stopwatch.
     */
    fun pause() {
        activeJob?.cancel()
        val current = _snapshot.value
        if (current.status != StopwatchStatus.Running) return

        val now = timeProvider()
        val added = maxOf(0L, now - current.startTime)
        val newAccumulated = current.accumulatedMillis + added

        _snapshot.value = StopwatchSnapshot(
            status = StopwatchStatus.Paused,
            startTime = 0L,
            accumulatedMillis = newAccumulated,
            wallClockStartTime = 0L,
            laps = current.laps
        )
        Log.d(TAG, "transition -> Paused (accumulated=$newAccumulated)")

        persistCurrentState()
        appContext?.let { StopwatchService.commandPause(it, newAccumulated) }
    }

    /**
     * Resumes a paused stopwatch.
     */
    fun resume() {
        activeJob?.cancel()
        val current = _snapshot.value
        if (current.status != StopwatchStatus.Paused) return

        val now = timeProvider()
        val nowWall = wallClockProvider()

        _snapshot.value = StopwatchSnapshot(
            status = StopwatchStatus.Running,
            startTime = now,
            accumulatedMillis = current.accumulatedMillis,
            wallClockStartTime = nowWall,
            laps = current.laps
        )
        Log.d(TAG, "transition -> Running (resume, startTime=$now, accumulated=${current.accumulatedMillis})")

        persistCurrentState()
        appContext?.let { StopwatchService.commandResume(it, current.accumulatedMillis) }
    }

    /**
     * Records a lap while the stopwatch is running.
     * Up to 99 laps. Newest lap placed at index 0.
     */
    fun lap(): LapItem? {
        val current = _snapshot.value
        if (current.status != StopwatchStatus.Running) return null
        if (current.laps.size >= MAX_LAPS) return null

        val now = timeProvider()
        val totalElapsed = current.accumulatedMillis + maxOf(0L, now - current.startTime)
        val lastLapTotal = current.laps.firstOrNull()?.totalTimeMillis ?: 0L
        val lapSplit = maxOf(0L, totalElapsed - lastLapTotal)

        val newLap = LapItem(
            lapNumber = current.laps.size + 1,
            lapTimeMillis = lapSplit,
            totalTimeMillis = totalElapsed
        )
        val newLaps = listOf(newLap) + current.laps

        _snapshot.value = current.copy(laps = newLaps)
        Log.d(TAG, "lap recorded: #${newLap.lapNumber}, split=$lapSplit, total=$totalElapsed")

        persistCurrentState()
        return newLap
    }

    /**
     * Resets the stopwatch to Idle and clears all laps.
     */
    fun reset() {
        activeJob?.cancel()
        _snapshot.value = StopwatchSnapshot(
            status = StopwatchStatus.Idle,
            startTime = 0L,
            accumulatedMillis = 0L,
            wallClockStartTime = 0L,
            laps = emptyList()
        )
        Log.d(TAG, "transition -> Idle (reset)")

        scope.launch {
            try {
                preferencesRepo?.clearSession()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear stopwatch session", e)
            }
        }
        appContext?.let { StopwatchService.commandStop(it) }
    }

    private fun persistCurrentState() {
        val current = _snapshot.value
        scope.launch {
            try {
                preferencesRepo?.saveSession(
                    status = current.status,
                    startTime = current.startTime,
                    accumulatedMillis = current.accumulatedMillis,
                    wallClockStartTime = current.wallClockStartTime,
                    laps = current.laps
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist stopwatch session", e)
            }
        }
    }
}
