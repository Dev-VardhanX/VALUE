package com.devvardhan.value

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devvardhan.value.core.BackAction
import com.devvardhan.value.core.BackPressHandlerLogic
import com.devvardhan.value.core.StartupDestination
import com.devvardhan.value.core.StartupResolver
import com.devvardhan.value.core.StartupState
import com.devvardhan.value.core.StopwatchPreferencesRepository
import com.devvardhan.value.core.TimerPreferencesRepository
import com.devvardhan.value.core.loadStartupState
import com.devvardhan.value.model.AlarmSound
import com.devvardhan.value.model.AppMode
import com.devvardhan.value.model.StopwatchStatus
import com.devvardhan.value.model.TimerStatus
import com.devvardhan.value.service.StopwatchService
import com.devvardhan.value.ui.screens.FinishedTimerScreen
import com.devvardhan.value.ui.screens.FullscreenStopwatchScreen
import com.devvardhan.value.ui.screens.FullscreenTimerScreen
import com.devvardhan.value.ui.screens.RunningTimerScreen
import com.devvardhan.value.ui.screens.SetTimerScreen
import com.devvardhan.value.ui.screens.SettingsScreen
import com.devvardhan.value.ui.screens.SoundScreen
import com.devvardhan.value.ui.screens.StopwatchScreen
import com.devvardhan.value.ui.screens.StyleGalleryScreen
import com.devvardhan.value.ui.theme.BorderSubtle
import com.devvardhan.value.ui.theme.Motion
import com.devvardhan.value.ui.theme.PureBlack
import com.devvardhan.value.ui.theme.PureWhite
import com.devvardhan.value.ui.theme.ValueTimerTheme
import com.devvardhan.value.ui.util.rootTestTagsAsResourceId
import com.devvardhan.value.viewmodel.StopwatchViewModel
import com.devvardhan.value.viewmodel.TimerViewModel
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN,
    GALLERY,
    SETTINGS,
    SOUND
}

private fun getScreenLevel(screen: AppScreen): Int = when (screen) {
    AppScreen.MAIN -> 0
    AppScreen.SETTINGS -> 1
    AppScreen.GALLERY, AppScreen.SOUND -> 2
}

class MainActivity : ComponentActivity() {

    private var initialTargetMode: String? = null
    private var startupState by mutableStateOf<StartupState>(StartupState.Loading)
    private var highRefreshModeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
        }
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        JankMonitor.setup(this)
        highRefreshModeId = findOptimalHighRefreshMode(this)

        initialTargetMode = intent?.getStringExtra(StopwatchService.EXTRA_TARGET_MODE)

        splashScreen.setKeepOnScreenCondition {
            startupState !is StartupState.Ready
        }

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val view = splashScreenViewProvider.view
            val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 200L
                doOnEnd { splashScreenViewProvider.remove() }
            }
            fadeOut.start()
        }

        lifecycleScope.launch {
            val timerRepo = TimerPreferencesRepository(applicationContext)
            val swRepo = StopwatchPreferencesRepository(applicationContext)
            startupState = loadStartupState(
                timerPrefs = timerRepo,
                stopwatchPrefs = swRepo,
                intentTargetMode = initialTargetMode
            )
        }

        setContent {
            ValueTimerTheme {
                ValueApp(
                    startupState = startupState,
                    initialMode = initialTargetMode
                )
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

    private var isHighRefreshPreferred: Boolean = false

    fun setHighRefreshRateEnabled(enabled: Boolean) {
        isHighRefreshPreferred = enabled
        applyPreferredDisplayMode(enabled)
    }

    override fun onResume() {
        super.onResume()
        if (isHighRefreshPreferred) {
            applyPreferredDisplayMode(true)
        }
    }

    override fun onPause() {
        super.onPause()
        // Battery guard: always reset preferredDisplayModeId to 0 when paused or backgrounded
        applyPreferredDisplayMode(false)
    }

    private fun applyPreferredDisplayMode(enabled: Boolean) {
        val win = window ?: return
        val params = win.attributes
        val targetModeId = if (enabled) highRefreshModeId else 0
        var changed = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (params.preferredDisplayModeId != targetModeId) {
                params.preferredDisplayModeId = targetModeId
                changed = true
            }
        }
        val targetRate = if (enabled && highRefreshModeId == 0) 120f else 0f
        if (params.preferredRefreshRate != targetRate) {
            params.preferredRefreshRate = targetRate
            changed = true
        }
        if (changed) {
            win.attributes = params
            if (BuildConfig.DEBUG) {
                val currentRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display?.mode?.refreshRate ?: 0f
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay?.mode?.refreshRate ?: 0f
                }
                Log.d("HighRefreshRate", "Preferred mode set: id=$targetModeId (enabled=$enabled), current display rate=$currentRate Hz")
            }
        }
    }
}

private fun findOptimalHighRefreshMode(activity: Activity): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay
            }
            if (display != null) {
                val currentMode = display.mode
                val matchingModes = display.supportedModes.filter {
                    it.physicalWidth == currentMode.physicalWidth && it.physicalHeight == currentMode.physicalHeight
                }
                val candidates = if (matchingModes.isNotEmpty()) matchingModes else display.supportedModes.toList()
                val best = candidates.maxByOrNull { it.refreshRate }
                if (best != null && best.refreshRate > 60.5f) {
                    return best.modeId
                }
            }
        } catch (_: Exception) {}
    }
    return 0
}

@Composable
fun ValueApp(
    startupState: StartupState = StartupResolver.defaultFallback,
    initialMode: String? = null,
    timerViewModel: TimerViewModel = viewModel(),
    stopwatchViewModel: StopwatchViewModel = viewModel()
) {
    // Pure black during splash hold - NO Timer UI composed or flashed
    if (startupState !is StartupState.Ready) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .rootTestTagsAsResourceId(),
            color = PureBlack
        ) {}
        return
    }

    val readyState = startupState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesRepo = remember { StopwatchPreferencesRepository(context) }
    val isReducedMotion = remember(context) { Motion.isAnimationsDisabled(context) }

    val initialScreen = remember(readyState) {
        when (readyState.destination) {
            is StartupDestination.FirstLaunch -> AppScreen.GALLERY
            is StartupDestination.Main -> AppScreen.MAIN
        }
    }
    val initialStep = remember(readyState) {
        when (val d = readyState.destination) {
            is StartupDestination.FirstLaunch -> d.step
            is StartupDestination.Main -> 1
        }
    }

    var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }
    var firstLaunchStep by rememberSaveable { mutableIntStateOf(initialStep) }
    var appMode by rememberSaveable { mutableStateOf(readyState.initialAppMode) }

    val timerState by timerViewModel.uiState.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.uiState.collectAsStateWithLifecycle()
    val hasChosenStyle by timerViewModel.hasChosenStyleFlow.collectAsStateWithLifecycle(
        initialValue = readyState.destination !is StartupDestination.FirstLaunch
    )
    val onboardingStep by timerViewModel.onboardingStepFlow.collectAsStateWithLifecycle(initialValue = initialStep)

    val vibrationEnabled by timerViewModel.vibrationEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val gradualVolumeEnabled by timerViewModel.gradualVolumeEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val selectedSoundId by timerViewModel.selectedSoundIdFlow.collectAsStateWithLifecycle(initialValue = AlarmSound.ID_PHONE_DEFAULT)
    val selectedSoundName by timerViewModel.selectedSoundNameFlow.collectAsStateWithLifecycle(initialValue = "Phone default")
    val importedSounds by timerViewModel.importedSoundsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val isTimerRunning = timerState.status == TimerStatus.Running
    val isStopwatchRunning = stopwatchState.status == StopwatchStatus.Running

    // Navigation state across sub-pages
    var galleryTargetMode by rememberSaveable { mutableStateOf(AppMode.TIMER) }
    var settingsSourceMode by rememberSaveable { mutableStateOf(AppMode.TIMER) }

    // Synchronize step with persistent onboarding progress
    LaunchedEffect(onboardingStep) {
        firstLaunchStep = onboardingStep
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var showExitToast by remember { mutableStateOf(false) }

    LaunchedEffect(showExitToast, lastBackPressTime) {
        if (showExitToast) {
            kotlinx.coroutines.delay(2000L)
            showExitToast = false
        }
    }

    val isFullscreen = (appMode == AppMode.STOPWATCH && stopwatchState.isFullscreen) ||
        (appMode == AppMode.TIMER && timerState.isFullscreen && timerState.status != TimerStatus.Finished)
    val isFirstLaunchGallery = hasChosenStyle != true && currentScreen == AppScreen.GALLERY
    val isSubpageOpen = (currentScreen == AppScreen.GALLERY && hasChosenStyle == true) || currentScreen == AppScreen.SOUND
    val isSettingsOpen = currentScreen == AppScreen.SETTINGS
    val isGalleryOpen = currentScreen == AppScreen.GALLERY
    val isTimerRunningOrPaused = timerState.status == TimerStatus.Running || timerState.status == TimerStatus.Paused
    val isStopwatchRunningOrPaused = stopwatchState.status == StopwatchStatus.Running || stopwatchState.status == StopwatchStatus.Paused

    BackHandler {
        val (action, newBackTime) = BackPressHandlerLogic.decideBackAction(
            isFullscreen = isFullscreen,
            isFirstLaunchGallery = isFirstLaunchGallery,
            galleryStep = firstLaunchStep,
            isSubpageOpen = isSubpageOpen,
            isSettingsOpen = isSettingsOpen,
            isGalleryOpen = isGalleryOpen,
            isTimerRunningOrPaused = isTimerRunningOrPaused,
            isStopwatchRunningOrPaused = isStopwatchRunningOrPaused,
            lastBackPressTimeMs = lastBackPressTime,
            currentTimeMs = System.currentTimeMillis()
        )
        lastBackPressTime = newBackTime

        when (action) {
            BackAction.EXIT_FULLSCREEN -> {
                if (appMode == AppMode.STOPWATCH) {
                    stopwatchViewModel.setFullscreen(false)
                } else {
                    timerViewModel.setFullscreen(false)
                }
            }
            BackAction.GALLERY_STEP_BACK -> {
                firstLaunchStep = 1
                timerViewModel.setOnboardingStep(1)
            }
            BackAction.DISMISS_SUBPAGE_TO_SETTINGS -> {
                currentScreen = AppScreen.SETTINGS
            }
            BackAction.DISMISS_SETTINGS_TO_MAIN -> {
                appMode = settingsSourceMode
                currentScreen = AppScreen.MAIN
            }
            BackAction.DISMISS_GALLERY -> {
                currentScreen = AppScreen.MAIN
            }
            BackAction.FINISH_ACTIVITY -> {
                context.findActivity()?.finish()
            }
            BackAction.MOVE_TO_BACK -> {
                context.findActivity()?.moveTaskToBack(true)
            }
            BackAction.SHOW_TOAST_AGAIN_TO_EXIT -> {
                showExitToast = true
            }
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

    // Dynamic high refresh rate preference (120Hz/144Hz during animation, battery saving when idle)
    var isWheelScrolling by remember { mutableStateOf(false) }
    val shouldPreferHighRefresh = isTimerRunning ||
        isStopwatchRunning ||
        (timerState.status == TimerStatus.Finished) ||
        currentScreen != AppScreen.MAIN ||
        isWheelScrolling

    val activity = remember(context) { context.findActivity() as? MainActivity }
    LaunchedEffect(shouldPreferHighRefresh) {
        activity?.setHighRefreshRateEnabled(shouldPreferHighRefresh)
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.setHighRefreshRateEnabled(false)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .rootTestTagsAsResourceId(),
        color = PureBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val onModeChange: (AppMode) -> Unit = { newMode ->
                appMode = newMode
                scope.launch {
                    preferencesRepo.saveAppMode(newMode)
                }
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val forward = getScreenLevel(targetState) >= getScreenLevel(initialState)
                    Motion.screenTransition(forward = forward, isZeroDuration = isReducedMotion)
                },
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { activeScreen ->
                when (activeScreen) {
                    AppScreen.GALLERY -> {
                        StyleGalleryScreen(
                            isFirstLaunch = hasChosenStyle != true,
                            firstLaunchStep = firstLaunchStep,
                            editMode = galleryTargetMode,
                            timerTheme = timerState.theme,
                            stopwatchTheme = stopwatchState.theme,
                            onTimerThemeSelected = { newTheme ->
                                timerViewModel.setTheme(newTheme)
                                timerViewModel.setOnboardingStep(2)
                                firstLaunchStep = 2
                            },
                            onStopwatchThemeSelected = { newTheme ->
                                stopwatchViewModel.setTheme(newTheme)
                                timerViewModel.setHasChosenStyle(true)
                                appMode = AppMode.TIMER
                                scope.launch {
                                    preferencesRepo.saveAppMode(AppMode.TIMER)
                                }
                                currentScreen = AppScreen.MAIN
                            },
                            onThemeSelectedInEditMode = { newTheme ->
                                if (galleryTargetMode == AppMode.STOPWATCH) {
                                    stopwatchViewModel.setTheme(newTheme)
                                } else {
                                    timerViewModel.setTheme(newTheme)
                                }
                                currentScreen = AppScreen.SETTINGS
                            },
                            onDismiss = {
                                currentScreen = if (hasChosenStyle == true) AppScreen.SETTINGS else AppScreen.MAIN
                            }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            timerStyleName = timerState.theme.displayName,
                            stopwatchStyleName = stopwatchState.theme.displayName,
                            alarmSoundName = selectedSoundName,
                            vibrationEnabled = vibrationEnabled,
                            gradualVolumeEnabled = gradualVolumeEnabled,
                            onOpenTimerStyle = {
                                galleryTargetMode = AppMode.TIMER
                                currentScreen = AppScreen.GALLERY
                            },
                            onOpenStopwatchStyle = {
                                galleryTargetMode = AppMode.STOPWATCH
                                currentScreen = AppScreen.GALLERY
                            },
                            onOpenSoundSettings = {
                                currentScreen = AppScreen.SOUND
                            },
                            onVibrationToggle = { timerViewModel.setVibrationEnabled(it) },
                            onGradualVolumeToggle = { timerViewModel.setGradualVolumeEnabled(it) },
                            onBack = {
                                appMode = settingsSourceMode
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }

                    AppScreen.SOUND -> {
                        SoundScreen(
                            selectedSoundId = selectedSoundId,
                            importedSounds = importedSounds,
                            onSelectSound = { sound ->
                                timerViewModel.selectSound(sound)
                            },
                            onAddUserSound = { userSound ->
                                timerViewModel.addImportedSound(userSound)
                            },
                            onBack = {
                                currentScreen = AppScreen.SETTINGS
                            }
                        )
                    }

                    AppScreen.MAIN -> {
                        AnimatedContent(
                            targetState = appMode,
                            transitionSpec = {
                                val forward = targetState == AppMode.STOPWATCH
                                Motion.modeSwitchTransition(forward = forward, isZeroDuration = isReducedMotion)
                            },
                            label = "AppModeTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { currentAppMode ->
                            if (currentAppMode == AppMode.STOPWATCH) {
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
                                        onExitFullscreen = { stopwatchViewModel.setFullscreen(false) },
                                        lastLapTotalMillis = stopwatchState.laps.firstOrNull()?.totalTimeMillis ?: 0L
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
                                        onOpenSettings = {
                                            settingsSourceMode = AppMode.STOPWATCH
                                            currentScreen = AppScreen.SETTINGS
                                        },
                                        onStartClick = { stopwatchViewModel.start() },
                                        onPauseClick = { stopwatchViewModel.pause() },
                                        onResumeClick = { stopwatchViewModel.resume() },
                                        onLapClick = { stopwatchViewModel.lap() },
                                        onResetClick = { stopwatchViewModel.reset() },
                                        onFullscreenClick = { stopwatchViewModel.setFullscreen(true) }
                                    )
                                }
                            } else {
                                val timerScreenKey = when {
                                    timerState.isFullscreen && timerState.status != TimerStatus.Finished -> "fullscreen"
                                    timerState.status == TimerStatus.Finished -> "finished"
                                    timerState.status == TimerStatus.Running || timerState.status == TimerStatus.Paused -> "running"
                                    else -> "set"
                                }

                                AnimatedContent(
                                    targetState = timerScreenKey,
                                    transitionSpec = {
                                        if (targetState == "fullscreen" || initialState == "fullscreen") {
                                            Motion.fullscreenTransition(isZeroDuration = isReducedMotion)
                                        } else {
                                            Motion.timerStateTransition(isZeroDuration = isReducedMotion)
                                        }
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
                                                onOpenSettings = {
                                                    settingsSourceMode = AppMode.TIMER
                                                    currentScreen = AppScreen.SETTINGS
                                                },
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
                                                onOpenSettings = {
                                                    settingsSourceMode = AppMode.TIMER
                                                    currentScreen = AppScreen.SETTINGS
                                                },
                                                onStartClick = { timerViewModel.startTimer() },
                                                currentMode = appMode,
                                                isStopwatchRunning = isStopwatchRunning,
                                                onModeSelected = onModeChange,
                                                onWheelScrollStateChange = { isWheelScrolling = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Minimal floating monochrome pill for double-back exit confirmation
            AnimatedVisibility(
                visible = showExitToast,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(20.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.press_back_again_to_exit),
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
