package com.devvardhan.value.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.net.toUri
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.devvardhan.value.model.AlarmSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream

/**
 * Handles playing the alarm sound with audio focus acquisition, gradual volume ramp-up,
 * sound selection (Phone default, Value Tones, My Sounds), and vibration when the timer finishes.
 * Also provides preview playback for the Sound configuration screen.
 */
class AlarmSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val preferencesRepo = TimerPreferencesRepository(context)
    private var audioFocusRequest: AudioFocusRequest? = null

    private var mediaPlayer: MediaPlayer? = null
    private var fallbackRingtone: Ringtone? = null
    private var isAlarmActive = false
    private var volumeRampJob: Job? = null
    private var alarmLaunchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // For preview playback on the sound configuration screen
    private var previewPlayer: MediaPlayer? = null
    private var previewTimeoutHandler = Handler(Looper.getMainLooper())
    private var previewStopRunnable: Runnable? = null

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to access vibrator service", e)
            null
        }
    }

    @Synchronized
    fun startAlarm() {
        if (isAlarmActive) return
        isAlarmActive = true
        Log.d(TAG, "startAlarm triggered")

        requestAudioFocus()

        alarmLaunchJob?.cancel()
        alarmLaunchJob = scope.launch {
            val (vibrationEnabled, gradualVolume, soundId) = try {
                Triple(
                    preferencesRepo.vibrationEnabledFlow.first(),
                    preferencesRepo.gradualVolumeEnabledFlow.first(),
                    preferencesRepo.selectedSoundIdFlow.first()
                )
            } catch (e: Exception) {
                Triple(true, false, AlarmSound.ID_PHONE_DEFAULT)
            }

            if (!isAlarmActive) return@launch

            startSound(soundId, gradualVolume)
            if (vibrationEnabled && isAlarmActive) {
                startVibration()
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setOnAudioFocusChangeListener { /* Alarm priority */ }
                    .build()

                audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    null,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus request failed", e)
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Abandon audio focus failed", e)
        }
    }

    private fun startSound(soundId: String, gradualVolume: Boolean) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
            }

            setDataSourceForSound(mediaPlayer!!, soundId)
            mediaPlayer?.prepare()

            if (gradualVolume) {
                mediaPlayer?.setVolume(0.08f, 0.08f)
                mediaPlayer?.start()
                startVolumeRamp()
            } else {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaPlayer failed for soundId: $soundId, trying default ringtone fallback", e)
            playFallbackRingtone()
        }
    }

    private fun setDataSourceForSound(player: MediaPlayer, soundId: String) {
        when {
            soundId.startsWith("builtin_") -> {
                val toneKey = soundId.removePrefix("builtin_")
                val file = ValueToneGenerator.getToneFile(context, toneKey)
                FileInputStream(file).use { fis ->
                    player.setDataSource(fis.fd)
                }
            }
            soundId == AlarmSound.ID_PHONE_DEFAULT -> {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                player.setDataSource(context, defaultUri)
            }
            else -> {
                // User imported URI
                try {
                    val uri = soundId.toUri()
                    player.setDataSource(context, uri)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load user sound URI, falling back to default", e)
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    player.setDataSource(context, defaultUri)
                }
            }
        }
    }

    private fun startVolumeRamp() {
        volumeRampJob?.cancel()
        volumeRampJob = CoroutineScope(Dispatchers.Default).launch {
            val totalSteps = 20
            val intervalMs = 500L // 20 * 500ms = 10 seconds total ramp
            for (step in 1..totalSteps) {
                delay(intervalMs)
                if (!isAlarmActive) break
                val fraction = step.toFloat() / totalSteps
                val volume = (0.08f + (1.0f - 0.08f) * fraction).coerceIn(0f, 1f)
                try {
                    mediaPlayer?.setVolume(volume, volume)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun playFallbackRingtone() {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            fallbackRingtone = RingtoneManager.getRingtone(context, fallbackUri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (fallbackEx: Exception) {
            Log.e(TAG, "Ringtone fallback also failed", fallbackEx)
        }
    }

    private fun startVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                val timings = longArrayOf(0, 500, 300, 500, 300, 700, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(timings, 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    @Synchronized
    fun stopAlarm() {
        if (!isAlarmActive) return
        isAlarmActive = false
        Log.d(TAG, "stopAlarm triggered")

        alarmLaunchJob?.cancel()
        alarmLaunchJob = null
        volumeRampJob?.cancel()
        volumeRampJob = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }

        try {
            fallbackRingtone?.let {
                if (it.isPlaying) it.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping fallback Ringtone", e)
        } finally {
            fallbackRingtone = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling Vibrator", e)
        }

        abandonAudioFocus()
    }

    /**
     * Previews an alarm sound for at most 5 seconds with audio focus on STREAM_ALARM.
     * Stops any ongoing preview immediately. Does not trigger vibration or start services.
     */
    @Synchronized
    fun previewSound(sound: AlarmSound, onFinished: () -> Unit = {}) {
        stopPreview()

        requestAudioFocus()
        try {
            previewPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
            }

            setDataSourceForSound(previewPlayer!!, sound.id)
            previewPlayer?.prepare()
            previewPlayer?.start()

            // 5 second maximum preview duration
            previewStopRunnable = Runnable {
                stopPreview()
                onFinished()
            }
            previewTimeoutHandler.postDelayed(previewStopRunnable!!, 5000L)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preview sound: ${sound.id}", e)
            stopPreview()
            onFinished()
        }
    }

    @Synchronized
    fun stopPreview() {
        previewStopRunnable?.let { previewTimeoutHandler.removeCallbacks(it) }
        previewStopRunnable = null

        try {
            previewPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing preview MediaPlayer", e)
        } finally {
            previewPlayer = null
        }

        if (!isAlarmActive) {
            abandonAudioFocus()
        }
    }

    companion object {
        private const val TAG = "AlarmSoundManager"
    }
}
