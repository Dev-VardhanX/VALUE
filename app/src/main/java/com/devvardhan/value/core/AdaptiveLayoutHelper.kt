package com.devvardhan.value.core

/**
 * Pure calculation helpers for adaptive portrait, landscape, and tablet responsive layouts.
 * Decoupled from Compose runtime to allow comprehensive JVM unit testing.
 */
object AdaptiveLayoutHelper {

    fun isLandscape(width: Float, height: Float, threshold: Float = 1.15f): Boolean {
        return width > height * threshold
    }

    fun calculateSquareFit(
        width: Float,
        height: Float,
        padding: Float = 32f,
        minSize: Float = 120f
    ): Float {
        val available = minOf(width, height) - padding
        return maxOf(minSize, available)
    }

    data class ConcentricRingsGeometry(
        val diameter: Float,
        val strokeWidth: Float,
        val ringGap: Float,
        val outerRadius: Float,
        val middleRadius: Float,
        val innerRadius: Float,
        val innerDiameter: Float,
        val maxFontSize: Float
    )

    fun calculateConcentricRingsGeometry(
        width: Float,
        height: Float,
        padding: Float = 32f
    ): ConcentricRingsGeometry {
        val diameter = calculateSquareFit(width, height, padding = padding, minSize = 120f)
        val strokeWidth = (diameter * 0.016f).coerceIn(2.5f, 7.5f)
        val ringGap = (diameter * 0.055f).coerceIn(10f, 26f)
        val outerRadius = (diameter / 2f) - strokeWidth
        val middleRadius = (outerRadius - ringGap).coerceAtLeast(10f)
        val innerRadius = (middleRadius - ringGap).coerceAtLeast(10f)
        val innerDiameter = innerRadius * 2f
        val maxFontSize = (innerDiameter * 0.22f).coerceIn(18f, 68f)

        return ConcentricRingsGeometry(
            diameter = diameter,
            strokeWidth = strokeWidth,
            ringGap = ringGap,
            outerRadius = outerRadius,
            middleRadius = middleRadius,
            innerRadius = innerRadius,
            innerDiameter = innerDiameter,
            maxFontSize = maxFontSize
        )
    }

    data class HourglassDimensions(
        val width: Float,
        val height: Float,
        val fontSize: Float,
        val isRowLayout: Boolean
    )

    fun calculateHourglassDimensions(
        availableWidth: Float,
        availableHeight: Float
    ): HourglassDimensions {
        val landscape = isLandscape(availableWidth, availableHeight, 1.2f)
        return if (landscape) {
            val h = (availableHeight * 0.82f).coerceAtLeast(120f)
            val w = h * 0.62f
            val fontSize = (availableHeight * 0.22f).coerceIn(26f, 72f)
            HourglassDimensions(width = w, height = h, fontSize = fontSize, isRowLayout = true)
        } else {
            val h = (availableHeight * 0.46f).coerceIn(160f, 380f)
            val w = h * 0.62f
            val fontSize = (availableWidth * 0.12f).coerceIn(28f, 54f)
            HourglassDimensions(width = w, height = h, fontSize = fontSize, isRowLayout = false)
        }
    }

    data class ThreeBoxDimensions(
        val panelWidth: Float,
        val panelHeight: Float,
        val fontSize: Float
    )

    fun calculateThreeBoxDimensions(
        availableWidth: Float,
        availableHeight: Float
    ): ThreeBoxDimensions {
        val landscape = isLandscape(availableWidth, availableHeight, 1.15f)
        return if (landscape) {
            val panelHeight = (availableHeight * 0.72f).coerceAtLeast(110f)
            val panelWidth = minOf((availableWidth - 80f) / 3.2f, panelHeight * 0.95f).coerceAtLeast(70f)
            val fontSize = (panelHeight * 0.38f).coerceIn(26f, 72f)
            ThreeBoxDimensions(panelWidth = panelWidth, panelHeight = panelHeight, fontSize = fontSize)
        } else {
            val panelWidth = ((availableWidth - 48f) / 3.15f).coerceIn(70f, 160f)
            val panelHeight = (panelWidth * 1.35f).coerceIn(95f, 220f)
            val fontSize = (panelHeight * 0.38f).coerceIn(24f, 56f)
            ThreeBoxDimensions(panelWidth = panelWidth, panelHeight = panelHeight, fontSize = fontSize)
        }
    }

    data class FlipClockDimensions(
        val cardWidth: Float,
        val cardHeight: Float,
        val fontSize: Float
    )

    fun calculateFlipClockDimensions(
        availableWidth: Float,
        availableHeight: Float
    ): FlipClockDimensions {
        val landscape = isLandscape(availableWidth, availableHeight, 1.15f)
        return if (landscape) {
            val targetHeight = (availableHeight * 0.58f).coerceAtLeast(80f)
            val cardWidth = minOf((availableWidth - 72f) / 7.2f, targetHeight / 1.45f).coerceAtLeast(36f)
            val cardHeight = cardWidth * 1.45f
            val fontSize = (cardHeight * 0.62f).coerceIn(22f, 88f)
            FlipClockDimensions(cardWidth = cardWidth, cardHeight = cardHeight, fontSize = fontSize)
        } else {
            val cardWidth = ((availableWidth - 48f) / 7.2f).coerceIn(34f, 75f)
            val cardHeight = cardWidth * 1.45f
            val fontSize = (cardHeight * 0.62f).coerceIn(24f, 54f)
            FlipClockDimensions(cardWidth = cardWidth, cardHeight = cardHeight, fontSize = fontSize)
        }
    }

    data class LiquidFillDimensions(
        val containerWidth: Float,
        val containerHeight: Float,
        val fontSize: Float
    )

    fun calculateLiquidFillDimensions(
        availableWidth: Float,
        availableHeight: Float
    ): LiquidFillDimensions {
        val landscape = isLandscape(availableWidth, availableHeight, 1.15f)
        return if (landscape) {
            val w = (availableWidth * 0.88f).coerceAtLeast(200f)
            val h = (availableHeight * 0.78f).coerceAtLeast(130f)
            val fontSize = minOf(w * 0.12f, h * 0.30f).coerceIn(28f, 70f)
            LiquidFillDimensions(containerWidth = w, containerHeight = h, fontSize = fontSize)
        } else {
            val w = (availableWidth * 0.86f).coerceAtLeast(200f)
            val h = minOf(availableHeight * 0.60f, w * 1.15f).coerceAtLeast(180f)
            val fontSize = (w * 0.14f).coerceIn(28f, 56f)
            LiquidFillDimensions(containerWidth = w, containerHeight = h, fontSize = fontSize)
        }
    }

    data class DotGridMatrix(
        val columns: Int,
        val rows: Int,
        val isRowLayout: Boolean,
        val gridWidth: Float,
        val gridHeight: Float,
        val fontSize: Float
    )

    fun calculateDotGridMatrix(
        availableWidth: Float,
        availableHeight: Float
    ): DotGridMatrix {
        val landscape = isLandscape(availableWidth, availableHeight, 1.15f)
        return if (landscape) {
            val columns = 12
            val rows = 5 // 12 * 5 = 60 dots
            val gridWidth = (availableWidth * 0.52f).coerceAtLeast(160f)
            val gridHeight = minOf(availableHeight * 0.75f, gridWidth * 0.55f).coerceAtLeast(100f)
            val fontSize = (availableHeight * 0.20f).coerceIn(24f, 68f)
            DotGridMatrix(
                columns = columns,
                rows = rows,
                isRowLayout = true,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                fontSize = fontSize
            )
        } else {
            val columns = 10
            val rows = 6 // 10 * 6 = 60 dots
            val gridWidth = (availableWidth * 0.88f).coerceAtLeast(180f)
            val gridHeight = minOf(availableHeight * 0.45f, gridWidth * 0.60f).coerceAtLeast(110f)
            val fontSize = (availableWidth * 0.13f).coerceIn(28f, 58f)
            DotGridMatrix(
                columns = columns,
                rows = rows,
                isRowLayout = false,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                fontSize = fontSize
            )
        }
    }
}

