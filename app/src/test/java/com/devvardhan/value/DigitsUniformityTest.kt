package com.devvardhan.value

import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.core.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DigitsUniformityTest {

    @Test
    fun testTwoDigitFormattingLengthInvariant() {
        for (i in 0..99) {
            val formatted = TimeUtils.formatTwoDigits(i)
            assertEquals("Every 0..99 value must format to exactly 2 characters", 2, formatted.length)
            assertTrue("Characters must all be digits", formatted.all { it.isDigit() })
        }
    }

    @Test
    fun testCentisecondsFormattingLengthInvariant() {
        for (ms in 0L..999L step 10L) {
            val centis = TimeUtils.formatStopwatchCentiseconds(ms)
            assertEquals("Centiseconds must format to exactly 2 characters", 2, centis.length)
            assertTrue("Characters must all be digits", centis.all { it.isDigit() })
        }
    }

    @Test
    fun testHmsFormattingLengthInvariantFor0To99Hours() {
        // Test lower bound (0s), arbitrary times, and upper bound 99:59:59
        val testTimesSeconds = listOf(
            0L,
            1L,
            59L,
            60L,
            3599L,
            3600L,
            12345L,
            86399L,
            86400L, // 24h
            359999L // 99h 59m 59s
        )

        for (seconds in testTimesSeconds) {
            val hms = TimeUtils.formatHmsFromSeconds(seconds)
            assertEquals("HH:MM:SS format must be exactly 8 characters", 8, hms.length)
            assertEquals(':', hms[2])
            assertEquals(':', hms[5])
            assertTrue("Digit characters must be digits", hms[0].isDigit() && hms[1].isDigit())
            assertTrue("Digit characters must be digits", hms[3].isDigit() && hms[4].isDigit())
            assertTrue("Digit characters must be digits", hms[6].isDigit() && hms[7].isDigit())
        }
    }

    @Test
    fun testScoreboardDigitsExtractionFullCoverage() {
        // Test across 0..9 for every position
        for (h in 0..99 step 11) {
            for (m in 0..59 step 11) {
                for (s in 0..59 step 11) {
                    val totalMs = (h * 3600L + m * 60L + s) * 1000L
                    val digits = ScoreboardDigitsHelper.extract(totalMs)
                    assertTrue("Hours tens is digit", digits.hoursTens.isDigit())
                    assertTrue("Hours ones is digit", digits.hoursOnes.isDigit())
                    assertTrue("Minutes tens is digit", digits.minutesTens.isDigit())
                    assertTrue("Minutes ones is digit", digits.minutesOnes.isDigit())
                    assertTrue("Seconds tens is digit", digits.secondsTens.isDigit())
                    assertTrue("Seconds ones is digit", digits.secondsOnes.isDigit())
                }
            }
        }
    }

    @Test
    fun testStopwatchMainUnitsLengthInvariant() {
        // Under 1 hour: "MM:SS" (length 5)
        for (s in 0L..3599L step 17L) {
            val mainUnits = TimeUtils.formatStopwatchMainUnits(s * 1000L)
            assertEquals("Under 1 hour main units must be MM:SS (5 chars)", 5, mainUnits.length)
            assertEquals(':', mainUnits[2])
        }

        // 1 hour and above: "H:MM:SS" or "HH:MM:SS"
        val hour1 = TimeUtils.formatStopwatchMainUnits(3600_000L)
        assertEquals("1:00:00", hour1)

        val hour99 = TimeUtils.formatStopwatchMainUnits((99 * 3600 + 59 * 60 + 59) * 1000L)
        assertEquals("99:59:59", hour99)
    }
}

