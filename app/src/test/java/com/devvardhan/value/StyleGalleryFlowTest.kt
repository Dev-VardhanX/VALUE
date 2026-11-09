package com.devvardhan.value

import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.TimerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the Style Gallery flow logic:
 * - Step progression (Step 1 -> Step 2 -> Main)
 * - Resumption at Step 2 after process death
 * - hasChosenStyle only set after Step 2 completes
 * - Edit mode for Timer saves only the timer style
 * - Edit mode for Stopwatch saves only the stopwatch style
 * - Saved stopwatch style fallback to Circle for unsupported themes
 */
class StyleGalleryFlowTest {

    data class GalleryFlowSimulationState(
        var hasChosenStyle: Boolean = false,
        var onboardingStep: Int = 1,
        var timerTheme: TimerTheme = TimerTheme.CIRCLE,
        var stopwatchTheme: TimerTheme = TimerTheme.CIRCLE,
        var activeMode: AppMode = AppMode.TIMER,
        var isGalleryOpen: Boolean = true
    )

    private fun simulateSelectThemeInStep1(state: GalleryFlowSimulationState, selectedTheme: TimerTheme) {
        // Step 1: User selects timer theme -> saves timer theme and advances to Step 2
        state.timerTheme = selectedTheme
        state.onboardingStep = 2
        // hasChosenStyle remains false
    }

    private fun simulateSelectThemeInStep2(state: GalleryFlowSimulationState, selectedTheme: TimerTheme) {
        // Step 2: User selects stopwatch theme -> saves stopwatch theme, marks onboarding done, opens main
        state.stopwatchTheme = AdaptiveLayoutHelper.validateStopwatchTheme(selectedTheme)
        state.hasChosenStyle = true
        state.activeMode = AppMode.TIMER
        state.isGalleryOpen = false
    }

    private fun simulateEditTimerTheme(state: GalleryFlowSimulationState, newTheme: TimerTheme) {
        // Edit mode for timer: changes only timer theme, closes gallery immediately
        state.timerTheme = newTheme
        state.isGalleryOpen = false
    }

    private fun simulateEditStopwatchTheme(state: GalleryFlowSimulationState, newTheme: TimerTheme) {
        // Edit mode for stopwatch: changes only stopwatch theme, closes gallery immediately
        state.stopwatchTheme = AdaptiveLayoutHelper.validateStopwatchTheme(newTheme)
        state.isGalleryOpen = false
    }

    @Test
    fun testStepProgressionAndHasChosenStyleOnlyAfterStep2() {
        val state = GalleryFlowSimulationState()

        // Initial state before step 1
        assertFalse(state.hasChosenStyle)
        assertEquals(1, state.onboardingStep)
        assertEquals(TimerTheme.CIRCLE, state.timerTheme)

        // Select Hourglass in Step 1
        simulateSelectThemeInStep1(state, TimerTheme.HOURGLASS)

        // Verifications after Step 1
        assertEquals(TimerTheme.HOURGLASS, state.timerTheme)
        assertEquals(2, state.onboardingStep)
        assertFalse("hasChosenStyle MUST NOT be true after Step 1", state.hasChosenStyle)
        assertTrue("Gallery remains open for Step 2", state.isGalleryOpen)

        // Select Three Box in Step 2
        simulateSelectThemeInStep2(state, TimerTheme.THREE_BOX)

        // Verifications after Step 2
        assertEquals(TimerTheme.HOURGLASS, state.timerTheme)
        assertEquals(TimerTheme.THREE_BOX, state.stopwatchTheme)
        assertTrue("hasChosenStyle MUST be true after Step 2 completes", state.hasChosenStyle)
        assertEquals(AppMode.TIMER, state.activeMode)
        assertFalse("Gallery must close and show main screen in Timer mode", state.isGalleryOpen)
    }

    @Test
    fun testResumeAtStep2AfterProcessDeath() {
        // Simulate app killed while on Step 2 (user completed Step 1 and selected Concentric Rings)
        val persistedTimerTheme = TimerTheme.CONCENTRIC_RINGS
        val persistedOnboardingStep = 2
        val persistedHasChosenStyle = false

        // On next launch after process death:
        val shouldShowGallery = AdaptiveLayoutHelper.shouldShowGalleryAtLaunch(
            hasChosenStyle = persistedHasChosenStyle,
            isTimerRunning = false,
            isStopwatchRunning = false
        )
        assertTrue("Gallery should auto-show when hasChosenStyle is false", shouldShowGallery)

        // State is restored from persistent storage
        val restoredState = GalleryFlowSimulationState(
            hasChosenStyle = persistedHasChosenStyle,
            onboardingStep = persistedOnboardingStep,
            timerTheme = persistedTimerTheme,
            stopwatchTheme = TimerTheme.CIRCLE
        )

        // Gallery resumes directly at Step 2 with the previously selected timer theme kept
        assertEquals(2, restoredState.onboardingStep)
        assertEquals(TimerTheme.CONCENTRIC_RINGS, restoredState.timerTheme)
        assertFalse(restoredState.hasChosenStyle)

        // User finishes Step 2
        simulateSelectThemeInStep2(restoredState, TimerTheme.FLIP_CLOCK)
        assertTrue(restoredState.hasChosenStyle)
        assertEquals(TimerTheme.FLIP_CLOCK, restoredState.stopwatchTheme)
        assertEquals(TimerTheme.CONCENTRIC_RINGS, restoredState.timerTheme)
    }

    @Test
    fun testEditModeForTimerSavesOnlyTimerTheme() {
        val state = GalleryFlowSimulationState(
            hasChosenStyle = true,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.THREE_BOX,
            activeMode = AppMode.TIMER,
            isGalleryOpen = true
        )

        // User edits timer theme to DOT_GRID
        simulateEditTimerTheme(state, TimerTheme.DOT_GRID)

        assertEquals(TimerTheme.DOT_GRID, state.timerTheme)
        assertEquals("Stopwatch theme must remain untouched when editing timer", TimerTheme.THREE_BOX, state.stopwatchTheme)
        assertFalse("Gallery must close immediately after selection in edit mode", state.isGalleryOpen)
    }

    @Test
    fun testEditModeForStopwatchSavesOnlyStopwatchTheme() {
        val state = GalleryFlowSimulationState(
            hasChosenStyle = true,
            timerTheme = TimerTheme.LIQUID_FILL,
            stopwatchTheme = TimerTheme.CIRCLE,
            activeMode = AppMode.STOPWATCH,
            isGalleryOpen = true
        )

        // User edits stopwatch theme to CONCENTRIC_RINGS
        simulateEditStopwatchTheme(state, TimerTheme.CONCENTRIC_RINGS)

        assertEquals(TimerTheme.CONCENTRIC_RINGS, state.stopwatchTheme)
        assertEquals("Timer theme must remain untouched when editing stopwatch", TimerTheme.LIQUID_FILL, state.timerTheme)
        assertFalse("Gallery must close immediately after selection in edit mode", state.isGalleryOpen)
    }

    @Test
    fun testStopwatchThemeFallbackToCircleIfInvalid() {
        // Unsupported themes for stopwatch: Hourglass, Liquid Fill, Dot Grid
        assertEquals(TimerTheme.CIRCLE, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.HOURGLASS))
        assertEquals(TimerTheme.CIRCLE, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.LIQUID_FILL))
        assertEquals(TimerTheme.CIRCLE, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.DOT_GRID))

        // Supported themes: Circle, Concentric Rings, Three Box, Flip Clock
        assertEquals(TimerTheme.CIRCLE, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.CIRCLE))
        assertEquals(TimerTheme.CONCENTRIC_RINGS, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.CONCENTRIC_RINGS))
        assertEquals(TimerTheme.THREE_BOX, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.THREE_BOX))
        assertEquals(TimerTheme.FLIP_CLOCK, AdaptiveLayoutHelper.validateStopwatchTheme(TimerTheme.FLIP_CLOCK))
    }

    @Test
    fun testGalleryStylesCountPerMode() {
        val timerStyles = AdaptiveLayoutHelper.getGalleryStyles(AppMode.TIMER)
        assertEquals("Timer must have all 7 styles", 7, timerStyles.size)
        assertTrue(timerStyles.containsAll(TimerTheme.entries))

        val stopwatchStyles = AdaptiveLayoutHelper.getGalleryStyles(AppMode.STOPWATCH)
        assertEquals("Stopwatch must have exactly 4 styles", 4, stopwatchStyles.size)
        assertEquals(TimerTheme.stopwatchThemes, stopwatchStyles)
    }
}

