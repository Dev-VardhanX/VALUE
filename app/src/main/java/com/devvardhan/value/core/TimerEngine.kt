package com.devvardhan.value.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Immutable
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.service.TimerService
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

/**
 * Immutable snapshot of the timer state.
 */
@Immutable
data class TimerSnapshot(
    val status: TimerStatus = TimerStatus.Idle,
    val targetEndTime: Long = 0L,
    val totalMillis: Long = 0L,
    val pausedRemainingMillis: Long = 0L,
    val lastSetDurationMillis: Long = 300_000L // Default 5 minutes
) {
    /**
     * Calculates the exact remaining milliseconds at the given system elapsed realtime.
     */
    fun computeRemainingMillis(now: Long = SystemClock.elapsedRealtime()): Long {
        return when (status) {
            TimerStatus.Running -> maxOf(0L, targetEndTime - now)
            TimerStatus.Paused -> pausedRemainingMillis
            TimerStatus.Finished -> 0L
            TimerStatus.Idle -> 0L
        }
    }
}

/**
 * Thread-safe single source of truth for the countdown timer.
 * Implements a strict, deterministic state machine with clean job cancellations,
 * background persistence, and AlarmManager exact alarm coordination.
 */
object TimerEngine {

    private const val TAG = "TimerEngine"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    private val _snapshot = MutableStateFlow(TimerSnapshot())
    val snapshot: StateFlow<TimerSnapshot> = _snapshot.asStateFlow()

    private var appContext: Context? = null
    private var preferencesRepo: TimerPreferencesRepository? = null

    fun initialize(context: Context) {
        val app = context.applicationContext
        appContext = app
        preferencesRepo = TimerPreferencesRepository(app)
        Log.d(TAG, "TimerEngine initialized with application context")

        // Check for active session recovery
        scope.launch {
            try {
                preferencesRepo?.activeSessionFlow?.collect { session ->
                    if (session.status == TimerStatus.Running) {
                        val now = SystemClock.elapsedRealtime()
                        if (now < session.targetEndTime) {
                            Log.d(TAG, "Restoring active running session from DataStore")
                            _snapshot.value = TimerSnapshot(
                                status = TimerStatus.Running,
                                targetEndTime = session.targetEndTime,
                                totalMillis = session.totalMillis,
                                pausedRemainingMillis = 0L,
                                lastSetDurationMillis = session.totalMillis
                            )
                            startHeartbeatTicker()
                        } else {
                            Log.d(TAG, "Restored session has already expired -> transition to Finished")
                            finish()
                        }
                    } else if (session.status == TimerStatus.Paused && session.pausedRemainingMillis > 0L) {
                        Log.d(TAG, "Restoring paused session from DataStore")
                        _snapshot.value = TimerSnapshot(
                            status = TimerStatus.Paused,
                            targetEndTime = 0L,
                            totalMillis = session.totalMillis,
                            pausedRemainingMillis = session.pausedRemainingMillis,
                            lastSetDurationMillis = session.totalMillis
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore session from DataStore", e)
            }
        }
    }

    /**
     * Starts the countdown timer for the given duration.
     * Transitions: IDLE -> RUNNING, PAUSED -> RUNNING, or FINISHED -> RUNNING.
     */
    @Synchronized
    fun start(durationMillis: Long) {
        if (durationMillis <= 0L) {
            Log.w(TAG, "Attempted to start timer with invalid duration: $durationMillis")
            return
        }

        val previous = _snapshot.value
        Log.d(TAG, "State transition: ${previous.status} -> RUNNING (duration = $durationMillis ms)")

        // 1. Cancel previous ticker job and alarm
        cancelTickerJob()
        cancelExactAlarm()

        // 2. Compute target end time
        val now = SystemClock.elapsedRealtime()
        val targetEndTime = now + durationMillis

        // 3. Update snapshot
        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Running,
            targetEndTime = targetEndTime,
            totalMillis = durationMillis,
            pausedRemainingMillis = 0L,
            lastSetDurationMillis = durationMillis
        )

        // 4. Schedule exact alarm for Doze mode
        scheduleExactAlarm(targetEndTime)

        // 5. Persist to DataStore
        scope.launch {
            preferencesRepo?.saveActiveSession(
                status = TimerStatus.Running,
                targetEndTime = targetEndTime,
                totalMillis = durationMillis,
                pausedRemainingMillis = 0L
            )
        }

        // 6. Notify Foreground Service and start 1Hz heartbeat
        appContext?.let { TimerService.commandStart(it, durationMillis) }
        startHeartbeatTicker()
    }

    /**
     * Pauses the active countdown.
     * Transition: RUNNING -> PAUSED.
     */
    @Synchronized
    fun pause() {
        val current = _snapshot.value
        if (current.status != TimerStatus.Running) {
            Log.w(TAG, "Cannot pause when not Running (current: ${current.status})")
            return
        }

        val now = SystemClock.elapsedRealtime()
        val remaining = maxOf(0L, current.targetEndTime - now)
        Log.d(TAG, "State transition: RUNNING -> PAUSED (remaining = $remaining ms)")

        cancelTickerJob()
        cancelExactAlarm()

        _snapshot.value = current.copy(
            status = TimerStatus.Paused,
            targetEndTime = 0L,
            pausedRemainingMillis = remaining
        )

        scope.launch {
            preferencesRepo?.saveActiveSession(
                status = TimerStatus.Paused,
                targetEndTime = 0L,
                totalMillis = current.totalMillis,
                pausedRemainingMillis = remaining
            )
        }

        appContext?.let { TimerService.commandPause(it) }
    }

    /**
     * Resumes the countdown from paused state.
     * Transition: PAUSED -> RUNNING.
     */
    @Synchronized
    fun resume() {
        val current = _snapshot.value
        if (current.status != TimerStatus.Paused) {
            Log.w(TAG, "Cannot resume when not Paused (current: ${current.status})")
            return
        }
        if (current.pausedRemainingMillis <= 0L) {
            finish()
            return
        }

        val now = SystemClock.elapsedRealtime()
        val targetEndTime = now + current.pausedRemainingMillis
        Log.d(TAG, "State transition: PAUSED -> RUNNING (remaining = ${current.pausedRemainingMillis} ms)")

        _snapshot.value = current.copy(
            status = TimerStatus.Running,
            targetEndTime = targetEndTime,
            pausedRemainingMillis = 0L
        )

        scheduleExactAlarm(targetEndTime)

        scope.launch {
            preferencesRepo?.saveActiveSession(
                status = TimerStatus.Running,
                targetEndTime = targetEndTime,
                totalMillis = current.totalMillis,
                pausedRemainingMillis = 0L
            )
        }

        appContext?.let { TimerService.commandResume(it) }
        startHeartbeatTicker()
    }

    /**
     * Resets the timer back to Idle state.
     * Transitions: ANY -> IDLE.
     */
    @Synchronized
    fun reset() {
        val previous = _snapshot.value
        Log.d(TAG, "State transition: ${previous.status} -> IDLE (Reset)")

        cancelTickerJob()
        cancelExactAlarm()

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Idle,
            targetEndTime = 0L,
            totalMillis = 0L,
            pausedRemainingMillis = 0L,
            lastSetDurationMillis = previous.lastSetDurationMillis
        )

        scope.launch {
            preferencesRepo?.clearActiveSession()
        }

        appContext?.let { TimerService.commandStop(it) }
    }

    /**
     * Dismisses the finished state and resets back to Idle.
     * Transitions: FINISHED -> IDLE.
     */
    @Synchronized
    fun dismiss() {
        val previous = _snapshot.value
        Log.d(TAG, "State transition: ${previous.status} -> IDLE (Dismiss)")

        cancelTickerJob()
        cancelExactAlarm()

        _snapshot.value = TimerSnapshot(
            status = TimerStatus.Idle,
            targetEndTime = 0L,
            totalMillis = 0L,
            pausedRemainingMillis = 0L,
            lastSetDurationMillis = previous.lastSetDurationMillis
        )

        scope.launch {
            preferencesRepo?.clearActiveSession()
        }

        appContext?.let { TimerService.commandStop(it) }
    }

    /**
     * Marks the timer as finished (target end time reached).
     * Transition: RUNNING -> FINISHED.
     */
    @Synchronized
    fun finish() {
        val current = _snapshot.value
        if (current.status == TimerStatus.Finished) return
        Log.d(TAG, "State transition: ${current.status} -> FINISHED")

        cancelTickerJob()
        cancelExactAlarm()

        _snapshot.value = current.copy(
            status = TimerStatus.Finished,
            targetEndTime = 0L,
            pausedRemainingMillis = 0L
        )

        scope.launch {
            preferencesRepo?.saveActiveSession(
                status = TimerStatus.Finished,
                targetEndTime = 0L,
                totalMillis = current.totalMillis,
                pausedRemainingMillis = 0L
            )
        }

        appContext?.let { TimerService.commandFinish(it) }
    }

    private fun cancelTickerJob() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun startHeartbeatTicker() {
        cancelTickerJob()
        tickerJob = scope.launch {
            while (isActive) {
                val current = _snapshot.value
                if (current.status != TimerStatus.Running) break

                val now = SystemClock.elapsedRealtime()
                if (now >= current.targetEndTime) {
                    finish()
                    break
                }
                // 1Hz logic tick solely for service status bar notification sync
                delay(1000L)
            }
        }
    }

    private fun scheduleExactAlarm(targetEndTime: Long) {
        try {
            val ctx = appContext ?: return
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(ctx, TimerService::class.java).apply {
                action = TimerService.ACTION_EXACT_ALARM_TRIGGER
            }
            val pendingIntent = PendingIntent.getService(
                ctx,
                REQUEST_CODE_EXACT_ALARM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        targetEndTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        targetEndTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    targetEndTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Exact alarm scheduled for $targetEndTime")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to schedule exact alarm", e)
        }
    }

    private fun cancelExactAlarm() {
        try {
            val ctx = appContext ?: return
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(ctx, TimerService::class.java).apply {
                action = TimerService.ACTION_EXACT_ALARM_TRIGGER
            }
            val pendingIntent = PendingIntent.getService(
                ctx,
                REQUEST_CODE_EXACT_ALARM,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Exact alarm cancelled")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cancel exact alarm", e)
        }
    }

    private const val REQUEST_CODE_EXACT_ALARM = 901
}
