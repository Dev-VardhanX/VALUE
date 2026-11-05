package com.android.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.core.TimeUtils
import com.android.value.model.TimerStatus
import com.android.value.ui.components.MinimalCircularProgress
import com.android.value.ui.components.PrimaryPillButton
import com.android.value.ui.components.SecondaryPillButton
import com.android.value.ui.theme.BorderSubtle
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray

/**
 * Screen 2: Running Timer screen.
 * Displays large tabular countdown, thin circular progress indicator,
 * Pause/Resume, Reset, and Fullscreen toggle buttons.
 */
@Composable
fun RunningTimerScreen(
    status: TimerStatus,
    remainingMillis: Long,
    totalMillis: Long,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onResetClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalMillis > 0L) {
        (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val isPaused = status == TimerStatus.Paused
    val formattedTime = TimeUtils.formatHms(remainingMillis)
    val totalTimeFormatted = TimeUtils.formatHms(totalMillis)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        // Top header with app name and fullscreen toggle
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

        // Centered circular progress with large remaining time
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MinimalCircularProgress(
                progress = progress,
                size = 300.dp,
                strokeWidth = 3.5.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Very large remaining time in HH:MM:SS using thin monospace font
                    Text(
                        text = formattedTime,
                        fontSize = 44.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPaused) "PAUSED" else "TOTAL $totalTimeFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubtleGray,
                        letterSpacing = 2.sp,
                        modifier = Modifier.alpha(if (isPaused) 1.0f else 0.7f)
                    )
                }
            }
        }

        // Bottom action controls: Reset and Pause/Resume
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp),
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
}

