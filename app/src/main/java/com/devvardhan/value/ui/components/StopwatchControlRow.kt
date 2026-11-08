package com.devvardhan.value.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devvardhan.value.R
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Unified Stopwatch Control Row:
 * One row always showing all three controls in fixed positions:
 * - LEFT: Circular outlined Lap (Flag) button
 * - CENTER: Main pill button (Start / Pause / Resume) taking remaining width (weight 1f)
 * - RIGHT: Circular outlined Reset button
 *
 * Spacing: 14dp between the three. Never shifts or wraps across state changes.
 * Disabled state: BorderSubtle gray outline with 40% alpha icon in fixed position.
 */
@Composable
fun StopwatchControlRow(
    status: StopwatchStatus,
    lapsCount: Int,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    latestSplitFormatted: String? = null,
    elapsedMillis: Long = 0L,
    lastLapTotalMillis: Long = 0L
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val state = AdaptiveLayoutHelper.getStopwatchControlRowState(
        status = status,
        lapsCount = lapsCount,
        elapsedMillis = elapsedMillis,
        lastLapTotalMillis = lastLapTotalMillis
    )

    val (mainLabel, onMainClick) = when (status) {
        StopwatchStatus.Idle -> stringResource(R.string.action_start) to onStartClick
        StopwatchStatus.Running -> stringResource(R.string.action_pause) to onPauseClick
        StopwatchStatus.Paused -> stringResource(R.string.action_resume) to onResumeClick
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT: Lap circular button
        CircularIconButton(
            icon = AppIcons.Flag,
            contentDescription = stringResource(R.string.action_lap),
            enabled = state.isLapEnabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                val lapNum = lapsCount + 1
                val announcement = if (latestSplitFormatted != null) {
                    "Lap $lapNum recorded, $latestSplitFormatted"
                } else {
                    "Lap $lapNum recorded"
                }
                @Suppress("DEPRECATION")
                view.announceForAccessibility(announcement)
                onLapClick()
            },
            size = height,
            modifier = Modifier.debugTestTag("btn_stopwatch_lap")
        )

        // CENTER: Main action pill button
        PrimaryPillButton(
            text = mainLabel,
            onClick = onMainClick,
            enabled = state.isMainEnabled,
            modifier = Modifier
                .weight(1f)
                .debugTestTag("btn_stopwatch_main"),
            height = height
        )

        // RIGHT: Reset circular button
        CircularIconButton(
            icon = AppIcons.Reset,
            contentDescription = stringResource(R.string.action_reset),
            enabled = state.isResetEnabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onResetClick()
            },
            size = height,
            modifier = Modifier.debugTestTag("btn_stopwatch_reset")
        )
    }
}

/**
 * Circular outlined icon button with 48dp+ interactive touch target.
 */
@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = if (enabled) PureWhite else BorderSubtle,
                shape = CircleShape
            )
            .background(PureBlack)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) PureWhite else PureWhite.copy(alpha = 0.40f),
            modifier = Modifier.size(iconSize)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Control Row - Idle (0 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360)
@Composable
private fun PreviewStopwatchControlRowIdle() {
    ValueTimerTheme {
        Box(modifier = Modifier.background(PureBlack)) {
            StopwatchControlRow(
                status = StopwatchStatus.Idle,
                lapsCount = 0,
                onStartClick = {},
                onPauseClick = {},
                onResumeClick = {},
                onLapClick = {},
                onResetClick = {}
            )
        }
    }
}

@Preview(name = "Control Row - Running (3 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360)
@Composable
private fun PreviewStopwatchControlRowRunning() {
    ValueTimerTheme {
        Box(modifier = Modifier.background(PureBlack)) {
            StopwatchControlRow(
                status = StopwatchStatus.Running,
                lapsCount = 3,
                latestSplitFormatted = "00:12.45",
                onStartClick = {},
                onPauseClick = {},
                onResumeClick = {},
                onLapClick = {},
                onResetClick = {}
            )
        }
    }
}

@Preview(name = "Control Row - Paused (3 laps)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360)
@Composable
private fun PreviewStopwatchControlRowPaused() {
    ValueTimerTheme {
        Box(modifier = Modifier.background(PureBlack)) {
            StopwatchControlRow(
                status = StopwatchStatus.Paused,
                lapsCount = 3,
                onStartClick = {},
                onPauseClick = {},
                onResumeClick = {},
                onLapClick = {},
                onResetClick = {}
            )
        }
    }
}

