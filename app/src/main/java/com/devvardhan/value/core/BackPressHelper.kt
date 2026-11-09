package com.devvardhan.value.core

/**
 * Resulting actions for back-press navigation.
 */
enum class BackAction {
    EXIT_FULLSCREEN,
    DISMISS_SUBPAGE_TO_SETTINGS,
    DISMISS_SETTINGS_TO_MAIN,
    DISMISS_GALLERY,
    GALLERY_STEP_BACK,
    FINISH_ACTIVITY,
    MOVE_TO_BACK,
    SHOW_TOAST_AGAIN_TO_EXIT
}

/**
 * Pure decision logic for system and gesture back navigation across all navigation levels:
 * Subpage (Gallery in edit mode, Sound) -> Settings -> Main
 */
object BackPressHandlerLogic {

    const val DEFAULT_DOUBLE_BACK_INTERVAL_MS = 2000L

    fun decideBackAction(
        isFullscreen: Boolean,
        isFirstLaunchGallery: Boolean,
        galleryStep: Int = 1,
        isSubpageOpen: Boolean = false,
        isSettingsOpen: Boolean = false,
        isGalleryOpen: Boolean = false,
        isTimerRunningOrPaused: Boolean = false,
        isStopwatchRunningOrPaused: Boolean = false,
        lastBackPressTimeMs: Long = 0L,
        currentTimeMs: Long = 0L,
        doubleBackIntervalMs: Long = DEFAULT_DOUBLE_BACK_INTERVAL_MS
    ): Pair<BackAction, Long> {
        // 1. Fullscreen: back exits fullscreen only
        if (isFullscreen) {
            return BackAction.EXIT_FULLSCREEN to lastBackPressTimeMs
        }

        // 2. First-launch guided gallery:
        //    Step 2: returns to Step 1.
        //    Step 1: exits the app.
        if (isFirstLaunchGallery) {
            return if (galleryStep == 2) {
                BackAction.GALLERY_STEP_BACK to lastBackPressTimeMs
            } else {
                BackAction.FINISH_ACTIVITY to lastBackPressTimeMs
            }
        }

        // 3. Sub-page level (Timer Style Gallery, Stopwatch Style Gallery, Sound screen):
        //    Always goes up exactly one level to Settings.
        if (isSubpageOpen) {
            return BackAction.DISMISS_SUBPAGE_TO_SETTINGS to lastBackPressTimeMs
        }

        // 4. Settings level:
        //    Always goes up to Main screen.
        if (isSettingsOpen) {
            return BackAction.DISMISS_SETTINGS_TO_MAIN to lastBackPressTimeMs
        }

        // Legacy / direct gallery without settings
        if (isGalleryOpen) {
            return BackAction.DISMISS_GALLERY to lastBackPressTimeMs
        }

        // 5. Main Screen:
        //    If timer or stopwatch is running or paused: move app to background
        if (isTimerRunningOrPaused || isStopwatchRunningOrPaused) {
            return BackAction.MOVE_TO_BACK to lastBackPressTimeMs
        }

        // 6. Main Screen Idle: double-press to exit within 2 seconds
        val elapsedSinceLastBack = currentTimeMs - lastBackPressTimeMs
        return if (elapsedSinceLastBack in 0..doubleBackIntervalMs) {
            BackAction.FINISH_ACTIVITY to 0L
        } else {
            BackAction.SHOW_TOAST_AGAIN_TO_EXIT to currentTimeMs
        }
    }
}
