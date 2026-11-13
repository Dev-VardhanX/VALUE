package com.devvardhan.value.ui.components

import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
 * - Built from three independent slots in one full-width Box in landscape:
 *   start slot = VALUE wordmark (aligned to start),
 *   center slot = ModeSwitch (aligned to exact horizontal center of the screen, not of leftover space),
 *   end slot = buttons Row (Fullscreen, then Settings) aligned to end.
 * - The Fullscreen button keeps its 48dp slot width (and 8dp spacing) at all times (reserved space).
 *   When hidden (alpha == 0), it is alpha 0, disabled, not clickable, and not focusable for accessibility.
 * - In portrait, maintains the 2-row arrangement with ModeSwitch centered on its own row,
 *   and the Fullscreen button reserving its slot in row 1.
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
        // Landscape layout: Three independent slots in one full-width Box:
        // - Start slot: VALUE wordmark (aligned to start)
        // - Center slot: ModeSwitch (aligned to exact horizontal center of the screen)
        // - End slot: Action buttons row (Fullscreen, then Settings) (aligned to end)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubtleGray,
                letterSpacing = 3.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                ModeSwitch(
                    currentMode = currentMode,
                    isTimerRunning = isTimerRunning,
                    isStopwatchRunning = isStopwatchRunning,
                    onModeSelected = onModeSelected,
                    modifier = Modifier.width(200.dp),
                    progressProvider = progressProvider
                )
            }

            HeaderActionButtons(
                alpha = fullscreenAlphaProvider(),
                fullscreenDesc = fullscreenDesc,
                settingsDesc = settingsDesc,
                onFullscreenClick = onFullscreenClick,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    } else {
        // Portrait layout:
        // Row 1: Wordmark on left, Action buttons (Fullscreen left of Settings) on right
        // Row 2: ModeSwitch directly below, centered on the screen
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtleGray,
                    letterSpacing = 3.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                HeaderActionButtons(
                    alpha = fullscreenAlphaProvider(),
                    fullscreenDesc = fullscreenDesc,
                    settingsDesc = settingsDesc,
                    onFullscreenClick = onFullscreenClick,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
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

@Composable
private fun HeaderActionButtons(
    alpha: Float,
    fullscreenDesc: String,
    settingsDesc: String,
    onFullscreenClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isInteractive = alpha > 0.5f

        // Fullscreen button keeps its 48dp slot width at all times (reserved space).
        // When hidden (alpha == 0): invisible (alpha 0), not clickable, not focusable for accessibility.
        IconButton(
            onClick = onFullscreenClick,
            enabled = isInteractive,
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { this.alpha = alpha }
                .border(
                    width = 1.dp,
                    color = BorderSubtle.copy(alpha = BorderSubtle.alpha * alpha),
                    shape = CircleShape
                )
                .then(
                    if (isInteractive) {
                        Modifier
                            .semantics { contentDescription = fullscreenDesc }
                            .debugTestTag("btn_fullscreen")
                    } else {
                        Modifier.clearAndSetSemantics { }
                    }
                )
        ) {
            Icon(
                imageVector = AppIcons.Fullscreen,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

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
