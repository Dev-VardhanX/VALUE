package com.devvardhan.value.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.themes.CircleTimerTheme
import com.devvardhan.value.ui.components.themes.ConcentricRingsTimerTheme
import com.devvardhan.value.ui.components.themes.DotGridTimerTheme
import com.devvardhan.value.ui.components.themes.FlipClockTimerTheme
import com.devvardhan.value.ui.components.themes.HourglassTimerTheme
import com.devvardhan.value.ui.components.themes.LiquidFillTimerTheme
import com.devvardhan.value.ui.components.themes.ThreeBoxTimerTheme

/**
 * Universal timer visual container that renders the selected theme with smooth 300ms crossfade transitions.
 */
@Composable
fun TimerVisual(
    theme: TimerTheme,
    remainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    modifier: Modifier = Modifier,
    layoutMode: ThemeLayoutMode = ThemeLayoutMode.AUTO
) {
    Crossfade(
        targetState = theme,
        animationSpec = tween(durationMillis = 300),
        label = "TimerThemeCrossfade",
        modifier = modifier
    ) { activeTheme ->
        when (activeTheme) {
            TimerTheme.CIRCLE -> CircleTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.HOURGLASS -> HourglassTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.THREE_BOX -> ThreeBoxTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.CONCENTRIC_RINGS -> ConcentricRingsTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.FLIP_CLOCK -> FlipClockTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.LIQUID_FILL -> LiquidFillTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )

            TimerTheme.DOT_GRID -> DotGridTimerTheme(
                remainingMillis = remainingMillis,
                totalMillis = totalMillis,
                status = status,
                layoutMode = layoutMode
            )
        }
    }
}
