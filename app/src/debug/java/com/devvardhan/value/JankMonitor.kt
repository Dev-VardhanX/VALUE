package com.devvardhan.value

import android.app.Activity
import android.util.Log
import androidx.metrics.performance.JankStats

object JankMonitor {
    fun setup(activity: Activity) {
        try {
            val jankStats = JankStats.createAndTrack(activity.window) { frameData ->
                if (frameData.isJank) {
                    val durationMs = frameData.frameDurationUiNanos / 1_000_000.0
                    Log.w("JankStats", "Jank frame detected: duration=${String.format(java.util.Locale.US, "%.2f", durationMs)}ms")
                }
            }
            jankStats.isTrackingEnabled = true
            Log.d("JankStats", "JankStats monitoring enabled on window")
        } catch (e: Exception) {
            Log.w("JankStats", "Failed to initialize JankStats", e)
        }
    }
}

