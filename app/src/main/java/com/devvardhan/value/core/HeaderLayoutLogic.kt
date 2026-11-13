package com.devvardhan.value.core

/**
 * Pure calculation logic for header slot positioning and slot verification.
 * Verifies that the center slot (ModeSwitch) is positioned at the exact
 * horizontal center of the screen/container, completely independent of the
 * width of the start slot or end slot.
 */
object HeaderLayoutLogic {

    const val DEFAULT_SWITCH_WIDTH_LANDSCAPE = 200f
    const val DEFAULT_SWITCH_WIDTH_PORTRAIT = 216f
    const val FULLSCREEN_BUTTON_SIZE = 48f
    const val BUTTON_SPACING = 8f
    const val SETTINGS_BUTTON_SIZE = 48f
    const val ACTION_BUTTONS_TOTAL_WIDTH = FULLSCREEN_BUTTON_SIZE + BUTTON_SPACING + SETTINGS_BUTTON_SIZE // 104f

    /**
     * Calculates the horizontal center X of the center slot (ModeSwitch) in pixels or dp.
     * In a Box layout where the container spans [containerWidth] and has equal start and end padding,
     * Alignment.Center always places the center of the slot at containerWidth / 2f,
     * regardless of [endSlotWidth] or [startSlotWidth].
     */
    fun calculateCenterSlotCenterX(
        containerWidth: Float,
        startPadding: Float = 0f,
        endPadding: Float = 0f
    ): Float {
        val contentWidth = containerWidth - startPadding - endPadding
        return startPadding + contentWidth / 2f
    }

    /**
     * Calculates the left X of the center slot (ModeSwitch).
     */
    fun calculateCenterSlotLeftX(
        containerWidth: Float,
        switchWidth: Float,
        startPadding: Float = 0f,
        endPadding: Float = 0f
    ): Float {
        val centerX = calculateCenterSlotCenterX(containerWidth, startPadding, endPadding)
        return centerX - switchWidth / 2f
    }

    /**
     * Calculates the right X of the center slot (ModeSwitch).
     */
    fun calculateCenterSlotRightX(
        containerWidth: Float,
        switchWidth: Float,
        startPadding: Float = 0f,
        endPadding: Float = 0f
    ): Float {
        val centerX = calculateCenterSlotCenterX(containerWidth, startPadding, endPadding)
        return centerX + switchWidth / 2f
    }

    /**
     * Calculates the left X of the Settings button in the end slot.
     * Since the action buttons row reserves the slot for Fullscreen (48dp) + spacing (8dp),
     * Settings is always the right-most element aligned to [containerWidth - endPadding - settingsButtonWidth].
     */
    fun calculateSettingsButtonLeftX(
        containerWidth: Float,
        endPadding: Float,
        settingsButtonWidth: Float = SETTINGS_BUTTON_SIZE
    ): Float {
        return containerWidth - endPadding - settingsButtonWidth
    }

    /**
     * Determines whether the three independent slots (start, center, end) can fit
     * on a single horizontal row without overlapping.
     *
     * @param containerWidth Total available width in dp.
     * @param startSlotWidth Width of start slot (e.g. VALUE wordmark).
     * @param switchWidth Width of center slot (ModeSwitch).
     * @param endSlotWidth Total width of end slot (104dp with reserved Fullscreen slot).
     * @param startPadding Horizontal start padding (e.g. 24dp in landscape).
     * @param endPadding Horizontal end padding (e.g. 24dp in landscape).
     * @param minSpacing Minimum gap required between slots (default 8dp).
     * @return true if all three slots fit without overlap.
     */
    fun canFitThreeSlotsOnSingleRow(
        containerWidth: Float,
        startSlotWidth: Float = 70f,
        switchWidth: Float = DEFAULT_SWITCH_WIDTH_LANDSCAPE,
        endSlotWidth: Float = ACTION_BUTTONS_TOTAL_WIDTH,
        startPadding: Float = 24f,
        endPadding: Float = 24f,
        minSpacing: Float = 8f
    ): Boolean {
        val centerLeft = calculateCenterSlotLeftX(containerWidth, switchWidth, startPadding, endPadding)
        val centerRight = calculateCenterSlotRightX(containerWidth, switchWidth, startPadding, endPadding)
        val startRight = startPadding + startSlotWidth
        val endLeft = containerWidth - endPadding - endSlotWidth

        return (centerLeft - startRight >= minSpacing) && (endLeft - centerRight >= minSpacing)
    }
}

