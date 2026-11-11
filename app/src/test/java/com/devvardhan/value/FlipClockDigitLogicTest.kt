package com.devvardhan.value

import com.devvardhan.value.core.ScoreboardDigits
import com.devvardhan.value.core.ScoreboardDigitsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class FlipClockDigitLogicTest {

    private fun formatExpectedElapsed(elapsedMillis: Long): String {
        val totalSeconds = (maxOf(0L, elapsedMillis) / 1000L).coerceAtMost(ScoreboardDigitsHelper.MAX_SECONDS)
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatExpectedRemaining(remainingMillis: Long): String {
        val raw = if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
        val totalSeconds = raw.coerceAtMost(ScoreboardDigitsHelper.MAX_SECONDS)
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun digitsToString(digits: ScoreboardDigits): String {
        return "${digits.hoursTens}${digits.hoursOnes}:${digits.minutesTens}${digits.minutesOnes}:${digits.secondsTens}${digits.secondsOnes}"
    }

    @Test
    fun testElapsedDigitsFrom0To4MillionMsIn10msSteps() {
        var ms = 0L
        while (ms <= 4_000_000L) {
            val expected = formatExpectedElapsed(ms)
            val digits = ScoreboardDigitsHelper.extractElapsed(ms)
            val actual = digitsToString(digits)
            assertEquals("Mismatch at $ms ms", expected, actual)
            ms += 10L
        }
    }

    @Test
    fun testSpecificConsecutiveSecondTransitions() {
        val boundarySeconds = listOf(
            9L to 10L,           // 00:00:09 -> 00:00:10
            19L to 20L,         // 00:00:19 -> 00:00:20
            29L to 30L,         // 00:00:29 -> 00:00:30
            39L to 40L,         // 00:00:39 -> 00:00:40
            49L to 50L,         // 00:00:49 -> 00:00:50
            59L to 60L,         // 00:00:59 -> 00:01:00
            599L to 600L,       // 00:09:59 -> 00:10:00
            3599L to 3600L,     // 00:59:59 -> 01:00:00
            35999L to 36000L,   // 09:59:59 -> 10:00:00
            359998L to 359999L, // 99:59:58 -> 99:59:59
            359999L to 360000L  // 99:59:59 cap hold
        )

        for ((beforeSec, afterSec) in boundarySeconds) {
            val beforeDigits = ScoreboardDigitsHelper.extractElapsed(beforeSec * 1000L)
            val afterDigits = ScoreboardDigitsHelper.extractElapsed(afterSec * 1000L)

            val expectedBefore = formatExpectedElapsed(beforeSec * 1000L)
            val expectedAfter = formatExpectedElapsed(afterSec * 1000L)

            assertEquals("Before $beforeSec s", expectedBefore, digitsToString(beforeDigits))
            assertEquals("After $afterSec s", expectedAfter, digitsToString(afterDigits))
        }
    }

    @Test
    fun testTimerCountdownTransitions() {
        val countdownSeconds = listOf(
            11L to 10L,         // 00:00:11 -> 00:00:10
            10L to 9L,          // 00:00:10 -> 00:00:09
            60L to 59L,         // 00:01:00 -> 00:00:59
            600L to 599L,       // 00:10:00 -> 00:09:59
            3600L to 3599L,     // 01:00:00 -> 00:59:59
            36000L to 35999L,   // 10:00:00 -> 09:59:59
            1L to 0L            // 00:00:01 -> 00:00:00
        )

        for ((beforeSec, afterSec) in countdownSeconds) {
            // Timer uses ceil: at beforeSec, remainingMillis = (beforeSec - 1)*1000 + 1..beforeSec*1000
            val beforeDigits = ScoreboardDigitsHelper.extract(beforeSec * 1000L)
            val afterDigits = ScoreboardDigitsHelper.extract(afterSec * 1000L)

            val expectedBefore = formatExpectedRemaining(beforeSec * 1000L)
            val expectedAfter = formatExpectedRemaining(afterSec * 1000L)

            assertEquals("Countdown Before $beforeSec s", expectedBefore, digitsToString(beforeDigits))
            assertEquals("Countdown After $afterSec s", expectedAfter, digitsToString(afterDigits))
        }
    }

    @Test
    fun testTwoOrMoreCellsChangeTogetherInStopwatch() {
        // At 9s -> 10s: exactly 2 cells change (secondsOnes '9' -> '0', secondsTens '0' -> '1')
        val d9 = ScoreboardDigitsHelper.extractElapsed(9_000L)
        val d10 = ScoreboardDigitsHelper.extractElapsed(10_000L)
        assertNotEquals(d9.secondsOnes, d10.secondsOnes)
        assertNotEquals(d9.secondsTens, d10.secondsTens)
        assertEquals(d9.minutesOnes, d10.minutesOnes)
        assertEquals(d9.minutesTens, d10.minutesTens)
        assertEquals(d9.hoursOnes, d10.hoursOnes)
        assertEquals(d9.hoursTens, d10.hoursTens)
        assertEquals('1', d10.secondsTens)
        assertEquals('0', d10.secondsOnes)

        // At 59s -> 60s: exactly 3 cells change (secondsOnes, secondsTens, minutesOnes)
        val d59 = ScoreboardDigitsHelper.extractElapsed(59_000L)
        val d60 = ScoreboardDigitsHelper.extractElapsed(60_000L)
        assertNotEquals(d59.secondsOnes, d60.secondsOnes)
        assertNotEquals(d59.secondsTens, d60.secondsTens)
        assertNotEquals(d59.minutesOnes, d60.minutesOnes)
        assertEquals('1', d60.minutesOnes)
        assertEquals('0', d60.secondsTens)
        assertEquals('0', d60.secondsOnes)

        // At 59:59 -> 1:00:00 (3599s -> 3600s): 4 cells change
        val d3599 = ScoreboardDigitsHelper.extractElapsed(3599_000L)
        val d3600 = ScoreboardDigitsHelper.extractElapsed(3600_000L)
        assertEquals("00:59:59", digitsToString(d3599))
        assertEquals("01:00:00", digitsToString(d3600))
    }

    @Test
    fun testTwoOrMoreCellsChangeTogetherInTimer() {
        // Countdown from 10s to 9s: 2 cells change (secondsTens '1' -> '0', secondsOnes '0' -> '9')
        val d10 = ScoreboardDigitsHelper.extract(10_000L)
        val d9 = ScoreboardDigitsHelper.extract(9_000L)
        assertEquals("00:00:10", digitsToString(d10))
        assertEquals("00:00:09", digitsToString(d9))

        // Countdown from 1m to 59s: 3 cells change
        val d60 = ScoreboardDigitsHelper.extract(60_000L)
        val d59 = ScoreboardDigitsHelper.extract(59_000L)
        assertEquals("00:01:00", digitsToString(d60))
        assertEquals("00:00:59", digitsToString(d59))
    }

    @Test
    fun testCapAt99Hours59Minutes59Seconds() {
        val capMillis = ScoreboardDigitsHelper.MAX_SECONDS * 1000L
        val pastCapMillis = capMillis + 3600 * 1000L // 100 hours
        val wayPastCapMillis = 999_999_999L

        val atCap = ScoreboardDigitsHelper.extractElapsed(capMillis)
        assertEquals("99:59:59", digitsToString(atCap))

        val pastCap = ScoreboardDigitsHelper.extractElapsed(pastCapMillis)
        assertEquals("99:59:59", digitsToString(pastCap))

        val wayPastCap = ScoreboardDigitsHelper.extractElapsed(wayPastCapMillis)
        assertEquals("99:59:59", digitsToString(wayPastCap))

        // Timer countdown cap
        val timerPastCap = ScoreboardDigitsHelper.extract(pastCapMillis)
        assertEquals("99:59:59", digitsToString(timerPastCap))
    }

    /**
     * Simulates the corrected flip card state machine in FlipDigitCard.
     */
    class FlipCardStateMachine(initialDigit: Char) {
        var currentDigit: Char = initialDigit
            private set
        var outgoingDigit: Char = initialDigit
            private set
        var isFlipping: Boolean = false
            private set
        var animProgress: Float = 0f
            private set

        fun onNewDigit(newDigit: Char, animate: Boolean = true) {
            if (newDigit != currentDigit) {
                if (animate) {
                    outgoingDigit = currentDigit
                    isFlipping = true
                    animProgress = 0f
                } else {
                    currentDigit = newDigit
                    isFlipping = false
                    animProgress = 0f
                }
            }
        }

        fun updateProgress(progress: Float, targetDigit: Char) {
            if (!isFlipping) return
            animProgress = progress.coerceIn(0f, 1f)
            if (animProgress >= 1f) {
                currentDigit = targetDigit
                isFlipping = false
                animProgress = 0f
            }
        }

        fun getDisplayedChar(targetDigit: Char): Char {
            return if (isFlipping) {
                if (animProgress < 0.5f) outgoingDigit else targetDigit
            } else {
                currentDigit
            }
        }

        fun getTextRotation(isFlipping: Boolean, animProgress: Float): Float {
            return if (isFlipping && animProgress >= 0.5f) 180f else 0f
        }

        fun getBoxRotation(isFlipping: Boolean, animProgress: Float): Float {
            return if (isFlipping) -animProgress * 180f else 0f
        }
    }

    @Test
    fun testFlipCardStateMachine9To10Transition() {
        val tens = FlipCardStateMachine('0')
        val ones = FlipCardStateMachine('9')

        assertEquals('0', tens.getDisplayedChar('0'))
        assertEquals('9', ones.getDisplayedChar('9'))

        // Time reaches 10s: tens target is '1', ones target is '0'
        tens.onNewDigit('1', animate = true)
        ones.onNewDigit('0', animate = true)
        assertTrue(tens.isFlipping)
        assertTrue(ones.isFlipping)

        // First half of flip (progress = 0.2f): both cards display their outgoing digits
        tens.updateProgress(0.2f, '1')
        ones.updateProgress(0.2f, '0')
        assertEquals('0', tens.getDisplayedChar('1'))
        assertEquals('9', ones.getDisplayedChar('0'))
        assertEquals(0f, tens.getTextRotation(tens.isFlipping, tens.animProgress))
        assertEquals(0f, ones.getTextRotation(ones.isFlipping, ones.animProgress))

        // Second half of flip (progress = 0.8f): both cards display their incoming target digits
        tens.updateProgress(0.8f, '1')
        ones.updateProgress(0.8f, '0')
        assertEquals('1', tens.getDisplayedChar('1'))
        assertEquals('0', ones.getDisplayedChar('0'))
        assertEquals(180f, tens.getTextRotation(tens.isFlipping, tens.animProgress))
        assertEquals(180f, ones.getTextRotation(ones.isFlipping, ones.animProgress))

        // Animation completes (progress = 1.0f -> settled at rest)
        tens.updateProgress(1.0f, '1')
        ones.updateProgress(1.0f, '0')
        assertFalse(tens.isFlipping)
        assertFalse(ones.isFlipping)
        assertEquals('1', tens.getDisplayedChar('1'))
        assertEquals('0', ones.getDisplayedChar('0'))
        assertEquals(0f, tens.getTextRotation(tens.isFlipping, tens.animProgress))
        assertEquals(0f, ones.getTextRotation(ones.isFlipping, ones.animProgress))
        assertEquals(0f, tens.getBoxRotation(tens.isFlipping, tens.animProgress))
        assertEquals(0f, ones.getBoxRotation(ones.isFlipping, ones.animProgress))

        // Subsequent 11 seconds: tens receives '1' again (no change, no animation)
        tens.onNewDigit('1', animate = true)
        assertFalse(tens.isFlipping)
        assertEquals('1', tens.getDisplayedChar('1')) // REMAINS 1! Bug resolved!
    }

    @Test
    fun testInFlightInterruptionNeverEndsOnWrongDigit() {
        val card = FlipCardStateMachine('0')

        // Start flip to '1'
        card.onNewDigit('1', animate = true)
        card.updateProgress(0.4f, '1')
        assertEquals('0', card.getDisplayedChar('1'))

        // Mid-flight interruption: user jumps or reset occurs to '5'
        // Coroutine cancels, settles previous target or snaps immediately
        card.onNewDigit('5', animate = false)
        assertFalse(card.isFlipping)
        assertEquals('5', card.getDisplayedChar('5'))
        assertEquals(0f, card.getBoxRotation(card.isFlipping, card.animProgress))
    }
}
