package com.android.value.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.ui.components.PrimaryPillButton
import com.android.value.ui.components.WheelTimePicker
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray

/**
 * Screen 1: Set Timer screen.
 * Allows user to pick Hours (0-23), Minutes (0-59), and Seconds (0-59) via wheel/tappable pickers.
 */
@Composable
fun SetTimerScreen(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = hours * 3600L + minutes * 60L + seconds
    val isStartEnabled = totalSeconds > 0L

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Minimal header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "VALUE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtleGray,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Set Timer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = PureWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // Wheel pickers in the center
            WheelTimePicker(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                onHoursChange = onHoursChange,
                onMinutesChange = onMinutesChange,
                onSecondsChange = onSecondsChange,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Large Start button at the bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryPillButton(
                    text = "Start",
                    onClick = onStartClick,
                    enabled = isStartEnabled,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

