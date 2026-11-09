package com.devvardhan.value.core

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates lightweight, pristine 16-bit mono 44.1kHz PCM WAV audio files
 * on demand in app cache. Zero assets or external libraries required.
 */
object ValueToneGenerator {

    private const val SAMPLE_RATE = 44100

    fun getToneFile(context: Context, toneKey: String): File {
        val dir = File(context.cacheDir, "tones").apply { mkdirs() }
        val file = File(dir, "$toneKey.wav")
        if (!file.exists() || file.length() < 100) {
            val samples = generateSamples(toneKey)
            writeWavFile(file, samples, SAMPLE_RATE)
        }
        return file
    }

    private fun generateSamples(toneKey: String): ShortArray {
        return when (toneKey) {
            "digital" -> generateDigitalBeep()
            "radar" -> generateRadarSweep()
            "pulse" -> generatePulse()
            "bell" -> generateBell()
            else -> generateChime() // Default "chime"
        }
    }

    /**
     * Warm two-tone harmonic bell: C5 (523 Hz) blending into E5 (659 Hz)
     */
    private fun generateChime(): ShortArray {
        val durationSec = 1.3f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val freq1 = 523.25 // C5
        val freq2 = 659.25 // E5
        val freq3 = 1046.5 // C6 overtone

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope1 = exp(-4.0 * t)
            val envelope2 = if (t > 0.2) exp(-3.0 * (t - 0.2)) else 0.0

            val v1 = sin(2 * PI * freq1 * t) * envelope1
            val v2 = sin(2 * PI * freq2 * t) * envelope2
            val v3 = sin(2 * PI * freq3 * t) * 0.2 * envelope1

            val mixed = (v1 * 0.55 + v2 * 0.45 + v3 * 0.15) * 0.85
            samples[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Classic electronic double-beep: 880 Hz (A5)
     */
    private fun generateDigitalBeep(): ShortArray {
        val durationSec = 1.0f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val freq = 880.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val inBeep1 = t in 0.0..0.12
            val inBeep2 = t in 0.20..0.32

            if (inBeep1 || inBeep2) {
                val beepTime = if (inBeep1) t else t - 0.20
                // Slight fade in/out to eliminate click
                val attackDecay = if (beepTime < 0.01) beepTime / 0.01
                else if (beepTime > 0.11) (0.12 - beepTime) / 0.01
                else 1.0

                val v = sin(2 * PI * freq * t) * attackDecay * 0.85
                samples[i] = (v * Short.MAX_VALUE).toInt().toShort()
            } else {
                samples[i] = 0
            }
        }
        return samples
    }

    /**
     * Smooth frequency sweep sonar ping: 440 Hz -> 880 Hz
     */
    private fun generateRadarSweep(): ShortArray {
        val durationSec = 1.1f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sweepFreq = 440.0 + 440.0 * (t / durationSec)
            val envelope = exp(-3.2 * t)
            val v = sin(2 * PI * sweepFreq * t) * envelope * 0.85
            samples[i] = (v * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Resonant triple pulse: 587 Hz (D5)
     */
    private fun generatePulse(): ShortArray {
        val durationSec = 1.2f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)
        val freq = 587.33

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val pulseIndex = (t / 0.28).toInt()
            val localT = t % 0.28

            if (pulseIndex < 3 && localT < 0.20) {
                val envelope = exp(-12.0 * localT)
                val v = sin(2 * PI * freq * t) * envelope * 0.85
                samples[i] = (v * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            } else {
                samples[i] = 0
            }
        }
        return samples
    }

    /**
     * High crystal bell ping with long harmonic decay: 1046 Hz (C6) + 2093 Hz
     */
    private fun generateBell(): ShortArray {
        val durationSec = 1.6f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val f1 = 1046.50
        val f2 = 2093.00

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env1 = exp(-2.5 * t)
            val env2 = exp(-5.0 * t)

            val v = (sin(2 * PI * f1 * t) * env1 * 0.7 + sin(2 * PI * f2 * t) * env2 * 0.3) * 0.85
            samples[i] = (v * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun writeWavFile(file: File, samples: ShortArray, sampleRate: Int) {
        val dataSize = samples.size * 2
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                // RIFF chunk descriptor
                put('R'.code.toByte())
                put('I'.code.toByte())
                put('F'.code.toByte())
                put('F'.code.toByte())
                putInt(totalSize)
                put('W'.code.toByte())
                put('A'.code.toByte())
                put('V'.code.toByte())
                put('E'.code.toByte())

                // "fmt " sub-chunk
                put('f'.code.toByte())
                put('m'.code.toByte())
                put('t'.code.toByte())
                put(' '.code.toByte())
                putInt(16) // Subchunk1Size for PCM
                putShort(1) // AudioFormat: 1 = PCM
                putShort(1) // NumChannels: 1 = Mono
                putInt(sampleRate) // SampleRate
                putInt(sampleRate * 2) // ByteRate = SampleRate * NumChannels * BitsPerSample/8
                putShort(2) // BlockAlign = NumChannels * BitsPerSample/8
                putShort(16) // BitsPerSample: 16

                // "data" sub-chunk
                put('d'.code.toByte())
                put('a'.code.toByte())
                put('t'.code.toByte())
                put('a'.code.toByte())
                putInt(dataSize)
            }
            out.write(header.array())

            val buffer = ByteBuffer.allocate(dataSize).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (s in samples) {
                    putShort(s)
                }
            }
            out.write(buffer.array())
        }
    }
}

