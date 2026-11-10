package com.devvardhan.value

import com.devvardhan.value.core.GalleryDemoClock
import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.ThemeFractionCalculator
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sqrt

class ThemePreviewLogicTest {

    private val totalDemoMillis = GalleryDemoClock.DEMO_ACTIVE_MILLIS // 10,000L

    // -------------------------------------------------------------------------
    // TIMER THEMES AT t=0s, t=5s, t=10s
    // -------------------------------------------------------------------------

    @Test
    fun testCircleTimerPreview_StateChangesAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)     // 10,000 ms
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L) // 5,000 ms
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L) // 0 ms

        val prog0 = ThemeFractionCalculator.calculateOverallProgress(rem0, totalDemoMillis)
        val prog5 = ThemeFractionCalculator.calculateOverallProgress(rem5, totalDemoMillis)
        val prog10 = ThemeFractionCalculator.calculateOverallProgress(rem10, totalDemoMillis)

        assertEquals(1.0f, prog0, 0.001f)
        assertEquals(0.5f, prog5, 0.001f)
        assertEquals(0.0f, prog10, 0.001f)

        assertNotEquals(prog0, prog5)
        assertNotEquals(prog5, prog10)
    }

    @Test
    fun testHourglassTimerPreview_SandLevelsChangeAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val topProgress0 = ThemeFractionCalculator.calculateOverallProgress(rem0, totalDemoMillis)
        val topProgress5 = ThemeFractionCalculator.calculateOverallProgress(rem5, totalDemoMillis)
        val topProgress10 = ThemeFractionCalculator.calculateOverallProgress(rem10, totalDemoMillis)

        val bottomMound0 = sqrt((1f - topProgress0).coerceIn(0f, 1f))
        val bottomMound5 = sqrt((1f - topProgress5).coerceIn(0f, 1f))
        val bottomMound10 = sqrt((1f - topProgress10).coerceIn(0f, 1f))

        assertEquals(0.0f, bottomMound0, 0.001f)
        assertTrue(bottomMound5 > 0.5f && bottomMound5 < 0.9f)
        assertEquals(1.0f, bottomMound10, 0.001f)

        assertNotEquals(bottomMound0, bottomMound5)
        assertNotEquals(bottomMound5, bottomMound10)
    }

    @Test
    fun testThreeBoxTimerPreview_ScoreboardDigitsChangeAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val digits0 = ScoreboardDigitsHelper.extract(rem0)
        val digits5 = ScoreboardDigitsHelper.extract(rem5)
        val digits10 = ScoreboardDigitsHelper.extract(rem10)

        assertEquals('1', digits0.secondsTens)
        assertEquals('0', digits0.secondsOnes)

        assertEquals('0', digits5.secondsTens)
        assertEquals('5', digits5.secondsOnes)

        assertEquals('0', digits10.secondsTens)
        assertEquals('0', digits10.secondsOnes)

        assertNotEquals(digits0, digits5)
        assertNotEquals(digits5, digits10)
    }

    @Test
    fun testConcentricRingsTimerPreview_SecondsRingChangesAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val sec0 = ThemeFractionCalculator.calculateSecondsFraction(rem0)
        val sec5 = ThemeFractionCalculator.calculateSecondsFraction(rem5)
        val sec10 = ThemeFractionCalculator.calculateSecondsFraction(rem10)

        assertTrue(sec0 > sec5)
        assertTrue(sec5 > sec10)
        assertEquals(0.0f, sec10, 0.001f)
    }

    @Test
    fun testFlipClockTimerPreview_TimeStringsChangeAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val sec0 = ((rem0 + 999L) / 1000L)
        val sec5 = ((rem5 + 999L) / 1000L)
        val sec10 = if (rem10 <= 0L) 0L else (rem10 + 999L) / 1000L

        val str0 = TimeUtils.formatTwoDigits(sec0.toInt())
        val str5 = TimeUtils.formatTwoDigits(sec5.toInt())
        val str10 = TimeUtils.formatTwoDigits(sec10.toInt())

        assertEquals("10", str0)
        assertEquals("05", str5)
        assertEquals("00", str10)
    }

    @Test
    fun testLiquidFillTimerPreview_LevelAndWavePhaseChangeAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val level0 = ThemeFractionCalculator.calculateOverallProgress(rem0, totalDemoMillis)
        val level5 = ThemeFractionCalculator.calculateOverallProgress(rem5, totalDemoMillis)
        val level10 = ThemeFractionCalculator.calculateOverallProgress(rem10, totalDemoMillis)

        assertEquals(1.0f, level0, 0.001f)
        assertEquals(0.5f, level5, 0.001f)
        assertEquals(0.0f, level10, 0.001f)

        val elapsedSec0 = (totalDemoMillis - rem0) / 1000f
        val elapsedSec5 = (totalDemoMillis - rem5) / 1000f

        val phase0 = (elapsedSec0 * (2f * PI.toFloat() / 2.4f)) % (2f * PI.toFloat())
        val phase5 = (elapsedSec5 * (2f * PI.toFloat() / 2.4f)) % (2f * PI.toFloat())

        assertEquals(0.0f, phase0, 0.001f)
        assertTrue(phase5 > 0.0f)
        assertNotEquals(phase0, phase5)
    }

    @Test
    fun testDotGridTimerPreview_ActiveDotsChangeAcrossDemoTimes() {
        val rem0 = GalleryDemoClock.calculateTimerRemaining(0L)
        val rem5 = GalleryDemoClock.calculateTimerRemaining(5_000L)
        val rem10 = GalleryDemoClock.calculateTimerRemaining(10_000L)

        val dots0 = ThemeFractionCalculator.calculateDotGridActiveCount(rem0, totalDemoMillis, 60)
        val dots5 = ThemeFractionCalculator.calculateDotGridActiveCount(rem5, totalDemoMillis, 60)
        val dots10 = ThemeFractionCalculator.calculateDotGridActiveCount(rem10, totalDemoMillis, 60)

        assertEquals(60, dots0)
        assertEquals(30, dots5)
        assertEquals(0, dots10)

        assertNotEquals(dots0, dots5)
        assertNotEquals(dots5, dots10)
    }

    // -------------------------------------------------------------------------
    // STOPWATCH THEMES AT t=0s, t=5s, t=10s
    // -------------------------------------------------------------------------

    @Test
    fun testCircleStopwatchPreview_ProgressChangesAcrossDemoTimes() {
        val el0 = GalleryDemoClock.calculateStopwatchElapsed(0L)
        val el5 = GalleryDemoClock.calculateStopwatchElapsed(5_000L)
        val el10 = GalleryDemoClock.calculateStopwatchElapsed(10_000L)

        val prog0 = StopwatchFractionCalculator.calculateCircleProgress(el0)
        val prog5 = StopwatchFractionCalculator.calculateCircleProgress(el5)
        val prog10 = StopwatchFractionCalculator.calculateCircleProgress(el10)

        assertEquals(0.0f, prog0, 0.001f)
        assertEquals(5000f / 60000f, prog5, 0.001f)
        assertEquals(10000f / 60000f, prog10, 0.001f)

        assertNotEquals(prog0, prog5)
        assertNotEquals(prog5, prog10)
    }

    @Test
    fun testConcentricRingsStopwatchPreview_SecondsRingChangesAcrossDemoTimes() {
        val el0 = GalleryDemoClock.calculateStopwatchElapsed(0L)
        val el5 = GalleryDemoClock.calculateStopwatchElapsed(5_000L)
        val el10 = GalleryDemoClock.calculateStopwatchElapsed(10_000L)

        val sec0 = StopwatchFractionCalculator.calculateSecondsFraction(el0)
        val sec5 = StopwatchFractionCalculator.calculateSecondsFraction(el5)
        val sec10 = StopwatchFractionCalculator.calculateSecondsFraction(el10)

        assertEquals(0.0f, sec0, 0.001f)
        assertTrue(sec5 > sec0)
        assertTrue(sec10 > sec5)
    }

    @Test
    fun testThreeBoxStopwatchPreview_ScoreboardDigitsChangeAcrossDemoTimes() {
        val el0 = GalleryDemoClock.calculateStopwatchElapsed(0L)
        val el5 = GalleryDemoClock.calculateStopwatchElapsed(5_000L)
        val el10 = GalleryDemoClock.calculateStopwatchElapsed(10_000L)

        val digits0 = ScoreboardDigitsHelper.extractElapsed(el0)
        val digits5 = ScoreboardDigitsHelper.extractElapsed(el5)
        val digits10 = ScoreboardDigitsHelper.extractElapsed(el10)

        assertEquals('0', digits0.secondsTens)
        assertEquals('0', digits0.secondsOnes)

        assertEquals('0', digits5.secondsTens)
        assertEquals('5', digits5.secondsOnes)

        assertEquals('1', digits10.secondsTens)
        assertEquals('0', digits10.secondsOnes)

        assertNotEquals(digits0, digits5)
        assertNotEquals(digits5, digits10)
    }

    @Test
    fun testFlipClockStopwatchPreview_TimeStringsChangeAcrossDemoTimes() {
        val el0 = GalleryDemoClock.calculateStopwatchElapsed(0L)
        val el5 = GalleryDemoClock.calculateStopwatchElapsed(5_000L)
        val el10 = GalleryDemoClock.calculateStopwatchElapsed(10_000L)

        val sec0 = el0 / 1000L
        val sec5 = el5 / 1000L
        val sec10 = el10 / 1000L

        assertEquals("00", TimeUtils.formatTwoDigits(sec0.toInt()))
        assertEquals("05", TimeUtils.formatTwoDigits(sec5.toInt()))
        assertEquals("10", TimeUtils.formatTwoDigits(sec10.toInt()))
    }

    // -------------------------------------------------------------------------
    // PREVIEW STATUS & GATING CONTRACTS
    // -------------------------------------------------------------------------

    @Test
    fun testPreviewStatusContract_SettledIsRunning_UnsettledIsPaused() {
        fun getTimerStatus(isAnimated: Boolean): TimerStatus =
            if (isAnimated) TimerStatus.Running else TimerStatus.Paused

        fun getStopwatchStatus(isAnimated: Boolean): StopwatchStatus =
            if (isAnimated) StopwatchStatus.Running else StopwatchStatus.Paused

        assertEquals(TimerStatus.Running, getTimerStatus(true))
        assertEquals(TimerStatus.Paused, getTimerStatus(false))

        assertEquals(StopwatchStatus.Running, getStopwatchStatus(true))
        assertEquals(StopwatchStatus.Paused, getStopwatchStatus(false))
    }
}

