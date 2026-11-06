package com.devvardhan.value

import com.devvardhan.value.core.BackAction
import com.devvardhan.value.core.BackPressHandlerLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class BackPressHandlerTest {

    @Test
    fun testFullscreenAlwaysExitsFullscreenOnly() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = true,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.EXIT_FULLSCREEN, action)
    }

    @Test
    fun testGalleryOpenedFromStyleDismissesToMain() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = true,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.DISMISS_GALLERY, action)
    }

    @Test
    fun testGalleryFirstLaunchFinishesActivity() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = true,
            isFirstLaunchGallery = true,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.FINISH_ACTIVITY, action)
    }

    @Test
    fun testMainScreenWithActiveTimerMovesToBack() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = true,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.MOVE_TO_BACK, action)
    }

    @Test
    fun testMainScreenWithActiveStopwatchMovesToBack() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = true,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.MOVE_TO_BACK, action)
    }

    @Test
    fun testMainScreenIdleDoublePressExit() {
        // First press: displays "Press back again to exit" toast/pill
        val (firstAction, firstTime) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 5000L
        )
        assertEquals(BackAction.SHOW_TOAST_AGAIN_TO_EXIT, firstAction)
        assertEquals(5000L, firstTime)

        // Second press within 1.5 seconds (less than 2000ms): FINISH ACTIVITY
        val (secondAction, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = firstTime,
            currentTimeMs = 6500L
        )
        assertEquals(BackAction.FINISH_ACTIVITY, secondAction)

        // Second press after 2.5 seconds (more than 2000ms): prompts again
        val (delayedAction, delayedTime) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = false,
            isFirstLaunchGallery = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = firstTime,
            currentTimeMs = 7600L
        )
        assertEquals(BackAction.SHOW_TOAST_AGAIN_TO_EXIT, delayedAction)
        assertEquals(7600L, delayedTime)
    }
}

