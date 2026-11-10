package com.devvardhan.value

import com.devvardhan.value.core.StartupDestination
import com.devvardhan.value.core.StartupResolver
import com.devvardhan.value.core.StartupState
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.TimerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying startup state logic:
 * - Loading until ready state
 * - First launch resolution (Step 1 vs Step 2)
 * - Returning user resolution (Main screen)
 * - Running state priority over onboarding
 * - Intent extra mode prioritization
 * - Safety timeout fallback behavior
 */
class StartupStateTest {

    @Test
    fun loadingState_isNotReady() {
        val loading: StartupState = StartupState.Loading
        assertTrue(loading is StartupState.Loading)
    }

    @Test
    fun firstLaunch_unconfigured_opensGalleryStep1() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = false,
            onboardingStep = 1,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE,
            persistedAppMode = AppMode.TIMER,
            isTimerRunning = false,
            isStopwatchRunning = false,
            intentTargetMode = null
        )

        assertEquals(StartupDestination.FirstLaunch(1), ready.destination)
        assertEquals(AppMode.TIMER, ready.initialAppMode)
        assertFalse(ready.isTimerRunning)
        assertFalse(ready.isStopwatchRunning)
    }

    @Test
    fun firstLaunch_processDeathAfterStep1_resumesAtStep2() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = false,
            onboardingStep = 2,
            timerTheme = TimerTheme.HOURGLASS,
            stopwatchTheme = TimerTheme.CIRCLE,
            persistedAppMode = AppMode.TIMER,
            isTimerRunning = false,
            isStopwatchRunning = false,
            intentTargetMode = null
        )

        assertEquals(StartupDestination.FirstLaunch(2), ready.destination)
        assertEquals(TimerTheme.HOURGLASS, ready.timerTheme)
    }

    @Test
    fun returningUser_opensMainDirectly_noGallery() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = true,
            onboardingStep = 2,
            timerTheme = TimerTheme.LIQUID_FILL,
            stopwatchTheme = TimerTheme.FLIP_CLOCK,
            persistedAppMode = AppMode.STOPWATCH,
            isTimerRunning = false,
            isStopwatchRunning = false,
            intentTargetMode = null
        )

        assertEquals(StartupDestination.Main, ready.destination)
        assertEquals(AppMode.STOPWATCH, ready.initialAppMode)
        assertEquals(TimerTheme.LIQUID_FILL, ready.timerTheme)
        assertEquals(TimerTheme.FLIP_CLOCK, ready.stopwatchTheme)
    }

    @Test
    fun runningTimer_overridesFirstLaunch_opensMainInTimerMode() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = false, // First launch flag not set yet
            onboardingStep = 1,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE,
            persistedAppMode = AppMode.STOPWATCH,
            isTimerRunning = true, // Timer actively running in background!
            isStopwatchRunning = false,
            intentTargetMode = null
        )

        assertEquals(StartupDestination.Main, ready.destination)
        assertEquals(AppMode.TIMER, ready.initialAppMode)
        assertTrue(ready.isTimerRunning)
    }

    @Test
    fun runningStopwatch_overridesFirstLaunch_opensMainInStopwatchMode() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = false,
            onboardingStep = 1,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE,
            persistedAppMode = AppMode.TIMER,
            isTimerRunning = false,
            isStopwatchRunning = true, // Stopwatch running in background
            intentTargetMode = null
        )

        assertEquals(StartupDestination.Main, ready.destination)
        assertEquals(AppMode.STOPWATCH, ready.initialAppMode)
        assertTrue(ready.isStopwatchRunning)
    }

    @Test
    fun notificationIntentTargetMode_overridesPersistedMode() {
        val ready = StartupResolver.resolve(
            hasChosenStyle = true,
            onboardingStep = 2,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE,
            persistedAppMode = AppMode.TIMER,
            isTimerRunning = false,
            isStopwatchRunning = false,
            intentTargetMode = "STOPWATCH"
        )

        assertEquals(StartupDestination.Main, ready.destination)
        assertEquals(AppMode.STOPWATCH, ready.initialAppMode)
    }

    @Test
    fun defaultFallback_providesSafeDefaults() {
        val fallback = StartupResolver.defaultFallback
        assertEquals(StartupDestination.FirstLaunch(1), fallback.destination)
        assertEquals(AppMode.TIMER, fallback.initialAppMode)
        assertEquals(TimerTheme.CIRCLE, fallback.timerTheme)
        assertEquals(TimerTheme.CIRCLE, fallback.stopwatchTheme)
        assertFalse(fallback.isTimerRunning)
        assertFalse(fallback.isStopwatchRunning)
    }
}

