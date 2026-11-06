package com.devvardhan.value.core

/**
 * Pure calculation helpers for visual stopwatch themes.
 */
object StopwatchFractionCalculator {

    private const val MILLIS_PER_SECOND = 1000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MILLIS_PER_HOUR = 3_600_000L
    private const val MILLIS_PER_12_HOURS = 12L * MILLIS_PER_HOUR

    /**
     * Circle theme: ring sweeps once per minute (60 seconds per full cycle).
     */
    fun calculateCircleProgress(elapsedMillis: Long): Float {
        if (elapsedMillis <= 0L) return 0f
        val currentMinuteMillis = elapsedMillis % MILLIS_PER_MINUTE
        return (currentMinuteMillis.toFloat() / MILLIS_PER_MINUTE.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Concentric Rings: inner ring fills up over 60 seconds.
     */
    fun calculateSecondsFraction(elapsedMillis: Long): Float {
        if (elapsedMillis <= 0L) return 0f
        val secMillis = elapsedMillis % MILLIS_PER_MINUTE
        return (secMillis.toFloat() / MILLIS_PER_MINUTE.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Concentric Rings: middle ring fills up over 60 minutes.
     */
    fun calculateMinutesFraction(elapsedMillis: Long): Float {
        if (elapsedMillis <= 0L) return 0f
        val minMillis = elapsedMillis % MILLIS_PER_HOUR
        return (minMillis.toFloat() / MILLIS_PER_HOUR.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Concentric Rings: outer ring fills up over 12 hours.
     */
    fun calculateHoursFraction(elapsedMillis: Long): Float {
        if (elapsedMillis <= 0L) return 0f
        val hrMillis = elapsedMillis % MILLIS_PER_12_HOURS
        return (hrMillis.toFloat() / MILLIS_PER_12_HOURS.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Dot Grid theme: 60 dots, 1 more dot lit per second, resets every minute.
     * Returns count of active dots (0..60).
     */
    fun calculateActiveDots(elapsedMillis: Long): Int {
        if (elapsedMillis <= 0L) return 0
        val secondInMinute = (elapsedMillis / MILLIS_PER_SECOND) % 60L
        return secondInMinute.toInt().coerceIn(0, 60)
    }
}

