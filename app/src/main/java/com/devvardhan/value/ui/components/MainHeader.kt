package com.devvardhan.value.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Fixed top header for VALUE: contains the wordmark, the fixed ModeSwitch, and action buttons.
 * Stays stationary while the main content slides below in HorizontalPager.
 * - Settings (gear) is ALWAYS the right-most button on every screen.
 * - Fullscreen sits directly to the LEFT of the gear on screens that have it (Running Timer, Stopwatch).
 *   On Set Timer it takes no space, so the gear never moves when switching modes or swiping.
 * - Horizontal margins are exactly equal on left (VALUE) and right (Settings).
 */
@Composable
fun MainHeader(
    currentMode: AppMode,
    isTimerRunning: Boolean,
    isStopwatchRunning: Boolean,
    onOpenSettings: () -> Unit,
    onFullscreenClick: () -> Unit,
    onModeSelected: (AppMode) -> Unit,
    progressProvider: () -> Float,
    fullscreenAlphaProvider: () -> Float,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val settingsDesc = stringResource(R.string.action_settings)
    val fullscreenDesc = stringResource(R.string.action_fullscreen)

    if (isLandscape) {
        // Landscape layout: Wordmark on left, ModeSwitch centered, Action buttons on right
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 3.sp
            )

            ModeSwitch(
                currentMode = currentMode,
                isTimerRunning = isTimerRunning,
                isStopwatchRunning = isStopwatchRunning,
                onModeSelected = onModeSelected,
                modifier = Modifier.width(200.dp),
                progressProvider = progressProvider
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val alpha = fullscreenAlphaProvider()
                if (alpha > 0f) {
                    IconButton(
                        onClick = onFullscreenClick,
                        enabled = alpha > 0.5f,
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { this.alpha = alpha }
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = fullscreenDesc }
                            .debugTestTag("btn_fullscreen")
                    ) {
                        Icon(
                            imageVector = AppIcons.Fullscreen,
                            contentDescription = fullscreenDesc,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .semantics { contentDescription = settingsDesc }
                        .debugTestTag("btn_settings")
                ) {
                    Icon(
                        imageVector = AppIcons.Settings,
                        contentDescription = settingsDesc,
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        // Portrait layout:
        // Row 1: Wordmark on left, Action buttons (Fullscreen left of Settings) on right
        // Row 2: ModeSwitch directly below, centered
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtleGray,
                    letterSpacing = 3.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val alpha = fullscreenAlphaProvider()
                    if (alpha > 0f) {
                        IconButton(
                            onClick = onFullscreenClick,
                            enabled = alpha > 0.5f,
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer { this.alpha = alpha }
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = fullscreenDesc }
                                .debugTestTag("btn_fullscreen")
                        ) {
                            Icon(
                                imageVector = AppIcons.Fullscreen,
                                contentDescription = fullscreenDesc,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = settingsDesc }
                            .debugTestTag("btn_settings")
                    ) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = settingsDesc,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                ModeSwitch(
                    currentMode = currentMode,
                    isTimerRunning = isTimerRunning,
                    isStopwatchRunning = isStopwatchRunning,
                    onModeSelected = onModeSelected,
                    modifier = Modifier.width(216.dp),
                    progressProvider = progressProvider
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
