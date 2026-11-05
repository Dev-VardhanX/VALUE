package com.devvardhan.value

import com.devvardhan.value.core.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun testZeroMillisReturnsZeroFormat() {
        assertEquals("00:00:00", TimeUtils.formatHms(0L))
        assertEquals("00:00:00", TimeUtils.formatHms(-100L))
    }

    @Test
    fun testSecondsCeilingBehavior() {
        assertEquals("00:00:01", TimeUtils.formatHms(1000L))
        assertEquals("00:00:01", TimeUtils.formatHms(200L))
        assertEquals("00:00:02", TimeUtils.formatHms(1050L))
    }

    @Test
    fun testMinutesAndHoursFormatting() {
        // 65 seconds
        assertEquals("00:01:05", TimeUtils.formatHms(64001L))
        // 1 hour, 2 minutes, 3 seconds = 3600 + 120 + 3 = 3723 seconds
        assertEquals("01:02:03", TimeUtils.formatHms(3722001L))
    }

    @Test
    fun testTwoDigitsFormatting() {
        assertEquals("00", TimeUtils.formatTwoDigits(0))
        assertEquals("09", TimeUtils.formatTwoDigits(9))
        assertEquals("10", TimeUtils.formatTwoDigits(10))
        assertEquals("59", TimeUtils.formatTwoDigits(59))
    }
}

