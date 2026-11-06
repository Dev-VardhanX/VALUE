package com.devvardhan.value.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stopwatchDataStore: DataStore<Preferences> by preferencesDataStore(name = "value_stopwatch_prefs")

data class PersistedStopwatchSession(
    val status: StopwatchStatus,
    val startTime: Long,
    val accumulatedMillis: Long,
    val wallClockStartTime: Long,
    val laps: List<LapItem>
)

/**
 * DataStore repository managing top-level AppMode, persistent Stopwatch themes,
 * and active stopwatch sessions to survive process death and handle reboots.
 */
class StopwatchPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val APP_MODE = stringPreferencesKey("app_mode")
        val STOPWATCH_THEME = stringPreferencesKey("stopwatch_theme")

        val SESSION_STATUS = stringPreferencesKey("sw_session_status")
        val SESSION_START_TIME = longPreferencesKey("sw_session_start_time")
        val SESSION_ACCUMULATED = longPreferencesKey("sw_session_accumulated")
        val SESSION_WALL_CLOCK_START = longPreferencesKey("sw_session_wall_clock_start")
        val SESSION_LAPS = stringPreferencesKey("sw_session_laps")
    }

    val appModeFlow: Flow<AppMode> = context.stopwatchDataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.APP_MODE]
        try {
            if (raw != null) AppMode.valueOf(raw) else AppMode.TIMER
        } catch (e: Exception) {
            AppMode.TIMER
        }
    }

    val stopwatchThemeFlow: Flow<TimerTheme> = context.stopwatchDataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.STOPWATCH_THEME]
        try {
            if (raw != null) {
                val theme = TimerTheme.valueOf(raw)
                // Filter out themes not available for stopwatch
                if (theme == TimerTheme.HOURGLASS || theme == TimerTheme.LIQUID_FILL) {
                    TimerTheme.CIRCLE
                } else {
                    theme
                }
            } else {
                TimerTheme.CIRCLE
            }
        } catch (e: Exception) {
            TimerTheme.CIRCLE
        }
    }

    val activeSessionFlow: Flow<PersistedStopwatchSession> = context.stopwatchDataStore.data.map { prefs ->
        val statusName = prefs[PreferencesKeys.SESSION_STATUS] ?: StopwatchStatus.Idle.name
        val status = try {
            StopwatchStatus.valueOf(statusName)
        } catch (e: Exception) {
            StopwatchStatus.Idle
        }
        val lapsString = prefs[PreferencesKeys.SESSION_LAPS]
        PersistedStopwatchSession(
            status = status,
            startTime = prefs[PreferencesKeys.SESSION_START_TIME] ?: 0L,
            accumulatedMillis = prefs[PreferencesKeys.SESSION_ACCUMULATED] ?: 0L,
            wallClockStartTime = prefs[PreferencesKeys.SESSION_WALL_CLOCK_START] ?: 0L,
            laps = deserializeLaps(lapsString)
        )
    }

    suspend fun saveAppMode(mode: AppMode) {
        context.stopwatchDataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_MODE] = mode.name
        }
    }

    suspend fun saveStopwatchTheme(theme: TimerTheme) {
        context.stopwatchDataStore.edit { prefs ->
            prefs[PreferencesKeys.STOPWATCH_THEME] = theme.name
        }
    }

    suspend fun saveSession(
        status: StopwatchStatus,
        startTime: Long,
        accumulatedMillis: Long,
        wallClockStartTime: Long,
        laps: List<LapItem>
    ) {
        context.stopwatchDataStore.edit { prefs ->
            prefs[PreferencesKeys.SESSION_STATUS] = status.name
            prefs[PreferencesKeys.SESSION_START_TIME] = startTime
            prefs[PreferencesKeys.SESSION_ACCUMULATED] = accumulatedMillis
            prefs[PreferencesKeys.SESSION_WALL_CLOCK_START] = wallClockStartTime
            prefs[PreferencesKeys.SESSION_LAPS] = serializeLaps(laps)
        }
    }

    suspend fun clearSession() {
        context.stopwatchDataStore.edit { prefs ->
            prefs[PreferencesKeys.SESSION_STATUS] = StopwatchStatus.Idle.name
            prefs[PreferencesKeys.SESSION_START_TIME] = 0L
            prefs[PreferencesKeys.SESSION_ACCUMULATED] = 0L
            prefs[PreferencesKeys.SESSION_WALL_CLOCK_START] = 0L
            prefs[PreferencesKeys.SESSION_LAPS] = ""
        }
    }

    companion object {
        fun serializeLaps(laps: List<LapItem>): String {
            return laps.joinToString(";") { "${it.lapNumber}:${it.lapTimeMillis}:${it.totalTimeMillis}" }
        }

        fun deserializeLaps(raw: String?): List<LapItem> {
            if (raw.isNullOrBlank()) return emptyList()
            return try {
                raw.split(";").mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 3) {
                        LapItem(
                            lapNumber = parts[0].toInt(),
                            lapTimeMillis = parts[1].toLong(),
                            totalTimeMillis = parts[2].toLong()
                        )
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

