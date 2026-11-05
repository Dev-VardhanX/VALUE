package com.devvardhan.value.model

/**
 * Visual themes available for timer countdown visualization.
 * All themes strictly adhere to the monochrome black, white, and subtle gray palette.
 */
enum class TimerTheme(val displayName: String) {
    CIRCLE("Circle"),
    HOURGLASS("Hourglass"),
    THREE_BOX("Three Box"),
    CONCENTRIC_RINGS("Concentric Rings"),
    FLIP_CLOCK("Flip Clock"),
    LIQUID_FILL("Liquid Fill"),
    DOT_GRID("Dot Grid");

    fun next(): TimerTheme {
        val nextIndex = (ordinal + 1) % entries.size
        return entries[nextIndex]
    }
}

