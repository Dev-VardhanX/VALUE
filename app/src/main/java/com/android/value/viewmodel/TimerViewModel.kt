package com.android.value.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.value.core.TimerEngine
import com.android.value.model.TimerStatus
import com.android.value.model.TimerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating timer state for the UI, surviving configuration changes and rotations.
 */
class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
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

