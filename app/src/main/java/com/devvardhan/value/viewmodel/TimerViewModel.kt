package com.devvardhan.value.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devvardhan.value.core.TimerEngine
import com.devvardhan.value.core.TimerPreferencesRepository
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.model.TimerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating timer state, duration persistence, and theme preferences.
 * Survives configuration changes and process recreations seamlessly.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepo = TimerPreferencesRepository(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        // Observe timer engine transitions
        viewModelScope.launch {
            TimerEngine.snapshot.collectLatest { snapshot ->
                _uiState.update { current ->
                    current.copy(
                        status = snapshot.status,
                        targetEndTime = snapshot.targetEndTime,
                        totalMillis = snapshot.totalMillis,
                        pausedRemainingMillis = snapshot.pausedRemainingMillis,
                        remainingMillis = snapshot.computeRemainingMillis(),
                        isFullscreen = if (snapshot.status == TimerStatus.Finished) false else current.isFullscreen
                    )
                }
            }
        }

        // Observe persistent theme selection
        viewModelScope.launch {
            preferencesRepo.selectedThemeFlow.collectLatest { savedTheme ->
                _uiState.update { it.copy(theme = savedTheme) }
            }
        }

        // Restore last used duration for the picker
        viewModelScope.launch {
            preferencesRepo.lastSetDurationFlow.collectLatest { duration ->
                _uiState.update { current ->
                    if (current.status == TimerStatus.Idle) {
                        current.copy(
                            selectedHours = duration.hours,
                            selectedMinutes = duration.minutes,
                            selectedSeconds = duration.seconds
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    val hasChosenStyleFlow = preferencesRepo.hasChosenStyleFlow
    val onboardingStepFlow = preferencesRepo.onboardingStepFlow

    fun setHasChosenStyle(chosen: Boolean = true) {
        viewModelScope.launch {
            preferencesRepo.setHasChosenStyle(chosen)
        }
    }

    fun setOnboardingStep(step: Int) {
        viewModelScope.launch {
            preferencesRepo.saveOnboardingStep(step)
        }
    }

    fun setTheme(theme: TimerTheme) {
        viewModelScope.launch {
            _uiState.update { it.copy(theme = theme) }
            preferencesRepo.saveTheme(theme)
        }
    }

    fun cycleNextTheme() {
        val nextTheme = _uiState.value.theme.next()
        setTheme(nextTheme)
    }

    fun setHours(hours: Int) {
        val clamped = hours.coerceIn(0, 99)
        _uiState.update { it.copy(selectedHours = clamped) }
        persistDuration()
    }

    fun setMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(0, 59)
        _uiState.update { it.copy(selectedMinutes = clamped) }
        persistDuration()
    }

    fun setSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(0, 59)
        _uiState.update { it.copy(selectedSeconds = clamped) }
        persistDuration()
    }

    private fun persistDuration() {
        val s = _uiState.value
        viewModelScope.launch {
            preferencesRepo.saveLastDuration(s.selectedHours, s.selectedMinutes, s.selectedSeconds)
        }
    }

    fun startTimer() {
        val state = _uiState.value
        val total = state.totalPickerMillis
        if (total > 0L) {
            persistDuration()
            TimerEngine.start(total)
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
