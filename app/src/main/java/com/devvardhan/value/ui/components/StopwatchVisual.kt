package com.devvardhan.value.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.themes.CircleTimerTheme
import com.devvardhan.value.ui.components.themes.ConcentricRingsTimerTheme
import com.devvardhan.value.ui.components.themes.DotGridTimerTheme
import com.devvardhan.value.ui.components.themes.FlipClockTimerTheme
import com.devvardhan.value.ui.components.themes.ThreeBoxTimerTheme

/**
 * Universal stopwatch visual container that renders the selected theme with smooth 300ms crossfade transitions.
 */
@Composable
fun StopwatchVisual(
    theme: TimerTheme,
    elapsedMillis: Long,
    status: StopwatchStatus,
    modifier: Modifier = Modifier
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
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )

            TimerTheme.CONCENTRIC_RINGS -> ConcentricRingsTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )

            TimerTheme.THREE_BOX -> ThreeBoxTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )

            TimerTheme.FLIP_CLOCK -> FlipClockTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )

            TimerTheme.DOT_GRID -> DotGridTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )

            // Hourglass and Liquid Fill fallback safely to Circle if ever encountered
            else -> CircleTimerTheme(
                isStopwatch = true,
                stopwatchElapsedMillis = elapsedMillis,
                stopwatchStatus = status
            )
        }
    }
}

