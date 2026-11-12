package com.devvardhan.value.core

import com.devvardhan.value.model.TimerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FullscreenSizingHelperTest {

    @Test
    fun `three box and flip clock scale is 0_95`() {
        assertEquals(0.95f, FullscreenSizingHelper.FULLSCREEN_SCALE_THREE_BOX_FLIP, 0.0001f)
        assertTrue(FullscreenSizingHelper.isScaledTheme(TimerTheme.THREE_BOX))
        assertTrue(FullscreenSizingHelper.isScaledTheme(TimerTheme.FLIP_CLOCK))
        assertEquals(0.95f, FullscreenSizingHelper.getThemeScale(TimerTheme.THREE_BOX), 0.0001f)
        assertEquals(0.95f, FullscreenSizingHelper.getThemeScale(TimerTheme.FLIP_CLOCK), 0.0001f)
    }

    @Test
    fun `other themes retain full scale of 1_0`() {
        val nonScaledThemes = listOf(
            TimerTheme.CIRCLE,
            TimerTheme.CONCENTRIC_RINGS,
            TimerTheme.HOURGLASS,
            TimerTheme.LIQUID_FILL,
            TimerTheme.DOT_GRID
        )
        for (theme in nonScaledThemes) {
            assertFalse("Theme $theme should not be scaled", FullscreenSizingHelper.isScaledTheme(theme))
            assertEquals(
                "Theme $theme scale should be 1.0",
                1.0f,
                FullscreenSizingHelper.getThemeScale(theme),
                0.0001f
            )
        }
    }

    @Test
    fun `symmetric padding for asymmetric cutouts`() {
        // Left only (landscape rotation 1, camera on left: 48dp)
        val leftOnly = FullscreenSizingHelper.calculateSymmetricPadding(
            insets = FullscreenSizingHelper.CutoutInsets(left = 48f, top = 0f, right = 0f, bottom = 0f),
            smallMargin = 16f
        )
        assertEquals(64f, leftOnly.horizontal, 0.001f) // max(48, 0) + 16
        assertEquals(16f, leftOnly.vertical, 0.001f)   // max(0, 0) + 16

        // Right only (landscape rotation 3, camera on right: 48dp)
        val rightOnly = FullscreenSizingHelper.calculateSymmetricPadding(
            insets = FullscreenSizingHelper.CutoutInsets(left = 0f, top = 0f, right = 48f, bottom = 0f),
            smallMargin = 16f
        )
        assertEquals(64f, rightOnly.horizontal, 0.001f) // max(0, 48) + 16
        assertEquals(16f, rightOnly.vertical, 0.001f)   // max(0, 0) + 16

        // Top only (portrait notch: 44dp)
        val topOnly = FullscreenSizingHelper.calculateSymmetricPadding(
            insets = FullscreenSizingHelper.CutoutInsets(left = 0f, top = 44f, right = 0f, bottom = 0f),
            smallMargin = 16f
        )
        assertEquals(16f, topOnly.horizontal, 0.001f) // max(0, 0) + 16
        assertEquals(60f, topOnly.vertical, 0.001f)   // max(44, 0) + 16
    }

    @Test
    fun `center of visual equals center of screen for asymmetric cutouts`() {
        val testCases = listOf(
            // Asymmetric left cutout (landscape rot 1)
            Pair(
                Pair(844f, 390f),
                FullscreenSizingHelper.CutoutInsets(left = 48f, top = 0f, right = 0f, bottom = 0f)
            ),
            // Asymmetric right cutout (landscape rot 3)
            Pair(
                Pair(844f, 390f),
                FullscreenSizingHelper.CutoutInsets(left = 0f, top = 0f, right = 48f, bottom = 0f)
            ),
            // Asymmetric top cutout (portrait notch)
            Pair(
                Pair(390f, 844f),
                FullscreenSizingHelper.CutoutInsets(left = 0f, top = 48f, right = 0f, bottom = 0f)
            ),
            // Corner punch hole (top-left)
            Pair(
                Pair(412f, 915f),
                FullscreenSizingHelper.CutoutInsets(left = 40f, top = 40f, right = 0f, bottom = 0f)
            ),
            // Corner punch hole (top-right)
            Pair(
                Pair(412f, 915f),
                FullscreenSizingHelper.CutoutInsets(left = 0f, top = 40f, right = 40f, bottom = 0f)
            )
        )

        for ((dimensions, insets) in testCases) {
            val (width, height) = dimensions
            val bounds = FullscreenSizingHelper.calculateFullscreenVisualBounds(
                screenWidth = width,
                screenHeight = height,
                insets = insets,
                smallMargin = 16f,
                scale = 0.95f
            )

            val screenCenterX = width / 2f
            val screenCenterY = height / 2f

            val offsetX = abs(bounds.visualCenterX - screenCenterX)
            val offsetY = abs(bounds.visualCenterY - screenCenterY)

            assertTrue("Visual horizontal center must match screen center (offset: $offsetX px)", offsetX < 0.001f)
            assertTrue("Visual vertical center must match screen center (offset: $offsetY px)", offsetY < 0.001f)
            assertTrue("Visual must be marked as exactly centered", bounds.isExactlyCentered)
        }
    }

    @Test
    fun `portrait visual is 95 percent of original fullscreen size`() {
        val screenWidth = 390f
        val screenHeight = 844f
        val smallMargin = 16f

        // Original fullscreen width before cutout change was: screenWidth - 2 * smallMargin
        val originalFullscreenWidth = screenWidth - (2 * smallMargin)

        val bounds = FullscreenSizingHelper.calculateFullscreenVisualBounds(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            theme = TimerTheme.THREE_BOX,
            insets = FullscreenSizingHelper.CutoutInsets(left = 0f, top = 48f, right = 0f, bottom = 0f),
            smallMargin = smallMargin
        )

        // In portrait, horizontal cutout is 0, so safeAreaWidth == originalFullscreenWidth
        assertEquals(originalFullscreenWidth, bounds.safeAreaWidth, 0.001f)

        // Scaled to 0.95f: visual width is exactly 95% of original fullscreen width
        assertEquals(originalFullscreenWidth * 0.95f, bounds.visualWidth, 0.001f)
        assertEquals(390f / 2f, bounds.visualCenterX, 0.001f)
        assertEquals(844f / 2f, bounds.visualCenterY, 0.001f)
    }

    @Test
    fun `landscape visual reduced only as needed by symmetric cutout padding`() {
        val screenWidth = 844f
        val screenHeight = 390f
        val cutout = 48f
        val smallMargin = 16f

        val bounds = FullscreenSizingHelper.calculateFullscreenVisualBounds(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            theme = TimerTheme.FLIP_CLOCK,
            insets = FullscreenSizingHelper.CutoutInsets(left = cutout, top = 0f, right = 0f, bottom = 0f),
            smallMargin = smallMargin
        )

        // Symmetric padding on both sides: cutout + smallMargin = 48 + 16 = 64
        val expectedSafeWidth = screenWidth - (2 * 64f)
        assertEquals(expectedSafeWidth, bounds.safeAreaWidth, 0.001f)
        assertEquals(expectedSafeWidth * 0.95f, bounds.visualWidth, 0.001f)

        // Exact center
        assertEquals(screenWidth / 2f, bounds.visualCenterX, 0.001f)
        assertEquals(screenHeight / 2f, bounds.visualCenterY, 0.001f)

        // Clearance to camera cutout:
        // Left padding is 64dp. The camera cutout is 48dp on the left edge.
        // The visual starts at or inside 64dp, so the clearance is >= 16dp.
        val visualLeft = bounds.visualCenterX - (bounds.visualWidth / 2f)
        val clearanceToCamera = visualLeft - cutout
        assertTrue("Clearance to camera must be positive: $clearanceToCamera", clearanceToCamera > 0f)
        assertTrue("Clearance to camera must be >= smallMargin: $clearanceToCamera", clearanceToCamera >= smallMargin)
    }

    @Test
    fun `visual is never larger than safe area across wide matrix of screen sizes and cutouts`() {
        val screens = listOf(
            Pair(360f, 640f),
            Pair(640f, 360f),
            Pair(390f, 844f),
            Pair(844f, 390f),
            Pair(412f, 915f),
            Pair(915f, 412f),
            Pair(800f, 1280f),
            Pair(1280f, 800f)
        )

        val cutouts = listOf(
            FullscreenSizingHelper.CutoutInsets(0f, 0f, 0f, 0f),
            FullscreenSizingHelper.CutoutInsets(0f, 48f, 0f, 0f),
            FullscreenSizingHelper.CutoutInsets(48f, 0f, 0f, 0f),
            FullscreenSizingHelper.CutoutInsets(0f, 0f, 48f, 0f),
            FullscreenSizingHelper.CutoutInsets(36f, 36f, 0f, 0f),
            FullscreenSizingHelper.CutoutInsets(0f, 36f, 36f, 0f)
        )

        for ((w, h) in screens) {
            for (cutout in cutouts) {
                for (theme in TimerTheme.entries) {
                    val bounds = FullscreenSizingHelper.calculateFullscreenVisualBounds(
                        screenWidth = w,
                        screenHeight = h,
                        theme = theme,
                        insets = cutout,
                        smallMargin = 16f
                    )

                    assertTrue(
                        "Visual width (${bounds.visualWidth}) must not exceed safe width (${bounds.safeAreaWidth}) for theme $theme on ${w}x$h",
                        bounds.visualWidth <= bounds.safeAreaWidth + 0.001f
                    )
                    assertTrue(
                        "Visual height (${bounds.visualHeight}) must not exceed safe height (${bounds.safeAreaHeight}) for theme $theme on ${w}x$h",
                        bounds.visualHeight <= bounds.safeAreaHeight + 0.001f
                    )
                    assertTrue(bounds.fitsWithinSafeArea)
                    assertTrue(bounds.isExactlyCentered)
                }
            }
        }
    }
}
