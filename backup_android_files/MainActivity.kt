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
import com.kidcurate.app.ui.screens.*
import com.kidcurate.app.ui.theme.KidCurateTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = ParentPreferences.get(this)
        if (com.kidcurate.app.BuildConfig.DEBUG) {
            val autoWalk = intent.getStringExtra("test_auto_walkthrough")
            if (autoWalk != null) {
                val tokenExtra = intent.getStringExtra("test_token")
                if (!tokenExtra.isNullOrBlank()) {
                    prefs.parentAuthToken = tokenExtra
                }
                val emailExtra = intent.getStringExtra("test_email")
                if (!emailExtra.isNullOrBlank()) {
                    prefs.parentEmail = emailExtra
                }
                if (prefs.serverUrl.isBlank()) {
                    prefs.serverUrl = intent.getStringExtra("test_server") ?: com.kidcurate.app.BuildConfig.API_BASE_URL
                }
                prefs.hasSeenOnboardingTour = true
                val pinExtra = intent.getStringExtra("test_parent_pin")
                if (!pinExtra.isNullOrBlank()) {
                    prefs.parentPin = pinExtra
                    prefs.parentPinOwnerEmail = prefs.parentEmail
                }
            } else {
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
    val autoWalkthrough = activity?.intent?.getStringExtra("test_auto_walkthrough")

    if (com.kidcurate.app.BuildConfig.DEBUG && autoWalkthrough != null) {
        LaunchedEffect(autoWalkthrough) {
            // 1. Wait for available profiles to load
            for (i in 0 until 50) {
                if (state.availableProfiles.isNotEmpty()) break
                kotlinx.coroutines.delay(200)
            }
            if (state.availableProfiles.isEmpty()) {
                val demoProfile = com.kidcurate.app.data.ChildProfileItem(
                    id = "89b9bed9-4135-4e94-bd95-c05101ebb062",
                    name = "Maya",
                    avatar = "rainbow"
                )
                val demoItem1 = com.kidcurate.app.data.VideoItem(
                    itemId = "dd83b682-bfc1-4dfd-b113-7dd95546d4e1",
                    position = 1,
                    youtubeId = "M7lc1UVf-VE",
                    title = "YouTube Developers Live Demo",
                    channelName = "Google Developers",
                    thumbnailUrl = "https://img.youtube.com/vi/M7lc1UVf-VE/hqdefault.jpg",
                    durationSeconds = 100,
                    subjectTags = listOf("learning", "tech")
                )
                val demoItem2 = com.kidcurate.app.data.VideoItem(
                    itemId = "ecf0f4a6-1fb4-4ab9-8c2e-fb2d26597d98",
                    position = 2,
                    youtubeId = "jNQXAC9IVRw",
                    title = "Me at the zoo",
                    channelName = "jawed",
                    thumbnailUrl = "https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg",
                    durationSeconds = 19,
                    subjectTags = listOf("animals")
                )
                val demoPlaylist = com.kidcurate.app.data.PlaylistData(
                    id = "61f78a6b-d857-4776-8132-4d0512756525",
                    childId = demoProfile.id,
                    title = "Maya's Curated Stories",
                    description = null,
                    scheduledDate = "2026-11-19",
                    isActive = true,
                    totalDurationSeconds = 119,
                    videoCount = 2,
                    items = listOf(demoItem1, demoItem2)
                )
                viewModel.setDemoWalkthroughData(demoProfile, demoPlaylist)
            }
            val profile = state.availableProfiles.find { it.name.contains("Maya", ignoreCase = true) }
                ?: state.availableProfiles.firstOrNull()

            if (profile != null) {
                // Step 1: Select profile
                viewModel.selectProfile(profile)
                kotlinx.coroutines.delay(1200)

                // Step 2: Unlock PIN
                val verified = viewModel.verifyPin(profile.id, "4321")
                if (!verified) {
                    viewModel.unlockChildDirectly(profile.id, profile.name, profile.avatar)
                }

                // Step 3: Wait for playlist to load
                for (i in 0 until 50) {
                    if (state.playlist?.items?.isNotEmpty() == true) break
                    kotlinx.coroutines.delay(200)
                }
                kotlinx.coroutines.delay(1500)

                // Step 4: Open video player
                viewModel.startPlaying(0)

                if (autoWalkthrough == "poco_rotation_splitscreen") {
                    kotlinx.coroutines.delay(4000)
                    // Rotate to Landscape
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    kotlinx.coroutines.delay(6000)
                    // Rotate back to Portrait
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    kotlinx.coroutines.delay(4000)
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
