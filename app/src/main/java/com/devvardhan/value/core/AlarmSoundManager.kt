package com.devvardhan.value.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.devvardhan.value.R
import com.devvardhan.value.model.AlarmSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Handles playing the alarm sound with audio focus acquisition, gradual volume ramp-up,
 * sound selection (Phone default, Value Tones, My Sounds), and vibration when the timer finishes.
 * Also provides preview playback for the Sound configuration screen.
 */
class AlarmSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val preferencesRepo = TimerPreferencesRepository(context)
    private var alarmAudioFocusRequest: AudioFocusRequest? = null
    private var previewAudioFocusRequest: AudioFocusRequest? = null

    private var mediaPlayer: MediaPlayer? = null
    private var fallbackRingtone: Ringtone? = null
    private var isAlarmActive = false
    private var volumeRampJob: Job? = null
    private var alarmLaunchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // For preview playback on the sound configuration screen
    private var previewPlayer: MediaPlayer? = null
    private var isPreviewActive = false
    private var previewVolumeRampJob: Job? = null
    private val _playingPreviewSoundId = MutableStateFlow<String?>(null)
    val playingPreviewSoundId: StateFlow<String?> = _playingPreviewSoundId.asStateFlow()

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

        requestAlarmAudioFocus()

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

    private fun requestAlarmAudioFocus() {
        try {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            alarmAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener { /* Alarm priority */ }
                .build()

            alarmAudioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Alarm audio focus request failed", e)
        }
    }

    private fun abandonAlarmAudioFocus() {
        try {
            alarmAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            alarmAudioFocusRequest = null
        } catch (e: Exception) {
            Log.w(TAG, "Abandon alarm audio focus failed", e)
        }
    }

    private fun requestPreviewAudioFocus() {
        try {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            previewAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            Log.d(TAG, "Audio focus lost during preview ($focusChange), stopping preview")
                            stopPreview()
                        }
                    }
                }
                .build()

            previewAudioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Preview audio focus request failed", e)
        }
    }

    private fun abandonPreviewAudioFocus() {
        try {
            previewAudioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            previewAudioFocusRequest = null
        } catch (e: Exception) {
            Log.w(TAG, "Abandon preview audio focus failed", e)
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
                val tone = AlarmSound.BUILT_IN_TONES.find { it.id == soundId }
                if (tone != null && tone.rawResId != 0) {
                    context.resources.openRawResourceFd(tone.rawResId)?.use { afd ->
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                } else {
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    player.setDataSource(context, defaultUri)
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
                val effect = VibrationEffect.createWaveform(timings, 0)
                vibrator?.vibrate(effect)
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

        abandonAlarmAudioFocus()
    }

    /**
     * Previews an alarm sound looping on STREAM_ALARM with audio focus.
     * Honors the gradual volume setting.
     * Loops indefinitely until the user or system interrupts playback.
     */
    @Synchronized
    fun previewSound(sound: AlarmSound, gradualVolume: Boolean = false) {
        stopPreview()

        // Missing file safety check for user imported audio
        if (sound is AlarmSound.UserSound) {
            try {
                val uri = sound.uriString.toUri()
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
                    showFileNotFoundToast()
                    return
                }
                pfd.close()
            } catch (e: Exception) {
                Log.w(TAG, "User sound file missing or inaccessible: ${sound.uriString}", e)
                showFileNotFoundToast()
                return
            }
        }

        requestPreviewAudioFocus()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
            }
            previewPlayer = player

            setDataSourceForSound(player, sound.id)
            player.prepare()
            // Ensure looping is retained after prepare() on all Android decoders
            player.isLooping = true

            // Safety listener in case native looping fails on short user audio files
            player.setOnCompletionListener { mp ->
                if (isPreviewActive) {
                    try {
                        mp.seekTo(0)
                        mp.start()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error in completion listener loop fallback", e)
                    }
                }
            }

            player.setOnErrorListener { _, what, extra ->
                Log.w(TAG, "Preview MediaPlayer error: what=$what, extra=$extra")
                stopPreview()
                true
            }

            if (gradualVolume) {
                player.setVolume(0.08f, 0.08f)
                player.start()
                startPreviewVolumeRamp()
            } else {
                player.setVolume(1.0f, 1.0f)
                player.start()
            }

            isPreviewActive = true
            _playingPreviewSoundId.value = sound.id
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preview sound: ${sound.id}", e)
            if (sound is AlarmSound.UserSound) {
                showFileNotFoundToast()
            }
            stopPreview()
        }
    }

    private fun showFileNotFoundToast() {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, R.string.error_file_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPreviewVolumeRamp() {
        previewVolumeRampJob?.cancel()
        previewVolumeRampJob = scope.launch(Dispatchers.Default) {
            val totalSteps = 20
            val intervalMs = 500L
            for (step in 1..totalSteps) {
                delay(intervalMs)
                if (!isPreviewActive) break
                val fraction = step.toFloat() / totalSteps
                val volume = (0.08f + (1.0f - 0.08f) * fraction).coerceIn(0f, 1f)
                try {
                    previewPlayer?.setVolume(volume, volume)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    @Synchronized
    fun stopPreview() {
        isPreviewActive = false
        _playingPreviewSoundId.value = null

        previewVolumeRampJob?.cancel()
        previewVolumeRampJob = null

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

        abandonPreviewAudioFocus()
    }

    companion object {
        private const val TAG = "AlarmSoundManager"
    }
}
