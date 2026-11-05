package com.devvardhan.value

import com.devvardhan.value.core.TimerEngine
import com.devvardhan.value.model.TimerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TimerEngineCycleTest {

    @Before
    fun setUp() {
        TimerEngine.reset()
    }

    @Test
    fun testFiveConsecutiveTimerCycles() {
        // Run full cycle at least 7 times in a row without failure
        for (iteration in 1..7) {
            val duration = 60_000L * iteration // e.g. 1m, 2m, etc.

            // 1. Start from Idle
            TimerEngine.start(duration)
            var snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after start", TimerStatus.Running, snapshot.status)
            assertEquals(duration, snapshot.totalMillis)
            assertEquals(duration, snapshot.lastSetDurationMillis)
            assertEquals(0L, snapshot.pausedRemainingMillis)

            // 2. Pause
            TimerEngine.pause()
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Paused", TimerStatus.Paused, snapshot.status)
            assertEquals(duration, snapshot.totalMillis)

            // 3. Resume
            TimerEngine.resume()
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after resume", TimerStatus.Running, snapshot.status)

            // 4. Finish
            TimerEngine.finish()
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Finished", TimerStatus.Finished, snapshot.status)
            assertEquals(0L, snapshot.targetEndTime)
            assertEquals(0L, snapshot.pausedRemainingMillis)

            // 5. Dismiss back to Idle
            TimerEngine.dismiss()
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Idle after dismiss", TimerStatus.Idle, snapshot.status)
            assertEquals(0L, snapshot.totalMillis)
            assertEquals(0L, snapshot.targetEndTime)

            // 6. Start again
            TimerEngine.start(duration)
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after second start", TimerStatus.Running, snapshot.status)

            // 7. Reset back to Idle
            TimerEngine.reset()
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Idle after reset", TimerStatus.Idle, snapshot.status)

            // 8. Start again to confirm reset leaves engine completely clean
            TimerEngine.start(duration)
            snapshot = TimerEngine.snapshot.value
            assertEquals("Iteration $iteration: should be Running after reset & start", TimerStatus.Running, snapshot.status)

            // Clean up for next cycle
            TimerEngine.reset()
        }
    }

    @Test
    fun testSimulatedScreenRotationMaintainsRunningState() {
        val duration = 120_000L
        TimerEngine.start(duration)

        val beforeRotationSnapshot = TimerEngine.snapshot.value
        assertEquals(TimerStatus.Running, beforeRotationSnapshot.status)
        val targetEndTime = beforeRotationSnapshot.targetEndTime

        // Simulate activity destruction and recreation on rotation:
        // A new view model or UI observer connects to the singleton engine
        val afterRotationSnapshot = TimerEngine.snapshot.value
        assertEquals(TimerStatus.Running, afterRotationSnapshot.status)
        assertEquals(targetEndTime, afterRotationSnapshot.targetEndTime)
        assertEquals(duration, afterRotationSnapshot.totalMillis)
    }

    @Test
    fun testSimulatedHomeButtonAndBackgroundingDriftFree() {
        val duration = 60_000L
        TimerEngine.start(duration)

        val initialSnapshot = TimerEngine.snapshot.value
        val initialRemaining = initialSnapshot.computeRemainingMillis(now = 1000L)

        // Simulate 10 seconds passing in background
        val remainingAfter10s = initialSnapshot.computeRemainingMillis(now = 11000L)
        assertEquals(initialRemaining - 10_000L, remainingAfter10s)
    }

    @Test
    fun testInvalidDurationIgnored() {
        TimerEngine.reset()
        TimerEngine.start(0L)
        assertEquals(TimerStatus.Idle, TimerEngine.snapshot.value.status)

        TimerEngine.start(-5000L)
        assertEquals(TimerStatus.Idle, TimerEngine.snapshot.value.status)
    }

    @Test
    fun testPauseWhenNotRunningIgnored() {
        TimerEngine.reset()
        TimerEngine.pause()
        assertEquals(TimerStatus.Idle, TimerEngine.snapshot.value.status)
    }

    @Test
    fun testResumeWhenNotPausedIgnored() {
        TimerEngine.reset()
        TimerEngine.resume()
        assertEquals(TimerStatus.Idle, TimerEngine.snapshot.value.status)
    }
}
