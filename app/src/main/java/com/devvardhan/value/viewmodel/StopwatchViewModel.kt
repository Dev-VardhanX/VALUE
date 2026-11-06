package com.devvardhan.value.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devvardhan.value.core.StopwatchEngine
import com.devvardhan.value.core.StopwatchPreferencesRepository
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.StopwatchUiState
import com.devvardhan.value.model.TimerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating stopwatch state, lap recordings, theme preferences, and fullscreen.
 * Does not emit state every frame; high-frequency visual updates are handled by frame clock.
 */
class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepo = StopwatchPreferencesRepository(application)

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    init {
        // Observe StopwatchEngine state transitions
        viewModelScope.launch {
            StopwatchEngine.snapshot.collectLatest { snapshot ->
                _uiState.update { current ->
                    current.copy(
                        status = snapshot.status,
                        startTime = snapshot.startTime,
                        accumulatedMillis = snapshot.accumulatedMillis,
                        laps = snapshot.laps
                    )
                }
            }
        }

        // Observe persistent theme selection for Stopwatch
        viewModelScope.launch {
            preferencesRepo.stopwatchThemeFlow.collectLatest { savedTheme ->
                _uiState.update { it.copy(theme = savedTheme) }
            }
        }
    }

    fun start() {
        StopwatchEngine.start()
    }

    fun pause() {
        StopwatchEngine.pause()
    }

    fun resume() {
        StopwatchEngine.resume()
    }

    fun lap(): LapItem? {
        return StopwatchEngine.lap()
    }

    fun reset() {
        StopwatchEngine.reset()
    }

    fun setTheme(theme: TimerTheme) {
        // Filter out themes not supported by Stopwatch
        val safeTheme = if (theme == TimerTheme.HOURGLASS || theme == TimerTheme.LIQUID_FILL) {
            TimerTheme.CIRCLE
        } else {
            theme
        }
        viewModelScope.launch {
            _uiState.update { it.copy(theme = safeTheme) }
            preferencesRepo.saveStopwatchTheme(safeTheme)
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }
}

