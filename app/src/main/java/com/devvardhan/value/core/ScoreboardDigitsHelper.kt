package com.devvardhan.value.core

import java.util.Locale

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
 */
object ScoreboardDigitsHelper {

    fun extract(remainingMillis: Long): ScoreboardDigits {
        val totalSeconds = if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val hStr = String.format(Locale.US, "%02d", hours)
        val mStr = String.format(Locale.US, "%02d", minutes)
        val sStr = String.format(Locale.US, "%02d", seconds)

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
        val totalSeconds = maxOf(0L, elapsedMillis) / 1000L
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val hStr = String.format(Locale.US, "%02d", hours)
        val mStr = String.format(Locale.US, "%02d", minutes)
        val sStr = String.format(Locale.US, "%02d", seconds)

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
