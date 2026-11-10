package com.devvardhan.value.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.devvardhan.value.R
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.GalleryDemoClock
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
import com.devvardhan.value.ui.theme.Motion
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.SubtleGray
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.debugTestTag
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue

/**
 * Screen for choosing visual countdown or stopwatch styles in a live gallery.
 *
 * A) First Launch (guided 2-step onboarding):
 *    - Step 1: Choose timer style. Selecting saves and auto-slides to Step 2.
 *    - Step 2: Choose stopwatch style. Selecting saves and finishes onboarding.
 *    - Header: Title, subtitle and a thin step progress bar. No close button.
 *
 * B) Editing Later (single mode, no auto-advance):
 *    - Header: Title, subtitle and a Close (X) button.
 *    - Selecting a style saves and returns to main immediately.
 */
@Composable
fun StyleGalleryScreen(
    isFirstLaunch: Boolean,
    firstLaunchStep: Int = 1,
    editMode: AppMode = AppMode.TIMER,
    timerTheme: TimerTheme = TimerTheme.CIRCLE,
    stopwatchTheme: TimerTheme = TimerTheme.CIRCLE,
    onTimerThemeSelected: (TimerTheme) -> Unit = {},
    onStopwatchThemeSelected: (TimerTheme) -> Unit = {},
    onThemeSelectedInEditMode: (TimerTheme) -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth
        val isLandscape = availableWidth > availableHeight * 1.15f

        val context = LocalContext.current
        val isReducedMotion = remember(context) { Motion.isAnimationsDisabled(context) }

        if (isFirstLaunch) {
            AnimatedContent(
                targetState = firstLaunchStep,
                transitionSpec = {
                    Motion.galleryStepTransition(
                        forward = targetState > initialState,
                        isZeroDuration = isReducedMotion
                    )
                },
                label = "FirstLaunchStepTransition",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                val currentMode = if (step == 1) AppMode.TIMER else AppMode.STOPWATCH
                val themes = remember(currentMode) { AdaptiveLayoutHelper.getGalleryStyles(currentMode) }
                val activeTheme = if (step == 1) timerTheme else stopwatchTheme
                val title = stringResource(
                    if (step == 1) R.string.gallery_step1_title else R.string.gallery_step2_title
                )
                val subtitle = stringResource(
                    if (step == 1) R.string.gallery_step1_subtitle else R.string.gallery_step2_subtitle
                )

                GalleryLayout(
                    mode = currentMode,
                    themes = themes,
                    selectedTheme = activeTheme,
                    title = title,
                    subtitle = subtitle,
                    showProgress = true,
                    progressStep = step,
                    showCloseButton = false,
                    isLandscape = isLandscape,
                    onThemeSelected = { selected ->
                        if (step == 1) onTimerThemeSelected(selected)
                        else onStopwatchThemeSelected(selected)
                    },
                    onDismiss = onDismiss
                )
            }
        } else {
            val themes = remember(editMode) { AdaptiveLayoutHelper.getGalleryStyles(editMode) }
            val activeTheme = if (editMode == AppMode.TIMER) timerTheme else stopwatchTheme
            val title = stringResource(
                if (editMode == AppMode.TIMER) R.string.gallery_edit_timer_title
                else R.string.gallery_edit_stopwatch_title
            )
            val subtitle = stringResource(R.string.gallery_edit_subtitle)

            GalleryLayout(
                mode = editMode,
                themes = themes,
                selectedTheme = activeTheme,
                title = title,
                subtitle = subtitle,
                showProgress = false,
                progressStep = 1,
                showCloseButton = true,
                isLandscape = isLandscape,
                onThemeSelected = onThemeSelectedInEditMode,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Responsive layout: header -> centered pager of equal-size cards -> page dots.
 */
@Composable
private fun GalleryLayout(
    mode: AppMode,
    themes: List<TimerTheme>,
    selectedTheme: TimerTheme,
    title: String,
    subtitle: String,
    showProgress: Boolean,
    progressStep: Int,
    showCloseButton: Boolean,
    isLandscape: Boolean,
    onThemeSelected: (TimerTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val initialIndex = remember(themes, selectedTheme) {
        themes.indexOf(selectedTheme).coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialIndex) { themes.size }
    val haptic = LocalHapticFeedback.current

    // Tactile haptic tick when snapping to a style card
    LaunchedEffect(pagerState.settledPage) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = if (isLandscape) 8.dp else 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderTexts(
                    title = title,
                    subtitle = subtitle,
                    titleSize = 18.sp,
                    subtitleSize = 11.sp,
                    gap = 2.dp,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (showProgress) {
                    StepProgressBar(
                        currentStep = progressStep,
                        modifier = Modifier.width(72.dp)
                    )
                } else if (showCloseButton) {
                    CloseButton(size = 40.dp, iconSize = 18.dp, onClick = onDismiss)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (showProgress) {
                    StepProgressBar(
                        currentStep = progressStep,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderTexts(
                        title = title,
                        subtitle = subtitle,
                        titleSize = 22.sp,
                        subtitleSize = 12.sp,
                        gap = 3.dp,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (showCloseButton) {
                        CloseButton(size = 48.dp, iconSize = 20.dp, onClick = onDismiss)
                    }
                }
            }
        }

        // --- MIDDLE: sized from the real remaining space ---
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Step 3: Compute target metrics, then smoothly animate dimensions on orientation changes
            val targetMetrics = remember(maxWidth, maxHeight, isLandscape) {
                computeCardMetrics(maxWidth, maxHeight, isLandscape)
            }
            val animatedCardWidth by animateDpAsState(
                targetValue = targetMetrics.cardWidth,
                animationSpec = tween(durationMillis = 250),
                label = "CardWidthAnimation"
            )
            val animatedCardHeight by animateDpAsState(
                targetValue = targetMetrics.cardHeight,
                animationSpec = tween(durationMillis = 250),
                label = "CardHeightAnimation"
            )
            val animatedPreviewWidth by animateDpAsState(
                targetValue = targetMetrics.previewWidth,
                animationSpec = tween(durationMillis = 250),
                label = "PreviewWidthAnimation"
            )
            val animatedPreviewHeight by animateDpAsState(
                targetValue = targetMetrics.previewHeight,
                animationSpec = tween(durationMillis = 250),
                label = "PreviewHeightAnimation"
            )

            val metrics = targetMetrics.copy(
                cardWidth = animatedCardWidth,
                cardHeight = animatedCardHeight,
                previewWidth = animatedPreviewWidth,
                previewHeight = animatedPreviewHeight
            )
            val sidePadding = ((maxWidth - metrics.cardWidth) / 2).coerceAtLeast(0.dp)

            // Step 2: Key pager items by stable theme name to avoid unnecessary item recreations
            HorizontalPager(
                state = pagerState,
                key = { themes[it].name },
                contentPadding = PaddingValues(horizontal = sidePadding),
                pageSize = PageSize.Fixed(metrics.cardWidth),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(1)
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.cardHeight)
            ) { page ->
                val themeItem = themes[page]
                val isSelected = themeItem == selectedTheme

                // Step 2: Freeze preview animations while scrolling; activate only when page is settled
                val isActive by remember(page) {
                    derivedStateOf {
                        pagerState.settledPage == page && !pagerState.isScrollInProgress
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Read in the draw phase: no recomposition while swiping
                            val offset = ((pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction).absoluteValue
                                .coerceIn(0f, 1f)
                            val s = lerp(1f, 0.94f, offset)
                            scaleX = s
                            scaleY = s
                            alpha = lerp(1f, 0.6f, offset)
                            // Step 2: Use ModulateAlpha to eliminate offscreen buffer allocations
                            compositingStrategy = CompositingStrategy.ModulateAlpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    StylePreviewCard(
                        theme = themeItem,
                        mode = mode,
                        isSelected = isSelected,
                        isAnimated = isActive,
                        isLandscape = isLandscape,
                        metrics = metrics,
                        onSelect = {
                            if (isSelected && showCloseButton) onDismiss()
                            else onThemeSelected(themeItem)
                        }
                    )
                }
            }
        }

        // --- BOTTOM: PAGE INDICATOR DOTS ---
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(themes.size) { index ->
                val isCurrent by remember(index) {
                    derivedStateOf { pagerState.currentPage == index }
                }
                GalleryPageDot(isSelected = isCurrent)
            }
        }
    }
}

/**
 * Isolated indicator dot composable to prevent recompilation of the entire gallery layout on page change.
 */
@Composable
private fun GalleryPageDot(
    isSelected: Boolean
) {
    val dotSize by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 6.dp,
        animationSpec = tween(150),
        label = "DotSize"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isSelected) PureWhite else Color(0xFF333333),
        animationSpec = tween(150),
        label = "DotColor"
    )
    Box(
        modifier = Modifier
            .size(dotSize)
            .clip(CircleShape)
            .background(dotColor)
    )
}

@Composable
private fun HeaderTexts(
    title: String,
    subtitle: String,
    titleSize: androidx.compose.ui.unit.TextUnit,
    subtitleSize: androidx.compose.ui.unit.TextUnit,
    gap: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = titleSize,
            fontWeight = FontWeight.Light,
            color = PureWhite,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(gap))
        Text(
            text = subtitle,
            fontSize = subtitleSize,
            color = SubtleGray,
            letterSpacing = 0.2.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CloseButton(
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .border(1.dp, BorderSubtle, CircleShape)
            .semantics { contentDescription = "Close gallery" }
            .debugTestTag("btn_close_gallery")
    ) {
        Icon(
            imageVector = AppIcons.Close,
            contentDescription = stringResource(R.string.action_close),
            tint = PureWhite,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Minimal read-only step indicator: thin segments that fill as onboarding progresses.
 */
@Composable
private fun StepProgressBar(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 2
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Step $currentStep of $totalSteps"
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            val filled = index < currentStep
            val color by animateColorAsState(
                targetValue = if (filled) PureWhite else Color(0xFF2A2A2A),
                animationSpec = tween(durationMillis = 300),
                label = "StepSegment$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// CARD METRICS
// -------------------------------------------------------------------------------------------------

@Immutable
private data class CardMetrics(
    val cardWidth: Dp,
    val cardHeight: Dp,
    val previewWidth: Dp,
    val previewHeight: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val titleHeight: Dp,
    val buttonHeight: Dp
)

private const val PREVIEW_ASPECT = 4f / 3f // same preview box for ALL styles

private fun computeCardMetrics(areaWidth: Dp, areaHeight: Dp, isLandscape: Boolean): CardMetrics {
    val cardPadding = if (isLandscape) 10.dp else 14.dp
    val itemSpacing = if (isLandscape) 8.dp else 12.dp
    val buttonHeight = if (isLandscape) 38.dp else 44.dp
    val titleHeight = if (isLandscape) 20.dp else 24.dp
    val breathing = 8.dp

    val chrome = cardPadding * 2 + itemSpacing * 2 + titleHeight + buttonHeight

    val maxCardWidth = if (isLandscape) {
        (areaWidth * 0.4f).coerceIn(240.dp, 440.dp)
    } else {
        (areaWidth - 84.dp).coerceIn(240.dp, 360.dp)
    }

    val maxPreviewHeight = (areaHeight - chrome - breathing * 2).coerceAtLeast(80.dp)
    val widthBasedPreviewHeight = (maxCardWidth - cardPadding * 2) / PREVIEW_ASPECT

    val previewHeight = minOf(widthBasedPreviewHeight, maxPreviewHeight)
    val previewWidth = previewHeight * PREVIEW_ASPECT

    return CardMetrics(
        cardWidth = previewWidth + cardPadding * 2,
        cardHeight = previewHeight + chrome,
        previewWidth = previewWidth,
        previewHeight = previewHeight,
        cardPadding = cardPadding,
        itemSpacing = itemSpacing,
        titleHeight = titleHeight,
        buttonHeight = buttonHeight
    )
}

// -------------------------------------------------------------------------------------------------
// CARD + LIVE PREVIEW
// -------------------------------------------------------------------------------------------------

@Composable
private fun StylePreviewCard(
    theme: TimerTheme,
    mode: AppMode,
    isSelected: Boolean,
    isAnimated: Boolean,
    isLandscape: Boolean,
    metrics: CardMetrics,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .size(metrics.cardWidth, metrics.cardHeight)
            .clip(cardShape)
            .background(Color(0xFF0C0C0C))
            .border(
                border = if (isSelected) BorderStroke(2.dp, PureWhite)
                else BorderStroke(1.dp, BorderSubtle),
                shape = cardShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(metrics.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(metrics.previewWidth, metrics.previewHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(PureBlack)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            LivePreview(theme = theme, mode = mode, isAnimated = isAnimated)
        }

        Spacer(modifier = Modifier.height(metrics.itemSpacing))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.titleHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = theme.displayName,
                fontSize = if (isLandscape) 14.sp else 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(metrics.itemSpacing))

        if (isSelected) {
            PrimaryPillButton(
                text = stringResource(R.string.action_selected),
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .debugTestTag("btn_select_style"),
                height = metrics.buttonHeight
            )
        } else {
            SecondaryPillButton(
                text = stringResource(R.string.action_select),
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .debugTestTag("btn_select_style"),
                height = metrics.buttonHeight
            )
        }
    }
}

/**
 * Drives every preview from ONE deterministic demo clock.
 * Restarts from zero whenever a card becomes the settled page and when gallery opens.
 * Non-settled cards show the static starting frame (10s full / 00:00:00).
 */
@Composable
private fun LivePreview(
    theme: TimerTheme,
    mode: AppMode,
    isAnimated: Boolean
) {
    val isStopwatchMode = mode == AppMode.STOPWATCH
    val totalDemoDuration = GalleryDemoClock.DEMO_ACTIVE_MILLIS
    val isStopwatchState = rememberUpdatedState(isStopwatchMode)
    val isAnimatedState = rememberUpdatedState(isAnimated)

    // Elapsed demo time; strictly resets to 0 whenever isAnimated becomes true
    val phaseNanosState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(isAnimated) {
        if (!isAnimated) {
            phaseNanosState.longValue = 0L
            return@LaunchedEffect
        }
        phaseNanosState.longValue = 0L
        val startNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                phaseNanosState.longValue = now - startNanos
            }
        }
    }

    val demoMillisProvider: () -> Long = remember {
        {
            if (!isAnimatedState.value) {
                if (isStopwatchState.value) 0L else totalDemoDuration
            } else {
                val elapsedMs = phaseNanosState.longValue / 1_000_000L
                if (isStopwatchState.value) {
                    GalleryDemoClock.calculateStopwatchElapsed(elapsedMs)
                } else {
                    GalleryDemoClock.calculateTimerRemaining(elapsedMs)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (isAnimatedState.value) {
                    val elapsedMs = phaseNanosState.longValue / 1_000_000L
                    alpha = GalleryDemoClock.calculateLoopAlpha(elapsedMs)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isStopwatchMode) {
            StopwatchVisual(
                theme = theme,
                elapsedMillisProvider = demoMillisProvider,
                status = if (isAnimated) StopwatchStatus.Running else StopwatchStatus.Paused,
                layoutMode = ThemeLayoutMode.PORTRAIT
            )
        } else {
            TimerVisual(
                theme = theme,
                remainingMillisProvider = demoMillisProvider,
                totalMillis = totalDemoDuration,
                status = if (isAnimated) TimerStatus.Running else TimerStatus.Paused,
                layoutMode = ThemeLayoutMode.PORTRAIT
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------------------------------------------

@Preview(name = "Step 1: First Launch (Timer) - Portrait 412x915", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915)
@Composable
private fun PreviewGalleryStep1PortraitLarge() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 1,
            timerTheme = TimerTheme.CIRCLE,
            stopwatchTheme = TimerTheme.CIRCLE
        )
    }
}

@Preview(name = "Step 1: First Launch (Timer) - Portrait 360x640", showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 640)
@Composable
private fun PreviewGalleryStep1PortraitSmall() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 1,
            timerTheme = TimerTheme.HOURGLASS,
            stopwatchTheme = TimerTheme.CIRCLE
        )
    }
}

@Preview(name = "Step 2: First Launch (Stopwatch) - Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915)
@Composable
private fun PreviewGalleryStep2Portrait() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 2,
            timerTheme = TimerTheme.CONCENTRIC_RINGS,
            stopwatchTheme = TimerTheme.THREE_BOX
        )
    }
}

@Preview(name = "Edit Mode: Timer - Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915)
@Composable
private fun PreviewGalleryEditTimerPortrait() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = false,
            editMode = AppMode.TIMER,
            timerTheme = TimerTheme.FLIP_CLOCK
        )
    }
}

@Preview(name = "Edit Mode: Stopwatch - Portrait", showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 915)
@Composable
private fun PreviewGalleryEditStopwatchPortrait() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = false,
            editMode = AppMode.STOPWATCH,
            stopwatchTheme = TimerTheme.CONCENTRIC_RINGS
        )
    }
}

@Preview(name = "Step 1: Landscape 915x412", showBackground = true, backgroundColor = 0xFF000000, widthDp = 915, heightDp = 412)
@Composable
private fun PreviewGalleryLandscapeStep1() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 1,
            timerTheme = TimerTheme.CIRCLE
        )
    }
}

@Preview(name = "Step 2: Landscape 915x412", showBackground = true, backgroundColor = 0xFF000000, widthDp = 915, heightDp = 412)
@Composable
private fun PreviewGalleryLandscapeStep2() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 2,
            stopwatchTheme = TimerTheme.FLIP_CLOCK
        )
    }
}

@Preview(name = "Edit Mode: Landscape 915x412", showBackground = true, backgroundColor = 0xFF000000, widthDp = 915, heightDp = 412)
@Composable
private fun PreviewGalleryLandscapeEdit() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = false,
            editMode = AppMode.TIMER,
            timerTheme = TimerTheme.THREE_BOX
        )
    }
}

@Preview(name = "Tablet 1200x800", showBackground = true, backgroundColor = 0xFF000000, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewGalleryTablet() {
    ValueTimerTheme {
        StyleGalleryScreen(
            isFirstLaunch = true,
            firstLaunchStep = 1,
            timerTheme = TimerTheme.LIQUID_FILL
        )
    }
}
