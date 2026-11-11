package com.devvardhan.value.core

data class ScoreboardDigits(
    val hoursTens: Char,
    val hoursOnes: Char,
    val minutesTens: Char,
    val minutesOnes: Char,
    val secondsTens: Char,
    val secondsOnes: Char
)

/**
 * Pure helper for extracting individual scoreboard panel digits.
 * Guarantees holding at 99:59:59 cap, floor seconds for stopwatch elapsed,
 * and ceil seconds for timer countdown.
 */
object ScoreboardDigitsHelper {

    const val MAX_SECONDS: Long = 99 * 3600L + 59 * 60L + 59L // 359,999 seconds (99:59:59)

    fun extract(remainingMillis: Long): ScoreboardDigits {
        val rawSeconds = if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
        val totalSeconds = rawSeconds.coerceAtMost(MAX_SECONDS)
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val hStr = TimeUtils.formatTwoDigits(hours)
        val mStr = TimeUtils.formatTwoDigits(minutes)
        val sStr = TimeUtils.formatTwoDigits(seconds)

        return ScoreboardDigits(
            hoursTens = hStr[0],
            hoursOnes = hStr[1],
            minutesTens = mStr[0],
            minutesOnes = mStr[1],
            secondsTens = sStr[0],
            secondsOnes = sStr[1]
        )
    }

    fun extractElapsed(elapsedMillis: Long): ScoreboardDigits {
        val rawSeconds = maxOf(0L, elapsedMillis) / 1000L
        val totalSeconds = rawSeconds.coerceAtMost(MAX_SECONDS)
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val hStr = TimeUtils.formatTwoDigits(hours)
        val mStr = TimeUtils.formatTwoDigits(minutes)
        val sStr = TimeUtils.formatTwoDigits(seconds)

        return ScoreboardDigits(
            hoursTens = hStr[0],
            hoursOnes = hStr[1],
            minutesTens = mStr[0],
            minutesOnes = mStr[1],
            secondsTens = sStr[0],
            secondsOnes = sStr[1]
        )
    }
}
