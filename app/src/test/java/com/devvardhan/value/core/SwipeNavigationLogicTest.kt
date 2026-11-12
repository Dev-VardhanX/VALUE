package com.devvardhan.value.core

import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.AppScreen
import com.devvardhan.value.model.TimerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeNavigationLogicTest {

    @Test
    fun testShouldAllowSwipe_AllowedInAllMainScreenTimerAndStopwatchStates() {
        // Idle / Set Timer state
        assertTrue(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = false,
                timerStatus = TimerStatus.Idle,
                hasChosenStyle = true
            )
        )

        // Running Timer state
        assertTrue(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = false,
                timerStatus = TimerStatus.Running,
                hasChosenStyle = true
            )
        )

        // Paused Timer state
        assertTrue(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = false,
                timerStatus = TimerStatus.Paused,
                hasChosenStyle = true
            )
        )
    }

    @Test
    fun testShouldAllowSwipe_BlockedOnSpecialScreens() {
        // Blocked on Settings
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.SETTINGS,
                isFullscreen = false,
                timerStatus = TimerStatus.Idle
            )
        )

        // Blocked on Sound
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.SOUND,
                isFullscreen = false,
                timerStatus = TimerStatus.Idle
            )
        )

        // Blocked on Style Gallery
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.GALLERY,
                isFullscreen = false,
                timerStatus = TimerStatus.Idle
            )
        )

        // Blocked when onboarding style is not yet chosen
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = false,
                timerStatus = TimerStatus.Idle,
                hasChosenStyle = false
            )
        )

        // Blocked in Fullscreen mode (both Timer and Stopwatch)
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = true,
                timerStatus = TimerStatus.Running
            )
        )

        // Blocked on Finished alarm screen
        assertFalse(
            SwipeNavigationLogic.shouldAllowSwipe(
                screen = AppScreen.MAIN,
                isFullscreen = false,
                timerStatus = TimerStatus.Finished
            )
        )
    }

    @Test
    fun testPageAndModeMapping_BiDirectional() {
        assertEquals(AppMode.TIMER, SwipeNavigationLogic.pageToMode(SwipeNavigationLogic.PAGE_TIMER))
        assertEquals(AppMode.STOPWATCH, SwipeNavigationLogic.pageToMode(SwipeNavigationLogic.PAGE_STOPWATCH))
        // Unknown defaults to Timer
        assertEquals(AppMode.TIMER, SwipeNavigationLogic.pageToMode(99))

        assertEquals(SwipeNavigationLogic.PAGE_TIMER, SwipeNavigationLogic.modeToPage(AppMode.TIMER))
        assertEquals(SwipeNavigationLogic.PAGE_STOPWATCH, SwipeNavigationLogic.modeToPage(AppMode.STOPWATCH))
    }

    @Test
    fun testCalculatePillProgress_ContinuousAndClamped() {
        assertEquals(0.0f, SwipeNavigationLogic.calculatePillProgress(0, 0.0f), 0.001f)
        assertEquals(0.25f, SwipeNavigationLogic.calculatePillProgress(0, 0.25f), 0.001f)
        assertEquals(0.50f, SwipeNavigationLogic.calculatePillProgress(0, 0.50f), 0.001f)
        assertEquals(0.50f, SwipeNavigationLogic.calculatePillProgress(1, -0.50f), 0.001f)
        assertEquals(0.75f, SwipeNavigationLogic.calculatePillProgress(1, -0.25f), 0.001f)
        assertEquals(1.0f, SwipeNavigationLogic.calculatePillProgress(1, 0.0f), 0.001f)

        // Over-scroll clamps
        assertEquals(0.0f, SwipeNavigationLogic.calculatePillProgress(0, -0.2f), 0.001f)
        assertEquals(1.0f, SwipeNavigationLogic.calculatePillProgress(1, 0.2f), 0.001f)
    }

    @Test
    fun testDecideSettleTarget_PositionalThreshold() {
        // From Page 0 (Timer), drag right-to-left (positive fraction)
        // Under 30% -> spring back to 0
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(0, 0.29f, flingVelocityPx = 0f))
        // Exactly or over 30% -> switch to 1 (Stopwatch)
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(0, 0.30f, flingVelocityPx = 0f))
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(0, 0.50f, flingVelocityPx = 0f))
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(0, 0.90f, flingVelocityPx = 0f))

        // From Page 1 (Stopwatch), drag left-to-right (negative fraction)
        // Under 30% -> spring back to 1
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(1, -0.29f, flingVelocityPx = 0f))
        // Exactly or over 30% -> switch to 0 (Timer)
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(1, -0.30f, flingVelocityPx = 0f))
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(1, -0.50f, flingVelocityPx = 0f))
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(1, -0.80f, flingVelocityPx = 0f))
    }

    @Test
    fun testDecideSettleTarget_FlingVelocityOverride() {
        // Fast fling towards next page overrides low distance
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(0, 0.10f, flingVelocityPx = 1500f))

        // Fast fling towards previous page cancels and springs back even if past 30%
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(0, 0.40f, flingVelocityPx = -1500f))

        // From Stopwatch (1), fast fling left-to-right switches to Timer (0)
        assertEquals(0, SwipeNavigationLogic.decideSettleTarget(1, -0.05f, flingVelocityPx = -1500f))

        // From Stopwatch (1), fast fling right-to-left springs back to Stopwatch (1)
        assertEquals(1, SwipeNavigationLogic.decideSettleTarget(1, -0.40f, flingVelocityPx = 1500f))
    }

    @Test
    fun testFullscreenButtonAlpha_CrossFading() {
        // Scenario A: SetTimer (no fullscreen) <-> Stopwatch (has fullscreen)
        assertEquals(0.0f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(false, true, 0.0f), 0.001f)
        assertEquals(0.5f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(false, true, 0.5f), 0.001f)
        assertEquals(1.0f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(false, true, 1.0f), 0.001f)

        // Scenario B: RunningTimer (has fullscreen) <-> Stopwatch (has fullscreen)
        assertEquals(1.0f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(true, true, 0.0f), 0.001f)
        assertEquals(1.0f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(true, true, 0.5f), 0.001f)
        assertEquals(1.0f, SwipeNavigationLogic.calculateFullscreenButtonAlpha(true, true, 1.0f), 0.001f)
    }
}

