package com.kidcurate.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.ConfettiCelebration
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.theme.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.ui.window.Dialog
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay

private fun findWebView(view: android.view.View): android.webkit.WebView? {
    if (view is android.webkit.WebView) return view
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = findWebView(view.getChildAt(i))
            if (child != null) return child
        }
    }
    return null
}

private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remSeconds)
}

@Composable
fun PlayerScreen(
    videos: List<VideoItem>,
    startIndex: Int,
    onVideoCompleted: (VideoItem, Int) -> Unit,
    onProgressUpdate: (VideoItem, Int) -> Unit,
    onAllCompleted: () -> Unit,
    onBack: () -> Unit,
) {
    var currentIndex by remember { mutableStateOf(startIndex) }
    var isFullscreen by remember { mutableStateOf(false) }

    val currentVideo = videos.getOrNull(currentIndex)
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? Activity
    val parentPrefs = remember { ParentPreferences.get(context) }
    var subtitlesEnabled by remember { mutableStateOf(parentPrefs.subtitlesEnabled) }

    var isChildLockActive by remember { mutableStateOf(false) }
    var showParentPasswordDialog by remember { mutableStateOf(false) }

    var parentOverrideActive by remember { mutableStateOf(false) }
    var curfewOrLimitTriggered by remember { mutableStateOf<String?>(null) }
    var showCurfewPasswordDialog by remember { mutableStateOf(false) }
    var lastRecordedSecond by remember { mutableStateOf(0) }
    var showEndCelebration by remember { mutableStateOf(false) }
    var celebrationCountdown by remember { mutableStateOf(4) }

    LaunchedEffect(showEndCelebration) {
        if (showEndCelebration) {
            SoundHaptics.playCelebration(view, haptic)
            celebrationCountdown = 4
            while (celebrationCountdown > 0) {
                delay(1000)
                celebrationCountdown--
            }
            showEndCelebration = false
            if (currentVideo != null) {
                onVideoCompleted(currentVideo, 60)
            }
            if (currentIndex + 1 < videos.size) {
                currentIndex++
            } else {
                onAllCompleted()
            }
        }
    }

    fun attemptBack() {
        if (isFullscreen) {
            isFullscreen = false
        } else if (isChildLockActive) {
            showParentPasswordDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        attemptBack()
    }

    LaunchedEffect(isFullscreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Always reset orientation and release wake lock when leaving the screen
    DisposableEffect(Unit) {
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.window?.let {
                WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // YouTube Player Component
            if (currentVideo != null) {
                Box(
                    modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    OptimizedVideoPlayer(
                        video = currentVideo,
                        isFullscreen = isFullscreen,
                        subtitlesEnabled = subtitlesEnabled,
                        onToggleSubtitles = {
                            val newState = !subtitlesEnabled
                            subtitlesEnabled = newState
                            parentPrefs.subtitlesEnabled = newState
                        },
                        onToggleFullscreen = { isFullscreen = !isFullscreen },
                        onVideoEnded = { _ ->
                            if (currentIndex + 1 < videos.size) {
                                showEndCelebration = true
                            } else {
                                if (currentVideo != null) onVideoCompleted(currentVideo, 60)
                                onAllCompleted()
                            }
                        },
                        onProgressUpdate = { second ->
                            val delta = second - lastRecordedSecond
                            if (delta in 1..5) {
                                parentPrefs.addWatchedSeconds(delta)
                            }
                            lastRecordedSecond = second
                            if (second > 0 && second % 30 == 0) {
                                onProgressUpdate(currentVideo, second)
                            }
                        },
                        onSkipNext = if (currentIndex + 1 < videos.size) { { currentIndex++ } } else null,
                    )
                }
            }

            // Playlist info & controls (hidden in fullscreen)
            if (!isFullscreen && currentVideo != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Text(
                        text = currentVideo.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentVideo.channelName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Video ${currentIndex + 1} of ${videos.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary,
                            )

                            // Quick Subtitle pill toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (subtitlesEnabled) BrandPrimary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        val newState = !subtitlesEnabled
                                        subtitlesEnabled = newState
                                        parentPrefs.subtitlesEnabled = newState
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                                    contentDescription = "Subtitles",
                                    tint = if (subtitlesEnabled) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (subtitlesEnabled) "CC ON" else "CC OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (subtitlesEnabled) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentIndex-- },
                                    modifier = Modifier.height(36.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            if (currentIndex + 1 < videos.size) {
                                Button(
                                    onClick = { currentIndex++ },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text("Next", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        Icons.Filled.SkipNext,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        videos.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            index < currentIndex -> BrandSuccess
                                            index == currentIndex -> BrandPrimary
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        }
                                    ),
                            )
                        }
                    }

                    // Up Next Video Queue
                    if (videos.size > 1) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Up Next",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "${videos.size} Videos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            itemsIndexed(videos) { idx, v ->
                                val isCurrent = idx == currentIndex
                                Surface(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable { currentIndex = idx },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isCurrent) 2.dp else 1.dp,
                                        if (isCurrent) BrandPrimary else MaterialTheme.colorScheme.outline
                                    ),
                                    shadowElevation = if (isCurrent) 4.dp else 1.dp,
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            AsyncImage(
                                                model = v.thumbnailUrl,
                                                contentDescription = v.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                            if (isCurrent) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = BrandPrimary,
                                                    modifier = Modifier.align(Alignment.Center)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.PlayArrow,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text("PLAYING", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                    }
                                                }
                                            } else if (v.durationSeconds != null) {
                                                val min = v.durationSeconds / 60
                                                val sec = v.durationSeconds % 60
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Black.copy(alpha = 0.75f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp),
                                                ) {
                                                    Text(
                                                        text = "$min:${"%02d".format(sec)}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = v.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 14.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Next button or All Caught Up
                    if (currentIndex + 1 < videos.size) {
                        Button(
                            onClick = { currentIndex++ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play Next Video ▶️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = { onAllCompleted() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSuccess),
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("All Caught Up! 🌟", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Back button and Lock toggle (hidden in fullscreen)
    if (!isFullscreen) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { attemptBack() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }

            // Lock indicator / toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isChildLockActive) BrandPrimary.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.65f),
                modifier = Modifier.clickable {
                    if (isChildLockActive) {
                        showParentPasswordDialog = true
                    } else {
                        isChildLockActive = true
                        try { activity?.startLockTask() } catch (_: Exception) {}
                    }
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        if (isChildLockActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = "Child Lock",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isChildLockActive) "Locked" else "Lock",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }

    if (showParentPasswordDialog) {
        ParentPasswordDialog(
            title = "Parent Unlock",
            subtitle = "Enter parent account password to exit video player",
            onSuccess = {
                showParentPasswordDialog = false
                isChildLockActive = false
                try { activity?.stopLockTask() } catch (_: Exception) {}
                onBack()
            },
            onDismiss = { showParentPasswordDialog = false },
        )
    }

    if (curfewOrLimitTriggered != null) {
        Surface(
            color = Color(0xFF111827).copy(alpha = 0.96f),
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (curfewOrLimitTriggered == "curfew") Icons.Filled.Bedtime else Icons.Filled.HourglassBottom,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (curfewOrLimitTriggered == "curfew") "Bedtime Curfew Active" else "Daily Screen Time Reached",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (curfewOrLimitTriggered == "curfew")
                        "KidCurate rests between ${parentPrefs.curfewStartHour}:00 PM and ${parentPrefs.curfewEndHour}:00 AM for healthy sleep."
                    else
                        "You have reached your daily screen time limit of ${parentPrefs.dailyScreenTimeMinutes} minutes for today.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showCurfewPasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Parent Unlock", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    ) {
                        Text("Exit Player", color = Color.White)
                    }
                }
            }
        }
    }

    if (showCurfewPasswordDialog) {
        ParentPasswordDialog(
            title = "Parent Unlock",
            subtitle = "Enter parent account password to bypass screen time limit or curfew",
            onSuccess = {
                showCurfewPasswordDialog = false
                parentOverrideActive = true
                curfewOrLimitTriggered = null
            },
            onDismiss = { showCurfewPasswordDialog = false },
        )
    }

    if (showEndCelebration && currentVideo != null) {
        val nextVideo = if (currentIndex + 1 < videos.size) videos[currentIndex + 1] else null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Up Next",
                            tint = BrandPrimary,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Text(
                        text = if (nextVideo != null) "Up Next" else "Playlist Finished",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (nextVideo != null) {
                        Text(
                            text = nextVideo.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Text(
                            text = "Playing in ${celebrationCountdown}s...",
                            fontSize = 12.sp,
                            color = BrandPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Button(
                        onClick = {
                            showEndCelebration = false
                            if (currentVideo != null) onVideoCompleted(currentVideo, 60)
                            if (currentIndex + 1 < videos.size) {
                                currentIndex++
                            } else {
                                onAllCompleted()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(if (nextVideo != null) "Play Now" else "Finish", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizedVideoPlayer(
    video: VideoItem,
    isFullscreen: Boolean,
    subtitlesEnabled: Boolean,
    onToggleSubtitles: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onVideoEnded: (Int) -> Unit,
    onProgressUpdate: (Int) -> Unit,
    onSkipNext: (() -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var currentSecond by remember { mutableStateOf(0f) }
    var videoDuration by remember { mutableStateOf(0f) }
    var playerState by remember { mutableStateOf(PlayerConstants.PlayerState.UNKNOWN) }
    var showControls by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var currentQuality by remember { mutableStateOf(parentPrefs.videoQuality) }

    fun applyVideoQuality(qualityKey: String, qualityLabel: String) {
        currentQuality = qualityKey
        parentPrefs.videoQuality = qualityKey
        val script = """
        (function() {
            try {
                if (typeof player !== 'undefined' && player) {
                    var q = '$qualityKey';
                    if (q === 'highres') {
                        if (player.setPlaybackQualityRange) player.setPlaybackQualityRange('highres', 'highres');
                        if (player.setPlaybackQuality) {
                            player.setPlaybackQuality('hd2160');
                            player.setPlaybackQuality('highres');
                        }
                    } else {
                        if (player.setPlaybackQuality) player.setPlaybackQuality(q);
                        if (player.setPlaybackQualityRange) player.setPlaybackQualityRange(q, q);
                    }
                }
            } catch(e) {}
        })();
        """.trimIndent()
        webViewRef?.post {
            webViewRef?.evaluateJavascript(script, null)
        }
        toastMessage = "Quality: $qualityLabel"
        showQualityDialog = false
    }

    fun applySubtitles(enabled: Boolean) {
        val script = if (enabled) {
            """
            (function() {
                try {
                    if (typeof player !== 'undefined' && player) {
                        player.loadModule('captions');
                        player.setOption('captions', 'track', {'languageCode': 'en'});
                        player.setOption('captions', 'reload', true);
                    }
                } catch(e) {}
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                try {
                    if (typeof player !== 'undefined' && player) {
                        player.unloadModule('captions');
                        player.setOption('captions', 'track', {});
                    }
                } catch(e) {}
            })();
            """.trimIndent()
        }
        webViewRef?.post {
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    // Auto-hide controls after a few seconds of playback
    LaunchedEffect(showControls, playerState) {
        if (showControls && playerState == PlayerConstants.PlayerState.PLAYING) {
            delay(3500)
            showControls = false
        }
    }

    // Auto-hide toast feedback message
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            toastMessage = null
        }
    }

    var isStuckUnplayable by remember { mutableStateOf(false) }

    // React dynamically to subtitlesEnabled changes
    LaunchedEffect(subtitlesEnabled, webViewRef) {
        applySubtitles(subtitlesEnabled)
    }

    LaunchedEffect(video.youtubeId) {
        playbackError = null
        isStuckUnplayable = false
        currentSecond = 0f
        videoDuration = 0f
        youTubePlayerRef?.loadVideo(video.youtubeId, 0f)

        // Watchdog: If after 3.5s the video has not started playing and has 0 duration, surface a helpful skip
        delay(3500)
        if (playerState != PlayerConstants.PlayerState.PLAYING && currentSecond == 0f && videoDuration == 0f) {
            isStuckUnplayable = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val container = FrameLayout(ctx)

                val playerView = YouTubePlayerView(ctx).apply {
                    enableAutomaticInitialization = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    lifecycleOwner.lifecycle.addObserver(this)

                    val options = IFramePlayerOptions.Builder(ctx)
                        .controls(0)
                        .rel(0)
                        .ivLoadPolicy(3) // hide annotations
                        .ccLoadPolicy(if (subtitlesEnabled) 1 else 0)
                        .build()

                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayerRef = youTubePlayer
                            playbackError = null
                            isStuckUnplayable = false
                            youTubePlayer.loadVideo(video.youtubeId, 0f)
                            val wv = findWebView(this@apply)
                            wv?.settings?.apply {
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                            }
                            try {
                                android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)
                            } catch (_: Exception) {}
                            webViewRef = wv
                            applySubtitles(subtitlesEnabled)
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentSecond = second
                            if (second > 0f) isStuckUnplayable = false
                            onProgressUpdate(second.toInt())
                        }

                        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                            videoDuration = duration
                            if (duration > 0f) isStuckUnplayable = false
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState,
                        ) {
                            playerState = state
                            if (state == PlayerConstants.PlayerState.PLAYING) {
                                isStuckUnplayable = false
                                applySubtitles(subtitlesEnabled)
                            }
                            if (state == PlayerConstants.PlayerState.ENDED) {
                                onVideoEnded(currentSecond.toInt())
                            }
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: PlayerConstants.PlayerError,
                        ) {
                            playbackError = when (error) {
                                PlayerConstants.PlayerError.VIDEO_NOT_FOUND ->
                                    "This video was not found on YouTube."
                                PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ->
                                    "The video creator has restricted playback in third-party mobile apps."
                                else -> "Video playback is currently unavailable."
                            }
                            isStuckUnplayable = true
                        }
                    }, options)
                }

                // Transparent View on top to intercept ALL touches and block WebView clicks
                val touchInterceptor = android.view.View(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        showControls = !showControls
                    }
                }

                container.addView(playerView)
                container.addView(touchInterceptor)

                val wv = findWebView(playerView)
                wv?.settings?.apply {
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                }
                try {
                    android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)
                } catch (_: Exception) {}
                webViewRef = wv
                container
            },
            update = { _ -> },
            modifier = Modifier.fillMaxSize(),
        )

        // Playback Error or Stuck Video Overlay
        if (playbackError != null || isStuckUnplayable) {
            Surface(
                color = Color.Black.copy(alpha = 0.92f),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Playback Notice",
                        tint = BrandAccent,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (playbackError != null) "Video Playback Notice" else "Playback Restricted",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = playbackError
                            ?: "The creator restricted playback of this video in third-party mobile apps.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onSkipNext != null) {
                            Button(
                                onClick = onSkipNext,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Next Video ▶️", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (isStuckUnplayable && playbackError == null) {
                            OutlinedButton(
                                onClick = {
                                    isStuckUnplayable = false
                                    youTubePlayerRef?.loadVideo(video.youtubeId, 0f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            ) {
                                Text("Replay 🔄", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Subtitles Feedback Toast Overlay (e.g. Subtitles ON / OFF)
        if (toastMessage != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.82f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (isFullscreen) 24.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                        contentDescription = null,
                        tint = if (subtitlesEnabled) BrandAccent else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Custom Overlay Controls
        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Center Play/Pause button
                IconButton(
                    onClick = {
                        youTubePlayerRef?.let { player ->
                            if (playerState == PlayerConstants.PlayerState.PLAYING) {
                                player.pause()
                            } else {
                                player.play()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        imageVector = if (playerState == PlayerConstants.PlayerState.PLAYING) 
                            Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Bottom bar: Progress, timestamps, Subtitle CC button, and Fullscreen toggle
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simple progress bar
                    LinearProgressIndicator(
                        progress = { if (videoDuration > 0) currentSecond / videoDuration else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = BrandPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))

                    // Time display e.g. 01:23 / 05:10
                    Text(
                        text = "${formatTime(currentSecond)} / ${formatTime(videoDuration)}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Subtitle / Closed Caption (CC) toggle button
                    IconButton(
                        onClick = {
                            onToggleSubtitles()
                            toastMessage = if (!subtitlesEnabled) "Subtitles ON (English)" else "Subtitles OFF"
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (subtitlesEnabled) BrandPrimary.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                            contentDescription = if (subtitlesEnabled) "Disable Subtitles" else "Enable Subtitles",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Video Resolution / Quality button
                    IconButton(
                        onClick = { showQualityDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentQuality != "auto" && currentQuality != "default") BrandPrimary.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.2f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HighQuality,
                            contentDescription = "Video Quality / Resolution",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Fullscreen toggle button
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Video Resolution / Quality Dialog
        if (showQualityDialog) {
            Dialog(onDismissRequest = { showQualityDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.HighQuality, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Video Resolution / Quality",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Choose preferred playback resolution",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))

                        val qualityOptions = listOf(
                            "default" to "Auto (Recommended)",
                            "highres" to "4K Ultra HD / 8K (Max Quality)",
                            "hd1440" to "2K Quad HD (1440p)",
                            "hd1080" to "1080p (Full HD)",
                            "hd720" to "720p (HD)",
                            "large" to "480p (Standard)",
                            "medium" to "360p (Data Saver)",
                        )

                        qualityOptions.forEach { (key, label) ->
                            val isSelected = currentQuality == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrandPrimary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { applyVideoQuality(key, label) }
                                    .padding(vertical = 4.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showQualityDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

