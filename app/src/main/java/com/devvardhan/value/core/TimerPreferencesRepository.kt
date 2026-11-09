package com.devvardhan.value.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "value_timer_prefs")

data class PersistedTimerSession(
    val status: TimerStatus,
    val targetEndTime: Long,
    val totalMillis: Long,
    val pausedRemainingMillis: Long
)

data class LastSetDuration(
    val hours: Int,
    val minutes: Int,
    val seconds: Int
) {
    val totalMillis: Long
        get() = (hours * 3600L + minutes * 60L + seconds) * 1000L
}

/**
 * DataStore repository managing persistent user preferences, last configured timer duration,
 * active timer session state, and first-launch style selection flag.
 */
class TimerPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val HAS_CHOSEN_STYLE = booleanPreferencesKey("has_chosen_style")
        val ONBOARDING_STEP = intPreferencesKey("onboarding_step")
        val LAST_HOURS = intPreferencesKey("last_hours")
        val LAST_MINUTES = intPreferencesKey("last_minutes")
        val LAST_SECONDS = intPreferencesKey("last_seconds")

        val SESSION_STATUS = stringPreferencesKey("session_status")
        val SESSION_TARGET_END_TIME = longPreferencesKey("session_target_end_time")
        val SESSION_TOTAL_MILLIS = longPreferencesKey("session_total_millis")
        val SESSION_PAUSED_REMAINING = longPreferencesKey("session_paused_remaining")
    }

    val hasChosenStyleFlow: Flow<Boolean> = context.timerDataStore.data.map { prefs ->
        prefs[PreferencesKeys.HAS_CHOSEN_STYLE] ?: false
    }

    val onboardingStepFlow: Flow<Int> = context.timerDataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_STEP] ?: 1
    }

    val selectedThemeFlow: Flow<TimerTheme> = context.timerDataStore.data.map { prefs ->
        val name = prefs[PreferencesKeys.SELECTED_THEME]
        if (name != null) {
            try {
                TimerTheme.valueOf(name)
            } catch (e: IllegalArgumentException) {
                TimerTheme.CIRCLE
            }
        } else {
            TimerTheme.CIRCLE
        }
    }

    val lastSetDurationFlow: Flow<LastSetDuration> = context.timerDataStore.data.map { prefs ->
        LastSetDuration(
            hours = prefs[PreferencesKeys.LAST_HOURS] ?: 0,
            minutes = prefs[PreferencesKeys.LAST_MINUTES] ?: 5,
            seconds = prefs[PreferencesKeys.LAST_SECONDS] ?: 0
        )
    }

    val activeSessionFlow: Flow<PersistedTimerSession> = context.timerDataStore.data.map { prefs ->
        val statusName = prefs[PreferencesKeys.SESSION_STATUS] ?: TimerStatus.Idle.name
        val status = try {
            TimerStatus.valueOf(statusName)
        } catch (e: Exception) {
            TimerStatus.Idle
        }
        PersistedTimerSession(
            status = status,
            targetEndTime = prefs[PreferencesKeys.SESSION_TARGET_END_TIME] ?: 0L,
            totalMillis = prefs[PreferencesKeys.SESSION_TOTAL_MILLIS] ?: 0L,
            pausedRemainingMillis = prefs[PreferencesKeys.SESSION_PAUSED_REMAINING] ?: 0L
        )
    }

    suspend fun saveTheme(theme: TimerTheme) {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_THEME] = theme.name
        }
    }

    suspend fun setHasChosenStyle(chosen: Boolean = true) {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.HAS_CHOSEN_STYLE] = chosen
            if (!chosen) {
                prefs[PreferencesKeys.ONBOARDING_STEP] = 1
            }
        }
    }

    suspend fun saveOnboardingStep(step: Int) {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_STEP] = step
        }
    }

    suspend fun saveLastDuration(hours: Int, minutes: Int, seconds: Int) {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_HOURS] = hours
            prefs[PreferencesKeys.LAST_MINUTES] = minutes
            prefs[PreferencesKeys.LAST_SECONDS] = seconds
        }
    }

    suspend fun saveActiveSession(
        status: TimerStatus,
        targetEndTime: Long,
        totalMillis: Long,
        pausedRemainingMillis: Long
    ) {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.SESSION_STATUS] = status.name
            prefs[PreferencesKeys.SESSION_TARGET_END_TIME] = targetEndTime
            prefs[PreferencesKeys.SESSION_TOTAL_MILLIS] = totalMillis
            prefs[PreferencesKeys.SESSION_PAUSED_REMAINING] = pausedRemainingMillis
        }
    }

    suspend fun clearActiveSession() {
        context.timerDataStore.edit { prefs ->
            prefs[PreferencesKeys.SESSION_STATUS] = TimerStatus.Idle.name
            prefs[PreferencesKeys.SESSION_TARGET_END_TIME] = 0L
            prefs[PreferencesKeys.SESSION_TOTAL_MILLIS] = 0L
            prefs[PreferencesKeys.SESSION_PAUSED_REMAINING] = 0L
        }
    }
}

