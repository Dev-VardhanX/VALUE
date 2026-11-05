package com.android.value.ui.screens

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.value.core.TimeUtils
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import kotlinx.coroutines.delay

/**
 * Screen 3: Fullscreen Mode.
 * Immersive sticky mode hiding status and navigation bars.
 * Shows only the huge countdown digits centered on pure black.
 * Single tap briefly reveals an exit fullscreen icon that auto-hides after 2 seconds.
 * Back press also exits fullscreen.
 * Scales dynamically to fill available width in landscape and portrait.
 */
@Composable
fun FullscreenTimerScreen(
    remainingMillis: Long,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExitIcon by remember { mutableStateOf(false) }

    // Intercept back gesture to exit fullscreen
    BackHandler {
        onExitFullscreen()
    }

    // Auto-hide the exit icon after 2 seconds
    LaunchedEffect(showExitIcon) {
        if (showExitIcon) {
            delay(2000L)
            showExitIcon = false
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

    val formattedTime = TimeUtils.formatHms(remainingMillis)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showExitIcon = true
            },
        contentAlignment = Alignment.Center
    ) {
        // Compute optimal font size based on screen dimensions so digits scale to fill width in landscape/portrait
        val computedFontSize = minOf(
            maxWidth.value / 5.2f,
            maxHeight.value * 0.70f
        ).coerceIn(36f, 260f).sp

        Text(
            text = formattedTime,
            fontSize = computedFontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraLight,
            color = PureWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )

        // Exit fullscreen icon button (auto-hides after 2 seconds)
        AnimatedVisibility(
            visible = showExitIcon,
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
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Finds the nearest Activity from Context, unwrapping any ContextWrappers.
 */
private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

