package com.android.value.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.value.model.TimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_preferences")

class ThemePreferencesRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("selected_timer_theme")

    val selectedThemeFlow: Flow<TimerTheme> = context.dataStore.data.map { preferences ->
        val savedName = preferences[themeKey]
        if (savedName != null) {
            try {
                TimerTheme.valueOf(savedName)
            } catch (e: IllegalArgumentException) {
                TimerTheme.CIRCLE
            }
        } else {
            TimerTheme.CIRCLE
        }
    }

    suspend fun setTheme(theme: TimerTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
}

