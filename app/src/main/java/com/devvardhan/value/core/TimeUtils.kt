package com.devvardhan.value.core

import java.util.Locale

object TimeUtils {

    /**
     * Formats milliseconds into HH:MM:SS display string with ceiling seconds.
     * Prevents jumping to zero before the final second has elapsed.
     */
    fun formatHms(remainingMillis: Long): String {
        if (remainingMillis <= 0L) {
            return "00:00:00"
        }
        val totalSeconds = (remainingMillis + 999L) / 1000L
        return formatHmsFromSeconds(totalSeconds)
    }

    /**
     * Formats total seconds into HH:MM:SS without additional ceiling math.
     */
    fun formatHmsFromSeconds(totalSeconds: Long): String {
        if (totalSeconds <= 0L) {
            return "00:00:00"
        }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Formats two-digit number for pickers (e.g., 0 -> "00", 5 -> "05").
     */
    fun formatTwoDigits(value: Int): String {
        return String.format(Locale.US, "%02d", value)
    }
}
