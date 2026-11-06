package com.devvardhan.value

import com.devvardhan.value.core.CellSizeHelper
import com.devvardhan.value.core.ThemeLayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CellSizeHelperTest {

    private val screenDimensions = listOf(
        Pair(360f, 640f),   // Standard compact phone
        Pair(412f, 915f),   // Modern tall phone
        Pair(840f, 390f),   // Landscape phone
        Pair(915f, 412f),   // Widescreen landscape phone
        Pair(800f, 1280f),  // Portrait tablet
        Pair(1280f, 800f),  // Landscape tablet
        Pair(224f, 224f),   // Style gallery preview box (portrait 360dp)
        Pair(280f, 280f)    // Style gallery preview box (portrait 412dp)
    )

    @Test
    fun testThreeBoxRowWidthNeverExceedsAvailableWidth() {
        for ((width, height) in screenDimensions) {
            // Test Timer mode
            val timerDims = CellSizeHelper.calculateThreeBoxDimensions(
                availableWidth = width,
                availableHeight = height,
                isStopwatch = false
            )
            assertTrue(
                "Timer row width (${timerDims.totalRowWidth}) must not exceed available width ($width)",
                timerDims.totalRowWidth <= width + 0.1f
            )

            // Test Stopwatch mode with centiseconds slot
            val stopwatchDims = CellSizeHelper.calculateThreeBoxDimensions(
                availableWidth = width,
                availableHeight = height,
                isStopwatch = true
            )
            assertTrue(
                "Stopwatch row width (${stopwatchDims.totalRowWidth}) must not exceed available width ($width)",
                stopwatchDims.totalRowWidth <= width + 0.1f
            )
        }
    }

    @Test
    fun testThreeBoxCellsStrictlyEqual() {
        val dims = CellSizeHelper.calculateThreeBoxDimensions(360f, 640f, isStopwatch = true)
        val panelWidth = dims.panelWidth
        val cellWidth = dims.cellWidth

        // Panel width is derived from 2 equal cells + fixed padding
        assertEquals(panelWidth, (cellWidth * 2f) + 4f + 10f, 0.01f)
        assertTrue("Cell width must be positive", cellWidth > 0f)
        assertTrue("Panel height must be positive", dims.panelHeight > 0f)
    }

    @Test
    fun testThreeBoxGlyphSizeNeverExceedsCellBounds() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateThreeBoxDimensions(width, height, isStopwatch = true)
            assertTrue(
                "Glyph must fit within 80% width and 75% height at ${width}x${height}",
                CellSizeHelper.isGlyphWithinCellBounds(
                    fontSize = dims.fontSize,
                    cellWidth = dims.cellWidth,
                    cellHeight = dims.cellHeight,
                    widthMarginPercent = 0.80f,
                    heightMarginPercent = 0.75f
                )
            )
        }
    }

    @Test
    fun testFlipClockRowWidthNeverExceedsAvailableWidth() {
        for ((width, height) in screenDimensions) {
            // 6 cards (HH:MM:SS) Timer mode
            val timerDims = CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = width,
                availableHeight = height,
                cardCount = 6,
                isStopwatch = false
            )
            assertTrue(
                "Flip clock 6-card row (${timerDims.totalRowWidth}) must not exceed available width ($width)",
                timerDims.totalRowWidth <= width + 0.1f
            )

            // 6 cards Stopwatch mode with centiseconds
            val sw6Dims = CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = width,
                availableHeight = height,
                cardCount = 6,
                isStopwatch = true
            )
            assertTrue(
                "Flip clock 6-card stopwatch (${sw6Dims.totalRowWidth}) must not exceed available width ($width)",
                sw6Dims.totalRowWidth <= width + 0.1f
            )

            // 4 cards (MM:SS) Stopwatch mode with centiseconds
            val sw4Dims = CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = width,
                availableHeight = height,
                cardCount = 4,
                isStopwatch = true
            )
            assertTrue(
                "Flip clock 4-card stopwatch (${sw4Dims.totalRowWidth}) must not exceed available width ($width)",
                sw4Dims.totalRowWidth <= width + 0.1f
            )
        }
    }

    @Test
    fun testFlipClockCardsStrictlyEqual() {
        val dims = CellSizeHelper.calculateFlipClockDimensions(360f, 640f, cardCount = 6, isStopwatch = false)
        assertTrue(dims.cardWidth > 0f)
        assertTrue(dims.cardHeight > dims.cardWidth)
        assertEquals(dims.cardHeight, dims.cardWidth * 1.40f, 0.01f)
    }

    @Test
    fun testFlipClockGlyphSizeNeverExceedsCardBounds() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateFlipClockDimensions(width, height, cardCount = 6, isStopwatch = true)
            assertTrue(
                "Flip clock glyph must fit within 80% width and 75% height at ${width}x${height}",
                CellSizeHelper.isGlyphWithinCellBounds(
                    fontSize = dims.fontSize,
                    cellWidth = dims.cardWidth,
                    cellHeight = dims.cardHeight,
                    widthMarginPercent = 0.80f,
                    heightMarginPercent = 0.75f
                )
            )
        }
    }

    @Test
    fun testPreviewSquareContainerNeverOverflows() {
        // Test 224x224 preview box with compact portrait forced
        val previewDims = CellSizeHelper.calculateFlipClockDimensions(
            availableWidth = 224f,
            availableHeight = 224f,
            cardCount = 6,
            isStopwatch = false,
            layoutMode = ThemeLayoutMode.PORTRAIT
        )
        assertTrue("Preview row width must fit inside 224dp", previewDims.totalRowWidth <= 224f)
        assertTrue("Card width must be scaled to fit", previewDims.cardWidth > 15f)
    }
}

