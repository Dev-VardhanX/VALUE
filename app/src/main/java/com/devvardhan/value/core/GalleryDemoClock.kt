package com.devvardhan.value.core

/**
 * Deterministic demo clock for Style Gallery previews.
 *
 * Cycle specification:
 * - 0..10,000 ms (10.0s): Active countdown / countup animation.
 * - 10,000..11,000 ms (1.0s): Hold phase (empty for timer, full 10s for stopwatch).
 * - Total cycle length: 11,000 ms (loops back to 0).
 */
object GalleryDemoClock {
    const val DEMO_ACTIVE_MILLIS = 10_000L
    const val DEMO_HOLD_MILLIS = 1_000L
    const val TOTAL_CYCLE_MILLIS = DEMO_ACTIVE_MILLIS + DEMO_HOLD_MILLIS // 11,000 ms

    /**
     * Calculates countdown timer remaining millis at [elapsedMillis].
     * Starts at 10,000 ms (00:00:10), counts down linearly to 0 ms at 10,000 ms,
     * holds at 0 ms until 11,000 ms, then loops seamlessly.
     */
    fun calculateTimerRemaining(elapsedMillis: Long): Long {
        if (elapsedMillis <= 0L) return DEMO_ACTIVE_MILLIS
        val cycle = elapsedMillis % TOTAL_CYCLE_MILLIS
        return if (cycle < DEMO_ACTIVE_MILLIS) {
            DEMO_ACTIVE_MILLIS - cycle
        } else {
            0L
        }
    }

    /**
     * Calculates stopwatch elapsed millis at [elapsedMillis].
     * Starts at 0 ms (00:00:00.00), counts up linearly to 10,000 ms at 10,000 ms,
     * holds at 10,000 ms until 11,000 ms, then loops seamlessly.
     */
    fun calculateStopwatchElapsed(elapsedMillis: Long): Long {
        if (elapsedMillis <= 0L) return 0L
        val cycle = elapsedMillis % TOTAL_CYCLE_MILLIS
        return if (cycle < DEMO_ACTIVE_MILLIS) {
            cycle
        } else {
            DEMO_ACTIVE_MILLIS
        }
    }

    /**
     * Progress fraction for timer (1.0f at t=0 down to 0.0f at t=10s, stays 0.0f during hold).
     */
    fun calculateTimerProgress(elapsedMillis: Long): Float {
        val rem = calculateTimerRemaining(elapsedMillis)
        return (rem.toFloat() / DEMO_ACTIVE_MILLIS.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Progress fraction for stopwatch (0.0f at t=0 up to 1.0f at t=10s, stays 1.0f during hold).
     */
    fun calculateStopwatchProgress(elapsedMillis: Long): Float {
        val el = calculateStopwatchElapsed(elapsedMillis)
        return (el.toFloat() / DEMO_ACTIVE_MILLIS.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Alpha factor (0.6f .. 1.0f) during loop reset to create a smooth crossfade back to start.
     */
    fun calculateLoopAlpha(elapsedMillis: Long): Float {
        if (elapsedMillis <= 0L) return 1f
        val cycle = elapsedMillis % TOTAL_CYCLE_MILLIS
        val resetWindow = 200L // 200ms reset crossfade window around loop point
        return when {
            cycle > TOTAL_CYCLE_MILLIS - resetWindow -> {
                val progress = (cycle - (TOTAL_CYCLE_MILLIS - resetWindow)).toFloat() / resetWindow
                1f - (progress * 0.4f)
            }
            cycle < resetWindow -> {
                val progress = cycle.toFloat() / resetWindow
                0.6f + (progress * 0.4f)
            }
            else -> 1f
        }
    }
}

