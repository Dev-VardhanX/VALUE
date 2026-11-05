package com.android.value

import android.Manifest
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.value.model.TimerStatus
import com.android.value.ui.screens.FinishedTimerScreen
import com.android.value.ui.screens.FullscreenTimerScreen
import com.android.value.ui.screens.RunningTimerScreen
import com.android.value.ui.screens.SetTimerScreen
import com.android.value.ui.theme.PureBlack
import com.android.value.ui.theme.ValueTimerTheme
import com.android.value.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ValueTimerTheme {
                TimerApp()
            }
        }
    }
}

@Composable
fun TimerApp(
    viewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+ (API 33+)
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

    // Keep screen awake while in fullscreen or while countdown timer is running
    val keepScreenAwake = (uiState.status == TimerStatus.Running) || uiState.isFullscreen
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
        // Screen mode evaluation
        val currentScreenKey = when {
            uiState.isFullscreen && uiState.status != TimerStatus.Finished -> "fullscreen"
            uiState.status == TimerStatus.Finished -> "finished"
            uiState.status == TimerStatus.Running || uiState.status == TimerStatus.Paused -> "running"
            else -> "set"
        }

        AnimatedContent(
            targetState = currentScreenKey,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = tween(300)
                )) togetherWith (fadeOut(animationSpec = tween(250)) + scaleOut(
                    targetScale = 1.03f,
                    animationSpec = tween(250)
                ))
            },
            label = "ScreenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                "fullscreen" -> {
                    FullscreenTimerScreen(
                        remainingMillis = uiState.remainingMillis,
                        onExitFullscreen = { viewModel.setFullscreen(false) }
                    )
                }

                "finished" -> {
                    FinishedTimerScreen(
                        onDismissClick = { viewModel.dismissFinished() }
                    )
                }

                "running" -> {
                    RunningTimerScreen(
                        status = uiState.status,
                        remainingMillis = uiState.remainingMillis,
                        totalMillis = uiState.totalMillis,
                        onPauseClick = { viewModel.pauseTimer() },
                        onResumeClick = { viewModel.resumeTimer() },
                        onResetClick = { viewModel.resetTimer() },
                        onFullscreenClick = { viewModel.setFullscreen(true) }
                    )
                }

                else -> {
                    SetTimerScreen(
                        hours = uiState.selectedHours,
                        minutes = uiState.selectedMinutes,
                        seconds = uiState.selectedSeconds,
                        onHoursChange = { viewModel.setHours(it) },
                        onMinutesChange = { viewModel.setMinutes(it) },
                        onSecondsChange = { viewModel.setSeconds(it) },
                        onStartClick = { viewModel.startTimer() }
                    )
                }
            }
        }
    }
}
