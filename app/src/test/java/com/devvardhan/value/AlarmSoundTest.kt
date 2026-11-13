package com.devvardhan.value

import com.devvardhan.value.model.AlarmSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSoundTest {

    @Test
    fun testFindByIdPhoneDefault() {
        val sound = AlarmSound.findById(AlarmSound.ID_PHONE_DEFAULT, emptyList())
        assertEquals(AlarmSound.PhoneDefault, sound)
    }

    @Test
    fun testFindByIdBuiltInTones() {
        for (tone in AlarmSound.BUILT_IN_TONES) {
            val found = AlarmSound.findById(tone.id, emptyList())
            assertEquals(tone, found)
        }
    }

    @Test
    fun testBuiltInTonesContents() {
        val expectedKeys = listOf("blast", "flow", "piece", "who", "wind")
        assertEquals(expectedKeys.size, AlarmSound.BUILT_IN_TONES.size)
        val actualKeys = AlarmSound.BUILT_IN_TONES.map { it.toneKey }
        assertEquals(expectedKeys, actualKeys)

        val expectedTitles = listOf("Blast", "Flow", "Piece", "Who", "Wind")
        val actualTitles = AlarmSound.BUILT_IN_TONES.map { it.title }
        assertEquals(expectedTitles, actualTitles)

        for (tone in AlarmSound.BUILT_IN_TONES) {
            assertTrue("ID should start with builtin_", tone.id.startsWith("builtin_"))
            assertTrue("Title should be non-empty", tone.title.isNotEmpty())
            assertNotEquals("rawResId should be set", 0, tone.rawResId)
        }
    }

    @Test
    fun testFindByIdUserSound() {
        val userSound = AlarmSound.UserSound(
            uriString = "content://media/external/audio/123",
            name = "Test Ringtone.mp3",
            sizeBytes = 1024L
        )
        val found = AlarmSound.findById("content://media/external/audio/123", listOf(userSound))
        assertEquals(userSound, found)
    }

    @Test
    fun testFindByIdUnknownFallsBackToPhoneDefault() {
        val found = AlarmSound.findById("unknown_sound_id", emptyList())
        assertEquals(AlarmSound.PhoneDefault, found)
    }
}
