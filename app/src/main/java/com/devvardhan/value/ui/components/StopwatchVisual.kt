package com.devvardhan.value.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.themes.CircleTimerTheme
import com.devvardhan.value.ui.components.themes.ConcentricRingsTimerTheme
import com.devvardhan.value.ui.components.themes.DotGridTimerTheme
import com.devvardhan.value.ui.components.themes.FlipClockTimerTheme
import com.devvardhan.value.ui.components.themes.ThreeBoxTimerTheme

/**
 * Universal stopwatch visual container that renders the selected theme with smooth 300ms crossfade transitions,
 * paired with a dedicated jitter-free CentisecondsSlot positioned stably below the visual.
 */
@Composable
fun StopwatchVisual(
    theme: TimerTheme,
    elapsedMillis: Long,
    status: StopwatchStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = theme,
                animationSpec = tween(durationMillis = 300),
                label = "StopwatchThemeCrossfade"
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

        CentisecondsSlot(
            elapsedMillis = elapsedMillis,
            fontSize = 20.sp
        )
    }
}
