package com.devvardhan.value.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.devvardhan.value.core.SwipeNavigationLogic
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.LapItem
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.model.TimerTheme
import com.devvardhan.value.ui.screens.RunningTimerContent
import com.devvardhan.value.ui.screens.StopwatchContent
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.ValueTimerTheme

@Composable
fun MainPagerMidSwipeContent(
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0.5f
    ) { SwipeNavigationLogic.TOTAL_PAGES }

    val pillProgressProvider = remember(pagerState) {
        {
            SwipeNavigationLogic.calculatePillProgress(
                pagerState.currentPage,
                pagerState.currentPageOffsetFraction
            )
        }
    }

    val fullscreenAlphaProvider = remember {
        {
            SwipeNavigationLogic.calculateFullscreenButtonAlpha(
                timerHasFullscreen = true,
                stopwatchHasFullscreen = true,
                progress = 0.5f
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight * 1.15f

        Column(modifier = Modifier.fillMaxSize()) {
            MainHeader(
                currentMode = AppMode.TIMER,
                isTimerRunning = true,
                isStopwatchRunning = true,
                onOpenSettings = {},
                onFullscreenClick = {},
                onModeSelected = {},
                progressProvider = pillProgressProvider,
                fullscreenAlphaProvider = fullscreenAlphaProvider,
                isLandscape = isLandscape
            )

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (page == SwipeNavigationLogic.PAGE_TIMER) {
                        RunningTimerContent(
                            status = TimerStatus.Running,
                            targetEndTime = System.currentTimeMillis() + 185_000L,
                            pausedRemainingMillis = 185_000L,
                            totalMillis = 300_000L,
                            theme = TimerTheme.CIRCLE,
                            onPauseClick = {},
                            onResumeClick = {},
                            onResetClick = {},
                            isLandscape = isLandscape,
                            isVisible = true
                        )
                    } else {
                        StopwatchContent(
                            status = StopwatchStatus.Running,
                            startTime = System.currentTimeMillis() - 72_340L,
                            accumulatedMillis = 72_340L,
                            laps = listOf(
                                LapItem(
                                    lapNumber = 1,
                                    lapTimeMillis = 32_100L,
                                    totalTimeMillis = 32_100L
                                )
                            ),
                            theme = TimerTheme.CIRCLE,
                            onStartClick = {},
                            onPauseClick = {},
                            onResumeClick = {},
                            onLapClick = {},
                            onResetClick = {},
                            isLandscape = isLandscape,
                            isVisible = true
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Mid-Swipe Portrait (50%)",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun MainPagerMidSwipePortraitPreview() {
    ValueTimerTheme {
        Surface(color = PureBlack) {
            MainPagerMidSwipeContent()
        }
    }
}

@Preview(
    name = "Mid-Swipe Landscape (50%)",
    widthDp = 844,
    heightDp = 390,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun MainPagerMidSwipeLandscapePreview() {
    ValueTimerTheme {
        Surface(color = PureBlack) {
            MainPagerMidSwipeContent()
        }
    }
}
