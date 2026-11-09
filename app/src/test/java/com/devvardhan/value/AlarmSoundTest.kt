package com.devvardhan.value

import android.content.Context
import android.content.ContextWrapper
import com.devvardhan.value.core.ValueToneGenerator
import com.devvardhan.value.model.AlarmSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream

class AlarmSoundTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

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

    @Test
    fun testValueToneGeneratorGeneratesValidWavFiles() {
        val mockCacheDir = tempFolder.newFolder("cache")
        val mockContext = object : ContextWrapper(null) {
            override fun getCacheDir(): File = mockCacheDir
        }

        val tones = listOf("chime", "digital", "radar", "pulse", "bell")
        for (tone in tones) {
            val file = ValueToneGenerator.getToneFile(mockContext, tone)
            assertTrue("File for $tone should exist", file.exists())
            assertTrue("File for $tone should be non-empty", file.length() > 1000)

            // Validate RIFF WAV header
            val header = ByteArray(12)
            FileInputStream(file).use { it.read(header) }
            val riff = String(header, 0, 4)
            val wave = String(header, 8, 4)
            assertEquals("RIFF", riff)
            assertEquals("WAVE", wave)
        }
    }
}
