package com.devvardhan.value.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.devvardhan.value.ui.util.debugTestTag

/**
 * Screen for choosing visual countdown or stopwatch styles in a live, high-fidelity gallery.
 *
 * Supports two distinct flows:
 * A) First Launch (guided 2-step onboarding):
 *    - Step 1: Choose timer style (all 7 styles). Selecting saves and auto-slides to Step 2.
 *    - Step 2: Choose stopwatch style (4 styles). Selecting saves, marks onboarding done, and opens main in Timer mode.
 *    - Header: Title, subtitle, read-only progress pill (Timer | Stopwatch). No close button.
 *    - Back on step 2 returns to step 1. Back on step 1 exits app.
 *
 * B) Editing Later (single mode, no auto-advance):
 *    - Timer screens open gallery for TIMER only. Stopwatch screen opens for STOPWATCH only.
 *    - Header: "Timer style" or "Stopwatch style", subtitle "Tap a style to apply it", Close (X) button.
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

        if (isFirstLaunch) {
            // First Launch: Guided 2-step flow with horizontal slide + fade animation
            AnimatedContent(
                targetState = firstLaunchStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(300)) { it } +
                            fadeIn(animationSpec = tween(300)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(300)) { -it } +
                                    fadeOut(animationSpec = tween(300))
                            )
                    } else {
                        (slideInHorizontally(animationSpec = tween(300)) { -it } +
                            fadeIn(animationSpec = tween(300)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(300)) { it } +
                                    fadeOut(animationSpec = tween(300))
                            )
                    }
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
                    showProgressPill = true,
                    progressStep = step,
                    showCloseButton = false,
                    isLandscape = isLandscape,
                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    onThemeSelected = { selected ->
                        if (step == 1) {
                            onTimerThemeSelected(selected)
                        } else {
                            onStopwatchThemeSelected(selected)
                        }
                    },
                    onDismiss = onDismiss
                )
            }
        } else {
            // Edit Mode: Single mode, no progress pill, with close button
            val themes = remember(editMode) { AdaptiveLayoutHelper.getGalleryStyles(editMode) }
            val activeTheme = if (editMode == AppMode.TIMER) timerTheme else stopwatchTheme
            val title = stringResource(
                if (editMode == AppMode.TIMER) R.string.gallery_edit_timer_title else R.string.gallery_edit_stopwatch_title
            )
            val subtitle = stringResource(R.string.gallery_edit_subtitle)

            GalleryLayout(
                mode = editMode,
                themes = themes,
                selectedTheme = activeTheme,
                title = title,
                subtitle = subtitle,
                showProgressPill = false,
                progressStep = 1,
                showCloseButton = true,
                isLandscape = isLandscape,
                availableWidth = availableWidth,
                availableHeight = availableHeight,
                onThemeSelected = onThemeSelectedInEditMode,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Responsive layout container for the gallery.
 * In Portrait: Header -> centered hugging card pager -> page indicator dots.
 * In Landscape: Compact slim header -> centered hugging card pager -> page indicator dots.
 */
@Composable
private fun GalleryLayout(
    mode: AppMode,
    themes: List<TimerTheme>,
    selectedTheme: TimerTheme,
    title: String,
    subtitle: String,
    showProgressPill: Boolean,
    progressStep: Int,
    showCloseButton: Boolean,
    isLandscape: Boolean,
    availableWidth: Dp,
    availableHeight: Dp,
    onThemeSelected: (TimerTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val initialIndex = remember(themes, selectedTheme) {
        val idx = themes.indexOf(selectedTheme)
        if (idx >= 0) idx else 0
    }
    val pagerState = rememberPagerState(initialPage = initialIndex) { themes.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = if (isLandscape) 8.dp else 14.dp,
                bottom = if (isLandscape) 8.dp else 14.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        if (isLandscape) {
            // Landscape Header: Title & Subtitle on left, Progress Pill or Close Button on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        color = PureWhite,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SubtleGray,
                        letterSpacing = 0.2.sp,
                        maxLines = 1
                    )
                }

                if (showProgressPill) {
                    ProgressModePill(
                        currentStep = progressStep,
                        modifier = Modifier.height(30.dp)
                    )
                } else if (showCloseButton) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .semantics { contentDescription = "Close gallery" }
                            .debugTestTag("btn_close_gallery")
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Portrait Header:
            // Row with Title & Subtitle on left, Close button on right (if editing)
            // Below it: Progress Pill (if first launch) with clean spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            color = PureWhite,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = SubtleGray,
                            letterSpacing = 0.2.sp,
                            maxLines = 1
                        )
                    }

                    if (showCloseButton) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, BorderSubtle, CircleShape)
                                .semantics { contentDescription = "Close gallery" }
                                .debugTestTag("btn_close_gallery")
                        ) {
                            Icon(
                                imageVector = AppIcons.Close,
                                contentDescription = stringResource(R.string.action_close),
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (showProgressPill) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressModePill(
                        currentStep = progressStep,
                        modifier = Modifier.height(34.dp)
                    )
                }
            }
        }

        // --- MIDDLE: PAGER WITH HUGGING CARDS CENTERED VERTICALLY ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(
                    horizontal = if (isLandscape) {
                        (availableWidth * 0.28f).coerceAtLeast(40.dp)
                    } else {
                        42.dp
                    }
                ),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val themeItem = themes[page]
                val isSelected = themeItem == selectedTheme
                val isCentered = pagerState.currentPage == page

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StylePreviewCard(
                        theme = themeItem,
                        mode = mode,
                        isSelected = isSelected,
                        isAnimated = isCentered,
                        isLandscape = isLandscape,
                        availableWidth = availableWidth,
                        availableHeight = availableHeight,
                        onSelect = {
                            if (isSelected && showCloseButton) {
                                // Tapping "Selected" in edit mode simply dismisses
                                onDismiss()
                            } else {
                                onThemeSelected(themeItem)
                            }
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

/**
 * Read-only segmented pill progress indicator ("Timer | Stopwatch").
 * Not clickable and not focusable, purely displays the current onboarding step.
 */
@Composable
private fun ProgressModePill(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF0C0C0C))
            .border(1.dp, BorderSubtle, CircleShape)
            .padding(3.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (currentStep == 1) {
                    "Step 1 of 2: Timer style"
                } else {
                    "Step 2 of 2: Stopwatch style"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressPillSegment(
                label = stringResource(R.string.mode_timer),
                isActive = currentStep == 1,
                modifier = Modifier.fillMaxHeight()
            )
            ProgressPillSegment(
                label = stringResource(R.string.mode_stopwatch),
                isActive = currentStep == 2,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ProgressPillSegment(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) PureWhite else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "ProgressPillBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) PureBlack else SubtleGray,
        animationSpec = tween(durationMillis = 200),
        label = "ProgressPillText"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 78.dp)
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            letterSpacing = 0.2.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Live preview card for a single theme.
 * Hugs its content tightly (preview box, style name, button) with equal padding
 * and eliminates unwanted dead space.
 */
@Composable
private fun StylePreviewCard(
    theme: TimerTheme,
    mode: AppMode,
    isSelected: Boolean,
    isAnimated: Boolean,
    isLandscape: Boolean,
    availableWidth: Dp,
    availableHeight: Dp,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStopwatchMode = mode == AppMode.STOPWATCH
    val cardShape = RoundedCornerShape(22.dp)
    val isWide = theme == TimerTheme.THREE_BOX || theme == TimerTheme.FLIP_CLOCK
    val hasMultiUnitDemo = isWide || theme == TimerTheme.CONCENTRIC_RINGS
    val totalDemoDuration = if (hasMultiUnitDemo && !isStopwatchMode) 75_420_000L else 10_000L

    var demoMillis by remember {
        mutableLongStateOf(
            if (hasMultiUnitDemo) {
                if (isStopwatchMode) 3_665_000L else 37_240_000L
            } else {
                if (isStopwatchMode) 3_500L else 7_200L
            }
        )
    }

    LaunchedEffect(isAnimated, hasMultiUnitDemo, isStopwatchMode) {
        if (!isAnimated) return@LaunchedEffect
        val startNano = System.nanoTime()
        while (true) {
            withFrameNanos { frameNano ->
                val elapsed = (frameNano - startNano) / 1_000_000L
                demoMillis = if (hasMultiUnitDemo) {
                    if (isStopwatchMode) {
                        3_665_000L + (elapsed % 10_000L)
                    } else {
                        37_240_000L - (elapsed % 10_000L)
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

    // Geometry calculations to ensure the card hugs its content
    val cardPadding = if (isLandscape) 10.dp else 14.dp
    val buttonHeight = if (isLandscape) 38.dp else 44.dp
    val itemSpacing = if (isLandscape) 8.dp else 12.dp

    // Available space for the card
    val maxCardWidth: Dp
    val previewWidth: Dp
    val previewHeight: Dp

    if (isLandscape) {
        // Landscape: bound by height
        val maxAvailableCardHeight = (availableHeight - 64.dp).coerceIn(180.dp, 330.dp)
        val nonPreviewHeight = cardPadding * 2 + itemSpacing * 2 + 24.dp + buttonHeight
        val maxPreviewHeight = (maxAvailableCardHeight - nonPreviewHeight).coerceIn(90.dp, 200.dp)

        val targetAspect = if (isWide) 3f / 2f else 1f
        previewHeight = maxPreviewHeight
        previewWidth = previewHeight * targetAspect
        maxCardWidth = previewWidth + cardPadding * 2
    } else {
        // Portrait: bound by width, capped so it never overflows screen height
        val maxAvailableCardHeight = (availableHeight - 160.dp).coerceAtLeast(240.dp)
        val nonPreviewHeight = cardPadding * 2 + itemSpacing * 2 + 26.dp + buttonHeight
        val maxAllowedPreviewHeight = (maxAvailableCardHeight - nonPreviewHeight).coerceAtLeast(140.dp)

        val targetAspect = if (isWide) 3f / 2f else 1f
        val boundedCardWidth = (availableWidth - 84.dp).coerceIn(240.dp, 360.dp)
        val candidatePreviewWidth = boundedCardWidth - cardPadding * 2
        val candidatePreviewHeight = candidatePreviewWidth / targetAspect

        if (candidatePreviewHeight > maxAllowedPreviewHeight) {
            previewHeight = maxAllowedPreviewHeight
            previewWidth = previewHeight * targetAspect
            maxCardWidth = previewWidth + cardPadding * 2
        } else {
            previewWidth = candidatePreviewWidth
            previewHeight = candidatePreviewHeight
            maxCardWidth = boundedCardWidth
        }
    }

    Box(
        modifier = modifier
            .width(maxCardWidth)
            .wrapContentHeight()
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
            .padding(cardPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Live Preview Box: 1:1 for circular styles, 3:2 for Three Box and Flip Clock
            Box(
                modifier = Modifier
                    .size(width = previewWidth, height = previewHeight)
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

            Spacer(modifier = Modifier.height(itemSpacing))

            // Style Title
            Text(
                text = theme.displayName,
                fontSize = if (isLandscape) 14.sp else 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(itemSpacing))

            // Select / Selected Button
            if (isSelected) {
                PrimaryPillButton(
                    text = stringResource(R.string.action_selected),
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .debugTestTag("btn_select_style"),
                    height = buttonHeight
                )
            } else {
                SecondaryPillButton(
                    text = stringResource(R.string.action_select),
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .debugTestTag("btn_select_style"),
                    height = buttonHeight
                )
            }
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
