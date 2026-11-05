package com.android.value.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Handles playing the alarm sound and vibrating the device when the timer finishes.
 */
class AlarmSoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isAlarmPlaying = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Starts continuous alarm playback and vibration until stopped.
     */
    @Synchronized
    fun startAlarm() {
        if (isAlarmPlaying) return
        isAlarmPlaying = true

        startSound()
        startVibration()
    }

    private fun startSound() {
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Failed to start MediaPlayer for alarm", e)
            try {
                // Fallback to simple Ringtone playback if MediaPlayer fails
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, fallbackUri)?.play()
            } catch (fallbackEx: Exception) {
                Log.e("AlarmSoundManager", "Fallback ringtone failed", fallbackEx)
            }
        }
    }

    private fun startVibration() {
        try {
            val timings = longArrayOf(0, 500, 300, 500, 300, 700, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, 0) // Repeat from index 0
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, 0)
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Failed to vibrate", e)
        }
    }

    /**
     * Stops the alarm sound and cancels vibration.
     */
    @Synchronized
    fun stopAlarm() {
        isAlarmPlaying = false

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Error stopping MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Error stopping Vibrator", e)
        }
    }
}

