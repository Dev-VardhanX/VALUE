package com.devvardhan.value.core

/**
 * Pure state logic for alarm sound previews.
 * Decoupled from Android framework for deterministic unit testing.
 * Previews loop indefinitely until explicitly stopped.
 */
class SoundPreviewLogic {

    var currentlyPlayingId: String? = null
        private set

    val isPlaying: Boolean
        get() = currentlyPlayingId != null

    fun isPlaying(soundId: String): Boolean = currentlyPlayingId == soundId

    sealed interface PreviewEvent {
        data class Play(val soundId: String) : PreviewEvent
        data object Stop : PreviewEvent
    }

    /**
     * Handles tapping a sound row.
     * If the tapped sound is currently playing -> Stop.
     * If stopped or a different sound -> Play tapped sound.
     */
    fun onSoundTapped(soundId: String): PreviewEvent {
        return if (currentlyPlayingId == soundId) {
            stop()
            PreviewEvent.Stop
        } else {
            currentlyPlayingId = soundId
            PreviewEvent.Play(soundId)
        }
    }

    /**
     * Stops the preview playback.
     * Returns true if playback was active and stopped.
     */
    fun stop(): Boolean {
        if (currentlyPlayingId != null) {
            currentlyPlayingId = null
            return true
        }
        return false
    }

    /**
     * Called when audio focus is lost.
     */
    fun onAudioFocusLoss(): Boolean = stop()

    /**
     * Called when the app is backgrounded or screen turns off.
     */
    fun onAppBackgrounded(): Boolean = stop()

    /**
     * Called when user navigates away from the sound screen or opens file picker.
     */
    fun onLeaveScreen(): Boolean = stop()
}
