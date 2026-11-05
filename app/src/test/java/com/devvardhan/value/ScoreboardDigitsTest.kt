package com.devvardhan.value

import com.devvardhan.value.core.ScoreboardDigitsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ScoreboardDigitsTest {

    @Test
    fun testExtractionZeroMillis() {
        val digits = ScoreboardDigitsHelper.extract(0L)
        assertEquals('0', digits.hoursTens)
        assertEquals('0', digits.hoursOnes)
        assertEquals('0', digits.minutesTens)
        assertEquals('0', digits.minutesOnes)
        assertEquals('0', digits.secondsTens)
        assertEquals('0', digits.secondsOnes)
    }

    @Test
    fun testExtractionTypicalValues() {
        // 1 hour, 23 minutes, 45 seconds = (3600 + 23*60 + 45) * 1000 = (3600 + 1380 + 45)*1000 = 5025000L
        val digits = ScoreboardDigitsHelper.extract(5_024_001L) // ceil = 5025s
        assertEquals('0', digits.hoursTens)
        assertEquals('1', digits.hoursOnes)
        assertEquals('2', digits.minutesTens)
        assertEquals('3', digits.minutesOnes)
        assertEquals('4', digits.secondsTens)
        assertEquals('5', digits.secondsOnes)
    }

    @Test
    fun testDigitChangeDetectionBetweenConsecutiveSeconds() {
        // From 5 seconds to 4 seconds: ONLY secondsOnes changes ('5' -> '4')
        val t5 = ScoreboardDigitsHelper.extract(4_500L) // 5s
        val t4 = ScoreboardDigitsHelper.extract(3_500L) // 4s

        assertEquals('5', t5.secondsOnes)
        assertEquals('4', t4.secondsOnes)
        assertNotEquals(t5.secondsOnes, t4.secondsOnes) // Animates

        assertEquals(t5.secondsTens, t4.secondsTens) // '0' == '0' (Does NOT animate)
        assertEquals(t5.minutesOnes, t4.minutesOnes) // Does NOT animate
        assertEquals(t5.minutesTens, t4.minutesTens) // Does NOT animate
        assertEquals(t5.hoursOnes, t4.hoursOnes) // Does NOT animate
        assertEquals(t5.hoursTens, t4.hoursTens) // Does NOT animate
    }

    @Test
    fun testMinuteBoundaryWrap() {
        // At 1 minute (60s): 00:01:00
        val atMinute = ScoreboardDigitsHelper.extract(59_001L) // 60s
        assertEquals('0', atMinute.minutesTens)
        assertEquals('1', atMinute.minutesOnes)
        assertEquals('0', atMinute.secondsTens)
        assertEquals('0', atMinute.secondsOnes)

        // At 59 seconds: 00:00:59
        val at59 = ScoreboardDigitsHelper.extract(58_500L) // 59s
        assertEquals('0', at59.minutesTens)
        assertEquals('0', at59.minutesOnes) // Changed from '1' to '0'
        assertEquals('5', at59.secondsTens) // Changed from '0' to '5'
        assertEquals('9', at59.secondsOnes) // Changed from '0' to '9'

        assertNotEquals(atMinute.minutesOnes, at59.minutesOnes)
        assertNotEquals(atMinute.secondsTens, at59.secondsTens)
        assertNotEquals(atMinute.secondsOnes, at59.secondsOnes)
    }
}

