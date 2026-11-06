package com.devvardhan.value.core

import kotlin.math.sqrt

/**
 * Pure mathematical helper for responsive, uniform digit cell layout calculations.
 * Ensures:
 * 1. Total row width never exceeds available width under any container constraints.
 * 2. Every digit cell / card in a theme is strictly identical in size.
 * 3. Exact tall aspect ratios: Three Box = 1 : 1.6, Flip Clock = 1 : 1.4.
 * 4. Glyph width <= 75% of cell width and glyph height <= 70% of cell height (no clipping).
 * 5. Concentric Rings safe-area sizing: (textWidth/2)^2 + (textHeight/2)^2 <= (innerRadius * 0.82)^2.
 * 6. Centiseconds slot width is cleanly reserved and subtracted before sizing cells.
 */
object CellSizeHelper {

    // Monospace tabular figures advance width and cap-height factors
    const val MONOSPACE_GLYPH_WIDTH_FACTOR = 0.60f
    const val MONOSPACE_GLYPH_HEIGHT_FACTOR = 0.70f

    // Target aspect ratios
    const val THREE_BOX_ASPECT_RATIO = 1.60f // height = width * 1.6
    const val FLIP_CLOCK_ASPECT_RATIO = 1.40f // height = width * 1.4

    // Limits for glyph fit inside cell
    const val MAX_GLYPH_WIDTH_RATIO = 0.75f // glyph width <= 75% of cell width
    const val MAX_GLYPH_HEIGHT_RATIO = 0.70f // glyph height <= 70% of cell height

    /**
     * Shared validation helper ensuring that the sum of cells, gaps, and padding
     * never exceeds the available width.
     */
    fun validateRowWidthWithinBounds(totalRowWidth: Float, availableWidth: Float): Boolean {
        return totalRowWidth <= availableWidth + 0.05f
    }

    data class ThreeBoxCellDimensions(
        val panelWidth: Float,
        val panelHeight: Float,
        val cellWidth: Float,
        val cellHeight: Float,
        val fontSize: Float,
        val colonWidth: Float,
        val gap: Float,
        val centisSlotWidth: Float,
        val totalRowWidth: Float,
        val labelRowHeight: Float,
        val panelPaddingHorizontal: Float,
        val panelPaddingVertical: Float,
        val innerCellGap: Float
    )

    /**
     * Sizes Three Box scoreboard:
     * - Tall cells with 1 : 1.6 aspect ratio.
     * - First computes cellWidth from available width.
     * - cellHeight = min(cellWidth * 1.6, availableHeight - labelRow - padding).
     * - If height is the limit, reduces cellWidth = cellHeight / 1.6 to keep the 1 : 1.6 aspect ratio.
     * - All 6 cells identical, all 3 panels identical.
     * - Never distorts, never exceeds available width or height.
     */
    fun calculateThreeBoxDimensions(
        availableWidth: Float,
        availableHeight: Float,
        isStopwatch: Boolean = false,
        layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
    ): ThreeBoxCellDimensions {
        val widthScale = (availableWidth / 360f).coerceIn(0.20f, 1.20f)
        val innerCellGap = (4f * widthScale).coerceAtLeast(1f)
        val panelPaddingH = (5f * widthScale).coerceAtLeast(1.5f)
        val panelPaddingV = (5f * widthScale).coerceAtLeast(1.5f)
        val colonWidth = (6f * widthScale).coerceAtLeast(2f)
        val gap = (4f * widthScale).coerceAtLeast(1f)
        val labelRowHeight = (15f * widthScale).coerceAtLeast(6f)

        val gapsCount = if (isStopwatch) 5 else 4
        val centisRatio = 0.80f

        val panelOverheadWidth = innerCellGap + (2f * panelPaddingH)
        val panelOverheadHeight = labelRowHeight + (2f * panelPaddingV)
        val fixedOverheadWidth = (3f * panelOverheadWidth) + (2f * colonWidth) + (gapsCount * gap)

        // Width-limited cell width
        val widthForCells = maxOf(0f, availableWidth - fixedOverheadWidth)
        val effectiveCellDivisor = 6f + (if (isStopwatch) centisRatio else 0f)
        val cellWidthFromWidth = widthForCells / effectiveCellDivisor

        // Height-limited cell width
        val maxCellHeightFromHeight = maxOf(1f, availableHeight - panelOverheadHeight)
        val maxCellWidthFromHeight = maxCellHeightFromHeight / THREE_BOX_ASPECT_RATIO

        // If height is the limit, reduce cellWidth to keep the aspect ratio
        var cellWidth = minOf(cellWidthFromWidth, maxCellWidthFromHeight)
        var cellHeight = cellWidth * THREE_BOX_ASPECT_RATIO

        // Verify total row width does not exceed available width
        var panelWidth = (cellWidth * 2f) + panelOverheadWidth
        var panelHeight = cellHeight + panelOverheadHeight
        var centisSlotWidth = if (isStopwatch) cellWidth * centisRatio else 0f
        var totalRowWidth = (panelWidth * 3f) + (colonWidth * 2f) + (gapsCount * gap) + centisSlotWidth

        if (!validateRowWidthWithinBounds(totalRowWidth, availableWidth) && totalRowWidth > 0f) {
            val scale = availableWidth / totalRowWidth
            cellWidth *= scale
            cellHeight = cellWidth * THREE_BOX_ASPECT_RATIO
            panelWidth = (cellWidth * 2f) + panelOverheadWidth
            panelHeight = cellHeight + panelOverheadHeight
            centisSlotWidth = if (isStopwatch) cellWidth * centisRatio else 0f
            totalRowWidth = (panelWidth * 3f) + (colonWidth * 2f) + (gapsCount * gap) + centisSlotWidth
        }

        // Font size: widest digit (0-9) <= 75% cellWidth, glyph height <= 70% cellHeight
        val maxFontFromWidth = (cellWidth * MAX_GLYPH_WIDTH_RATIO) / MONOSPACE_GLYPH_WIDTH_FACTOR
        val maxFontFromHeight = (cellHeight * MAX_GLYPH_HEIGHT_RATIO) / MONOSPACE_GLYPH_HEIGHT_FACTOR
        val fontSize = minOf(maxFontFromWidth, maxFontFromHeight)

        return ThreeBoxCellDimensions(
            panelWidth = panelWidth,
            panelHeight = panelHeight,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            fontSize = fontSize,
            colonWidth = colonWidth,
            gap = gap,
            centisSlotWidth = centisSlotWidth,
            totalRowWidth = totalRowWidth,
            labelRowHeight = labelRowHeight,
            panelPaddingHorizontal = panelPaddingH,
            panelPaddingVertical = panelPaddingV,
            innerCellGap = innerCellGap
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

    /**
     * Sizes Flip Clock:
     * - Always shows 6 cards (HH:MM:SS) in both timer and stopwatch.
     * - Tall flip cards with 1 : 1.4 aspect ratio.
     * - All cards identical.
     * - Font size sized so glyph <= 75% width and <= 70% height.
     */
    fun calculateFlipClockDimensions(
        availableWidth: Float,
        availableHeight: Float,
        cardCount: Int = 6,
        isStopwatch: Boolean = false,
        layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
    ): FlipClockCellDimensions {
        val widthScale = (availableWidth / 360f).coerceIn(0.20f, 1.20f)
        val colonWidth = (6f * widthScale).coerceAtLeast(2f)
        val gap = (4f * widthScale).coerceAtLeast(1f)
        val centisRatio = 0.80f

        val colonCount = 2 // Always 2 colons for 6 cards (HH:MM:SS)
        val totalItems = 6 + colonCount + (if (isStopwatch) 1 else 0)
        val gapCount = totalItems - 1

        val fixedOverhead = (colonCount * colonWidth) + (gapCount * gap)
        val effectiveDivisor = 6f + (if (isStopwatch) centisRatio else 0f)

        // Width-limited card width
        val widthForCards = maxOf(0f, availableWidth - fixedOverhead)
        val cardWidthFromWidth = widthForCards / effectiveDivisor

        // Height-limited card width
        val maxCardHeightFromHeight = maxOf(1f, availableHeight)
        val maxCardWidthFromHeight = maxCardHeightFromHeight / FLIP_CLOCK_ASPECT_RATIO

        var cardWidth = minOf(cardWidthFromWidth, maxCardWidthFromHeight)
        var cardHeight = cardWidth * FLIP_CLOCK_ASPECT_RATIO
        var centisSlotWidth = if (isStopwatch) cardWidth * centisRatio else 0f
        var totalRowWidth = (cardWidth * 6f) + (colonCount * colonWidth) + (gapCount * gap) + centisSlotWidth

        if (!validateRowWidthWithinBounds(totalRowWidth, availableWidth) && totalRowWidth > 0f) {
            val scale = availableWidth / totalRowWidth
            cardWidth *= scale
            cardHeight = cardWidth * FLIP_CLOCK_ASPECT_RATIO
            centisSlotWidth = if (isStopwatch) cardWidth * centisRatio else 0f
            totalRowWidth = (cardWidth * 6f) + (colonCount * colonWidth) + (gapCount * gap) + centisSlotWidth
        }

        // Font size: widest digit (0-9) <= 75% cardWidth, glyph height <= 70% cardHeight
        val maxFontFromWidth = (cardWidth * MAX_GLYPH_WIDTH_RATIO) / MONOSPACE_GLYPH_WIDTH_FACTOR
        val maxFontFromHeight = (cardHeight * MAX_GLYPH_HEIGHT_RATIO) / MONOSPACE_GLYPH_HEIGHT_FACTOR
        val fontSize = minOf(maxFontFromWidth, maxFontFromHeight)

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
     * Sizing helper for Concentric Rings text:
     * Calculates the exact font size such that the entire text box
     * (digits + centis slot + label) fits inside the inner ring circle,
     * satisfying: (textWidth/2)^2 + (textHeight/2)^2 <= (innerRadius * 0.82)^2.
     */
    fun calculateInnerRingSafeFontSize(
        innerRadius: Float,
        hasHours: Boolean,
        isStopwatch: Boolean,
        hasLabel: Boolean = false
    ): Float {
        val safeRadius = innerRadius * 0.82f
        if (safeRadius <= 1f) return 10f

        // Effective char count:
        // Timer: "HH:MM:SS" = 8 chars
        // Stopwatch with hours: "H:MM:SS" (7) + ".00" (1.2) = 8.2 chars
        // Stopwatch without hours: "00:01" (5) + ".00" (1.2) = 6.2 chars
        val effectiveCharCount = when {
            isStopwatch && hasHours -> 8.2f
            isStopwatch && !hasHours -> 6.5f
            else -> 8.0f // Timer HH:MM:SS
        }

        val widthFactor = (effectiveCharCount * MONOSPACE_GLYPH_WIDTH_FACTOR) / 2f
        val heightRatio = if (hasLabel) 1.55f else 1.0f
        val heightFactor = heightRatio / 2f

        val diagonalFactor = sqrt(widthFactor * widthFactor + heightFactor * heightFactor)
        val maxSafeFontSize = safeRadius / diagonalFactor

        return maxSafeFontSize
    }

    /**
     * Validates whether a monospace glyph of the given font size fits within the cell bounds.
     */
    fun isGlyphWithinCellBounds(
        fontSize: Float,
        cellWidth: Float,
        cellHeight: Float,
        widthMarginPercent: Float = MAX_GLYPH_WIDTH_RATIO,
        heightMarginPercent: Float = MAX_GLYPH_HEIGHT_RATIO
    ): Boolean {
        val glyphWidth = fontSize * MONOSPACE_GLYPH_WIDTH_FACTOR
        val glyphHeight = fontSize * MONOSPACE_GLYPH_HEIGHT_FACTOR
        return (glyphWidth <= cellWidth * widthMarginPercent + 0.05f) &&
            (glyphHeight <= cellHeight * heightMarginPercent + 0.05f)
    }

    /**
     * Validates whether a text box of (textWidth, textHeight) fits within the inner ring safe circle.
     */
    fun isTextBoxWithinInnerRingSafeCircle(
        textWidth: Float,
        textHeight: Float,
        innerRadius: Float,
        safetyFactor: Float = 0.82f
    ): Boolean {
        val halfW = textWidth / 2f
        val halfH = textHeight / 2f
        val radiusSquared = halfW * halfW + halfH * halfH
        val safeRadius = innerRadius * safetyFactor
        return radiusSquared <= (safeRadius * safeRadius) + 0.05f
    }
}
