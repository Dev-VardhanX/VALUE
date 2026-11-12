package com.devvardhan.value.core

import com.devvardhan.value.model.TimerTheme
import kotlin.math.abs

/**
 * Pure mathematical helper for fullscreen visual and safe area calculations.
 * Ensures:
 * 1. Symmetric padding on both sides:
 *    horizontal = max(cutout left, cutout right) + small margin
 *    vertical = max(cutout top, cutout bottom) + small margin
 * 2. Visual is EXACTLY centered on the screen (visual center == screen center) for symmetric and asymmetric cutouts.
 * 3. Three Box and Flip Clock scale is [FULLSCREEN_SCALE_THREE_BOX_FLIP] (0.95f), applied once to layout size.
 * 4. In portrait, visual is ~95% of original fullscreen size; in landscape, reduced only as needed by symmetric cutout padding.
 * 5. Other themes retain full 100% scale.
 */
object FullscreenSizingHelper {

    /**
     * Scaling factor applied to Three Box and Flip Clock visuals in fullscreen mode.
     * Keeps cells tall and proportional, visually clear, and centered without being too small.
     */
    const val FULLSCREEN_SCALE_THREE_BOX_FLIP = 0.95f

    const val DEFAULT_SMALL_MARGIN = 16f

    data class CutoutInsets(
        val left: Float = 0f,
        val top: Float = 0f,
        val right: Float = 0f,
        val bottom: Float = 0f
    )

    data class SymmetricPadding(
        val horizontal: Float,
        val vertical: Float
    )

    data class FullscreenVisualBounds(
        val safeAreaWidth: Float,
        val safeAreaHeight: Float,
        val visualWidth: Float,
        val visualHeight: Float,
        val visualCenterX: Float,
        val visualCenterY: Float,
        val screenCenterX: Float,
        val screenCenterY: Float,
        val scale: Float
    ) {
        val fitsWithinSafeArea: Boolean
            get() = visualWidth <= safeAreaWidth + 0.001f && visualHeight <= safeAreaHeight + 0.001f

        val isExactlyCentered: Boolean
            get() = abs(visualCenterX - screenCenterX) <= 0.001f &&
                    abs(visualCenterY - screenCenterY) <= 0.001f
    }

    /**
     * Determines whether a theme is scaled down in fullscreen mode.
     */
    fun isScaledTheme(theme: TimerTheme): Boolean {
        return theme == TimerTheme.THREE_BOX || theme == TimerTheme.FLIP_CLOCK
    }

    /**
     * Returns the fullscreen scale factor for the given theme.
     */
    fun getThemeScale(theme: TimerTheme): Float {
        return if (isScaledTheme(theme)) FULLSCREEN_SCALE_THREE_BOX_FLIP else 1.0f
    }

    /**
     * Calculates symmetric horizontal and vertical padding:
     * horizontal = max(cutout left, cutout right) + small margin
     * vertical = max(cutout top, cutout bottom) + small margin
     */
    fun calculateSymmetricPadding(
        insets: CutoutInsets = CutoutInsets(),
        smallMargin: Float = DEFAULT_SMALL_MARGIN
    ): SymmetricPadding {
        val maxHorizontal = maxOf(insets.left, insets.right)
        val maxVertical = maxOf(insets.top, insets.bottom)
        return SymmetricPadding(
            horizontal = maxHorizontal + smallMargin,
            vertical = maxVertical + smallMargin
        )
    }

    /**
     * Calculates the safe area and visual dimensions with exact screen-centering:
     * - Uses symmetric padding so visual center strictly matches screen center.
     * - Multiplies safe area by scale (0.95 for Three Box & Flip Clock, 1.0 for other themes).
     * - Guarantees visual dimensions never exceed safe area.
     */
    fun calculateFullscreenVisualBounds(
        screenWidth: Float,
        screenHeight: Float,
        insets: CutoutInsets = CutoutInsets(),
        smallMargin: Float = DEFAULT_SMALL_MARGIN,
        scale: Float = 1.0f
    ): FullscreenVisualBounds {
        val padding = calculateSymmetricPadding(insets, smallMargin)

        val safeAreaWidth = maxOf(0f, screenWidth - (padding.horizontal * 2f))
        val safeAreaHeight = maxOf(0f, screenHeight - (padding.vertical * 2f))

        val clampedScale = scale.coerceIn(0f, 1.0f)
        val visualWidth = safeAreaWidth * clampedScale
        val visualHeight = safeAreaHeight * clampedScale

        val screenCenterX = screenWidth / 2f
        val screenCenterY = screenHeight / 2f

        val visualCenterX = padding.horizontal + (safeAreaWidth / 2f)
        val visualCenterY = padding.vertical + (safeAreaHeight / 2f)

        return FullscreenVisualBounds(
            safeAreaWidth = safeAreaWidth,
            safeAreaHeight = safeAreaHeight,
            visualWidth = visualWidth,
            visualHeight = visualHeight,
            visualCenterX = visualCenterX,
            visualCenterY = visualCenterY,
            screenCenterX = screenCenterX,
            screenCenterY = screenCenterY,
            scale = clampedScale
        )
    }

    /**
     * Theme-aware overload of [calculateFullscreenVisualBounds].
     */
    fun calculateFullscreenVisualBounds(
        screenWidth: Float,
        screenHeight: Float,
        theme: TimerTheme,
        insets: CutoutInsets = CutoutInsets(),
        smallMargin: Float = DEFAULT_SMALL_MARGIN
    ): FullscreenVisualBounds {
        return calculateFullscreenVisualBounds(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            insets = insets,
            smallMargin = smallMargin,
            scale = getThemeScale(theme)
        )
    }
}
