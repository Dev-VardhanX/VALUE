package com.devvardhan.value

import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.model.StopwatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutHelperTest {

    @Test
    fun testIsLandscapeOrientationDetection() {
        // Portrait phone (390 x 844)
        assertFalse(AdaptiveLayoutHelper.isLandscape(390f, 844f))

        // Landscape phone (844 x 390)
        assertTrue(AdaptiveLayoutHelper.isLandscape(844f, 390f))

        // Square display (600 x 600)
        assertFalse(AdaptiveLayoutHelper.isLandscape(600f, 600f))

        // Landscape tablet (1280 x 800)
        assertTrue(AdaptiveLayoutHelper.isLandscape(1280f, 800f))

        // Portrait tablet (800 x 1280)
        assertFalse(AdaptiveLayoutHelper.isLandscape(800f, 1280f))
    }

    @Test
    fun testSquareFitCalculation() {
        // In landscape (800w x 400h, padding=32), min is 400 - 32 = 368
        val fitLandscape = AdaptiveLayoutHelper.calculateSquareFit(800f, 400f, padding = 32f)
        assertEquals(368f, fitLandscape, 0.01f)

        // In portrait (400w x 800h, padding=32), min is 400 - 32 = 368
        val fitPortrait = AdaptiveLayoutHelper.calculateSquareFit(400f, 800f, padding = 32f)
        assertEquals(368f, fitPortrait, 0.01f)

        // Very small constraint falls back to minSize
        val fitSmall = AdaptiveLayoutHelper.calculateSquareFit(100f, 100f, padding = 32f, minSize = 120f)
        assertEquals(120f, fitSmall, 0.01f)
    }

    @Test
    fun testConcentricRingsGeometryNeverOverlapsDigits() {
        val geometry = AdaptiveLayoutHelper.calculateConcentricRingsGeometry(400f, 400f, padding = 32f)

        // Diameter must be 368
        assertEquals(368f, geometry.diameter, 0.01f)

        // Inner radius must be strictly positive and smaller than middle radius
        assertTrue(geometry.innerRadius > 0f)
        assertTrue(geometry.middleRadius > geometry.innerRadius)
        assertTrue(geometry.outerRadius > geometry.middleRadius)

        // Ring gap and stroke width are proportional
        assertTrue(geometry.strokeWidth in 2.5f..7.5f)
        assertTrue(geometry.ringGap in 10f..26f)

        // Font size must stay within inner diameter bound
        assertTrue(geometry.maxFontSize < geometry.innerDiameter)
    }

    @Test
    fun testHourglassDimensionsResponsiveLayout() {
        // Landscape phone
        val dimsLandscape = AdaptiveLayoutHelper.calculateHourglassDimensions(800f, 400f)
        assertTrue(dimsLandscape.isRowLayout)
        assertTrue(dimsLandscape.height > 200f)
        assertTrue(dimsLandscape.width > 100f)

        // Portrait phone
        val dimsPortrait = AdaptiveLayoutHelper.calculateHourglassDimensions(400f, 800f)
        assertFalse(dimsPortrait.isRowLayout)
        assertTrue(dimsPortrait.height in 160f..380f)
    }

    @Test
    fun testThreeBoxDimensionsResponsiveLayout() {
        // Landscape: 3 boxes row layout
        val dimsLandscape = AdaptiveLayoutHelper.calculateThreeBoxDimensions(840f, 390f)
        assertTrue(dimsLandscape.panelWidth in 70f..300f)
        assertTrue(dimsLandscape.panelHeight in 110f..350f)

        // Portrait: 3 boxes row layout
        val dimsPortrait = AdaptiveLayoutHelper.calculateThreeBoxDimensions(390f, 840f)
        assertTrue(dimsPortrait.panelWidth in 70f..160f)
        assertTrue(dimsPortrait.panelHeight in 90f..220f)
    }

    @Test
    fun testFlipClockDimensionsResponsiveLayout() {
        val dimsLandscape = AdaptiveLayoutHelper.calculateFlipClockDimensions(840f, 390f)
        assertTrue(dimsLandscape.cardWidth in 36f..120f)
        assertTrue(dimsLandscape.cardHeight > dimsLandscape.cardWidth)

        val dimsPortrait = AdaptiveLayoutHelper.calculateFlipClockDimensions(390f, 840f)
        assertTrue(dimsPortrait.cardWidth in 34f..75f)
        assertTrue(dimsPortrait.cardHeight > dimsPortrait.cardWidth)
    }

    @Test
    fun testLiquidFillDimensionsResponsiveLayout() {
        val dimsLandscape = AdaptiveLayoutHelper.calculateLiquidFillDimensions(840f, 390f)
        assertTrue(dimsLandscape.containerWidth > dimsLandscape.containerHeight)

        val dimsPortrait = AdaptiveLayoutHelper.calculateLiquidFillDimensions(390f, 840f)
        assertTrue(dimsPortrait.containerHeight >= dimsPortrait.containerWidth * 0.8f)
    }

    @Test
    fun testDotGridMatrix60DotsInvariant() {
        // Landscape (12 x 5 = 60 dots)
        val landscapeGrid = AdaptiveLayoutHelper.calculateDotGridMatrix(840f, 390f)
        assertEquals(60, landscapeGrid.columns * landscapeGrid.rows)
        assertTrue(landscapeGrid.isRowLayout)

        // Portrait (10 x 6 = 60 dots)
        val portraitGrid = AdaptiveLayoutHelper.calculateDotGridMatrix(390f, 840f)
        assertEquals(60, portraitGrid.columns * portraitGrid.rows)
        assertFalse(portraitGrid.isRowLayout)
    }

    @Test
    fun testStopwatchPortraitHeightsDistribution() {
        // Standard 640dp height small phone
        val layout640 = AdaptiveLayoutHelper.calculateStopwatchPortraitHeights(640f)
        assertEquals(84f, layout640.headerHeight, 0.01f)
        assertEquals(72f, layout640.controlsHeight, 0.01f)
        assertTrue("Visual height should be about 42% on 640dp", layout640.visualHeight in 240f..280f)
        assertTrue("Lap list must have at least 140dp for 2-3 rows", layout640.lapListMinHeight >= 140f)

        // Tall 844dp phone
        val layout844 = AdaptiveLayoutHelper.calculateStopwatchPortraitHeights(844f)
        assertTrue("Visual height should be ~354dp on 844dp", layout844.visualHeight in 340f..360f)
        assertTrue("Lap list receives remaining height", layout844.lapListMinHeight > 300f)

        // Extremely short screen (e.g. 400dp height): visual must shrink, never lap list below 140dp
        val layoutShort = AdaptiveLayoutHelper.calculateStopwatchPortraitHeights(400f)
        assertTrue("Lap list guaranteed at least 140dp", layoutShort.lapListMinHeight >= 140f)
        assertTrue("Visual shrinks to accommodate lap list", layoutShort.visualHeight <= 140f)
    }

    @Test
    fun testStopwatchButtonsStateTransitions() {
        // 1. Idle state: 1 full-width Start button
        val idleState = AdaptiveLayoutHelper.getStopwatchButtonsState(StopwatchStatus.Idle)
        assertTrue(idleState.isFullWidthSingleButton)
        assertEquals(AdaptiveLayoutHelper.StopwatchButtonAction.START, idleState.primaryAction)
        assertTrue(idleState.isPrimaryEnabled)
        assertNull(idleState.secondaryAction)

        // 2. Running state with 0 laps: Lap (enabled) + Pause (primary)
        val runningState0 = AdaptiveLayoutHelper.getStopwatchButtonsState(StopwatchStatus.Running, lapsCount = 0)
        assertFalse(runningState0.isFullWidthSingleButton)
        assertEquals(AdaptiveLayoutHelper.StopwatchButtonAction.PAUSE, runningState0.primaryAction)
        assertEquals(AdaptiveLayoutHelper.StopwatchButtonAction.LAP, runningState0.secondaryAction)
        assertTrue(runningState0.isPrimaryEnabled)
        assertTrue(runningState0.isSecondaryEnabled)

        // 3. Running state with 99 laps (maxed): Lap button becomes disabled
        val runningState99 = AdaptiveLayoutHelper.getStopwatchButtonsState(StopwatchStatus.Running, lapsCount = 99)
        assertTrue(runningState99.isPrimaryEnabled)
        assertFalse("Lap button disabled at 99 laps", runningState99.isSecondaryEnabled)

        // 4. Paused state: Reset (secondary) + Resume (primary)
        val pausedState = AdaptiveLayoutHelper.getStopwatchButtonsState(StopwatchStatus.Paused, lapsCount = 10)
        assertFalse(pausedState.isFullWidthSingleButton)
        assertEquals(AdaptiveLayoutHelper.StopwatchButtonAction.RESUME, pausedState.primaryAction)
        assertEquals(AdaptiveLayoutHelper.StopwatchButtonAction.RESET, pausedState.secondaryAction)
        assertTrue(pausedState.isPrimaryEnabled)
        assertTrue(pausedState.isSecondaryEnabled)
    }
}
