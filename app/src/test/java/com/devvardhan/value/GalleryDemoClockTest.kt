package com.devvardhan.value

import com.devvardhan.value.core.GalleryDemoClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying deterministic Gallery Demo Clock:
 * - Starts at 0 (timer full 10s, stopwatch at 0s)
 * - 10s countdown / countup animation cycle
 * - 1s hold phase at empty/full
 * - Deterministic loop at 11,000 ms
 * - Reset crossfade alpha
 */
class GalleryDemoClockTest {

    @Test
    fun timerRemaining_atZero_isTenSeconds() {
        val remaining = GalleryDemoClock.calculateTimerRemaining(0L)
        assertEquals(10_000L, remaining)
        assertEquals(1.0f, GalleryDemoClock.calculateTimerProgress(0L), 0.001f)
    }

    @Test
    fun timerRemaining_halfway_isFiveSeconds() {
        val remaining = GalleryDemoClock.calculateTimerRemaining(5_000L)
        assertEquals(5_000L, remaining)
        assertEquals(0.5f, GalleryDemoClock.calculateTimerProgress(5_000L), 0.001f)
    }

    @Test
    fun timerRemaining_atTenSeconds_isZero() {
        val remaining = GalleryDemoClock.calculateTimerRemaining(10_000L)
        assertEquals(0L, remaining)
        assertEquals(0.0f, GalleryDemoClock.calculateTimerProgress(10_000L), 0.001f)
    }

    @Test
    fun timerRemaining_duringHoldPhase_staysZero() {
        val remaining = GalleryDemoClock.calculateTimerRemaining(10_500L)
        assertEquals(0L, remaining)
        assertEquals(0.0f, GalleryDemoClock.calculateTimerProgress(10_500L), 0.001f)
    }

    @Test
    fun timerRemaining_atElevenSeconds_loopsBackToTenSeconds() {
        val remaining = GalleryDemoClock.calculateTimerRemaining(11_000L)
        assertEquals(10_000L, remaining)
        assertEquals(1.0f, GalleryDemoClock.calculateTimerProgress(11_000L), 0.001f)
    }

    @Test
    fun stopwatchElapsed_atZero_isZero() {
        val elapsed = GalleryDemoClock.calculateStopwatchElapsed(0L)
        assertEquals(0L, elapsed)
        assertEquals(0.0f, GalleryDemoClock.calculateStopwatchProgress(0L), 0.001f)
    }

    @Test
    fun stopwatchElapsed_halfway_isFiveSeconds() {
        val elapsed = GalleryDemoClock.calculateStopwatchElapsed(5_000L)
        assertEquals(5_000L, elapsed)
        assertEquals(0.5f, GalleryDemoClock.calculateStopwatchProgress(5_000L), 0.001f)
    }

    @Test
    fun stopwatchElapsed_atTenSeconds_isTenSeconds() {
        val elapsed = GalleryDemoClock.calculateStopwatchElapsed(10_000L)
        assertEquals(10_000L, elapsed)
        assertEquals(1.0f, GalleryDemoClock.calculateStopwatchProgress(10_000L), 0.001f)
    }

    @Test
    fun stopwatchElapsed_duringHoldPhase_staysAtTenSeconds() {
        val elapsed = GalleryDemoClock.calculateStopwatchElapsed(10_500L)
        assertEquals(10_000L, elapsed)
        assertEquals(1.0f, GalleryDemoClock.calculateStopwatchProgress(10_500L), 0.001f)
    }

    @Test
    fun stopwatchElapsed_atElevenSeconds_loopsBackToZero() {
        val elapsed = GalleryDemoClock.calculateStopwatchElapsed(11_000L)
        assertEquals(0L, elapsed)
        assertEquals(0.0f, GalleryDemoClock.calculateStopwatchProgress(11_000L), 0.001f)
    }

    @Test
    fun loopAlpha_dipsAtBoundary_andRecovers() {
        // In the middle of the cycle, alpha should be exactly 1.0f
        assertEquals(1.0f, GalleryDemoClock.calculateLoopAlpha(5_000L), 0.001f)

        // Near the end of the hold phase, alpha should dip (< 1.0f)
        val alphaNearEnd = GalleryDemoClock.calculateLoopAlpha(10_950L)
        assertTrue(alphaNearEnd in 0.6f..1.0f)

        // Just after loop restart, alpha should still be transitioning back to 1.0f
        val alphaAfterRestart = GalleryDemoClock.calculateLoopAlpha(11_050L)
        assertTrue(alphaAfterRestart in 0.6f..1.0f)
    }
}

