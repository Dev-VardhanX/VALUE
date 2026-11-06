package com.devvardhan.value.core

/**
 * Resulting actions for back-press navigation.
 */
enum class BackAction {
    EXIT_FULLSCREEN,
    DISMISS_GALLERY,
    FINISH_ACTIVITY,
    MOVE_TO_BACK,
    SHOW_TOAST_AGAIN_TO_EXIT
}

/**
 * Pure decision logic for system and gesture back navigation.
 */
object BackPressHandlerLogic {

    const val DEFAULT_DOUBLE_BACK_INTERVAL_MS = 2000L

    fun decideBackAction(
        isFullscreen: Boolean,
        isGalleryOpen: Boolean,
        isFirstLaunchGallery: Boolean,
        isTimerRunningOrPaused: Boolean,
        isStopwatchRunningOrPaused: Boolean,
        lastBackPressTimeMs: Long,
        currentTimeMs: Long,
        doubleBackIntervalMs: Long = DEFAULT_DOUBLE_BACK_INTERVAL_MS
    ): Pair<BackAction, Long> {
        // 1. Fullscreen: back exits fullscreen only
        if (isFullscreen) {
            return BackAction.EXIT_FULLSCREEN to lastBackPressTimeMs
        }

        // 2. Gallery:
        // Opened from Style button: back returns to main screen, never exits app.
        // First launch (no style chosen yet): back finishes activity.
        if (isGalleryOpen) {
            return if (isFirstLaunchGallery) {
                BackAction.FINISH_ACTIVITY to lastBackPressTimeMs
            } else {
                BackAction.DISMISS_GALLERY to lastBackPressTimeMs
            }
        }

        // 3. Main Screen:
        // If timer or stopwatch is running or paused: move app to background (moveTaskToBack)
        if (isTimerRunningOrPaused || isStopwatchRunningOrPaused) {
            return BackAction.MOVE_TO_BACK to lastBackPressTimeMs
        }

        // 4. Main Screen Idle: double-press to exit within 2 seconds
        val elapsedSinceLastBack = currentTimeMs - lastBackPressTimeMs
        return if (elapsedSinceLastBack in 0..doubleBackIntervalMs) {
            BackAction.FINISH_ACTIVITY to 0L
        } else {
            BackAction.SHOW_TOAST_AGAIN_TO_EXIT to currentTimeMs
        }
    }
}

