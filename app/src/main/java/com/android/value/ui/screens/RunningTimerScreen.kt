package com.android.value.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.model.TimerStatus
import com.android.value.model.TimerTheme
import com.android.value.ui.components.PrimaryPillButton
import com.android.value.ui.components.SecondaryPillButton
import com.android.value.ui.components.ThemeBottomSheet
import com.android.value.ui.components.TimerVisual
import com.android.value.ui.theme.BorderSubtle
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray

/**
 * Screen 2: Running Timer screen.
 * Displays the selected dynamic TimerVisual, top actions (theme picker, fullscreen),
 * and bottom controls (Reset, Pause/Resume).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningTimerScreen(
    status: TimerStatus,
    remainingMillis: Long,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top header with app name, theme palette button, and fullscreen toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VALUE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtleGray,
                    letterSpacing = 4.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Palette button opens theme picker bottom sheet
                    IconButton(
                        onClick = { showThemeSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change Theme",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Fullscreen toggle button
                    IconButton(
                        onClick = onFullscreenClick,
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Centered visualization rendering the active theme
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                TimerVisual(
                    theme = theme,
                    remainingMillis = remainingMillis,
                    totalMillis = totalMillis,
                    status = status
                )
            }

            // Bottom action controls: Reset and Pause/Resume
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryPillButton(
                    text = "Reset",
                    onClick = onResetClick,
                    modifier = Modifier.weight(1f)
                )

                PrimaryPillButton(
                    text = if (isPaused) "Resume" else "Pause",
                    onClick = if (isPaused) onResumeClick else onPauseClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live theme picker bottom sheet
        if (showThemeSheet) {
            ThemeBottomSheet(
                selectedTheme = theme,
                onThemeSelected = { newTheme ->
                    onThemeChange(newTheme)
                },
                onDismissRequest = { showThemeSheet = false }
            )
        }
    }
}
