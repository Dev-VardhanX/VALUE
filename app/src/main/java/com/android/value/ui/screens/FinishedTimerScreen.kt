package com.android.value.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.value.ui.components.MinimalCircularProgress
import com.android.value.ui.components.PrimaryPillButton
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.PureWhite
import com.android.value.ui.theme.SubtleGray

/**
 * Screen 4: Finished state.
 * Displays "Time's up" with 00:00:00 and a large Dismiss pill button.
 */
@Composable
fun FinishedTimerScreen(
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle pulsating opacity animation for the zero ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
            // Header
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
            }

            // Center indicator with 00:00:00 and "Time's up"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MinimalCircularProgress(
                    progress = 1.0f,
                    size = 280.dp,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.alpha(pulseAlpha)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "00:00:00",
                            fontSize = 44.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                            color = PureWhite,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TIME'S UP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubtleGray,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            // Dismiss Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryPillButton(
                    text = "Dismiss",
                    onClick = onDismissClick,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

