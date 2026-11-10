package com.devvardhan.value.core

import android.os.SystemClock
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

sealed interface StartupDestination {
    data class FirstLaunch(val step: Int) : StartupDestination
    data object Main : StartupDestination
}

sealed interface StartupState {
    data object Loading : StartupState
    data class Ready(
        val destination: StartupDestination,
        val initialAppMode: AppMode,
        val timerTheme: TimerTheme,
        val stopwatchTheme: TimerTheme,
        val isTimerRunning: Boolean,
        val isStopwatchRunning: Boolean
    ) : StartupState {
        val isReady: Boolean get() = true
    }
}

object StartupResolver {
    fun resolve(
        hasChosenStyle: Boolean,
        onboardingStep: Int,
        timerTheme: TimerTheme,
        stopwatchTheme: TimerTheme,
        persistedAppMode: AppMode,
        isTimerRunning: Boolean,
        isStopwatchRunning: Boolean,
        intentTargetMode: String?
    ): StartupState.Ready {
        val effectiveAppMode = when {
            intentTargetMode == "STOPWATCH" -> AppMode.STOPWATCH
            intentTargetMode == "TIMER" -> AppMode.TIMER
            isStopwatchRunning && !isTimerRunning -> AppMode.STOPWATCH
            isTimerRunning && !isStopwatchRunning -> AppMode.TIMER
            else -> persistedAppMode
        }

        val shouldShowGallery = AdaptiveLayoutHelper.shouldShowGalleryAtLaunch(
            hasChosenStyle = hasChosenStyle,
            isTimerRunning = isTimerRunning,
            isStopwatchRunning = isStopwatchRunning
        )

        val destination = if (shouldShowGallery) {
            val step = if (onboardingStep in 1..2) onboardingStep else 1
            StartupDestination.FirstLaunch(step)
        } else {
            StartupDestination.Main
        }

        return StartupState.Ready(
            destination = destination,
            initialAppMode = effectiveAppMode,
            timerTheme = timerTheme,
            stopwatchTheme = stopwatchTheme,
            isTimerRunning = isTimerRunning,
            isStopwatchRunning = isStopwatchRunning
        )
    }

    val defaultFallback: StartupState.Ready
        get() = StartupState.Ready(
            destination = StartupDestination.FirstLaunch(1),
            initialAppMode = AppMode.TIMER,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE,
            isTimerRunning = false,
            isStopwatchRunning = false
        )
}

suspend fun loadStartupState(
    timerPrefs: TimerPreferencesRepository,
    stopwatchPrefs: StopwatchPreferencesRepository,
    isTimerRunningNow: () -> Boolean = { TimerEngine.snapshot.value.status == TimerStatus.Running },
    isStopwatchRunningNow: () -> Boolean = { StopwatchEngine.snapshot.value.status == StopwatchStatus.Running },
    currentTimeProvider: () -> Long = { SystemClock.elapsedRealtime() },
    intentTargetMode: String? = null,
    timeoutMillis: Long = 1000L
): StartupState.Ready {
    return try {
        withTimeoutOrNull(timeoutMillis) {
            val hasChosen = timerPrefs.hasChosenStyleFlow.first()
            val step = timerPrefs.onboardingStepFlow.first()
            val tTheme = timerPrefs.selectedThemeFlow.first()
            val swTheme = stopwatchPrefs.stopwatchThemeFlow.first()
            val mode = stopwatchPrefs.appModeFlow.first()
            val timerSession = timerPrefs.activeSessionFlow.first()
            val swSession = stopwatchPrefs.activeSessionFlow.first()

            val now = currentTimeProvider()
            val timerRunning = isTimerRunningNow() ||
                (timerSession.status == TimerStatus.Running && now < timerSession.targetEndTime)
            val swRunning = isStopwatchRunningNow() ||
                (swSession.status == StopwatchStatus.Running)

            StartupResolver.resolve(
                hasChosenStyle = hasChosen,
                onboardingStep = step,
                timerTheme = tTheme,
                stopwatchTheme = swTheme,
                persistedAppMode = mode,
                isTimerRunning = timerRunning,
                isStopwatchRunning = swRunning,
                intentTargetMode = intentTargetMode
            )
        } ?: StartupResolver.defaultFallback
    } catch (e: Exception) {
        StartupResolver.defaultFallback
    }
}

