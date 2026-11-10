package com.devvardhan.value.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devvardhan.value.R
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.TimerVisual
import com.devvardhan.value.ui.components.rememberCountdownRemainingMillis
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag
import kotlinx.coroutines.delay

/**
 * Screen 3: Fullscreen Timer Mode.
 * Immersive sticky mode displaying the selected theme visual scaled to the screen bounds on pure black.
 * The exit-fullscreen icon auto-hides after 3 seconds of no touch.
 * Any touch anywhere fades it back in and restarts the 3s timer.
 * The touch that reveals hidden controls consumes the event, avoiding accidental button clicks underneath.
 */
@Composable
fun FullscreenTimerScreen(
    theme: TimerTheme,
    targetEndTime: Long,
    pausedRemainingMillis: Long,
    totalMillis: Long,
    status: TimerStatus,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    initialControlsVisible: Boolean = true
) {
    val context = LocalContext.current
    var isExitIconVisible by remember { mutableStateOf(initialControlsVisible) }
    var lastInteractionTime by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    // Smooth 300ms alpha fade without recomposing the whole screen
    val exitIconAlpha by animateFloatAsState(
        targetValue = if (isExitIconVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "FullscreenTimerExitIconAlpha"
    )

    // High performance frame clock; stored in State and provided as lambda to avoid recomposing the screen
    val liveRemainingMillisState = rememberCountdownRemainingMillis(
        status = status,
        targetEndTime = targetEndTime,
        pausedRemainingMillis = pausedRemainingMillis
    )

    // Handle back button to exit fullscreen
    BackHandler {
        onExitFullscreen()
    }

    // Auto-hide exit button after 3 seconds of inactivity
    LaunchedEffect(lastInteractionTime, isExitIconVisible) {
        if (isExitIconVisible) {
            delay(3000L)
            isExitIconVisible = false
        }
    }

    // Manage immersive system bars
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
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val hasDown = event.changes.any { it.pressed && !it.previousPressed }
                        if (hasDown) {
                            if (!isExitIconVisible) {
                                // First touch while hidden: CONSUME event so it only reveals controls
                                event.changes.forEach { it.consume() }
                                isExitIconVisible = true
                                lastInteractionTime = SystemClock.elapsedRealtime()
                            } else {
                                // Touch while already visible: do NOT consume so exit button works
                                lastInteractionTime = SystemClock.elapsedRealtime()
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Active visual scaled to the full viewport with safe minimal margins
        TimerVisual(
            theme = theme,
            remainingMillisProvider = { liveRemainingMillisState.value },
            totalMillis = totalMillis,
            status = status,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        // Exit fullscreen icon button (auto-hides after 3 seconds, 300ms fade)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .graphicsLayer { alpha = exitIconAlpha }
                .semantics {
                    if (!isExitIconVisible) {
                        hideFromAccessibility()
                    }
                }
        ) {
            IconButton(
                onClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onExitFullscreen()
                },
                enabled = isExitIconVisible,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .semantics { contentDescription = "Exit fullscreen mode" }
                    .debugTestTag("btn_exit_fullscreen")
            ) {
                Icon(
                    imageVector = AppIcons.FullscreenExit,
                    contentDescription = stringResource(R.string.action_exit_fullscreen),
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
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

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Fullscreen Timer - Visible", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFullscreenTimerVisible() {
    ValueTimerTheme {
        FullscreenTimerScreen(
            theme = TimerTheme.CIRCLE,
            targetEndTime = System.currentTimeMillis() + 45000L,
            pausedRemainingMillis = 45000L,
            totalMillis = 60000L,
            status = TimerStatus.Running,
            onExitFullscreen = {},
            initialControlsVisible = true
        )
    }
}

@Preview(name = "Fullscreen Timer - Hidden", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFullscreenTimerHidden() {
    ValueTimerTheme {
        FullscreenTimerScreen(
            theme = TimerTheme.CIRCLE,
            targetEndTime = System.currentTimeMillis() + 45000L,
            pausedRemainingMillis = 45000L,
            totalMillis = 60000L,
            status = TimerStatus.Running,
            onExitFullscreen = {},
            initialControlsVisible = false
        )
    }
}
