package com.devvardhan.value.core

/**
 * Pure mathematical helper for responsive, uniform digit cell layout calculations.
 * Ensures:
 * 1. Total row width never exceeds available width.
 * 2. Every digit cell / card in a theme is strictly identical in size.
 * 3. Glyph width <= 80% of cell width and glyph height <= 75% of cell height (no clipping).
 * 4. Centiseconds slot width is cleanly reserved and subtracted before sizing cells.
 */
object CellSizeHelper {

    const val MONOSPACE_GLYPH_WIDTH_FACTOR = 0.60f
    const val MONOSPACE_GLYPH_HEIGHT_FACTOR = 0.70f

    data class ThreeBoxCellDimensions(
        val panelWidth: Float,
        val panelHeight: Float,
        val cellWidth: Float,
        val cellHeight: Float,
        val fontSize: Float,
        val colonWidth: Float,
        val gap: Float,
        val centisSlotWidth: Float,
        val totalRowWidth: Float
    )

    fun calculateThreeBoxDimensions(
        availableWidth: Float,
        availableHeight: Float,
        isStopwatch: Boolean = false,
        layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
    ): ThreeBoxCellDimensions {
        val isLandscape = when (layoutMode) {
            ThemeLayoutMode.AUTO -> availableWidth > availableHeight * 1.15f
            ThemeLayoutMode.PORTRAIT -> false
            ThemeLayoutMode.LANDSCAPE -> true
        }

        val innerCellGap = 4f
        val panelPadding = 10f // 5f left + 5f right
        val colonWidth = 8f
        val gap = if (isLandscape) 6f else 4f
        val gapsCount = if (isStopwatch) 5 else 4
        val centisRatio = 0.85f

        val fixedOverhead = (3 * innerCellGap) + (3 * panelPadding) + (2 * colonWidth) + (gapsCount * gap)
        val effectiveCellDivisor = 6f + (if (isStopwatch) centisRatio else 0f)

        // Initial cell width constrained by available width
        val widthForCells = maxOf(0f, availableWidth - fixedOverhead)
        var cellWidth = widthForCells / effectiveCellDivisor

        // Constrain cell width by available height
        val heightFillFactor = if (isLandscape) 0.80f else 0.88f
        val maxPanelHeight = maxOf(40f, availableHeight * heightFillFactor)
        val labelAndPaddingHeight = 28f
        val maxCellHeightFromContainer = maxOf(20f, maxPanelHeight - labelAndPaddingHeight)
        val maxCellWidthFromHeight = maxCellHeightFromContainer / 1.30f

        cellWidth = minOf(cellWidth, maxCellWidthFromHeight).coerceAtLeast(14f)

        val cellHeight = cellWidth * 1.30f
        val panelWidth = (cellWidth * 2f) + innerCellGap + panelPadding
        val panelHeight = cellHeight + labelAndPaddingHeight
        val centisSlotWidth = if (isStopwatch) cellWidth * centisRatio else 0f
        val totalRowWidth = (panelWidth * 3f) + (colonWidth * 2f) + (gapsCount * gap) + centisSlotWidth

        // Sizing glyph so width <= 75% cellWidth and height <= 70% cellHeight
        val maxFontFromWidth = (cellWidth * 0.75f) / MONOSPACE_GLYPH_WIDTH_FACTOR
        val maxFontFromHeight = (cellHeight * 0.70f) / MONOSPACE_GLYPH_HEIGHT_FACTOR
        val fontSize = minOf(maxFontFromWidth, maxFontFromHeight).coerceAtLeast(10f)

        return ThreeBoxCellDimensions(
            panelWidth = panelWidth,
            panelHeight = panelHeight,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            fontSize = fontSize,
            colonWidth = colonWidth,
            gap = gap,
            centisSlotWidth = centisSlotWidth,
            totalRowWidth = totalRowWidth
        )
    }

    data class FlipClockCellDimensions(
        val cardWidth: Float,
        val cardHeight: Float,
        val fontSize: Float,
        val colonWidth: Float,
        val gap: Float,
        val centisSlotWidth: Float,
        val totalRowWidth: Float
    )

    fun calculateFlipClockDimensions(
        availableWidth: Float,
        availableHeight: Float,
        cardCount: Int,
        isStopwatch: Boolean = false,
        layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
    ): FlipClockCellDimensions {
        val isLandscape = when (layoutMode) {
            ThemeLayoutMode.AUTO -> availableWidth > availableHeight * 1.15f
            ThemeLayoutMode.PORTRAIT -> false
            ThemeLayoutMode.LANDSCAPE -> true
        }

        val colonCount = if (cardCount >= 6) 2 else 1
        val colonWidth = 8f
        val centisRatio = 0.85f

        val totalItems = cardCount + colonCount + (if (isStopwatch) 1 else 0)
        val gapCount = maxOf(1, totalItems - 1)
        val gap = if (isLandscape) 5f else 3.5f

        val fixedOverhead = (colonCount * colonWidth) + (gapCount * gap)
        val effectiveDivisor = cardCount.toFloat() + (if (isStopwatch) centisRatio else 0f)

        val widthForCards = maxOf(0f, availableWidth - fixedOverhead)
        var cardWidth = widthForCards / effectiveDivisor

        // Constrain card width by available height
        val heightFillFactor = if (isLandscape) 0.65f else 0.75f
        val maxCardHeight = maxOf(30f, availableHeight * heightFillFactor)
        val maxCardWidthFromHeight = maxCardHeight / 1.40f

        cardWidth = minOf(cardWidth, maxCardWidthFromHeight).coerceAtLeast(14f)

        val cardHeight = cardWidth * 1.40f
        val centisSlotWidth = if (isStopwatch) cardWidth * centisRatio else 0f
        val totalRowWidth = (cardWidth * cardCount) + (colonCount * colonWidth) + (gapCount * gap) + centisSlotWidth

        // Sizing glyph so width <= 75% cardWidth and height <= 70% cardHeight
        val maxFontFromWidth = (cardWidth * 0.75f) / MONOSPACE_GLYPH_WIDTH_FACTOR
        val maxFontFromHeight = (cardHeight * 0.70f) / MONOSPACE_GLYPH_HEIGHT_FACTOR
        val fontSize = minOf(maxFontFromWidth, maxFontFromHeight).coerceAtLeast(10f)

        return FlipClockCellDimensions(
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            fontSize = fontSize,
            colonWidth = colonWidth,
            gap = gap,
            centisSlotWidth = centisSlotWidth,
            totalRowWidth = totalRowWidth
        )
    }

    /**
     * Sizing helper for circular and concentric ring digits to guarantee they never overflow inner diameter.
     */
    fun calculateInnerRingFontSize(
        innerDiameter: Float,
        hasHours: Boolean,
        isStopwatch: Boolean
    ): Float {
        val usableWidth = maxOf(20f, innerDiameter - 16f)
        val effectiveCharCount = when {
            isStopwatch && hasHours -> 8.2f // H:MM:SS + .cc
            isStopwatch && !hasHours -> 6.2f // MM:SS + .cc
            hasHours -> 8.0f // HH:MM:SS
            else -> 8.0f // HH:MM:SS
        }
        val maxFontFromWidth = (usableWidth * 0.85f) / (effectiveCharCount * MONOSPACE_GLYPH_WIDTH_FACTOR)
        val maxFontFromHeight = innerDiameter * 0.28f
        return minOf(maxFontFromWidth, maxFontFromHeight).coerceIn(14f, 72f)
    }

    fun isGlyphWithinCellBounds(
        fontSize: Float,
        cellWidth: Float,
        cellHeight: Float,
        widthMarginPercent: Float = 0.80f,
        heightMarginPercent: Float = 0.75f
    ): Boolean {
        val glyphWidth = fontSize * MONOSPACE_GLYPH_WIDTH_FACTOR
        val glyphHeight = fontSize * MONOSPACE_GLYPH_HEIGHT_FACTOR
        return (glyphWidth <= cellWidth * widthMarginPercent + 0.01f) &&
               (glyphHeight <= cellHeight * heightMarginPercent + 0.01f)
    }
}

