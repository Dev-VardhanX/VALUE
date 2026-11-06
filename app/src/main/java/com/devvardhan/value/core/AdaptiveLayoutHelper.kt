package com.devvardhan.value.core

import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme

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
        val innerRadius = (middleRadius - gapSafe(middleRadius, ringGap)).coerceAtLeast(10f)
        val innerDiameter = innerRadius * 2f
        val maxFontSize = (innerDiameter * 0.20f).coerceIn(16f, 60f)

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

    private fun gapSafe(radius: Float, gap: Float): Float {
        return if (radius - gap < 10f) maxOf(0f, radius - 10f) else gap
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
        availableHeight: Float,
        isStopwatch: Boolean = false
    ): ThreeBoxDimensions {
        val landscape = isLandscape(availableWidth, availableHeight, 1.15f)
        return if (landscape) {
            val panelHeight = (availableHeight * 0.82f).coerceAtLeast(110f)
            val centisAllowance = if (isStopwatch) 48f else 0f
            val panelWidth = minOf((availableWidth - 64f - centisAllowance) / 3.1f, panelHeight * 0.95f).coerceAtLeast(65f)
            val fontSize = (panelHeight * 0.38f).coerceIn(24f, 72f)
            ThreeBoxDimensions(panelWidth = panelWidth, panelHeight = panelHeight, fontSize = fontSize)
        } else {
            val centisAllowance = if (isStopwatch) 52f else 0f
            val panelWidth = ((availableWidth - 36f - centisAllowance) / 3.05f).coerceIn(60f, 150f)
            val panelHeight = minOf(panelWidth * 1.32f, availableHeight * 0.92f).coerceIn(88f, 240f)
            val fontSize = (panelHeight * 0.40f).coerceIn(24f, 64f)
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

    data class StopwatchPortraitLayout(
        val visualHeight: Float,
        val lapListMinHeight: Float,
        val controlsHeight: Float,
        val headerHeight: Float
    )

    /**
     * Calculates vertical height distribution for the Stopwatch screen in portrait mode.
     * Guarantees the lap list receives at least 2-3 rows (minLapListHeight), shrinking the visual first if needed.
     */
    fun calculateStopwatchPortraitHeights(
        totalHeight: Float,
        headerHeight: Float = 84f,
        controlsHeight: Float = 72f,
        minLapListHeight: Float = 140f
    ): StopwatchPortraitLayout {
        val remainingForVisualAndLaps = (totalHeight - headerHeight - controlsHeight).coerceAtLeast(minLapListHeight + 120f)
        val targetVisualHeight = totalHeight * 0.42f
        val maxVisualHeight = (remainingForVisualAndLaps - minLapListHeight).coerceAtLeast(120f)
        val actualVisualHeight = minOf(targetVisualHeight, maxVisualHeight).coerceIn(120f, 360f)
        val remainingForLaps = (remainingForVisualAndLaps - actualVisualHeight).coerceAtLeast(minLapListHeight)

        return StopwatchPortraitLayout(
            visualHeight = actualVisualHeight,
            lapListMinHeight = remainingForLaps,
            controlsHeight = controlsHeight,
            headerHeight = headerHeight
        )
    }

    data class StopwatchControlRowState(
        val mainAction: MainAction,
        val isMainEnabled: Boolean,
        val isLapEnabled: Boolean,
        val isResetEnabled: Boolean
    ) {
        enum class MainAction { START, PAUSE, RESUME }
    }

    /**
     * Resolves the stopwatch control row states:
     * - LEFT: Flag/Lap button (enabled ONLY while running, capped at maxLaps)
     * - CENTER: Main pill button (Start when Idle, Pause when Running, Resume when Paused; always enabled)
     * - RIGHT: Reset button (enabled when Paused or when laps exist)
     */
    fun getStopwatchControlRowState(
        status: StopwatchStatus,
        lapsCount: Int = 0,
        maxLaps: Int = 99
    ): StopwatchControlRowState {
        val mainAction = when (status) {
            StopwatchStatus.Idle -> StopwatchControlRowState.MainAction.START
            StopwatchStatus.Running -> StopwatchControlRowState.MainAction.PAUSE
            StopwatchStatus.Paused -> StopwatchControlRowState.MainAction.RESUME
        }
        val isLapEnabled = (status == StopwatchStatus.Running) && (lapsCount < maxLaps)
        val isResetEnabled = (status == StopwatchStatus.Paused) || (status == StopwatchStatus.Idle && lapsCount > 0)

        return StopwatchControlRowState(
            mainAction = mainAction,
            isMainEnabled = true,
            isLapEnabled = isLapEnabled,
            isResetEnabled = isResetEnabled
        )
    }

    /**
     * Returns the styles displayed in the gallery based on the mode.
     * Timer: All 7 styles.
     * Stopwatch: 5 stopwatch-compatible styles (excluding Hourglass and Liquid Fill).
     */
    fun getGalleryStyles(mode: AppMode): List<TimerTheme> {
        return when (mode) {
            AppMode.TIMER -> TimerTheme.entries
            AppMode.STOPWATCH -> TimerTheme.stopwatchThemes
        }
    }

    /**
     * Determines whether the Style Gallery should be shown at application launch.
     * Shows on very first launch only when no style was previously chosen AND
     * neither timer nor stopwatch is currently running.
     */
    fun shouldShowGalleryAtLaunch(
        hasChosenStyle: Boolean,
        isTimerRunning: Boolean,
        isStopwatchRunning: Boolean
    ): Boolean {
        if (isTimerRunning || isStopwatchRunning) return false
        return !hasChosenStyle
    }
}
