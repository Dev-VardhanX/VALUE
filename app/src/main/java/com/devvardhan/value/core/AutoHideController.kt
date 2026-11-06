package com.devvardhan.value.core

/**
 * Pure state controller for fullscreen controls auto-hide behavior.
 * Encapsulates the 3-second timeout, touch-reveal interception, and clock decoupling
 * to enable deterministic unit testing with a fake clock.
 */
class AutoHideController(
    val timeoutMillis: Long = 3000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    var isVisible: Boolean = true
        private set

    var lastInteractionTime: Long = clock()
        private set

    /**
     * Handles a screen touch event.
     * @return `true` if the touch occurred while hidden and was consumed to reveal controls
     *         without triggering children buttons underneath; `false` if controls were already
     *         visible (touch should NOT be consumed, allowing button actions to proceed).
     */
    fun onTouch(): Boolean {
        lastInteractionTime = clock()
        if (!isVisible) {
            isVisible = true
            return true
        }
        return false
    }

    /**
     * Explicitly registers a control button press to restart the auto-hide timer.
     */
    fun onControlPressed() {
        lastInteractionTime = clock()
        isVisible = true
    }

    /**
     * Checks whether the inactivity timeout has expired.
     * @return `true` if visibility transitioned from visible to hidden, `false` otherwise.
     */
    fun checkTimeout(currentTime: Long = clock()): Boolean {
        if (isVisible && (currentTime - lastInteractionTime >= timeoutMillis)) {
            isVisible = false
            return true
        }
        return false
    }

    /**
     * Forces controls visible (e.g. on entering screen).
     */
    fun show() {
        lastInteractionTime = clock()
        isVisible = true
    }

    /**
     * Forces controls hidden.
     */
    fun hide() {
        isVisible = false
    }
}

