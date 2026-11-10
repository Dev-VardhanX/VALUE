package com.devvardhan.value.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Three-wheel time duration picker (Hours 0..99, Minutes 0..59, Seconds 0..59)
 * with circular infinite looping, snapping momentum scrolling, looping +/- steppers,
 * and tabular monospace figure formatting.
 */
@Composable
fun WheelTimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onScrollStateChange: (Boolean) -> Unit = {}
) {
    var hoursScrolling by remember { mutableStateOf(false) }
    var minutesScrolling by remember { mutableStateOf(false) }
    var secondsScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(hoursScrolling, minutesScrolling, secondsScrolling) {
        onScrollStateChange(hoursScrolling || minutesScrolling || secondsScrolling)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours Wheel (0..99)
        WheelColumn(
            label = "HOURS",
            selectedValue = hours,
            maxValue = 99,
            onValueChange = onHoursChange,
            onScrollStateChange = { hoursScrolling = it },
            modifier = Modifier.weight(1f)
        )

        ColonSeparator()

        // Minutes Wheel (0..59)
        WheelColumn(
            label = "MINUTES",
            selectedValue = minutes,
            maxValue = 59,
            onValueChange = onMinutesChange,
            onScrollStateChange = { minutesScrolling = it },
            modifier = Modifier.weight(1f)
        )

        ColonSeparator()

        // Seconds Wheel (0..59)
        WheelColumn(
            label = "SECONDS",
            selectedValue = seconds,
            maxValue = 59,
            onValueChange = onSecondsChange,
            onScrollStateChange = { secondsScrolling = it },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ColonSeparator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Spacer matching label height
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = ":",
            fontSize = 36.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            color = SubtleGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    label: String,
    selectedValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onScrollStateChange: (Boolean) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val itemHeight = 56.dp
    val totalWheelHeight = itemHeight * 3

    val rangeSize = maxValue + 1
    val repeatCount = 1000
    val totalVirtualItems = rangeSize * repeatCount
    val middleBaseIndex = (repeatCount / 2) * rangeSize

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = middleBaseIndex + selectedValue)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

    // Tactile haptic tick on wheel snap during scroll
    LaunchedEffect(lazyListState.firstVisibleItemIndex) {
        if (lazyListState.isScrollInProgress) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Sync scrolling state to selectedValue and notify high refresh rate controller
    LaunchedEffect(lazyListState.isScrollInProgress) {
        onScrollStateChange(lazyListState.isScrollInProgress)
        if (!lazyListState.isScrollInProgress) {
            val currentIndex = lazyListState.firstVisibleItemIndex
            val resolvedValue = (currentIndex % rangeSize + rangeSize) % rangeSize
            if (resolvedValue != selectedValue) {
                onValueChange(resolvedValue)
            }
        }
    }

    // Sync external selectedValue changes (such as +/- buttons) to list scroll with shortest path wrap
    LaunchedEffect(selectedValue) {
        if (!lazyListState.isScrollInProgress) {
            val currentIndex = lazyListState.firstVisibleItemIndex
            val currentResolved = (currentIndex % rangeSize + rangeSize) % rangeSize
            if (currentResolved != selectedValue) {
                var diff = selectedValue - currentResolved
                if (diff > rangeSize / 2) {
                    diff -= rangeSize
                } else if (diff < -rangeSize / 2) {
                    diff += rangeSize
                }
                val targetIndex = currentIndex + diff
                lazyListState.animateScrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top column label
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = SubtleGray,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Increment button (+) with circular looping
        IconButton(
            onClick = {
                val next = (selectedValue + 1) % rangeSize
                onValueChange(next)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = AppIcons.Add,
                contentDescription = "Increment $label",
                tint = SubtleGray,
                modifier = Modifier.size(18.dp)
            )
        }

        // Wheel viewport with center selection indicator lines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalWheelHeight),
            contentAlignment = Alignment.Center
        ) {
            // Subtle selection frame lines
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
            }

            // Infinite circular scrollable numbers
            LazyColumn(
                state = lazyListState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    count = totalVirtualItems,
                    key = { it }
                ) { index ->
                    val value = index % rangeSize

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(index)
                                }
                            }
                            .graphicsLayer {
                                val itemHeightPx = itemHeight.toPx()
                                val scrollOffset = if (itemHeightPx > 0f) {
                                    lazyListState.firstVisibleItemScrollOffset / itemHeightPx
                                } else 0f
                                val continuousIndex = lazyListState.firstVisibleItemIndex.toFloat() + scrollOffset
                                val diff = (index.toFloat() - continuousIndex).absoluteValue

                                // Smoothly interpolated alpha: 1.0 at center, ~0.35 at ±1, ~0.10 at ±2
                                val calculatedAlpha = when {
                                    diff <= 1f -> 1.0f - diff * 0.65f
                                    diff <= 2f -> 0.35f - (diff - 1f) * 0.25f
                                    else -> 0.10f
                                }.coerceIn(0.10f, 1.0f)

                                // Smoothly interpolated scale: 1.0 at center, ~0.65 at ±1
                                val calculatedScale = (1.0f - diff * 0.35f).coerceIn(0.60f, 1.0f)

                                alpha = calculatedAlpha
                                scaleX = calculatedScale
                                scaleY = calculatedScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = TimeUtils.formatTwoDigits(value),
                            style = TextStyle(
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                fontFeatureSettings = "tnum"
                            ),
                            color = PureWhite
                        )
                    }
                }
            }
        }

        // Decrement button (-) with circular looping
        IconButton(
            onClick = {
                val prev = (selectedValue - 1 + rangeSize) % rangeSize
                onValueChange(prev)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = AppIcons.Remove,
                contentDescription = "Decrement $label",
                tint = SubtleGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
