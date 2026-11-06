package com.devvardhan.value.core

import java.util.Locale

object TimeUtils {

    /**
     * Formats countdown milliseconds into HH:MM:SS display string with ceiling seconds.
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

    /**
     * Formats stopwatch elapsed time adaptively:
     * - Under 1 hour: MM:SS.cc (e.g. "00:00.00" up to "59:59.99")
     * - 1 hour and above: H:MM:SS.cc (e.g. "1:00:00.00")
     */
    fun formatStopwatchAdaptive(elapsedMillis: Long): String {
        val safeElapsed = maxOf(0L, elapsedMillis)
        val centis = (safeElapsed % 1000L) / 10L
        val totalSeconds = safeElapsed / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d:%02d.%02d", hours, minutes, seconds, centis)
        } else {
            String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
        }
    }

    /**
     * Formats only the primary unit text (excluding centiseconds):
     * - Under 1 hour: "MM:SS" (e.g. "00:00")
     * - 1 hour and above: "H:MM:SS" (e.g. "1:00:00")
     */
    fun formatStopwatchMainUnits(elapsedMillis: Long): String {
        val safeElapsed = maxOf(0L, elapsedMillis)
        val totalSeconds = safeElapsed / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats the two-digit centiseconds component (00..99).
     */
    fun formatStopwatchCentiseconds(elapsedMillis: Long): String {
        val safeElapsed = maxOf(0L, elapsedMillis)
        val centis = (safeElapsed % 1000L) / 10L
        return String.format(Locale.US, "%02d", centis)
    }

    /**
     * Formats full 8-digit + 2 centiseconds representation: "HH:MM:SS.cc".
     */
    fun formatStopwatchFull(elapsedMillis: Long): String {
        val safeElapsed = maxOf(0L, elapsedMillis)
        val centis = (safeElapsed % 1000L) / 10L
        val totalSeconds = safeElapsed / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, centis)
    }

    /**
     * Formats lap split/total time with clean centiseconds resolution.
     */
    fun formatLapTime(millis: Long): String {
        return formatStopwatchAdaptive(millis)
    }
}
