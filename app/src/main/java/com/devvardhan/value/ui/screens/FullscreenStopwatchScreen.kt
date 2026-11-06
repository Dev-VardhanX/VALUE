package com.devvardhan.value.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devvardhan.value.R
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.SecondaryPillButton
import com.devvardhan.value.ui.components.StopwatchVisual
import com.devvardhan.value.ui.components.rememberStopwatchElapsedMillis
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import kotlinx.coroutines.delay

/**
 * Fullscreen Stopwatch Screen:
 * Immersive sticky mode displaying the active stopwatch visual on pure black.
 * Single tap briefly reveals an exit button and a small Lap button that auto-hide after 2 seconds.
 */
@Composable
fun FullscreenStopwatchScreen(
    theme: TimerTheme,
    status: StopwatchStatus,
    startTime: Long,
    accumulatedMillis: Long,
    onLapClick: () -> Unit,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(false) }

    // Decoupled 60fps frame clock
    val liveElapsedMillis by rememberStopwatchElapsedMillis(
        status = status,
        startTime = startTime,
        accumulatedMillis = accumulatedMillis
    )

    // Back gesture exits fullscreen
    BackHandler {
        onExitFullscreen()
    }

    // Auto-hide controls after 2 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(2000L)
            showControls = false
        }
    }

    // Manage immersive sticky mode
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            val win = context.findActivity()?.window
            if (win != null) {
                val controller = WindowCompat.getInsetsController(win, win.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = true
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual container scaled to viewport
        StopwatchVisual(
            theme = theme,
            elapsedMillis = liveElapsedMillis,
            status = status,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        // Exit fullscreen icon (auto-hiding)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            IconButton(
                onClick = onExitFullscreen,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .semantics { contentDescription = "Exit fullscreen mode" }
            ) {
                Icon(
                    imageVector = AppIcons.FullscreenExit,
                    contentDescription = stringResource(R.string.action_exit_fullscreen),
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Temporary Lap button (auto-hiding)
        AnimatedVisibility(
            visible = showControls && status == StopwatchStatus.Running,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            SecondaryPillButton(
                text = stringResource(R.string.action_lap),
                onClick = onLapClick,
                modifier = Modifier.size(width = 120.dp, height = 44.dp)
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Preview(name = "Fullscreen Stopwatch Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun FullscreenStopwatchPortraitPreview() {
    ValueTimerTheme {
        FullscreenStopwatchScreen(
            theme = TimerTheme.CIRCLE,
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 34000L,
            accumulatedMillis = 34000L,
            onLapClick = {},
            onExitFullscreen = {}
        )
    }
}

