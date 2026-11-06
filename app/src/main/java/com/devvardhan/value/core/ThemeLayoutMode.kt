package com.devvardhan.value.core

/**
 * Explicit layout mode override for theme rendering.
 * - AUTO: Responsive detection based on available aspect ratio.
 * - PORTRAIT: Forces compact vertical presentation (used for previews and square cards).
 * - LANDSCAPE: Forces horizontal widescreen layout.
 */
enum class ThemeLayoutMode {
    AUTO,
    PORTRAIT,
    LANDSCAPE
}

