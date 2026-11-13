package com.devvardhan.value

import com.devvardhan.value.core.BackAction
import com.devvardhan.value.core.BackPressHandlerLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class BackPressHandlerTest {

    @Test
    fun testSubpageDismissesToSettings() {
        // Subpage (Timer Style Gallery edit mode, Stopwatch Style Gallery edit mode, Sound screen, Privacy Policy)
        // returns to Settings screen
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isFirstLaunchGallery = false,
            isSubpageOpen = true,
            isSettingsOpen = false,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.DISMISS_SUBPAGE_TO_SETTINGS, action)
    }

    @Test
    fun testPrivacyPolicySubpageDismissesToSettings() {
        // Privacy Policy subpage back press always returns to Settings screen
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isFirstLaunchGallery = false,
            isSubpageOpen = true,
            isSettingsOpen = false,
            isTimerRunningOrPaused = true,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.DISMISS_SUBPAGE_TO_SETTINGS, action)
    }

    @Test
    fun testSettingsDismissesToMain() {
        // Settings screen back press returns to Main screen
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isFirstLaunchGallery = false,
            isSubpageOpen = false,
            isSettingsOpen = true,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.DISMISS_SETTINGS_TO_MAIN, action)
    }

    @Test
    fun testSettingsDismissesToMainEvenWhenTimerIsRunning() {
        // Settings screen back press always returns to Main screen, even if a timer is running
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isFirstLaunchGallery = false,
            isSubpageOpen = false,
            isSettingsOpen = true,
            isTimerRunningOrPaused = true,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.DISMISS_SETTINGS_TO_MAIN, action)
    }

    @Test
    fun testFullscreenTakesPrecedenceOverSettings() {
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = true,
            isFirstLaunchGallery = false,
            isSubpageOpen = false,
            isSettingsOpen = true,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L
        )
        assertEquals(BackAction.EXIT_FULLSCREEN, action)
    }

    @Test
    fun testGalleryEditModeDismissesToMain() {
        // Edit mode (isFirstLaunchGallery = false) returns to main screen without changing anything
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
    fun testGalleryFirstLaunchStep1ExitsApp() {
        // First launch Step 1 (galleryStep = 1): Back exits the application
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = true,
            isFirstLaunchGallery = true,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L,
            galleryStep = 1
        )
        assertEquals(BackAction.FINISH_ACTIVITY, action)
    }

    @Test
    fun testGalleryFirstLaunchStep2ReturnsToStep1() {
        // First launch Step 2 (galleryStep = 2): Back returns to Step 1
        val (action, _) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = false,
            isGalleryOpen = true,
            isFirstLaunchGallery = true,
            isTimerRunningOrPaused = false,
            isStopwatchRunningOrPaused = false,
            lastBackPressTimeMs = 0L,
            currentTimeMs = 1000L,
            galleryStep = 2
        )
        assertEquals(BackAction.GALLERY_STEP_BACK, action)
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
