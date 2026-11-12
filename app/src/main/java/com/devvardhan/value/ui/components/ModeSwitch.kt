package com.devvardhan.value.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.Motion
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Minimal black-and-white segmented pill switch between Timer and Stopwatch.
 * Uses a smoothly sliding physical white pill with subtle spring physics and light haptic feedback.
 * Fixed-size text with single line, no truncation, fits comfortably at font scale 1.3.
 * Displays an active status dot on unselected segments when that engine is currently running.
 * Each segment has a full 48dp height touch target covering its entire half of the switch.
 */
@Composable
fun ModeSwitch(
    currentMode: AppMode,
    isTimerRunning: Boolean,
    isStopwatchRunning: Boolean,
    onModeSelected: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
    progressProvider: (() -> Float)? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val isReducedMotion = remember(context) { Motion.isAnimationsDisabled(context) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        val innerSegmentWidth = (maxWidth - 6.dp) / 2f

        val pillOffset by animateDpAsState(
            targetValue = if (currentMode == AppMode.TIMER) 0.dp else innerSegmentWidth,
            animationSpec = if (isReducedMotion) tween(0) else Motion.PillSpringDp,
            label = "SlidingPill"
        )

        // Visible pill background track (38dp high, centered vertically within the 48dp touch area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C0C0C))
                .border(1.dp, BorderSubtle, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Smoothly sliding physical white pill: fraction-driven in draw phase when pager provides it
            Box(
                modifier = Modifier
                    .width(innerSegmentWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        val progress = progressProvider?.invoke()?.coerceIn(0f, 1f)
                        translationX = if (progress != null) {
                            val segmentWidthPx = density.run { innerSegmentWidth.toPx() }
                            progress * segmentWidthPx
                        } else {
                            pillOffset.toPx()
                        }
                    }
                    .clip(CircleShape)
                    .background(PureWhite)
            )
        }

        // Tap targets row: full 48dp height, each half covers its whole half of the switch
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegment(
                label = stringResource(R.string.mode_timer),
                isSelected = currentMode == AppMode.TIMER,
                showActiveDot = (currentMode != AppMode.TIMER) && isTimerRunning,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelected(AppMode.TIMER)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .debugTestTag("mode_timer")
            )

            ModeSegment(
                label = stringResource(R.string.mode_stopwatch),
                isSelected = currentMode == AppMode.STOPWATCH,
                showActiveDot = (currentMode != AppMode.STOPWATCH) && isStopwatchRunning,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModeSelected(AppMode.STOPWATCH)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .debugTestTag("mode_stopwatch")
            )
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    isSelected: Boolean,
    showActiveDot: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Unselected label contrast raised to ~70% alpha monochrome white for clear legibility in both modes
    val unselectedColor = PureWhite.copy(alpha = 0.70f)
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PureBlack else unselectedColor,
        animationSpec = tween(durationMillis = 180),
        label = "SegmentText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = "$label tab, ${if (isSelected) "selected" else "not selected"}"
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                softWrap = false
            )

            if (showActiveDot) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(PureWhite, CircleShape)
                        .semantics { contentDescription = "$label currently running" }
                )
            }
        }
    }
}
