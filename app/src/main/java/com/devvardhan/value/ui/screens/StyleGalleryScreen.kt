package com.devvardhan.value.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devvardhan.value.R
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.ThemeLayoutMode
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.components.AppIcons
import com.devvardhan.value.ui.components.PrimaryPillButton
import com.devvardhan.value.ui.components.SecondaryPillButton
import com.devvardhan.value.ui.components.StopwatchVisual
import com.devvardhan.value.ui.components.TimerVisual
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme

/**
 * Screen for choosing visual countdown or stopwatch styles in a live, high-fidelity gallery.
 * Shown automatically on first launch or opened via the top Style button.
 */
@Composable
fun StyleGalleryScreen(
    mode: AppMode,
    selectedTheme: TimerTheme,
    canDismiss: Boolean,
    onThemeSelected: (TimerTheme) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themes = remember(mode) { AdaptiveLayoutHelper.getGalleryStyles(mode) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth
        val isLandscape = availableWidth > availableHeight * 1.15f
        val initialIndex = remember(themes, selectedTheme) {
            val idx = themes.indexOf(selectedTheme)
            if (idx >= 0) idx else 0
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = if (isLandscape) 10.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.gallery_title),
                        fontSize = if (isLandscape) 20.sp else 24.sp,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.gallery_subtitle),
                        fontSize = 12.sp,
                        color = SubtleGray,
                        letterSpacing = 0.2.sp
                    )
                }

                if (canDismiss) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = "Close gallery" }
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 18.dp))

            if (isLandscape) {
                // Landscape: Horizontal scrollable row of cards
                val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                val centeredIndex by remember {
                    derivedStateOf {
                        val layoutInfo = lazyListState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) initialIndex
                        else {
                            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                            visibleItems.minByOrNull { item ->
                                val itemCenter = item.offset + item.size / 2
                                kotlin.math.abs(itemCenter - viewportCenter)
                            }?.index ?: initialIndex
                        }
                    }
                }

                val cardHeight = (availableHeight - 64.dp).coerceAtLeast(180.dp)

                LazyRow(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(themes, key = { _, item -> item.name }) { index, themeItem ->
                        val isSelected = themeItem == selectedTheme
                        val isCentered = centeredIndex == index
                        val isWide = themeItem == TimerTheme.THREE_BOX || themeItem == TimerTheme.FLIP_CLOCK
                        val cardWidth = if (isWide) {
                            (cardHeight * 1.15f).coerceIn(260.dp, 420.dp)
                        } else {
                            (cardHeight * 0.88f).coerceIn(210.dp, 320.dp)
                        }

                        StylePreviewCard(
                            theme = themeItem,
                            mode = mode,
                            isSelected = isSelected,
                            isAnimated = isCentered,
                            onSelect = { onThemeSelected(themeItem) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(cardWidth)
                        )
                    }
                }
            } else {
                // Portrait: HorizontalPager with peeking cards and dot indicator
                val pagerState = rememberPagerState(initialPage = initialIndex) { themes.size }

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val themeItem = themes[page]
                    val isSelected = themeItem == selectedTheme
                    val isCentered = pagerState.currentPage == page

                    StylePreviewCard(
                        theme = themeItem,
                        mode = mode,
                        isSelected = isSelected,
                        isAnimated = isCentered,
                        onSelect = { onThemeSelected(themeItem) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(themes.size) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) PureWhite else Color(0xFF333333))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Live preview card for a single theme.
 * Runs a lightweight 10-second demo loop only when `isAnimated` is true.
 */
@Composable
private fun StylePreviewCard(
    theme: TimerTheme,
    mode: AppMode,
    isSelected: Boolean,
    isAnimated: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStopwatchMode = mode == AppMode.STOPWATCH
    val cardShape = RoundedCornerShape(22.dp)
    val isWide = theme == TimerTheme.THREE_BOX || theme == TimerTheme.FLIP_CLOCK
    val totalDemoDuration = if (isWide && !isStopwatchMode) 75_420_000L else 10_000L

    // Demo clock: For Three Box / Flip Clock, show HH:MM:SS with all six digits visible
    var demoMillis by remember {
        mutableLongStateOf(
            if (isWide) {
                if (isStopwatchMode) 3_665_000L else 37_240_000L
            } else {
                if (isStopwatchMode) 3_500L else 7_200L
            }
        )
    }

    LaunchedEffect(isAnimated, isWide, isStopwatchMode) {
        if (!isAnimated) return@LaunchedEffect
        val startNano = System.nanoTime()
        while (true) {
            withFrameNanos { frameNano ->
                val elapsed = (frameNano - startNano) / 1_000_000L
                demoMillis = if (isWide) {
                    if (isStopwatchMode) {
                        3_665_000L + (elapsed % 10_000L) // 01:01:05.xx to 01:01:15.xx (all 6 digits visible)
                    } else {
                        37_240_000L - (elapsed % 10_000L) // ~10h 20m 40s (all 6 digits visible)
                    }
                } else {
                    if (isStopwatchMode) {
                        elapsed % 10_000L
                    } else {
                        10_000L - (elapsed % 10_000L)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(Color(0xFF0C0C0C))
            .border(
                border = if (isSelected) BorderStroke(2.dp, PureWhite) else BorderStroke(1.dp, BorderSubtle),
                shape = cardShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Preview Display Area - 1:1 for circular styles, 3:2 for wide styles (Three Box, Flip Clock)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val targetAspect = if (isWide) 3f / 2f else 1f
                val (boxWidth, boxHeight) = if (maxWidth / maxHeight > targetAspect) {
                    val h = maxHeight * 0.92f // 8% margin
                    Pair(h * targetAspect, h)
                } else {
                    val w = maxWidth * 0.92f // 8% margin
                    Pair(w, w / targetAspect)
                }

                Box(
                    modifier = Modifier
                        .size(width = boxWidth, height = boxHeight)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PureBlack)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isStopwatchMode) {
                        StopwatchVisual(
                            theme = theme,
                            elapsedMillis = demoMillis,
                            status = if (isAnimated) StopwatchStatus.Running else StopwatchStatus.Paused,
                            layoutMode = ThemeLayoutMode.PORTRAIT
                        )
                    } else {
                        TimerVisual(
                            theme = theme,
                            remainingMillis = demoMillis,
                            totalMillis = totalDemoDuration,
                            status = if (isAnimated) TimerStatus.Running else TimerStatus.Paused,
                            layoutMode = ThemeLayoutMode.PORTRAIT
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Style Title
            Text(
                text = theme.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                letterSpacing = 1.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Select Button
            if (isSelected) {
                PrimaryPillButton(
                    text = stringResource(R.string.action_selected),
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    height = 44.dp
                )
            } else {
                SecondaryPillButton(
                    text = stringResource(R.string.action_select),
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    height = 44.dp
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Gallery Portrait (Timer)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewStyleGalleryPortraitTimer() {
    ValueTimerTheme {
        StyleGalleryScreen(
            mode = AppMode.TIMER,
            selectedTheme = TimerTheme.CIRCLE,
            canDismiss = true,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Gallery Portrait (Stopwatch First Launch)", showBackground = true, backgroundColor = 0xFF000000, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewStyleGalleryPortraitStopwatchFirstLaunch() {
    ValueTimerTheme {
        StyleGalleryScreen(
            mode = AppMode.STOPWATCH,
            selectedTheme = TimerTheme.THREE_BOX,
            canDismiss = false,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Gallery Landscape", showBackground = true, backgroundColor = 0xFF000000, widthDp = 844, heightDp = 390)
@Composable
private fun PreviewStyleGalleryLandscape() {
    ValueTimerTheme {
        StyleGalleryScreen(
            mode = AppMode.TIMER,
            selectedTheme = TimerTheme.CONCENTRIC_RINGS,
            canDismiss = true,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Gallery Tablet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewStyleGalleryTablet() {
    ValueTimerTheme {
        StyleGalleryScreen(
            mode = AppMode.TIMER,
            selectedTheme = TimerTheme.FLIP_CLOCK,
            canDismiss = true,
            onThemeSelected = {},
            onDismiss = {}
        )
    }
}
