package com.android.value.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.value.core.ThemePreferencesRepository
import com.android.value.core.TimerEngine
import com.android.value.model.TimerStatus
import com.android.value.model.TimerTheme
import com.android.value.model.TimerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating timer state and theme preferences for the UI,
 * surviving configuration changes and rotations.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemePreferencesRepository(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        // Observe timer engine changes
        viewModelScope.launch {
            TimerEngine.snapshot.collectLatest { snapshot ->
                _uiState.update { current ->
                    current.copy(
                        status = snapshot.status,
                        remainingMillis = snapshot.remainingMillis,
                        totalMillis = snapshot.totalMillis,
                        // Exit fullscreen automatically when finished
                        isFullscreen = if (snapshot.status == TimerStatus.Finished) false else current.isFullscreen
                    )
                }
            }
        }

        // Observe persisted theme changes
        viewModelScope.launch {
            themeRepository.selectedThemeFlow.collectLatest { savedTheme ->
                _uiState.update { it.copy(theme = savedTheme) }
            }
        }
    }

    fun setTheme(theme: TimerTheme) {
        viewModelScope.launch {
            _uiState.update { it.copy(theme = theme) }
            themeRepository.setTheme(theme)
        }
    }

    fun cycleNextTheme() {
        val nextTheme = _uiState.value.theme.next()
        setTheme(nextTheme)
    }

    fun setHours(hours: Int) {
        _uiState.update { it.copy(selectedHours = hours.coerceIn(0, 23)) }
    }

    fun setMinutes(minutes: Int) {
        _uiState.update { it.copy(selectedMinutes = minutes.coerceIn(0, 59)) }
    }

    fun setSeconds(seconds: Int) {
        _uiState.update { it.copy(selectedSeconds = seconds.coerceIn(0, 59)) }
    }

    fun startTimer() {
        val currentState = _uiState.value
        val totalDurationMillis = currentState.totalPickerMillis
        if (totalDurationMillis > 0L) {
            TimerEngine.start(totalDurationMillis)
        }
    }

    fun pauseTimer() {
        TimerEngine.pause()
    }

    fun resumeTimer() {
        TimerEngine.resume()
    }

    fun resetTimer() {
        _uiState.update { it.copy(isFullscreen = false) }
        TimerEngine.reset()
    }

    fun dismissFinished() {
        _uiState.update { it.copy(isFullscreen = false) }
        TimerEngine.dismiss()
    }

    fun setFullscreen(enabled: Boolean) {
        _uiState.update { it.copy(isFullscreen = enabled) }
    }
}
