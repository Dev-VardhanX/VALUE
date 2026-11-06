package com.devvardhan.value

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devvardhan.value.core.AdaptiveLayoutHelper
import com.devvardhan.value.core.StopwatchPreferencesRepository
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.service.StopwatchService
import com.devvardhan.value.ui.screens.FinishedTimerScreen
import com.devvardhan.value.ui.screens.FullscreenStopwatchScreen
import com.devvardhan.value.ui.screens.FullscreenTimerScreen
import com.devvardhan.value.ui.screens.RunningTimerScreen
import com.devvardhan.value.ui.screens.SetTimerScreen
import com.devvardhan.value.ui.screens.StopwatchScreen
import com.devvardhan.value.ui.screens.StyleGalleryScreen
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.viewmodel.StopwatchViewModel
import com.devvardhan.value.viewmodel.TimerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN,
    GALLERY
}

class MainActivity : ComponentActivity() {

    private var initialTargetMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initialTargetMode = intent?.getStringExtra(StopwatchService.EXTRA_TARGET_MODE)

        setContent {
            ValueTimerTheme {
                ValueApp(initialMode = initialTargetMode)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val modeExtra = intent.getStringExtra(StopwatchService.EXTRA_TARGET_MODE)
        if (modeExtra != null) {
            initialTargetMode = modeExtra
        }
    }
}

@Composable
fun ValueApp(
    initialMode: String? = null,
    timerViewModel: TimerViewModel = viewModel(),
    stopwatchViewModel: StopwatchViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesRepo = remember { StopwatchPreferencesRepository(context) }

    var appMode by remember {
        mutableStateOf(
            if (initialMode == "STOPWATCH") AppMode.STOPWATCH else AppMode.TIMER
        )
    }

    // Restore persistent AppMode on launch
    LaunchedEffect(Unit) {
        if (initialMode == null) {
            try {
                appMode = preferencesRepo.appModeFlow.first()
            } catch (e: Exception) {
                appMode = AppMode.TIMER
            }
        }
    }

    val timerState by timerViewModel.uiState.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.uiState.collectAsStateWithLifecycle()
    val hasChosenStyle by timerViewModel.hasChosenStyleFlow.collectAsStateWithLifecycle(initialValue = null)

    val isTimerRunning = timerState.status == TimerStatus.Running
    val isStopwatchRunning = stopwatchState.status == StopwatchStatus.Running

    // Navigation state between Style Gallery and Main App
    var currentScreen by rememberSaveable { mutableStateOf<AppScreen?>(null) }

    // First-launch determination: auto-open gallery on very first launch unless something is running
    LaunchedEffect(hasChosenStyle, isTimerRunning, isStopwatchRunning) {
        if (currentScreen == null && hasChosenStyle != null) {
            val shouldShowGallery = AdaptiveLayoutHelper.shouldShowGalleryAtLaunch(
                hasChosenStyle = hasChosenStyle == true,
                isTimerRunning = isTimerRunning,
                isStopwatchRunning = isStopwatchRunning
            )
            currentScreen = if (shouldShowGallery) AppScreen.GALLERY else AppScreen.MAIN
        }
    }

    // Request notification permission gracefully on Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Keep screen awake while countdown timer or stopwatch is running or in fullscreen
    val keepScreenAwake = isTimerRunning || timerState.isFullscreen || isStopwatchRunning || stopwatchState.isFullscreen
    DisposableEffect(keepScreenAwake) {
        val window = (context as? ComponentActivity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack),
        color = PureBlack
    ) {
        // Mode switching handler (persists mode without touching running state)
        val onModeChange: (AppMode) -> Unit = { newMode ->
            appMode = newMode
            scope.launch {
                preferencesRepo.saveAppMode(newMode)
            }
        }

        AnimatedContent(
            targetState = currentScreen ?: AppScreen.MAIN,
            transitionSpec = {
                fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "GalleryMainTransition",
            modifier = Modifier.fillMaxSize()
        ) { activeScreen ->
            when (activeScreen) {
                AppScreen.GALLERY -> {
                    val activeTheme = if (appMode == AppMode.STOPWATCH) {
                        stopwatchState.theme
                    } else {
                        timerState.theme
                    }

                    StyleGalleryScreen(
                        mode = appMode,
                        selectedTheme = activeTheme,
                        canDismiss = hasChosenStyle == true,
                        onThemeSelected = { newTheme ->
                            if (appMode == AppMode.STOPWATCH) {
                                stopwatchViewModel.setTheme(newTheme)
                            } else {
                                timerViewModel.setTheme(newTheme)
                            }
                            timerViewModel.setHasChosenStyle(true)
                            currentScreen = AppScreen.MAIN
                        },
                        onDismiss = {
                            currentScreen = AppScreen.MAIN
                        }
                    )
                }

                AppScreen.MAIN -> {
                    if (appMode == AppMode.STOPWATCH) {
                        if (stopwatchState.isFullscreen) {
                            FullscreenStopwatchScreen(
                                theme = stopwatchState.theme,
                                status = stopwatchState.status,
                                startTime = stopwatchState.startTime,
                                accumulatedMillis = stopwatchState.accumulatedMillis,
                                lapsCount = stopwatchState.laps.size,
                                onStartClick = { stopwatchViewModel.start() },
                                onPauseClick = { stopwatchViewModel.pause() },
                                onResumeClick = { stopwatchViewModel.resume() },
                                onLapClick = { stopwatchViewModel.lap() },
                                onResetClick = { stopwatchViewModel.reset() },
                                onExitFullscreen = { stopwatchViewModel.setFullscreen(false) }
                            )
                        } else {
                            StopwatchScreen(
                                status = stopwatchState.status,
                                startTime = stopwatchState.startTime,
                                accumulatedMillis = stopwatchState.accumulatedMillis,
                                laps = stopwatchState.laps,
                                theme = stopwatchState.theme,
                                currentMode = appMode,
                                isTimerRunning = isTimerRunning,
                                onModeSelected = onModeChange,
                                onOpenGallery = { currentScreen = AppScreen.GALLERY },
                                onStartClick = { stopwatchViewModel.start() },
                                onPauseClick = { stopwatchViewModel.pause() },
                                onResumeClick = { stopwatchViewModel.resume() },
                                onLapClick = { stopwatchViewModel.lap() },
                                onResetClick = { stopwatchViewModel.reset() },
                                onFullscreenClick = { stopwatchViewModel.setFullscreen(true) }
                            )
                        }
                    } else {
                        // Timer Screen flow
                        val timerScreenKey = when {
                            timerState.isFullscreen && timerState.status != TimerStatus.Finished -> "fullscreen"
                            timerState.status == TimerStatus.Finished -> "finished"
                            timerState.status == TimerStatus.Running || timerState.status == TimerStatus.Paused -> "running"
                            else -> "set"
                        }

                        AnimatedContent(
                            targetState = timerScreenKey,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300)) + scaleIn(
                                    initialScale = 0.97f,
                                    animationSpec = tween(300)
                                )) togetherWith (fadeOut(animationSpec = tween(250)) + scaleOut(
                                    targetScale = 1.03f,
                                    animationSpec = tween(250)
                                ))
                            },
                            label = "TimerScreenTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { screen ->
                            when (screen) {
                                "fullscreen" -> {
                                    FullscreenTimerScreen(
                                        theme = timerState.theme,
                                        targetEndTime = timerState.targetEndTime,
                                        pausedRemainingMillis = timerState.pausedRemainingMillis,
                                        totalMillis = timerState.totalMillis,
                                        status = timerState.status,
                                        onExitFullscreen = { timerViewModel.setFullscreen(false) }
                                    )
                                }

                                "finished" -> {
                                    FinishedTimerScreen(
                                        onDismissClick = { timerViewModel.dismissFinished() }
                                    )
                                }

                                "running" -> {
                                    RunningTimerScreen(
                                        status = timerState.status,
                                        targetEndTime = timerState.targetEndTime,
                                        pausedRemainingMillis = timerState.pausedRemainingMillis,
                                        totalMillis = timerState.totalMillis,
                                        theme = timerState.theme,
                                        onOpenGallery = { currentScreen = AppScreen.GALLERY },
                                        onPauseClick = { timerViewModel.pauseTimer() },
                                        onResumeClick = { timerViewModel.resumeTimer() },
                                        onResetClick = { timerViewModel.resetTimer() },
                                        onFullscreenClick = { timerViewModel.setFullscreen(true) },
                                        currentMode = appMode,
                                        isStopwatchRunning = isStopwatchRunning,
                                        onModeSelected = onModeChange
                                    )
                                }

                                else -> {
                                    SetTimerScreen(
                                        hours = timerState.selectedHours,
                                        minutes = timerState.selectedMinutes,
                                        seconds = timerState.selectedSeconds,
                                        selectedTheme = timerState.theme,
                                        onHoursChange = { timerViewModel.setHours(it) },
                                        onMinutesChange = { timerViewModel.setMinutes(it) },
                                        onSecondsChange = { timerViewModel.setSeconds(it) },
                                        onOpenGallery = { currentScreen = AppScreen.GALLERY },
                                        onStartClick = { timerViewModel.startTimer() },
                                        currentMode = appMode,
                                        isStopwatchRunning = isStopwatchRunning,
                                        onModeSelected = onModeChange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
