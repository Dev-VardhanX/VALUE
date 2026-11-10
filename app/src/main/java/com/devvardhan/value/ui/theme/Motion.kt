package com.devvardhan.value.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp

/**
 * Single source of truth for motion and animation specifications in VALUE.
 *
 * Provides standardized durations, easings, spring curves, micro-interactions,
 * and screen transition specs while strictly respecting system accessibility
 * "Remove animations" / animator duration scale settings.
 */
object Motion {

    // Durations (ms)
    const val DurationInstant = 0
    const val DurationFast = 150
    const val DurationMedium = 280
    const val DurationEmphasized = 320
    const val DurationLong = 400

    // Easings
    val EmphasizedEasing: Easing = FastOutSlowInEasing
    val DecelerateEasing: Easing = LinearOutSlowInEasing
    val StandardEasing: Easing = FastOutSlowInEasing

    // Springs
    val PillSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMedium
    )
    val PillSpringDp = spring<Dp>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMedium
    )

    // Micro-interactions
    const val PressedScale = 0.97f
    const val PressedDurationMs = 100
    const val ReleaseDurationMs = 150

    /**
     * Checks if system animations are disabled via Accessibility settings.
     */
    fun isAnimationsDisabled(context: Context): Boolean {
        return try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale <= 0.05f
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Standard screen transition (slide + parallax + fade ~280-320 ms).
     * [forward]: true if navigating deeper into hierarchy (e.g. Main -> Settings or Settings -> Subpage),
     *            false if popping back.
     */
    fun screenTransition(forward: Boolean, isZeroDuration: Boolean = false): ContentTransform {
        if (isZeroDuration) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        val duration = DurationEmphasized
        return if (forward) {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeIn(tween(duration, easing = EmphasizedEasing))) togetherWith
            (slideOutHorizontally(
                targetOffsetX = { fullWidth -> (-fullWidth * 0.20f).toInt() },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeOut(tween(duration, easing = EmphasizedEasing)))
        } else {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> (-fullWidth * 0.20f).toInt() },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeIn(tween(duration, easing = EmphasizedEasing))) togetherWith
            (slideOutHorizontally(
                targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeOut(tween(duration, easing = EmphasizedEasing)))
        }
    }

    /**
     * Mode switch transition between Timer and Stopwatch:
     * Smooth crossfade of content with subtle scale (0.98 -> 1.0).
     */
    fun modeSwitchTransition(isZeroDuration: Boolean = false): ContentTransform {
        if (isZeroDuration) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        return (fadeIn(tween(DurationMedium, easing = EmphasizedEasing)) + scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(DurationMedium, easing = EmphasizedEasing)
        )) togetherWith (fadeOut(tween(DurationFast, easing = EmphasizedEasing)) + scaleOut(
            targetScale = 1.02f,
            animationSpec = tween(DurationFast, easing = EmphasizedEasing)
        ))
    }

    /**
     * Fullscreen enter / exit transition:
     * Smooth zoom and fade.
     */
    fun fullscreenTransition(isZeroDuration: Boolean = false): ContentTransform {
        if (isZeroDuration) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        return (fadeIn(tween(DurationMedium, easing = EmphasizedEasing)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(DurationMedium, easing = EmphasizedEasing)
        )) togetherWith (fadeOut(tween(DurationFast, easing = EmphasizedEasing)) + scaleOut(
            targetScale = 1.04f,
            animationSpec = tween(DurationFast, easing = EmphasizedEasing)
        ))
    }

    /**
     * Timer screen state transition (Set Timer <-> Running Timer <-> Finished):
     * Smooth transition with no layout jumps.
     */
    fun timerStateTransition(isZeroDuration: Boolean = false): ContentTransform {
        if (isZeroDuration) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        return (fadeIn(tween(DurationMedium, easing = EmphasizedEasing)) + scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(DurationMedium, easing = EmphasizedEasing)
        )) togetherWith (fadeOut(tween(DurationFast, easing = EmphasizedEasing)) + scaleOut(
            targetScale = 1.02f,
            animationSpec = tween(DurationFast, easing = EmphasizedEasing)
        ))
    }

    /**
     * Gallery step transition between Step 1 and Step 2:
     * Horizontal slide + fade.
     */
    fun galleryStepTransition(forward: Boolean, isZeroDuration: Boolean = false): ContentTransform {
        if (isZeroDuration) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }
        val duration = DurationEmphasized
        return if (forward) {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeIn(tween(duration, easing = EmphasizedEasing))) togetherWith
            (slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeOut(tween(duration, easing = EmphasizedEasing)))
        } else {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeIn(tween(duration, easing = EmphasizedEasing))) togetherWith
            (slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(duration, easing = EmphasizedEasing)
            ) + fadeOut(tween(duration, easing = EmphasizedEasing)))
        }
    }
}

/**
 * Modifier creating subtle button press micro-interaction (scale to 0.97 via graphicsLayer,
 * fast 100ms release, and light haptic feedback).
 */
@Composable
fun Modifier.pressScaleEffect(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    targetScale: Float = Motion.PressedScale,
    enabled: Boolean = true,
    onHaptic: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isReducedMotion = remember(context) { Motion.isAnimationsDisabled(context) }

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled && onHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isReducedMotion) targetScale else 1.0f,
        animationSpec = tween(
            durationMillis = if (isPressed) Motion.PressedDurationMs else Motion.ReleaseDurationMs,
            easing = Motion.StandardEasing
        ),
        label = "PressScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
