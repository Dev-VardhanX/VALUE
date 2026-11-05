package com.devvardhan.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.SecondaryPillButton
import com.devvardhan.value.ui.components.ThemeBottomSheet
import com.devvardhan.value.ui.components.TimerVisual
import com.devvardhan.value.ui.components.rememberCountdownRemainingMillis
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray

/**
 * Screen 2: Running Timer screen.
 * Driven by an allocation-free 60fps frame clock decoupled from state emissions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningTimerScreen(
    status: TimerStatus,
    targetEndTime: Long,
    pausedRemainingMillis: Long,
    totalMillis: Long,
    theme: TimerTheme,
    onThemeChange: (TimerTheme) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaused = status == TimerStatus.Paused
    var showThemeSheet by remember { mutableStateOf(false) }

    // High performance frame clock for smooth 60fps drawing without recomposition
    val liveRemainingMillis by rememberCountdownRemainingMillis(
        status = status,
        targetEndTime = targetEndTime,
        pausedRemainingMillis = pausedRemainingMillis
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtleGray,
                    letterSpacing = 4.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = "Choose theme" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = stringResource(R.string.action_change_theme),
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onFullscreenClick,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = "Enter fullscreen mode" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = stringResource(R.string.action_fullscreen),
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Visual container with 60fps frame updates
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TimerVisual(
                    theme = theme,
                    remainingMillis = liveRemainingMillis,
                    totalMillis = totalMillis,
                    status = status
                )
            }

            // Bottom action controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryPillButton(
                    text = stringResource(R.string.action_reset),
                    onClick = onResetClick,
                    modifier = Modifier.weight(1f)
                )

                PrimaryPillButton(
                    text = stringResource(if (isPaused) R.string.action_resume else R.string.action_pause),
                    onClick = if (isPaused) onResumeClick else onPauseClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (showThemeSheet) {
            ThemeBottomSheet(
                selectedTheme = theme,
                onThemeSelected = onThemeChange,
                onDismissRequest = { showThemeSheet = false }
            )
        }
    }
}
