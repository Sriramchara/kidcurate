package com.kidcurate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.kidcurate.app.ui.screens.*
import com.kidcurate.app.ui.theme.KidCurateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = ParentPreferences.get(this)
        if (com.kidcurate.app.BuildConfig.DEBUG) {
            if (intent.hasExtra("test_token")) {
                prefs.parentAuthToken = intent.getStringExtra("test_token") ?: ""
                prefs.parentEmail = intent.getStringExtra("test_email") ?: ""
                prefs.hasSeenOnboardingTour = true
                val testServer = intent.getStringExtra("test_server") ?: "http://127.0.0.1:8000/"
                prefs.serverUrl = testServer
                if (intent.hasExtra("test_profiles_json")) {
                    prefs.cachedProfilesJson = intent.getStringExtra("test_profiles_json") ?: ""
                }
            }
            if (intent.hasExtra("test_parent_pin")) {
                prefs.parentPin = intent.getStringExtra("test_parent_pin") ?: "1234"
                prefs.parentPinOwnerEmail = prefs.parentEmail
            }
        }
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
                    KidApp(
                        onThemeChanged = { newTheme ->
                            currentTheme = newTheme
                            parentPrefs.appTheme = newTheme
                        }
                    )
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.ensureParentSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val autoWalkthrough = activity?.intent?.getStringExtra("test_auto_walkthrough")

    if (com.kidcurate.app.BuildConfig.DEBUG && autoWalkthrough != null) {
        LaunchedEffect(autoWalkthrough) {
            when (autoWalkthrough) {
                "poco_rotation_splitscreen" -> {
                    kotlinx.coroutines.delay(1200)
                    val maya = state.availableProfiles.find { it.name.contains("Maya", ignoreCase = true) }
                        ?: state.availableProfiles.firstOrNull()
                    if (maya != null) {
                        viewModel.selectProfile(maya)
                        kotlinx.coroutines.delay(1200)
                        viewModel.verifyPin(maya.id, "4321")
                        kotlinx.coroutines.delay(1500)
                        viewModel.startPlaying(0)
                        kotlinx.coroutines.delay(4000)
                        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        kotlinx.coroutines.delay(5000)
                        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        kotlinx.coroutines.delay(4000)
                    }
                }
                "child_lock_check" -> {
                    kotlinx.coroutines.delay(1200)
                    val maya = state.availableProfiles.find { it.name.contains("Maya", ignoreCase = true) }
                        ?: state.availableProfiles.firstOrNull()
                    if (maya != null) {
                        viewModel.selectProfile(maya)
                        kotlinx.coroutines.delay(1200)
                        viewModel.verifyPin(maya.id, "4321")
                        kotlinx.coroutines.delay(1500)
                        viewModel.startPlaying(0)
                    }
                }
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
                    onStartPlaying = { startIndex ->
                        viewModel.startPlaying(startIndex)
                    },
                    onBack = {
                        viewModel.backToChildSelect()
                    },
                    onOpenParentZone = {
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
                    onVideoCompleted = { video, seconds ->
                        completedCount++
                        viewModel.reportCompleted(video, seconds)
                    },
                    onProgressUpdate = { video, seconds ->
                        viewModel.reportProgress(video, seconds)
                    },
                    onAllCompleted = {
                        val totalMinutes = (state.playlist?.totalDurationSeconds ?: 0) / 60
                        viewModel.showCompletion(
                            videosWatched = videos.size,
                            totalMinutes = totalMinutes,
                        )
                    },
                    onBack = { viewModel.goBackToPlaylist() },
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
