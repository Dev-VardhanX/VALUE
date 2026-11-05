package com.android.value

import com.android.value.core.ThemeFractionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeFractionCalculatorTest {

    @Test
    fun testOverallProgress() {
        assertEquals(0f, ThemeFractionCalculator.calculateOverallProgress(0L, 10000L), 0.001f)
        assertEquals(1f, ThemeFractionCalculator.calculateOverallProgress(10000L, 10000L), 0.001f)
        assertEquals(0.5f, ThemeFractionCalculator.calculateOverallProgress(5000L, 10000L), 0.001f)
        assertEquals(0f, ThemeFractionCalculator.calculateOverallProgress(1000L, 0L), 0.001f)
    }

    @Test
    fun testSecondsFraction() {
        // Zero remaining
        assertEquals(0f, ThemeFractionCalculator.calculateSecondsFraction(0L), 0.001f)

        // Exact minute boundary (60,000 ms) should be full (1.0)
        assertEquals(1.0f, ThemeFractionCalculator.calculateSecondsFraction(60_000L), 0.001f)

        // 30 seconds remaining
        assertEquals(0.5f, ThemeFractionCalculator.calculateSecondsFraction(30_000L), 0.001f)

        // Across minute boundary: 65,000 ms -> 5,000 ms into minute -> 5/60 = 0.0833f
        assertEquals(5_000f / 60_000f, ThemeFractionCalculator.calculateSecondsFraction(65_000L), 0.001f)

        // 1 ms remaining
        val oneMsFraction = ThemeFractionCalculator.calculateSecondsFraction(1L)
        assertTrue(oneMsFraction > 0f && oneMsFraction < 0.001f)
    }

    @Test
    fun testMinutesFraction() {
        assertEquals(0f, ThemeFractionCalculator.calculateMinutesFraction(0L), 0.001f)
        assertEquals(1.0f, ThemeFractionCalculator.calculateMinutesFraction(3_600_000L), 0.001f)
        assertEquals(0.5f, ThemeFractionCalculator.calculateMinutesFraction(1_800_000L), 0.001f)
    }

    @Test
    fun testHoursFraction() {
        // 0 total hours
        assertEquals(0f, ThemeFractionCalculator.calculateHoursFraction(1_800_000L, 1_800_000L), 0.001f)

        // 2 total hours, 1 hour remaining
        assertEquals(0.5f, ThemeFractionCalculator.calculateHoursFraction(3_600_000L, 7_200_000L), 0.001f)
    }

    @Test
    fun testDotGridActiveCount() {
        assertEquals(0, ThemeFractionCalculator.calculateDotGridActiveCount(0L, 60_000L, 60))
        assertEquals(60, ThemeFractionCalculator.calculateDotGridActiveCount(60_000L, 60_000L, 60))
        assertEquals(30, ThemeFractionCalculator.calculateDotGridActiveCount(30_000L, 60_000L, 60))
        assertEquals(1, ThemeFractionCalculator.calculateDotGridActiveCount(100L, 60_000L, 60))
    }
}

