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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Minimal black-and-white segmented pill switch between Timer and Stopwatch.
 * Uses a smoothly sliding physical white pill with subtle spring physics and light haptic feedback.
 * Fixed-size text with single line, no truncation, fits comfortably at font scale 1.3.
 * Displays an active status dot on unselected segments when that engine is currently running.
 */
@Composable
fun ModeSwitch(
    currentMode: AppMode,
    isTimerRunning: Boolean,
    isStopwatchRunning: Boolean,
    onModeSelected: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val isReducedMotion = remember(context) { Motion.isAnimationsDisabled(context) }

    BoxWithConstraints(
        modifier = modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF0C0C0C))
            .border(1.dp, BorderSubtle, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val segmentWidth = maxWidth / 2f
        val pillOffset by animateDpAsState(
            targetValue = if (currentMode == AppMode.TIMER) 0.dp else segmentWidth,
            animationSpec = if (isReducedMotion) tween(0) else Motion.PillSpringDp,
            label = "SlidingPill"
        )

        // Smoothly sliding physical white pill
        Box(
            modifier = Modifier
                .width(segmentWidth)
                .fillMaxHeight()
                .graphicsLayer { translationX = pillOffset.toPx() }
                .clip(CircleShape)
                .background(PureWhite)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegment(
                label = stringResource(R.string.mode_timer),
                isSelected = currentMode == AppMode.TIMER,
                showActiveDot = (currentMode != AppMode.TIMER) && isTimerRunning,
                onClick = {
                    if (currentMode != AppMode.TIMER) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onModeSelected(AppMode.TIMER)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .debugTestTag("mode_timer")
            )

            ModeSegment(
                label = stringResource(R.string.mode_stopwatch),
                isSelected = currentMode == AppMode.STOPWATCH,
                showActiveDot = (currentMode != AppMode.STOPWATCH) && isStopwatchRunning,
                onClick = {
                    if (currentMode != AppMode.STOPWATCH) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onModeSelected(AppMode.STOPWATCH)
                    }
                },
                modifier = Modifier
                    .weight(1f)
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
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PureBlack else SubtleGray,
        animationSpec = tween(durationMillis = 180),
        label = "SegmentText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .defaultMinSize(minWidth = 98.dp)
            .clip(CircleShape)
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
