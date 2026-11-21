package com.kidcurate.app

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.components.StartupSplashOverlay
import com.kidcurate.app.ui.screens.*
import com.kidcurate.app.ui.theme.KidCurateTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = ParentPreferences.get(this)
        if (prefs.serverUrl.isNotBlank()) {
            com.kidcurate.app.data.ApiClient.updateBaseUrl(prefs.serverUrl)
        }
        if (prefs.parentAuthToken.isNotBlank()) {
            com.kidcurate.app.data.ApiClient.authToken = prefs.parentAuthToken
        }
        setContent {
            val context = LocalContext.current
            val parentPrefs = remember { ParentPreferences.get(context) }
            var currentTheme by remember { mutableStateOf(parentPrefs.appTheme) }
            var showStartupSplash by rememberSaveable { mutableStateOf(true) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (currentTheme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            KidCurateTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize().displayCutoutPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        KidApp(
                            onThemeChanged = { newTheme ->
                                currentTheme = newTheme
                                parentPrefs.appTheme = newTheme
                            }
                        )
                        if (showStartupSplash) {
                            StartupSplashOverlay(
                                onFinished = { showStartupSplash = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KidApp(
    onThemeChanged: (String) -> Unit = {},
) {
    val viewModel: KidViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val activity = context as? Activity
    val activityManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.ensureParentSession()
                if (state.isChildLockActive && activityManager?.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    try {
                        activity?.startLockTask()
                    } catch (_: Exception) {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.isChildLockActive) {
        val act = activity ?: return@LaunchedEffect
        if (state.isChildLockActive) {
            try {
                act.startLockTask()
            } catch (e: Exception) {
                android.util.Log.e("KidCurate", "startLockTask failed", e)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                try {
                    act.setShowWhenLocked(true)
                    act.setTurnScreenOn(true)
                } catch (_: Exception) {}
            }
        } else {
            try {
                if (activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                    act.stopLockTask()
                }
            } catch (e: Exception) {
                android.util.Log.e("KidCurate", "stopLockTask failed", e)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                try {
                    act.setShowWhenLocked(false)
                    act.setTurnScreenOn(false)
                } catch (_: Exception) {}
            }
        }
    }

    AnimatedContent(
        targetState = state.screen,
        transitionSpec = {
            (fadeIn() + slideInHorizontally { it }) togetherWith
                    (fadeOut() + slideOutHorizontally { -it })
        },
        label = "screen_transition",
    ) { screen ->
        when (screen) {
            is AppScreen.ChildSelect -> {
                ChildSelectScreen(
                    profiles = state.availableProfiles,
                    isLoading = state.isLoading,
                    errorMessage = state.error,
                    onSelectProfile = { profile ->
                        viewModel.selectProfile(profile)
                    },
                    onOpenParentZone = {
                        viewModel.openParentZone()
                    },
                    onRetry = {
                        viewModel.loadProfiles()
                    },
                    onUpdateServerUrl = { newUrl ->
                        viewModel.updateServerUrl(newUrl)
                    },
                )
            }

            is AppScreen.ParentZone -> {
                ParentZoneScreen(
                    profiles = state.availableProfiles,
                    onThemeChanged = onThemeChanged,
                    onBackToChildMode = {
                        viewModel.ensureParentSession()
                        viewModel.loadProfiles()
                        viewModel.backToChildSelect()
                    },
                    onAccountDeleted = {
                        viewModel.resetAllAccountData()
                    },
                )
            }

            is AppScreen.PinEntry -> {
                PinEntryScreen(
                    childId = screen.childId,
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    onPinVerified = { _, _, _ -> },
                    onVerifyPin = { id, pin ->
                        viewModel.verifyPin(id, pin)
                    },
                    onClearError = { viewModel.clearError() },
                    onParentUnlock = {
                        viewModel.unlockChildDirectly(screen.childId, screen.childName, screen.childAvatar)
                    },
                    isLoading = state.isLoading,
                    errorMessage = state.error,
                    onBack = { viewModel.backToChildSelect() },
                )
            }

            is AppScreen.Playlist -> {
                TodaysPlaylistScreen(
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    playlist = state.playlist,
                    isLoading = state.isLoading,
                    isChildLockActive = state.isChildLockActive,
                    onToggleChildLock = { active -> viewModel.setChildLock(active) },
                    onStartPlaying = { startIndex ->
                        viewModel.startPlaying(startIndex)
                    },
                    onBack = {
                        viewModel.setChildLock(false)
                        viewModel.backToChildSelect()
                    },
                    onOpenParentZone = {
                        viewModel.setChildLock(false)
                        viewModel.openParentZone()
                    },
                )
            }

            is AppScreen.Player -> {
                val videos = state.playlist?.items ?: emptyList()
                var completedCount by remember { mutableStateOf(0) }

                PlayerScreen(
                    videos = videos,
                    startIndex = screen.startIndex,
                    isChildLockActive = state.isChildLockActive,
                    onToggleChildLock = { active -> viewModel.setChildLock(active) },
                    onVideoCompleted = { video, seconds ->
                        completedCount++
                        viewModel.reportCompleted(video, seconds)
                    },
                    onProgressUpdate = { video, seconds ->
                        viewModel.reportProgress(video, seconds)
                    },
                    onAllCompleted = {
                        viewModel.setChildLock(false)
                        val totalMinutes = (state.playlist?.totalDurationSeconds ?: 0) / 60
                        val actualWatched = if (completedCount > 0) completedCount else (videos.size - screen.startIndex).coerceAtLeast(1)
                        viewModel.showCompletion(
                            videosWatched = actualWatched,
                            totalMinutes = totalMinutes,
                        )
                    },
                    onBack = {
                        viewModel.setChildLock(false)
                        viewModel.goBackToPlaylist()
                    },
                )
            }

            is AppScreen.Completion -> {
                CompletionScreen(
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    videosWatched = screen.videosWatched,
                    totalMinutes = screen.totalMinutes,
                    onGoHome = { viewModel.goBackToPlaylist() },
                )
            }
        }
    }
}
