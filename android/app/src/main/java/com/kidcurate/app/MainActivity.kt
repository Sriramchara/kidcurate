package com.kidcurate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.screens.*
import com.kidcurate.app.ui.theme.KidCurateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val parentPrefs = remember { ParentPreferences.get(context) }
            LaunchedEffect(Unit) {
                if (parentPrefs.serverUrl.isNotBlank()) {
                    com.kidcurate.app.data.ApiClient.updateBaseUrl(parentPrefs.serverUrl)
                }
                if (parentPrefs.parentAuthToken.isNotBlank()) {
                    com.kidcurate.app.data.ApiClient.authToken = parentPrefs.parentAuthToken
                }
            }
            var currentTheme by remember { mutableStateOf(parentPrefs.appTheme) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (currentTheme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            KidCurateTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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
                        viewModel.loadProfiles()
                        viewModel.backToChildSelect()
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
                        !state.error.isNullOrEmpty().not()
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
