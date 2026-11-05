package com.devvardhan.value.core

import kotlin.math.ceil

/**
 * Pure mathematical helpers for calculating smooth 60fps unit and overall progress fractions.
 */
object ThemeFractionCalculator {

    const val MILLIS_PER_SECOND = 1_000L
    const val MILLIS_PER_MINUTE = 60_000L
    const val MILLIS_PER_HOUR = 3_600_000L

    /**
     * Calculates the overall timer progress fraction between 0.0f (completed) and 1.0f (full duration remaining).
     */
    fun calculateOverallProgress(remainingMillis: Long, totalMillis: Long): Float {
        if (totalMillis <= 0L || remainingMillis <= 0L) return 0f
        return (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Calculates the fraction of the current minute remaining (0.0f to 1.0f).
     * Drains smoothly every 60 seconds.
     */
    fun calculateSecondsFraction(remainingMillis: Long): Float {
        if (remainingMillis <= 0L) return 0f
        val msInMinute = ((remainingMillis - 1L) % MILLIS_PER_MINUTE) + 1L
        return (msInMinute.toFloat() / MILLIS_PER_MINUTE.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Calculates the fraction of the current hour remaining (0.0f to 1.0f).
     * Drains smoothly every 60 minutes.
     */
    fun calculateMinutesFraction(remainingMillis: Long): Float {
        if (remainingMillis <= 0L) return 0f
        val msInHour = ((remainingMillis - 1L) % MILLIS_PER_HOUR) + 1L
        return (msInHour.toFloat() / MILLIS_PER_HOUR.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Calculates the fraction of the total hours remaining (0.0f to 1.0f).
     * If 0 hours were originally set, returns 0.0f.
     */
    fun calculateHoursFraction(remainingMillis: Long, totalMillis: Long): Float {
        if (totalMillis <= 0L || remainingMillis <= 0L) return 0f
        val totalHours = totalMillis / MILLIS_PER_HOUR
        if (totalHours <= 0L) return 0f
        val totalHourMillis = totalHours * MILLIS_PER_HOUR
        return (remainingMillis.toFloat() / totalHourMillis.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Calculates the number of active remaining dots in a grid (0 to totalDots).
     */
    fun calculateDotGridActiveCount(remainingMillis: Long, totalMillis: Long, totalDots: Int = 60): Int {
        if (remainingMillis <= 0L || totalMillis <= 0L) return 0
        val progress = calculateOverallProgress(remainingMillis, totalMillis)
        return ceil(progress * totalDots).toInt().coerceIn(0, totalDots)
    }
}

