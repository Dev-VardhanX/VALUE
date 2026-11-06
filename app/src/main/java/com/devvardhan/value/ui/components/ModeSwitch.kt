package com.devvardhan.value.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Minimal black-and-white segmented pill switch between Timer and Stopwatch.
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
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF0C0C0C))
            .border(1.dp, BorderSubtle, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegment(
                label = stringResource(R.string.mode_timer),
                isSelected = currentMode == AppMode.TIMER,
                showActiveDot = (currentMode != AppMode.TIMER) && isTimerRunning,
                onClick = { onModeSelected(AppMode.TIMER) },
                modifier = Modifier.weight(1f)
            )

            ModeSegment(
                label = stringResource(R.string.mode_stopwatch),
                isSelected = currentMode == AppMode.STOPWATCH,
                showActiveDot = (currentMode != AppMode.STOPWATCH) && isStopwatchRunning,
                onClick = { onModeSelected(AppMode.STOPWATCH) },
                modifier = Modifier.weight(1f)
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
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PureWhite else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "SegmentBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PureBlack else SubtleGray,
        animationSpec = tween(durationMillis = 200),
        label = "SegmentText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = "$label tab, ${if (isSelected) "selected" else "not selected"}"
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                letterSpacing = 0.5.sp
            )

            if (showActiveDot) {
                Spacer(modifier = Modifier.width(6.dp))
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

