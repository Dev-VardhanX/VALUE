package com.devvardhan.value.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.themes.CircleTimerTheme
import com.devvardhan.value.ui.components.themes.ConcentricRingsTimerTheme
import com.devvardhan.value.ui.components.themes.FlipClockTimerTheme
import com.devvardhan.value.ui.components.themes.ThreeBoxTimerTheme

/**
 * Universal stopwatch visual container that renders the selected theme with smooth 300ms crossfade transitions.
 * Centiseconds are rendered inline with the time on the same baseline across all themes.
 * Supported themes: Circle, Concentric Rings, Three Box, Flip Clock.
 *
 * Performance optimization: Accepts a lambda provider for elapsedMillis so callers do not
 * recompose on every animation frame. The value is evaluated strictly in the draw phase.
 */
@Composable
fun StopwatchVisual(
    theme: TimerTheme,
    elapsedMillisProvider: () -> Long,
    status: StopwatchStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    Crossfade(
        targetState = theme,
        animationSpec = tween(durationMillis = 300),
        label = "StopwatchThemeCrossfade",
        modifier = modifier
    ) { activeTheme ->
        when (activeTheme) {
            TimerTheme.CIRCLE -> CircleTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillisProvider = elapsedMillisProvider,
                stopwatchStatus = status,
                layoutMode = layoutMode
            )

            TimerTheme.CONCENTRIC_RINGS -> ConcentricRingsTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillisProvider = elapsedMillisProvider,
                stopwatchStatus = status,
                layoutMode = layoutMode
            )

            TimerTheme.THREE_BOX -> ThreeBoxTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillisProvider = elapsedMillisProvider,
                stopwatchStatus = status,
                layoutMode = layoutMode
            )

            TimerTheme.FLIP_CLOCK -> FlipClockTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillisProvider = elapsedMillisProvider,
                stopwatchStatus = status,
                layoutMode = layoutMode
            )

            // Hourglass, Liquid Fill, Dot Grid fallback safely to Circle if ever encountered
            else -> CircleTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillisProvider = elapsedMillisProvider,
                stopwatchStatus = status,
                layoutMode = layoutMode
            )
        }
    }
}

/**
 * Backward-compatible overload accepting a direct Long value.
 */
@Composable
fun StopwatchVisual(
    theme: TimerTheme,
    elapsedMillis: Long,
    status: StopwatchStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) = StopwatchVisual(
    theme = theme,
    elapsedMillisProvider = { elapsedMillis },
    status = status,
    modifier = modifier,
    layoutMode = layoutMode
)
