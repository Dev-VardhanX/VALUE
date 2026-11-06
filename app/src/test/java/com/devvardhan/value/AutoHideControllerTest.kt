package com.devvardhan.value

import com.devvardhan.value.core.AutoHideController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoHideControllerTest {

    private class FakeClock(var currentTime: Long = 1000L) {
        fun now(): Long = currentTime
        fun advance(millis: Long) {
            currentTime += millis
        }
    }

    @Test
    fun testInitialStateIsVisible() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        assertTrue("Controls must be visible initially", controller.isVisible)
        assertEquals(1000L, controller.lastInteractionTime)
    }

    @Test
    fun testHideAfter3SecondsIdle() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        // After 2999ms, must still be visible
        clock.advance(2999L)
        val transitionedAt2999 = controller.checkTimeout(clock.now())
        assertFalse("Must not time out before 3000ms", transitionedAt2999)
        assertTrue(controller.isVisible)

        // Exactly at 3000ms, must hide
        clock.advance(1L)
        val transitionedAt3000 = controller.checkTimeout(clock.now())
        assertTrue("Must time out at 3000ms idle", transitionedAt3000)
        assertFalse("Controls must be hidden after 3 seconds", controller.isVisible)
    }

    @Test
    fun testReappearOnTouchWhenHiddenAndConsumesEvent() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        // Advance clock to trigger auto-hide
        clock.advance(3500L)
        controller.checkTimeout(clock.now())
        assertFalse("Precondition: controls are hidden", controller.isVisible)

        // Touch while hidden: must consume event and reveal controls
        clock.advance(500L)
        val consumed = controller.onTouch()
        assertTrue("Touch while hidden must be consumed to prevent triggering buttons underneath", consumed)
        assertTrue("Controls must reappear on touch", controller.isVisible)
        assertEquals(clock.now(), controller.lastInteractionTime)
    }

    @Test
    fun testTouchWhenVisibleDoesNotConsumeAndRestartsTimer() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        // Advance 2 seconds (still visible)
        clock.advance(2000L)
        assertFalse(controller.checkTimeout(clock.now()))
        assertTrue(controller.isVisible)

        // Touch while visible: must NOT consume so buttons receive clicks
        val consumed = controller.onTouch()
        assertFalse("Touch while visible must NOT be consumed so buttons work", consumed)
        assertTrue(controller.isVisible)
        assertEquals(clock.now(), controller.lastInteractionTime)

        // After another 2 seconds (4 seconds from start, but 2 seconds from touch), controls must still be visible
        clock.advance(2000L)
        assertFalse("Timer must have restarted on touch", controller.checkTimeout(clock.now()))
        assertTrue(controller.isVisible)

        // After another 1000ms (3000ms from touch), it times out
        clock.advance(1000L)
        assertTrue(controller.checkTimeout(clock.now()))
        assertFalse(controller.isVisible)
    }

    @Test
    fun testRestartOnButtonPress() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        // Advance 2500ms
        clock.advance(2500L)
        assertFalse(controller.checkTimeout(clock.now()))
        assertTrue(controller.isVisible)

        // Button is pressed: restarts timer
        controller.onControlPressed()
        assertEquals(3500L, controller.lastInteractionTime)
        assertTrue(controller.isVisible)

        // Advance 2000ms after button press (total 4500ms since start): must still be visible!
        clock.advance(2000L)
        assertFalse("Must stay visible 2s after button press", controller.checkTimeout(clock.now()))
        assertTrue(controller.isVisible)

        // Advance another 1000ms: now times out (3000ms since button press)
        clock.advance(1000L)
        assertTrue("Must time out 3s after button press", controller.checkTimeout(clock.now()))
        assertFalse(controller.isVisible)
    }

    @Test
    fun testManualShowAndHide() {
        val clock = FakeClock(1000L)
        val controller = AutoHideController(timeoutMillis = 3000L, clock = clock::now)

        controller.hide()
        assertFalse(controller.isVisible)

        clock.advance(100L)
        controller.show()
        assertTrue(controller.isVisible)
        assertEquals(1100L, controller.lastInteractionTime)
    }
}

