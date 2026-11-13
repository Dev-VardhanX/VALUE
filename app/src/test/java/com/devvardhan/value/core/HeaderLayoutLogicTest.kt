package com.devvardhan.value.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderLayoutLogicTest {

    @Test
    fun testCenterSlotCenterX_equalsScreenCenterForAnyEndSlotWidth() {
        // Container width: 1280dp (Tablet landscape)
        val screenWidth = 1280f
        val expectedCenter = screenWidth / 2f // 640f

        // Equal padding (24dp start and 24dp end)
        val centerXWithPadding = HeaderLayoutLogic.calculateCenterSlotCenterX(
            containerWidth = screenWidth,
            startPadding = 24f,
            endPadding = 24f
        )
        assertEquals(expectedCenter, centerXWithPadding, 0.001f)

        // Center X is completely independent of end slot width (whether 0dp, 48dp, 104dp, or 200dp)
        listOf(0f, 48f, 104f, 200f).forEach { endSlotWidth ->
            val computedCenter = HeaderLayoutLogic.calculateCenterSlotCenterX(
                containerWidth = screenWidth,
                startPadding = 24f,
                endPadding = 24f
            )
            assertEquals("Center slot must equal screen center for endSlotWidth=$endSlotWidth", expectedCenter, computedCenter, 0.001f)
        }
    }

    @Test
    fun testCenterSlotLeftX_independentOfFullscreenVisibility() {
        val containerWidth = 1280f
        val switchWidth = 200f

        // On Set Timer (Fullscreen hidden, alpha 0):
        val leftXSetTimer = HeaderLayoutLogic.calculateCenterSlotLeftX(
            containerWidth = containerWidth,
            switchWidth = switchWidth,
            startPadding = 24f,
            endPadding = 24f
        )

        // On Stopwatch / Running Timer (Fullscreen visible, alpha 1):
        val leftXStopwatch = HeaderLayoutLogic.calculateCenterSlotLeftX(
            containerWidth = containerWidth,
            switchWidth = switchWidth,
            startPadding = 24f,
            endPadding = 24f
        )

        // Both must be exactly identical: (1280 - 200) / 2 = 540f
        assertEquals(540f, leftXSetTimer, 0.001f)
        assertEquals(540f, leftXStopwatch, 0.001f)
        assertEquals(leftXSetTimer, leftXStopwatch, 0.001f)
    }

    @Test
    fun testSettingsButtonPosition_identicalAcrossScreens() {
        val containerWidth = 1280f
        val endPadding = 24f
        val settingsWidth = 48f

        // Settings button left X: 1280 - 24 - 48 = 1208f
        val settingsX = HeaderLayoutLogic.calculateSettingsButtonLeftX(
            containerWidth = containerWidth,
            endPadding = endPadding,
            settingsButtonWidth = settingsWidth
        )

        assertEquals(1208f, settingsX, 0.001f)
    }

    @Test
    fun testCanFitThreeSlotsOnSingleRow() {
        // Tablet landscape (1280dp): fits with generous space
        assertTrue(
            HeaderLayoutLogic.canFitThreeSlotsOnSingleRow(
                containerWidth = 1280f,
                startSlotWidth = 70f,
                switchWidth = 200f,
                endSlotWidth = 104f,
                startPadding = 24f,
                endPadding = 24f
            )
        )

        // Phone landscape (800dp): fits comfortably
        assertTrue(
            HeaderLayoutLogic.canFitThreeSlotsOnSingleRow(
                containerWidth = 800f,
                startSlotWidth = 70f,
                switchWidth = 200f,
                endSlotWidth = 104f,
                startPadding = 24f,
                endPadding = 24f
            )
        )

        // Typical phone portrait (390dp): does NOT fit (would overlap), triggers fallback to 2-row layout
        assertFalse(
            HeaderLayoutLogic.canFitThreeSlotsOnSingleRow(
                containerWidth = 390f,
                startSlotWidth = 70f,
                switchWidth = 200f,
                endSlotWidth = 104f,
                startPadding = 20f,
                endPadding = 20f
            )
        )

        // Narrow phone portrait (360dp): does NOT fit
        assertFalse(
            HeaderLayoutLogic.canFitThreeSlotsOnSingleRow(
                containerWidth = 360f,
                startSlotWidth = 70f,
                switchWidth = 200f,
                endSlotWidth = 104f,
                startPadding = 20f,
                endPadding = 20f
            )
        )
    }

    @Test
    fun testPortraitTwoRowLayout_modeSwitchCenteredOnScreen() {
        val portraitWidth = 390f
        val switchWidth = 216f

        // Row 2 Box has fillMaxWidth() and contentAlignment = Alignment.Center
        val centerSlotCenterX = HeaderLayoutLogic.calculateCenterSlotCenterX(
            containerWidth = portraitWidth,
            startPadding = 0f,
            endPadding = 0f
        )
        val expectedCenter = portraitWidth / 2f // 195f
        assertEquals(expectedCenter, centerSlotCenterX, 0.001f)

        val centerSlotLeftX = HeaderLayoutLogic.calculateCenterSlotLeftX(
            containerWidth = portraitWidth,
            switchWidth = switchWidth,
            startPadding = 0f,
            endPadding = 0f
        )
        assertEquals((390f - 216f) / 2f, centerSlotLeftX, 0.001f)
    }
}

