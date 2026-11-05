package com.devvardhan.value.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import kotlinx.coroutines.launch

/**
 * Three-wheel time duration picker (Hours, Minutes, Seconds) with snapping momentum scrolling
 * and direct tactile tappable steppers.
 */
@Composable
fun WheelTimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours Wheel
        WheelColumn(
            label = "HOURS",
            selectedValue = hours,
            maxValue = 23,
            onValueChange = onHoursChange,
            modifier = Modifier.weight(1f)
        )

        ColonSeparator()

        // Minutes Wheel
        WheelColumn(
            label = "MINUTES",
            selectedValue = minutes,
            maxValue = 59,
            onValueChange = onMinutesChange,
            modifier = Modifier.weight(1f)
        )

        ColonSeparator()

        // Seconds Wheel
        WheelColumn(
            label = "SECONDS",
            selectedValue = seconds,
            maxValue = 59,
            onValueChange = onSecondsChange,
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
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 56.dp
    val totalWheelHeight = itemHeight * 3

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedValue)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

    // Sync scrolling state to selectedValue
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val snappedIndex = lazyListState.firstVisibleItemIndex
            if (snappedIndex in 0..maxValue && snappedIndex != selectedValue) {
                onValueChange(snappedIndex)
            }
        }
    }

    // Sync external selectedValue changes to list scroll
    LaunchedEffect(selectedValue) {
        if (!lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex != selectedValue) {
            lazyListState.animateScrollToItem(selectedValue)
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

        // Increment button (+)
        IconButton(
            onClick = {
                val next = (selectedValue + 1).coerceAtMost(maxValue)
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

            // Scrollable numbers
            LazyColumn(
                state = lazyListState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(maxValue + 1) { index ->
                    val isSelected = index == selectedValue
                    val isAdjacent = kotlin.math.abs(index - selectedValue) == 1

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
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = TimeUtils.formatTwoDigits(index),
                            fontSize = if (isSelected) 42.sp else 24.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Normal else FontWeight.ExtraLight,
                            color = if (isSelected) PureWhite else SubtleGray,
                            modifier = Modifier.alpha(
                                if (isSelected) 1.0f else if (isAdjacent) 0.45f else 0.15f
                            )
                        )
                    }
                }
            }
        }

        // Decrement button (-)
        IconButton(
            onClick = {
                val prev = (selectedValue - 1).coerceAtLeast(0)
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

