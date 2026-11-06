package com.devvardhan.value

import com.devvardhan.value.core.CellSizeHelper
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.model.TimerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CellSizeHelperTest {

    private val testWidths = listOf(
        120f,  // Small preview
        150f,
        200f,  // Card preview
        224f,  // Gallery square preview (compact phone)
        280f,  // Gallery wide preview
        320f,  // Small phone portrait
        360f,  // Standard compact phone
        390f,  // Modern phone
        412f,  // Pixel / tall phone
        600f,  // Foldable unfolded
        800f,  // Small tablet / landscape
        915f,  // Widescreen landscape phone
        1080f, // Large screen
        1280f  // Tablet landscape
    )

    private val screenDimensions = listOf(
        Pair(120f, 80f),    // Small preview
        Pair(200f, 133f),   // Card preview
        Pair(224f, 224f),   // Gallery preview box (360dp)
        Pair(280f, 280f),   // Gallery preview box (412dp)
        Pair(360f, 640f),   // Standard compact phone
        Pair(412f, 915f),   // Modern tall phone
        Pair(840f, 390f),   // Landscape phone
        Pair(915f, 412f),   // Widescreen landscape phone
        Pair(800f, 1280f),  // Portrait tablet
        Pair(1280f, 800f)   // Landscape tablet
    )

    @Test
    fun testThreeBoxRowWidthNeverExceedsAvailableWidthAcrossAllWidths() {
        for (width in testWidths) {
            val height = 400f

            // Test Timer mode
            val timerDims = CellSizeHelper.calculateThreeBoxDimensions(
                availableWidth = width,
                availableHeight = height,
                isStopwatch = false
            )
            assertTrue(
                "Timer ThreeBox totalRowWidth (${timerDims.totalRowWidth}) must not exceed available width ($width)",
                timerDims.totalRowWidth <= width + 0.05f
            )

            // Test Stopwatch mode with centiseconds slot
            val stopwatchDims = CellSizeHelper.calculateThreeBoxDimensions(
                availableWidth = width,
                availableHeight = height,
                isStopwatch = true
            )
            assertTrue(
                "Stopwatch ThreeBox totalRowWidth (${stopwatchDims.totalRowWidth}) must not exceed available width ($width)",
                stopwatchDims.totalRowWidth <= width + 0.05f
            )
        }
    }

    @Test
    fun testThreeBoxAspectRatioStrictlyOneToOnePointSix() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateThreeBoxDimensions(width, height, isStopwatch = false)
            assertTrue("Cell width must be positive at ${width}x${height}", dims.cellWidth > 0f)
            assertTrue("Cell height must be positive at ${width}x${height}", dims.cellHeight > 0f)
            assertEquals(
                "Aspect ratio cellHeight / cellWidth must be 1.6 at ${width}x${height}",
                1.60f,
                dims.cellHeight / dims.cellWidth,
                0.01f
            )
        }
    }

    @Test
    fun testThreeBoxCellsAndPanelsStrictlyEqual() {
        val dims = CellSizeHelper.calculateThreeBoxDimensions(360f, 640f, isStopwatch = true)
        val panelWidth = dims.panelWidth
        val cellWidth = dims.cellWidth

        // Panel width is derived from 2 equal cells + inner gap + 2 * padding
        val expectedPanelWidth = (cellWidth * 2f) + dims.innerCellGap + (2f * dims.panelPaddingHorizontal)
        assertEquals(expectedPanelWidth, panelWidth, 0.01f)
        assertTrue("Cell width must be positive", cellWidth > 0f)
        assertTrue("Panel height must be positive", dims.panelHeight > 0f)
    }

    @Test
    fun testThreeBoxGlyphSizeNeverExceedsCellBoundsForDigits0Through9() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateThreeBoxDimensions(width, height, isStopwatch = true)
            // Monospace tabular figures 0-9 all share identical advance width and height bounds
            val glyphWidth = dims.fontSize * CellSizeHelper.MONOSPACE_GLYPH_WIDTH_FACTOR
            val glyphHeight = dims.fontSize * CellSizeHelper.MONOSPACE_GLYPH_HEIGHT_FACTOR

            assertTrue(
                "Glyph width ($glyphWidth) must be <= 75% of cell width (${dims.cellWidth}) at ${width}x${height}",
                glyphWidth <= (dims.cellWidth * 0.75f) + 0.05f
            )
            assertTrue(
                "Glyph height ($glyphHeight) must be <= 70% of cell height (${dims.cellHeight}) at ${width}x${height}",
                glyphHeight <= (dims.cellHeight * 0.70f) + 0.05f
            )
            assertTrue(
                "Helper isGlyphWithinCellBounds must return true at ${width}x${height}",
                CellSizeHelper.isGlyphWithinCellBounds(dims.fontSize, dims.cellWidth, dims.cellHeight)
            )
        }
    }

    @Test
    fun testFlipClockRowWidthNeverExceedsAvailableWidthAcrossAllWidths() {
        for (width in testWidths) {
            val height = 400f

            // 6 cards (HH:MM:SS) Timer mode
            val timerDims = CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = width,
                availableHeight = height,
                cardCount = 6,
                isStopwatch = false
            )
            assertTrue(
                "FlipClock Timer 6-card row (${timerDims.totalRowWidth}) must not exceed available width ($width)",
                timerDims.totalRowWidth <= width + 0.05f
            )

            // 6 cards Stopwatch mode with centiseconds
            val sw6Dims = CellSizeHelper.calculateFlipClockDimensions(
                availableWidth = width,
                availableHeight = height,
                cardCount = 6,
                isStopwatch = true
            )
            assertTrue(
                "FlipClock Stopwatch 6-card row (${sw6Dims.totalRowWidth}) must not exceed available width ($width)",
                sw6Dims.totalRowWidth <= width + 0.05f
            )
        }
    }

    @Test
    fun testFlipClockAspectRatioStrictlyOneToOnePointFour() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateFlipClockDimensions(width, height, cardCount = 6, isStopwatch = false)
            assertTrue("Card width must be positive at ${width}x${height}", dims.cardWidth > 0f)
            assertTrue("Card height must be positive at ${width}x${height}", dims.cardHeight > 0f)
            assertEquals(
                "Aspect ratio cardHeight / cardWidth must be 1.4 at ${width}x${height}",
                1.40f,
                dims.cardHeight / dims.cardWidth,
                0.01f
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
    fun testFlipClockGlyphSizeNeverExceedsCardBoundsForDigits0Through9() {
        for ((width, height) in screenDimensions) {
            val dims = CellSizeHelper.calculateFlipClockDimensions(width, height, cardCount = 6, isStopwatch = true)
            val glyphWidth = dims.fontSize * CellSizeHelper.MONOSPACE_GLYPH_WIDTH_FACTOR
            val glyphHeight = dims.fontSize * CellSizeHelper.MONOSPACE_GLYPH_HEIGHT_FACTOR

            assertTrue(
                "FlipClock glyph width ($glyphWidth) must be <= 75% of card width (${dims.cardWidth}) at ${width}x${height}",
                glyphWidth <= (dims.cardWidth * 0.75f) + 0.05f
            )
            assertTrue(
                "FlipClock glyph height ($glyphHeight) must be <= 70% of card height (${dims.cardHeight}) at ${width}x${height}",
                glyphHeight <= (dims.cardHeight * 0.70f) + 0.05f
            )
            assertTrue(
                "Helper isGlyphWithinCellBounds must return true for FlipClock at ${width}x${height}",
                CellSizeHelper.isGlyphWithinCellBounds(dims.fontSize, dims.cardWidth, dims.cardHeight)
            )
        }
    }

    @Test
    fun testConcentricRingsSafeAreaFitFormula() {
        val testRadii = listOf(20f, 35f, 50f, 75f, 100f, 150f, 200f)

        for (radius in testRadii) {
            // Timer string "00:00:07" with label "TOTAL 01:00:00"
            val timerFontSize = CellSizeHelper.calculateInnerRingSafeFontSize(
                innerRadius = radius,
                hasHours = false,
                isStopwatch = false,
                hasLabel = true
            )
            val timerTextWidth = 8.0f * CellSizeHelper.MONOSPACE_GLYPH_WIDTH_FACTOR * timerFontSize
            val timerTextHeight = 1.55f * timerFontSize

            assertTrue(
                "Timer text box ($timerTextWidth x $timerTextHeight) must fit inside safe circle of radius ${radius * 0.82f}",
                CellSizeHelper.isTextBoxWithinInnerRingSafeCircle(timerTextWidth, timerTextHeight, radius)
            )

            // Stopwatch string "00:01" + centis ".00" with "PAUSED" label
            val swFontSize = CellSizeHelper.calculateInnerRingSafeFontSize(
                innerRadius = radius,
                hasHours = false,
                isStopwatch = true,
                hasLabel = true
            )
            val swTextWidth = 6.5f * CellSizeHelper.MONOSPACE_GLYPH_WIDTH_FACTOR * swFontSize
            val swTextHeight = 1.55f * swFontSize

            assertTrue(
                "Stopwatch text box ($swTextWidth x $swTextHeight) must fit inside safe circle of radius ${radius * 0.82f}",
                CellSizeHelper.isTextBoxWithinInnerRingSafeCircle(swTextWidth, swTextHeight, radius)
            )
        }
    }

    @Test
    fun testStopwatchStylesListDoesNotContainDotGrid() {
        val stopwatchThemes = TimerTheme.stopwatchThemes
        assertEquals("Stopwatch must have exactly 4 themes", 4, stopwatchThemes.size)
        assertTrue(stopwatchThemes.contains(TimerTheme.CIRCLE))
        assertTrue(stopwatchThemes.contains(TimerTheme.CONCENTRIC_RINGS))
        assertTrue(stopwatchThemes.contains(TimerTheme.THREE_BOX))
        assertTrue(stopwatchThemes.contains(TimerTheme.FLIP_CLOCK))
        assertFalse("Dot Grid must NOT be in stopwatch themes", stopwatchThemes.contains(TimerTheme.DOT_GRID))
        assertFalse("Hourglass must NOT be in stopwatch themes", stopwatchThemes.contains(TimerTheme.HOURGLASS))
        assertFalse("Liquid Fill must NOT be in stopwatch themes", stopwatchThemes.contains(TimerTheme.LIQUID_FILL))
    }

    @Test
    fun testStopwatchMigrationFallback() {
        // If a saved stopwatch style is DOT_GRID, HOURGLASS, LIQUID_FILL or unknown, it must fall back to CIRCLE
        val fallbackForDotGrid = if (TimerTheme.DOT_GRID in TimerTheme.stopwatchThemes) {
            TimerTheme.DOT_GRID
        } else {
            TimerTheme.CIRCLE
        }
        assertEquals(TimerTheme.CIRCLE, fallbackForDotGrid)

        val fallbackForHourglass = if (TimerTheme.HOURGLASS in TimerTheme.stopwatchThemes) {
            TimerTheme.HOURGLASS
        } else {
            TimerTheme.CIRCLE
        }
        assertEquals(TimerTheme.CIRCLE, fallbackForHourglass)

        val fallbackForLiquidFill = if (TimerTheme.LIQUID_FILL in TimerTheme.stopwatchThemes) {
            TimerTheme.LIQUID_FILL
        } else {
            TimerTheme.CIRCLE
        }
        assertEquals(TimerTheme.CIRCLE, fallbackForLiquidFill)
    }
}
