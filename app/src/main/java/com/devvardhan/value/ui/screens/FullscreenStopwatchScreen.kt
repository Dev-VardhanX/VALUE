package com.devvardhan.value.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.devvardhan.value.core.FullscreenSizingHelper
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.StopwatchControlRow
import com.devvardhan.value.ui.components.StopwatchVisual
import com.devvardhan.value.ui.components.rememberStopwatchElapsedMillis
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag
import kotlinx.coroutines.delay

/**
 * Fullscreen Stopwatch Screen:
 * Immersive sticky mode displaying the stopwatch visual on pure black.
 * Controls (Exit button and Flag | Start/Pause/Resume | Reset row) auto-hide after 3 seconds of no touch.
 * Any touch anywhere fades them back in and restarts the 3s timer.
 * The touch that reveals hidden controls consumes the event, avoiding accidental button clicks underneath.
 */
@Composable
fun FullscreenStopwatchScreen(
    theme: TimerTheme,
    status: StopwatchStatus,
    startTime: Long,
    accumulatedMillis: Long,
    lapsCount: Int,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    initialControlsVisible: Boolean = true,
    lastLapTotalMillis: Long = 0L
) {
    val context = LocalContext.current
    var areControlsVisible by remember { mutableStateOf(initialControlsVisible) }
    var lastInteractionTime by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    // Smooth 300ms alpha fade
    val controlsAlpha by animateFloatAsState(
        targetValue = if (areControlsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "FullscreenStopwatchControlsAlpha"
    )

    // Decoupled 60fps frame clock; stored in State to avoid recomposing the screen
    val liveElapsedMillisState = rememberStopwatchElapsedMillis(
        status = status,
        startTime = startTime,
        accumulatedMillis = accumulatedMillis
    )

    // Back gesture exits fullscreen
    BackHandler {
        onExitFullscreen()
    }

    // Auto-hide controls after 3 seconds with no touch
    LaunchedEffect(lastInteractionTime, areControlsVisible) {
        if (areControlsVisible) {
            delay(3000L)
            areControlsVisible = false
        }
    }

    // Manage immersive sticky mode and display cutout mode
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val prevCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window?.attributes?.layoutInDisplayCutoutMode
        } else null

        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            val win = context.findActivity()?.window
            if (win != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && prevCutoutMode != null) {
                    win.attributes = win.attributes.apply {
                        layoutInDisplayCutoutMode = prevCutoutMode
                    }
                }
                val controller = WindowCompat.getInsetsController(win, win.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val displayCutout = WindowInsets.displayCutout.asPaddingValues()
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    val cutoutLeft = displayCutout.calculateLeftPadding(layoutDirection)
    val cutoutRight = displayCutout.calculateRightPadding(layoutDirection)
    val cutoutTop = displayCutout.calculateTopPadding()
    val cutoutBottom = displayCutout.calculateBottomPadding()

    val smallMargin = 16.dp
    val symmetricHorizontalPadding = maxOf(cutoutLeft, cutoutRight) + smallMargin
    val symmetricVerticalPadding = maxOf(cutoutTop, cutoutBottom) + smallMargin

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val hasDown = event.changes.any { it.pressed && !it.previousPressed }
                        if (hasDown) {
                            if (!areControlsVisible) {
                                // First touch while hidden: CONSUME the event so children buttons
                                // are NOT triggered, reveal controls, and reset timer
                                event.changes.forEach { it.consume() }
                                areControlsVisible = true
                                lastInteractionTime = SystemClock.elapsedRealtime()
                            } else {
                                // Touch while already visible: do NOT consume so buttons still work,
                                // but reset the 3s auto-hide timer
                                lastInteractionTime = SystemClock.elapsedRealtime()
                            }
                        }
                    }
                }
            }
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f
        val isThreeBoxOrFlip = FullscreenSizingHelper.isScaledTheme(theme)

        // 1. Stopwatch visual: centered on the screen with symmetric cutout padding for Three Box and Flip Clock;
        // other themes keep their original layout clearance
        val visualPaddingStart = if (isThreeBoxOrFlip) symmetricHorizontalPadding else maxOf(cutoutLeft, systemBars.calculateStartPadding(layoutDirection)) + 16.dp
        val visualPaddingEnd = if (isThreeBoxOrFlip) symmetricHorizontalPadding else maxOf(cutoutRight, systemBars.calculateEndPadding(layoutDirection)) + 16.dp
        val visualPaddingTop = if (isThreeBoxOrFlip) symmetricVerticalPadding else maxOf(cutoutTop, systemBars.calculateTopPadding()) + (if (isLandscape) 12.dp else 24.dp)
        val visualPaddingBottom = if (isThreeBoxOrFlip) symmetricVerticalPadding else maxOf(cutoutBottom, systemBars.calculateBottomPadding()) + (if (isLandscape) 72.dp else 96.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = visualPaddingStart,
                    end = visualPaddingEnd,
                    top = visualPaddingTop,
                    bottom = visualPaddingBottom
                ),
            contentAlignment = Alignment.Center
        ) {
            val visualModifier = if (isThreeBoxOrFlip) {
                Modifier.fillMaxSize(FullscreenSizingHelper.FULLSCREEN_SCALE_THREE_BOX_FLIP)
            } else {
                Modifier.fillMaxSize()
            }

            StopwatchVisual(
                theme = theme,
                elapsedMillisProvider = { liveElapsedMillisState.value },
                status = status,
                modifier = visualModifier
            )
        }

        // 2. Exit fullscreen icon button (auto-hiding), safe from cutouts
        val safeTop = maxOf(cutoutTop, systemBars.calculateTopPadding())
        val safeEnd = maxOf(cutoutRight, systemBars.calculateEndPadding(layoutDirection))

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = safeTop + 24.dp,
                    end = safeEnd + 24.dp
                )
                .graphicsLayer { alpha = controlsAlpha }
                .semantics {
                    if (!areControlsVisible) {
                        hideFromAccessibility()
                    }
                }
        ) {
            IconButton(
                onClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onExitFullscreen()
                },
                enabled = areControlsVisible,
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

        // 3. Compact bottom controls overlay (Flag | Start/Pause/Resume | Reset), safe from bottom cutouts/bars
        val bottomControlsWidth = if (isLandscape) {
            Modifier.width(360.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
        }

        val safeStart = maxOf(cutoutLeft, systemBars.calculateStartPadding(layoutDirection))
        val safeBottom = maxOf(cutoutBottom, systemBars.calculateBottomPadding())

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = safeStart + 24.dp,
                    end = safeEnd + 24.dp,
                    bottom = safeBottom + (if (isLandscape) 16.dp else 32.dp)
                )
                .then(bottomControlsWidth)
                .graphicsLayer { alpha = controlsAlpha }
                .semantics {
                    if (!areControlsVisible) {
                        hideFromAccessibility()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            StopwatchControlRow(
                status = status,
                lapsCount = lapsCount,
                onStartClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onStartClick()
                },
                onPauseClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onPauseClick()
                },
                onResumeClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onResumeClick()
                },
                onLapClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onLapClick()
                },
                onResetClick = {
                    lastInteractionTime = SystemClock.elapsedRealtime()
                    onResetClick()
                },
                height = if (isLandscape) 52.dp else 56.dp,
                elapsedMillis = liveElapsedMillisState.value,
                lastLapTotalMillis = lastLapTotalMillis
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

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Fullscreen Stopwatch - Visible", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFullscreenStopwatchVisible() {
    ValueTimerTheme {
        FullscreenStopwatchScreen(
            theme = TimerTheme.THREE_BOX,
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 25000L,
            accumulatedMillis = 25000L,
            lapsCount = 2,
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onExitFullscreen = {},
            initialControlsVisible = true
        )
    }
}

@Preview(name = "Fullscreen Stopwatch - Hidden", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFullscreenStopwatchHidden() {
    ValueTimerTheme {
        FullscreenStopwatchScreen(
            theme = TimerTheme.THREE_BOX,
            status = StopwatchStatus.Running,
            startTime = System.currentTimeMillis() - 25000L,
            accumulatedMillis = 25000L,
            lapsCount = 2,
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onExitFullscreen = {},
            initialControlsVisible = false
        )
    }
}

@Preview(name = "Fullscreen Stopwatch - Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewFullscreenStopwatchLandscape() {
    ValueTimerTheme {
        FullscreenStopwatchScreen(
            theme = TimerTheme.CONCENTRIC_RINGS,
            status = StopwatchStatus.Paused,
            startTime = 0L,
            accumulatedMillis = 48000L,
            lapsCount = 1,
            onStartClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onLapClick = {},
            onResetClick = {},
            onExitFullscreen = {},
            initialControlsVisible = true
        )
    }
}
