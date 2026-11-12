package com.devvardhan.value.core

import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.AppScreen
import com.devvardhan.value.model.TimerStatus
import kotlin.math.abs

/**
 * Pure decision and calculation logic for mode swipe navigation between Timer and Stopwatch.
 * Decoupled from Android/Compose runtime to allow comprehensive JVM unit testing.
 */
object SwipeNavigationLogic {

    const val PAGE_TIMER = 0
    const val PAGE_STOPWATCH = 1
    const val TOTAL_PAGES = 2
    const val DEFAULT_POSITIONAL_THRESHOLD = 0.30f
    const val DEFAULT_VELOCITY_THRESHOLD_PX = 1000f

    /**
     * Determines whether the horizontal swipe gesture between Timer and Stopwatch is allowed.
     * Blocked on:
     * - Settings screen and its sub-pages (Sound)
     * - Style galleries (first launch and edit mode)
     * - Fullscreen modes (Timer and Stopwatch)
     * - Finished alarm screen
     */
    fun shouldAllowSwipe(
        screen: AppScreen,
        isFullscreen: Boolean,
        timerStatus: TimerStatus,
        hasChosenStyle: Boolean = true
    ): Boolean {
        if (!hasChosenStyle) return false
        if (screen != AppScreen.MAIN) return false
        if (isFullscreen) return false
        if (timerStatus == TimerStatus.Finished) return false
        return true
    }

    /**
     * Maps pager page index (0 or 1) to [AppMode].
     */
    fun pageToMode(page: Int): AppMode {
        return when (page) {
            PAGE_STOPWATCH -> AppMode.STOPWATCH
            else -> AppMode.TIMER
        }
    }

    /**
     * Maps [AppMode] to pager page index (0 or 1).
     */
    fun modeToPage(mode: AppMode): Int {
        return when (mode) {
            AppMode.STOPWATCH -> PAGE_STOPWATCH
            AppMode.TIMER -> PAGE_TIMER
        }
    }

    /**
     * Computes the continuous progress fraction from 0.0f (Timer) to 1.0f (Stopwatch).
     * [currentPage] is 0 or 1.
     * [offsetFraction] is in range [-0.5f, 0.5f].
     */
    fun calculatePillProgress(currentPage: Int, offsetFraction: Float): Float {
        return (currentPage + offsetFraction).coerceIn(0f, 1f)
    }

    /**
     * Pure settle decision logic:
     * Determines which page the pager should settle on based on drag distance fraction
     * and fling velocity.
     *
     * @param currentPage Current page index before gesture (0 or 1).
     * @param dragOffsetFraction Fractional distance dragged relative to currentPage:
     *        positive means dragged towards next page (Stopwatch),
     *        negative means dragged towards previous page (Timer).
     * @param flingVelocityPx Velocity of the fling in pixels per second.
     *        positive means flinging towards next page (finger moved left),
     *        negative means flinging towards previous page (finger moved right).
     * @param positionalThreshold Minimum fraction of width to trigger a page switch without fling (default 30%).
     * @param velocityThresholdPx Minimum velocity to trigger a switch regardless of distance.
     * @return Target settled page (0 or 1).
     */
    fun decideSettleTarget(
        currentPage: Int,
        dragOffsetFraction: Float,
        flingVelocityPx: Float = 0f,
        positionalThreshold: Float = DEFAULT_POSITIONAL_THRESHOLD,
        velocityThresholdPx: Float = DEFAULT_VELOCITY_THRESHOLD_PX
    ): Int {
        // High-velocity fling override
        if (abs(flingVelocityPx) >= velocityThresholdPx) {
            return if (flingVelocityPx > 0f) {
                // Fling towards next page (Stopwatch)
                (currentPage + 1).coerceAtMost(PAGE_STOPWATCH)
            } else {
                // Fling towards previous page (Timer)
                (currentPage - 1).coerceAtLeast(PAGE_TIMER)
            }
        }

        // Distance threshold rule (30% of page width)
        return when {
            dragOffsetFraction >= positionalThreshold -> {
                (currentPage + 1).coerceAtMost(PAGE_STOPWATCH)
            }
            dragOffsetFraction <= -positionalThreshold -> {
                (currentPage - 1).coerceAtLeast(PAGE_TIMER)
            }
            else -> {
                // Spring back to current page
                currentPage
            }
        }
    }

    /**
     * Calculates the alpha of the Fullscreen button in the header during swiping.
     * Fullscreen button is always present on Stopwatch (alpha 1),
     * but only present on Timer when running or paused.
     * Cross-fades smoothly between modes.
     */
    fun calculateFullscreenButtonAlpha(
        timerHasFullscreen: Boolean,
        stopwatchHasFullscreen: Boolean,
        progress: Float
    ): Float {
        val timerAlpha = if (timerHasFullscreen) 1.0f else 0.0f
        val stopwatchAlpha = if (stopwatchHasFullscreen) 1.0f else 0.0f
        val clampedProgress = progress.coerceIn(0f, 1f)
        return timerAlpha + (stopwatchAlpha - timerAlpha) * clampedProgress
    }
}

