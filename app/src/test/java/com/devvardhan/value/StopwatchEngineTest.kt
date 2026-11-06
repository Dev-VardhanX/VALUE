package com.devvardhan.value

import com.devvardhan.value.core.PersistedStopwatchSession
import com.devvardhan.value.core.ScoreboardDigitsHelper
import com.devvardhan.value.core.StopwatchEngine
import com.devvardhan.value.core.StopwatchFractionCalculator
import com.devvardhan.value.core.StopwatchPreferencesRepository
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StopwatchEngineTest {

    private var fakeCurrentTime = 1_000_000L
    private var fakeWallClockTime = 1_700_000_000_000L

    @Before
    fun setUp() {
        fakeCurrentTime = 1_000_000L
        fakeWallClockTime = 1_700_000_000_000L
        StopwatchEngine.timeProvider = { fakeCurrentTime }
        StopwatchEngine.wallClockProvider = { fakeWallClockTime }
        StopwatchEngine.reset()
    }

    @After
    fun tearDown() {
        StopwatchEngine.reset()
        // Restore default system time providers
        StopwatchEngine.timeProvider = { android.os.SystemClock.elapsedRealtime() }
        StopwatchEngine.wallClockProvider = { System.currentTimeMillis() }
    }

    @Test
    fun testSevenConsecutiveStopwatchCycles() {
        // Run full cycle at least 7 times in a row without failure
        for (iteration in 1..7) {
            // 1. Start from Idle
            StopwatchEngine.start()
            var snapshot = StopwatchEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after start", StopwatchStatus.Running, snapshot.status)
            assertEquals(fakeCurrentTime, snapshot.startTime)
            assertEquals(0L, snapshot.accumulatedMillis)
            assertTrue(snapshot.laps.isEmpty())

            // Advance time by 3 seconds
            fakeCurrentTime += 3000L
            val elapsedAfter3s = snapshot.computeElapsedMillis(fakeCurrentTime)
            assertEquals(3000L, elapsedAfter3s)

            // 2. Record Lap 1
            val lap1 = StopwatchEngine.lap()
            assertNotNull(lap1)
            assertEquals(1, lap1?.lapNumber)
            assertEquals(3000L, lap1?.lapTimeMillis)
            assertEquals(3000L, lap1?.totalTimeMillis)
            assertEquals(1, StopwatchEngine.snapshot.value.laps.size)

            // Advance time by 4 seconds
            fakeCurrentTime += 4000L

            // 3. Pause
            StopwatchEngine.pause()
            snapshot = StopwatchEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Paused", StopwatchStatus.Paused, snapshot.status)
            assertEquals(7000L, snapshot.accumulatedMillis)
            assertEquals(0L, snapshot.startTime)

            // Calling lap while paused must return null
            assertNull("Lap while paused should be null", StopwatchEngine.lap())

            // Advance fake time while paused - elapsed must not change
            fakeCurrentTime += 20_000L
            assertEquals(7000L, snapshot.computeElapsedMillis(fakeCurrentTime))

            // 4. Resume
            StopwatchEngine.resume()
            snapshot = StopwatchEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after resume", StopwatchStatus.Running, snapshot.status)
            assertEquals(fakeCurrentTime, snapshot.startTime)
            assertEquals(7000L, snapshot.accumulatedMillis)

            // Advance time by 2 seconds
            fakeCurrentTime += 2000L
            assertEquals(9000L, snapshot.computeElapsedMillis(fakeCurrentTime))

            // 5. Record Lap 2
            val lap2 = StopwatchEngine.lap()
            assertNotNull(lap2)
            assertEquals(2, lap2?.lapNumber)
            assertEquals(6000L, lap2?.lapTimeMillis) // 9000 - 3000
            assertEquals(9000L, lap2?.totalTimeMillis)
            assertEquals(2, StopwatchEngine.snapshot.value.laps.size)

            // 6. Reset back to Idle
            StopwatchEngine.reset()
            snapshot = StopwatchEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Idle after reset", StopwatchStatus.Idle, snapshot.status)
            assertEquals(0L, snapshot.accumulatedMillis)
            assertEquals(0L, snapshot.startTime)
            assertTrue(snapshot.laps.isEmpty())

            // 7. Verify engine is completely clean and immediately startable again
            StopwatchEngine.start()
            snapshot = StopwatchEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after reset & immediate start", StopwatchStatus.Running, snapshot.status)

            // Clean up for next iteration
            StopwatchEngine.reset()
        }
    }

    @Test
    fun testElapsedAccuracyAndPauseIntervals() {
        // Start at t=1000
        fakeCurrentTime = 1000L
        StopwatchEngine.start()

        // Advance 2.5s -> t=3500
        fakeCurrentTime = 3500L
        var snapshot = StopwatchEngine.snapshot.value
        assertEquals(2500L, snapshot.computeElapsedMillis(fakeCurrentTime))

        // Pause at t=3500
        StopwatchEngine.pause()
        snapshot = StopwatchEngine.snapshot.value
        assertEquals(StopwatchStatus.Paused, snapshot.status)
        assertEquals(2500L, snapshot.accumulatedMillis)

        // Advance by 1 hour while paused (t=3,603,500)
        fakeCurrentTime += 3_600_000L
        assertEquals("Elapsed must remain exactly 2500ms during pause", 2500L, snapshot.computeElapsedMillis(fakeCurrentTime))

        // Resume at t=3,603,500
        StopwatchEngine.resume()
        snapshot = StopwatchEngine.snapshot.value
        assertEquals(StopwatchStatus.Running, snapshot.status)
        assertEquals(fakeCurrentTime, snapshot.startTime)
        assertEquals(2500L, snapshot.accumulatedMillis)

        // Advance another 5 seconds
        fakeCurrentTime += 5000L
        assertEquals(7500L, snapshot.computeElapsedMillis(fakeCurrentTime))
    }

    @Test
    fun testLapSplitCalculationAndFastestSlowestIdentification() {
        StopwatchEngine.start()

        // Lap 1 at 5000ms: split = 5000, total = 5000
        fakeCurrentTime += 5000L
        val lap1 = StopwatchEngine.lap()
        assertNotNull(lap1)
        assertEquals(1, lap1?.lapNumber)
        assertEquals(5000L, lap1?.lapTimeMillis)
        assertEquals(5000L, lap1?.totalTimeMillis)

        // Lap 2 at 12000ms (+7000ms): split = 7000, total = 12000
        fakeCurrentTime += 7000L
        val lap2 = StopwatchEngine.lap()
        assertNotNull(lap2)
        assertEquals(2, lap2?.lapNumber)
        assertEquals(7000L, lap2?.lapTimeMillis)
        assertEquals(12000L, lap2?.totalTimeMillis)

        // Lap 3 at 16000ms (+4000ms): split = 4000, total = 16000
        fakeCurrentTime += 4000L
        val lap3 = StopwatchEngine.lap()
        assertNotNull(lap3)
        assertEquals(3, lap3?.lapNumber)
        assertEquals(4000L, lap3?.lapTimeMillis)
        assertEquals(16000L, lap3?.totalTimeMillis)

        val allLaps = StopwatchEngine.snapshot.value.laps
        assertEquals(3, allLaps.size)
        // Newest on top
        assertEquals(3, allLaps[0].lapNumber)
        assertEquals(2, allLaps[1].lapNumber)
        assertEquals(1, allLaps[2].lapNumber)

        // Verify Best (fastest split) and Worst (slowest split)
        val minSplit = allLaps.minOf { it.lapTimeMillis }
        val maxSplit = allLaps.maxOf { it.lapTimeMillis }
        val bestLap = allLaps.first { it.lapTimeMillis == minSplit }
        val worstLap = allLaps.first { it.lapTimeMillis == maxSplit }

        assertEquals("Best lap should be Lap 3 with 4000ms split", 3, bestLap.lapNumber)
        assertEquals("Worst lap should be Lap 2 with 7000ms split", 2, worstLap.lapNumber)
    }

    @Test
    fun testMax99LapsEnforced() {
        StopwatchEngine.start()

        // Add 99 laps
        for (i in 1..99) {
            fakeCurrentTime += 1000L
            val lap = StopwatchEngine.lap()
            assertNotNull("Lap $i should be recorded", lap)
            assertEquals(i, lap?.lapNumber)
        }

        assertEquals(99, StopwatchEngine.snapshot.value.laps.size)

        // Attempting 100th lap should return null and remain at 99 laps
        fakeCurrentTime += 1000L
        val lap100 = StopwatchEngine.lap()
        assertNull("100th lap must return null (capped at 99)", lap100)
        assertEquals(99, StopwatchEngine.snapshot.value.laps.size)
    }

    @Test
    fun testFormattingAdaptiveAndHourBoundary() {
        // 0ms
        assertEquals("00:00.00", TimeUtils.formatStopwatchAdaptive(0L))
        assertEquals("00:00", TimeUtils.formatStopwatchMainUnits(0L))
        assertEquals("00", TimeUtils.formatStopwatchCentiseconds(0L))

        // 1 second, 5 centiseconds (1050ms)
        assertEquals("00:01.05", TimeUtils.formatStopwatchAdaptive(1050L))
        assertEquals("00:01", TimeUtils.formatStopwatchMainUnits(1050L))
        assertEquals("05", TimeUtils.formatStopwatchCentiseconds(1050L))

        // 59 minutes, 59 seconds, 99 centiseconds (3,599,990ms) - Under 1 hour
        val justUnderOneHour = 59 * 60 * 1000L + 59 * 1000L + 990L
        assertEquals("59:59.99", TimeUtils.formatStopwatchAdaptive(justUnderOneHour))
        assertEquals("59:59", TimeUtils.formatStopwatchMainUnits(justUnderOneHour))
        assertEquals("99", TimeUtils.formatStopwatchCentiseconds(justUnderOneHour))

        // Exactly 1 hour (3,600,000ms) - 1 hour boundary (hours revealed)
        val exactlyOneHour = 3_600_000L
        assertEquals("1:00:00.00", TimeUtils.formatStopwatchAdaptive(exactlyOneHour))
        assertEquals("1:00:00", TimeUtils.formatStopwatchMainUnits(exactlyOneHour))
        assertEquals("00", TimeUtils.formatStopwatchCentiseconds(exactlyOneHour))

        // 12 hours, 34 minutes, 56 seconds, 78 centiseconds
        val complexTime = 12 * 3600 * 1000L + 34 * 60 * 1000L + 56 * 1000L + 780L
        assertEquals("12:34:56.78", TimeUtils.formatStopwatchAdaptive(complexTime))
        assertEquals("12:34:56", TimeUtils.formatStopwatchMainUnits(complexTime))
        assertEquals("78", TimeUtils.formatStopwatchCentiseconds(complexTime))

        // Full 8-digit format test
        assertEquals("00:00:00.00", TimeUtils.formatStopwatchFull(0L))
        assertEquals("01:00:00.00", TimeUtils.formatStopwatchFull(exactlyOneHour))
        assertEquals("12:34:56.78", TimeUtils.formatStopwatchFull(complexTime))
    }

    @Test
    fun testScoreboardDigitsElapsed() {
        // Test extraction for odometer/scoreboard flip
        val digitsZero = ScoreboardDigitsHelper.extractElapsed(0L)
        assertEquals('0', digitsZero.hoursTens)
        assertEquals('0', digitsZero.hoursOnes)
        assertEquals('0', digitsZero.minutesTens)
        assertEquals('0', digitsZero.minutesOnes)
        assertEquals('0', digitsZero.secondsTens)
        assertEquals('0', digitsZero.secondsOnes)

        val digitsElapsed = ScoreboardDigitsHelper.extractElapsed(3723000L) // 1h 2m 3s
        assertEquals('0', digitsElapsed.hoursTens)
        assertEquals('1', digitsElapsed.hoursOnes)
        assertEquals('0', digitsElapsed.minutesTens)
        assertEquals('2', digitsElapsed.minutesOnes)
        assertEquals('0', digitsElapsed.secondsTens)
        assertEquals('3', digitsElapsed.secondsOnes)
    }

    @Test
    fun testSessionRestorationNormalProcessDeath() {
        val session = PersistedStopwatchSession(
            status = StopwatchStatus.Running,
            startTime = 500_000L,
            accumulatedMillis = 10_000L,
            wallClockStartTime = 1_700_000_000_000L,
            laps = listOf(LapItem(1, 10_000L, 10_000L))
        )

        // Fake time is 600_000L (> startTime 500_000L)
        fakeCurrentTime = 600_000L
        StopwatchEngine.restoreSession(session)

        val snapshot = StopwatchEngine.snapshot.value
        assertEquals(StopwatchStatus.Running, snapshot.status)
        assertEquals(500_000L, snapshot.startTime)
        assertEquals(10_000L, snapshot.accumulatedMillis)
        assertEquals(1, snapshot.laps.size)

        // Elapsed = 10_000 + (600_000 - 500_000) = 110_000L
        assertEquals(110_000L, snapshot.computeElapsedMillis(fakeCurrentTime))
    }

    @Test
    fun testSessionRestorationRebootFallback() {
        // Device rebooted: elapsedRealtime reset to small number (e.g. 50_000L),
        // but session.startTime was 900_000L from before reboot.
        val session = PersistedStopwatchSession(
            status = StopwatchStatus.Running,
            startTime = 900_000L,
            accumulatedMillis = 20_000L,
            wallClockStartTime = 1_700_000_000_000L,
            laps = emptyList()
        )

        fakeCurrentTime = 50_000L // elapsedRealtime is now < startTime!
        fakeWallClockTime = 1_700_000_060_000L // 60 seconds of wall clock elapsed

        StopwatchEngine.restoreSession(session)

        val snapshot = StopwatchEngine.snapshot.value
        // Should pause cleanly instead of crashing or showing negative time
        assertEquals(StopwatchStatus.Paused, snapshot.status)
        // Total accumulated = 20_000 + 60_000 = 80_000L
        assertEquals(80_000L, snapshot.accumulatedMillis)
    }

    @Test
    fun testSessionRestorationPaused() {
        val session = PersistedStopwatchSession(
            status = StopwatchStatus.Paused,
            startTime = 0L,
            accumulatedMillis = 42_000L,
            wallClockStartTime = 0L,
            laps = listOf(LapItem(1, 42_000L, 42_000L))
        )

        StopwatchEngine.restoreSession(session)
        val snapshot = StopwatchEngine.snapshot.value
        assertEquals(StopwatchStatus.Paused, snapshot.status)
        assertEquals(42_000L, snapshot.accumulatedMillis)
        assertEquals(1, snapshot.laps.size)
    }

    @Test
    fun testLapSerializationAndDeserialization() {
        val laps = listOf(
            LapItem(lapNumber = 3, lapTimeMillis = 3500L, totalTimeMillis = 15000L),
            LapItem(lapNumber = 2, lapTimeMillis = 5000L, totalTimeMillis = 11500L),
            LapItem(lapNumber = 1, lapTimeMillis = 6500L, totalTimeMillis = 6500L)
        )

        val serialized = StopwatchPreferencesRepository.serializeLaps(laps)
        val deserialized = StopwatchPreferencesRepository.deserializeLaps(serialized)

        assertEquals(laps.size, deserialized.size)
        for (i in laps.indices) {
            assertEquals(laps[i].lapNumber, deserialized[i].lapNumber)
            assertEquals(laps[i].lapTimeMillis, deserialized[i].lapTimeMillis)
            assertEquals(laps[i].totalTimeMillis, deserialized[i].totalTimeMillis)
        }

        // Test null or empty string
        assertTrue(StopwatchPreferencesRepository.deserializeLaps(null).isEmpty())
        assertTrue(StopwatchPreferencesRepository.deserializeLaps("").isEmpty())
    }

    @Test
    fun testStopwatchFractionCalculator() {
        // Circle theme: sweeps 60 seconds
        assertEquals(0f, StopwatchFractionCalculator.calculateCircleProgress(0L), 0.001f)
        assertEquals(0.5f, StopwatchFractionCalculator.calculateCircleProgress(30_000L), 0.001f)
        assertEquals(0f, StopwatchFractionCalculator.calculateCircleProgress(60_000L), 0.001f) // wraps
        assertEquals(0.25f, StopwatchFractionCalculator.calculateCircleProgress(75_000L), 0.001f)

        // Concentric Rings
        // Seconds fraction: 0..60s
        assertEquals(0f, StopwatchFractionCalculator.calculateSecondsFraction(0L), 0.001f)
        assertEquals(0.25f, StopwatchFractionCalculator.calculateSecondsFraction(15_000L), 0.001f)
        assertEquals(0.75f, StopwatchFractionCalculator.calculateSecondsFraction(45_000L), 0.001f)

        // Minutes fraction: 0..60m
        assertEquals(0f, StopwatchFractionCalculator.calculateMinutesFraction(0L), 0.001f)
        assertEquals(0.5f, StopwatchFractionCalculator.calculateMinutesFraction(30 * 60_000L), 0.001f)

        // Hours fraction: 0..12h
        assertEquals(0f, StopwatchFractionCalculator.calculateHoursFraction(0L), 0.001f)
        assertEquals(0.5f, StopwatchFractionCalculator.calculateHoursFraction(6 * 3_600_000L), 0.001f)
    }

    @Test
    fun testFiveConsecutiveCyclesWithFullscreenAutoHideSequence() {
        val autoHide = com.devvardhan.value.core.AutoHideController(
            timeoutMillis = 3000L,
            clock = { fakeCurrentTime }
        )

        for (cycle in 1..7) {
            // --- Normal Control Row Verification ---
            // 1. Initial Idle
            var controls = com.devvardhan.value.core.AdaptiveLayoutHelper.getStopwatchControlRowState(
                StopwatchStatus.Idle,
                lapsCount = 0
            )
            assertEquals("Cycle $cycle: Main should be START", com.devvardhan.value.core.AdaptiveLayoutHelper.StopwatchControlRowState.MainAction.START, controls.mainAction)
            assertTrue(controls.isMainEnabled)
            assertFalse(controls.isLapEnabled)
            assertFalse(controls.isResetEnabled)

            // START
            StopwatchEngine.start()
            fakeCurrentTime += 2500L

            // 2. Running state: Reset must be enabled while running
            controls = com.devvardhan.value.core.AdaptiveLayoutHelper.getStopwatchControlRowState(
                StopwatchStatus.Running,
                lapsCount = 0
            )
            assertEquals("Cycle $cycle: Main should be PAUSE", com.devvardhan.value.core.AdaptiveLayoutHelper.StopwatchControlRowState.MainAction.PAUSE, controls.mainAction)
            assertTrue(controls.isMainEnabled)
            assertTrue(controls.isLapEnabled)
            assertTrue("Reset must be enabled while Running", controls.isResetEnabled)

            // LAP
            val lap = StopwatchEngine.lap()
            assertNotNull("Cycle $cycle: Lap must be recorded", lap)
            assertEquals(1, StopwatchEngine.snapshot.value.laps.size)

            fakeCurrentTime += 1500L

            // In even cycles, test Reset directly while Running
            if (cycle % 2 == 0) {
                StopwatchEngine.reset()
                assertEquals("Cycle $cycle: Must be Idle after reset while running", StopwatchStatus.Idle, StopwatchEngine.snapshot.value.status)
                assertEquals(0, StopwatchEngine.snapshot.value.laps.size)
                assertEquals(0L, StopwatchEngine.snapshot.value.computeElapsedMillis(fakeCurrentTime))
            } else {
                // PAUSE
                StopwatchEngine.pause()
                controls = com.devvardhan.value.core.AdaptiveLayoutHelper.getStopwatchControlRowState(
                    StopwatchStatus.Paused,
                    lapsCount = 1
                )
                assertEquals("Cycle $cycle: Main should be RESUME", com.devvardhan.value.core.AdaptiveLayoutHelper.StopwatchControlRowState.MainAction.RESUME, controls.mainAction)
                assertTrue(controls.isMainEnabled)
                assertFalse(controls.isLapEnabled)
                assertTrue(controls.isResetEnabled)

                // RESUME
                StopwatchEngine.resume()
                fakeCurrentTime += 1000L

                // RESET from Paused
                StopwatchEngine.pause()
                StopwatchEngine.reset()
                assertEquals("Cycle $cycle: Must be Idle after reset", StopwatchStatus.Idle, StopwatchEngine.snapshot.value.status)
                assertEquals(0, StopwatchEngine.snapshot.value.laps.size)
            }

            // --- Fullscreen Auto-Hide Simulation in this cycle ---
            autoHide.show()
            assertTrue("Cycle $cycle: Fullscreen controls visible on entry", autoHide.isVisible)

            // Let 3s pass: controls must auto-hide
            fakeCurrentTime += 3000L
            assertTrue(autoHide.checkTimeout(fakeCurrentTime))
            assertFalse("Cycle $cycle: Controls should be hidden after 3s", autoHide.isVisible)

            // Screen touch: must be consumed to reveal controls, without triggering buttons
            val consumed = autoHide.onTouch()
            assertTrue("Cycle $cycle: Reveal touch must be consumed", consumed)
            assertTrue("Cycle $cycle: Controls must reappear", autoHide.isVisible)

            // Button press restarts the auto-hide timer
            autoHide.onControlPressed()
            fakeCurrentTime += 2000L
            assertFalse("Cycle $cycle: Controls still visible 2s after press", autoHide.checkTimeout(fakeCurrentTime))
            fakeCurrentTime += 1000L
            assertTrue("Cycle $cycle: Controls hide 3s after press", autoHide.checkTimeout(fakeCurrentTime))
            assertFalse(autoHide.isVisible)
        }
    }
}

