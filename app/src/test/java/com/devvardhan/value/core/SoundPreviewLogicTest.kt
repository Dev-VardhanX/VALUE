package com.devvardhan.value.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundPreviewLogicTest {

    private val logic = SoundPreviewLogic()

    @Test
    fun testTapToPlay() {
        assertFalse(logic.isPlaying)
        assertNull(logic.currentlyPlayingId)

        val result = logic.onSoundTapped("blast")
        assertEquals(SoundPreviewLogic.PreviewEvent.Play("blast"), result)
        assertTrue(logic.isPlaying)
        assertEquals("blast", logic.currentlyPlayingId)
        assertTrue(logic.isPlaying("blast"))
        assertFalse(logic.isPlaying("flow"))
    }

    @Test
    fun testTapAgainToStop() {
        logic.onSoundTapped("blast")
        assertTrue(logic.isPlaying)

        val result = logic.onSoundTapped("blast")
        assertEquals(SoundPreviewLogic.PreviewEvent.Stop, result)
        assertFalse(logic.isPlaying)
        assertNull(logic.currentlyPlayingId)
    }

    @Test
    fun testTapAnotherSwitches() {
        logic.onSoundTapped("blast")
        assertEquals("blast", logic.currentlyPlayingId)

        val result = logic.onSoundTapped("flow")
        assertEquals(SoundPreviewLogic.PreviewEvent.Play("flow"), result)
        assertTrue(logic.isPlaying)
        assertEquals("flow", logic.currentlyPlayingId)
    }

    @Test
    fun testTapStoppedAlreadySelectedRowPlaysAgain() {
        // Start playing
        logic.onSoundTapped("blast")
        assertTrue(logic.isPlaying)

        // Stop it (still selected in UI)
        logic.onSoundTapped("blast")
        assertFalse(logic.isPlaying)

        // Tap again -> starts playing again
        val result = logic.onSoundTapped("blast")
        assertEquals(SoundPreviewLogic.PreviewEvent.Play("blast"), result)
        assertTrue(logic.isPlaying)
        assertEquals("blast", logic.currentlyPlayingId)
    }

    @Test
    fun testIndefinitePlaybackNoTimeout() {
        logic.onSoundTapped("blast")
        assertTrue(logic.isPlaying)
        // With no timeout limit, the preview stays playing indefinitely until an explicit stop action
        assertEquals("blast", logic.currentlyPlayingId)
        assertTrue(logic.isPlaying("blast"))
    }

    @Test
    fun testStopOnLeavingScreen() {
        logic.onSoundTapped("piece")
        assertTrue(logic.isPlaying)

        val stopped = logic.onLeaveScreen()
        assertTrue(stopped)
        assertFalse(logic.isPlaying)
        assertNull(logic.currentlyPlayingId)
    }

    @Test
    fun testStopOnBackground() {
        logic.onSoundTapped("who")
        assertTrue(logic.isPlaying)

        val stopped = logic.onAppBackgrounded()
        assertTrue(stopped)
        assertFalse(logic.isPlaying)
        assertNull(logic.currentlyPlayingId)
    }

    @Test
    fun testAudioFocusLoss() {
        logic.onSoundTapped("wind")
        assertTrue(logic.isPlaying)

        val stopped = logic.onAudioFocusLoss()
        assertTrue(stopped)
        assertFalse(logic.isPlaying)
        assertNull(logic.currentlyPlayingId)
    }

    @Test
    fun testStopWhenNotPlayingReturnsFalse() {
        assertFalse(logic.stop())
        assertFalse(logic.onAudioFocusLoss())
        assertFalse(logic.onAppBackgrounded())
        assertFalse(logic.onLeaveScreen())
    }
}
