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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.ConfettiCelebration
import com.kidcurate.app.ui.components.GlassCard
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.theme.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.ui.text.style.TextAlign
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
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, remSeconds)
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
    var currentIndex by remember { mutableIntStateOf(startIndex) }
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
    var isExitingPlayer by remember { mutableStateOf(false) }
    var curfewBypassed by remember { mutableStateOf(false) }
    var dailyLimitBypassed by remember { mutableStateOf(false) }
    var activeLimitType by remember { mutableStateOf<String?>(null) } // "curfew" or "limit"
    var pendingUnlockAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var lastRecordedSecond by remember(currentIndex) { mutableIntStateOf(0) }
    var showEndCelebration by remember { mutableStateOf(false) }
    var celebrationCountdown by remember { mutableIntStateOf(4) }

    var isCurrentVideoCompleted by remember(currentIndex) { mutableStateOf(false) }
    var currentVideoSeconds by remember(currentIndex) { mutableFloatStateOf(0f) }
    var currentVideoTotalDuration by remember(currentIndex) { mutableFloatStateOf(0f) }
    var currentVideoHasError by remember(currentIndex) { mutableStateOf(false) }

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

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isWindowLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    // Tablets in landscape get two-pane layout, NOT forced fullscreen
    val effectiveFullscreen = isFullscreen || (isWindowLandscape && !isTablet)

    fun attemptBack() {
        if (isFullscreen) {
            isFullscreen = false
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        } else if (isChildLockActive) {
            isExitingPlayer = true
            showParentPasswordDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        attemptBack()
    }

    LaunchedEffect(effectiveFullscreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (effectiveFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }

    // Reset orientation to user preference, release lock task, and release wake lock when leaving the screen
    DisposableEffect(Unit) {
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            try { activity?.stopLockTask() } catch (_: Exception) {}
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            activity?.window?.let {
                WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Retain video player instance across orientation & layout transitions with zero teardown
    val playerComposable = remember(currentVideo?.youtubeId) {
        movableContentOf { isFs: Boolean ->
            if (currentVideo != null) {
                OptimizedVideoPlayer(
                    video = currentVideo,
                    isFullscreen = isFs,
                    subtitlesEnabled = subtitlesEnabled,
                    onToggleSubtitles = {
                        val newState = !subtitlesEnabled
                        subtitlesEnabled = newState
                        parentPrefs.subtitlesEnabled = newState
                    },
                    onToggleFullscreen = { isFullscreen = !isFs },
                    onVideoEnded = { _ ->
                        isCurrentVideoCompleted = true
                        if (currentIndex + 1 < videos.size) {
                            showEndCelebration = true
                        } else {
                            onVideoCompleted(currentVideo, currentVideoSeconds.toInt())
                            onAllCompleted()
                        }
                    },
                    onProgressUpdate = { sec, dur ->
                        val secInt = sec.toInt()
                        val delta = secInt - lastRecordedSecond
                        if (delta in 1..5) {
                            parentPrefs.addWatchedSeconds(delta)
                        }
                        lastRecordedSecond = secInt
                        currentVideoSeconds = sec
                        currentVideoTotalDuration = dur
                        if (dur > 5f && sec >= (dur - 4f).coerceAtMost(dur * 0.85f)) {
                            isCurrentVideoCompleted = true
                        }
                        if (secInt > 0 && secInt % 30 == 0) {
                            onProgressUpdate(currentVideo, secInt)
                        }
                        if (!curfewBypassed && parentPrefs.isCurfewActive()) {
                            activeLimitType = "curfew"
                        } else if (!dailyLimitBypassed && parentPrefs.isDailyLimitReached()) {
                            activeLimitType = "limit"
                        }
                    },
                    onSkipNext = if (currentIndex + 1 < videos.size && (isCurrentVideoCompleted || currentVideoHasError)) { { currentIndex++ } } else null,
                    onFinishPlaylist = if (currentIndex + 1 >= videos.size) { { onBack() } } else null,
                    onPlaybackError = { hasErr -> currentVideoHasError = hasErr },
                    onAttemptBack = { attemptBack() },
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Tablet landscape: two-pane Row (video left 65% | queue right 35%)
        if (isTablet && isWindowLandscape && !isFullscreen && currentVideo != null) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left pane: video player (retained via movableContentOf)
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    playerComposable(false)
                }
                // Right pane: title, controls, queue
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(12.dp),
                ) {
                    Text(
                        text = currentVideo.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentVideo.channelName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Video ${currentIndex + 1} of ${videos.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentIndex-- },
                                    modifier = Modifier.height(34.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                ) { Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(16.dp)) }
                            }
                            if (currentIndex + 1 < videos.size) {
                                val canProceed = isCurrentVideoCompleted || currentVideoHasError
                                Button(
                                    onClick = { if (canProceed) currentIndex++ },
                                    enabled = canProceed,
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                ) {
                                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        videos.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                    .background(when {
                                        idx < currentIndex -> BrandSuccess
                                        idx == currentIndex -> BrandPrimary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    })
                            )
                        }
                    }
                    if (videos.size > 1) {
                        Spacer(Modifier.height(12.dp))
                        Text("Up Next", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(6.dp))
                        videos.forEachIndexed { idx, v ->
                            val isCurrent = idx == currentIndex
                            Surface(
                                onClick = {
                                    if (idx <= currentIndex || isCurrentVideoCompleted || currentVideoHasError) {
                                        currentIndex = idx
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) BrandPrimary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            ) {
                                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.width(72.dp).height(44.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                        AsyncImage(
                                            model = coil.request.ImageRequest.Builder(LocalContext.current).data(v.thumbnailUrl).crossfade(true).build(),
                                            contentDescription = v.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(v.title, fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {

        Column(modifier = Modifier.fillMaxSize()) {

            // YouTube Player Component (retained via movableContentOf across layouts)
            if (currentVideo != null) {
                Box(
                    modifier = if (effectiveFullscreen) Modifier.fillMaxSize() else Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    playerComposable(effectiveFullscreen)
                }
            }

            // Playlist info & controls (hidden in fullscreen)
            if (!effectiveFullscreen && currentVideo != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
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
                                    text = if (subtitlesEnabled) {
                                        if (parentPrefs.earlyReaderSubtitles) "PHONICS CC" else "CC ON"
                                    } else "CC OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (subtitlesEnabled) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Touch Lock button right next to Next button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChildLockActive) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isChildLockActive) BrandPrimary else MaterialTheme.colorScheme.outline),
                                onClick = {
                                    if (isChildLockActive) {
                                        isExitingPlayer = false
                                        showParentPasswordDialog = true
                                    } else {
                                        isChildLockActive = true
                                        try { activity?.startLockTask() } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier.height(36.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        if (isChildLockActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        contentDescription = "Lock",
                                        tint = if (isChildLockActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Text(
                                        text = if (isChildLockActive) "Locked" else "Lock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChildLockActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

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
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
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
                            itemsIndexed(videos, key = { _, v -> v.itemId }) { idx, v ->
                                val isCurrent = idx == currentIndex
                                Surface(
                                    onClick = { currentIndex = idx },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isCurrent) 2.dp else 1.dp,
                                        if (isCurrent) BrandPrimary else MaterialTheme.colorScheme.outline
                                    ),
                                    shadowElevation = if (isCurrent) 4.dp else 1.dp,
                                    modifier = Modifier.width(140.dp),
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
                                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                    .data(v.thumbnailUrl)
                                                    .crossfade(true)
                                                    .build(),
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

                    // Next button or All Caught Up (Requires completing current video or handling unplayable video)
                    if (isCurrentVideoCompleted || currentVideoHasError) {
                        if (currentIndex + 1 < videos.size) {
                            Button(
                                onClick = {
                                    if (isCurrentVideoCompleted) onVideoCompleted(currentVideo, currentVideoSeconds.toInt())
                                    currentIndex++
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (currentVideoHasError) "Skip Unavailable Video" else "Play Next Video", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isCurrentVideoCompleted) onVideoCompleted(currentVideo, currentVideoSeconds.toInt())
                                    if (isCurrentVideoCompleted) onAllCompleted() else onBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSuccess),
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (currentVideoHasError) "Back to Playlist" else "Finish Playlist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        // Video in progress: Anti-cheat banner prevents closing incomplete videos
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                val percent = if (currentVideoTotalDuration > 0) ((currentVideoSeconds / currentVideoTotalDuration) * 100).toInt().coerceIn(0, 100) else 0
                                Text(
                                    text = if (percent > 0) "Watching: $percent% complete" else "Watch video to complete",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        } // end else (non-tablet-landscape)

        // Floating Back button over the player (hidden in fullscreen)
        if (!effectiveFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                IconButton(
                    onClick = { attemptBack() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f)),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

    if (showParentPasswordDialog) {
        ParentPasswordDialog(
            title = if (pendingUnlockAction != null) "Parent Unlock" else if (isExitingPlayer) "Exit Video Player" else "Unlock Controls",
            subtitle = "Enter 4-digit Parent PIN to ${if (pendingUnlockAction != null) "continue watching" else if (isExitingPlayer) "exit player" else "unlock screen controls"}",
            allowGoogleSignIn = false,
            onSuccess = {
                showParentPasswordDialog = false
                isChildLockActive = false
                try { activity?.stopLockTask() } catch (_: Exception) {}
                pendingUnlockAction?.invoke()
                pendingUnlockAction = null
                if (isExitingPlayer) {
                    isExitingPlayer = false
                    onBack()
                }
            },
            onDismiss = {
                showParentPasswordDialog = false
                isExitingPlayer = false
                pendingUnlockAction = null
            },
        )
    }

    if (activeLimitType != null) {
        val limitKind = activeLimitType
        AlertDialog(
            onDismissRequest = { /* Modal */ },
            icon = {
                Icon(
                    imageVector = if (limitKind == "curfew") Icons.Filled.Bedtime else Icons.Filled.Timer,
                    contentDescription = null,
                    tint = if (limitKind == "curfew") Color(0xFF6C5CE7) else Color(0xFFFF9F43),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (limitKind == "curfew") "Bedtime Mode 🌙" else "Daily Limit Reached ⏱️",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (limitKind == "curfew")
                        "It's curfew time! Time to rest your eyes and recharge for tomorrow."
                    else
                        "You've reached your daily screen time of ${parentPrefs.dailyScreenTimeMinutes} minutes for today. Great job learning!",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUnlockAction = {
                            if (limitKind == "curfew") {
                                curfewBypassed = true
                            } else {
                                dailyLimitBypassed = true
                            }
                            activeLimitType = null
                        }
                        showParentPasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Parent Unlock")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        activeLimitType = null
                        onBack()
                    }
                ) {
                    Text("Done for Today")
                }
            }
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
                            onVideoCompleted(currentVideo, 60)
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
}

@Composable
fun OptimizedVideoPlayer(
    video: VideoItem,
    isFullscreen: Boolean,
    subtitlesEnabled: Boolean,
    onToggleSubtitles: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onVideoEnded: (Int) -> Unit,
    onProgressUpdate: (Float, Float) -> Unit,
    onSkipNext: (() -> Unit)? = null,
    onFinishPlaylist: (() -> Unit)? = null,
    onPlaybackError: ((Boolean) -> Unit)? = null,
    onAttemptBack: (() -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var playerViewRef by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var currentSecond by rememberSaveable(video.youtubeId) { mutableFloatStateOf(0f) }
    var videoDuration by rememberSaveable(video.youtubeId) { mutableFloatStateOf(0f) }
    var playerState by remember { mutableStateOf(PlayerConstants.PlayerState.UNKNOWN) }
    var showControls by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var currentQuality by remember { mutableStateOf(parentPrefs.videoQuality) }
    var availableQualities by remember { mutableStateOf<List<String>>(emptyList()) }

    fun queryAvailableQualities() {
        val script = """
        (function() {
            try {
                if (typeof player !== 'undefined' && player && player.getAvailableQualityLevels) {
                    var levels = player.getAvailableQualityLevels();
                    if (levels && levels.length > 0) return JSON.stringify(levels);
                }
            } catch(e) {}
            return "[]";
        })();
        """.trimIndent()
        webViewRef?.post {
            webViewRef?.evaluateJavascript(script) { resultJson ->
                try {
                    if (!resultJson.isNullOrBlank() && resultJson != "null" && resultJson != "\"[]\"") {
                        val cleaned = resultJson.trim().removeSurrounding("\"").replace("\\\"", "\"")
                        val parsed = org.json.JSONArray(cleaned)
                        val list = mutableListOf<String>()
                        for (i in 0 until parsed.length()) {
                            val q = parsed.optString(i)
                            if (q.isNotBlank()) list.add(q)
                        }
                        if (list.isNotEmpty()) {
                            availableQualities = list
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

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

    fun applySubtitles(enabled: Boolean, isKaraoke: Boolean = parentPrefs.earlyReaderSubtitles) {
        val script = if (enabled) {
            val karaokeCss = if (isKaraoke) {
                """
                var styleId = 'kidcurate-karaoke-subtitles-style';
                var styleEl = document.getElementById(styleId);
                if (!styleEl) {
                    styleEl = document.createElement('style');
                    styleEl.id = styleId;
                    styleEl.innerHTML = `
                        .caption-window {
                            background-color: rgba(15, 23, 42, 0.88) !important;
                            border: 2px solid #F59E0B !important;
                            border-radius: 14px !important;
                            padding: 8px 16px !important;
                            box-shadow: 0 4px 18px rgba(0,0,0,0.7) !important;
                        }
                        .ytp-caption-segment {
                            font-size: 26px !important;
                            line-height: 1.35 !important;
                            font-weight: 800 !important;
                            font-family: system-ui, -apple-system, Roboto, sans-serif !important;
                            letter-spacing: 0.06em !important;
                            word-spacing: 0.12em !important;
                            color: #FFFFFF !important;
                            text-shadow: 0 2px 4px rgba(0,0,0,0.9) !important;
                        }
                        .ytp-caption-segment:last-child {
                            color: #FBBF24 !important;
                            text-shadow: 0 0 12px rgba(251, 191, 36, 0.8), 0 2px 4px rgba(0,0,0,0.9) !important;
                        }
                    `;
                    document.head.appendChild(styleEl);
                }
                """.trimIndent()
            } else {
                """
                var oldStyle = document.getElementById('kidcurate-karaoke-subtitles-style');
                if (oldStyle) oldStyle.remove();
                """.trimIndent()
            }

            """
            (function() {
                try {
                    $karaokeCss
                    if (typeof player !== 'undefined' && player) {
                        player.loadModule('captions');
                        player.setOption('captions', 'track', {'languageCode': 'en'});
                        player.setOption('captions', 'reload', true);
                        if (player.setOption) {
                            player.setOption('captions', 'fontSize', ${if (isKaraoke) 3 else 1});
                        }
                    }
                } catch(e) {}
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                try {
                    var oldStyle = document.getElementById('kidcurate-karaoke-subtitles-style');
                    if (oldStyle) oldStyle.remove();
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

    // React dynamically to subtitlesEnabled changes
    LaunchedEffect(subtitlesEnabled, webViewRef) {
        applySubtitles(subtitlesEnabled)
    }

    // Release YouTube player and unbind lifecycle observer when composable leaves composition
    DisposableEffect(lifecycleOwner) {
        onDispose {
            playerViewRef?.let { pv ->
                try {
                    lifecycleOwner.lifecycle.removeObserver(pv)
                    pv.release()
                } catch (_: Exception) {}
            }
            playerViewRef = null
            youTubePlayerRef = null
            webViewRef = null
        }
    }

    LaunchedEffect(video.youtubeId) {
        playbackError = null
        currentSecond = 0f
        videoDuration = 0f
        youTubePlayerRef?.loadVideo(video.youtubeId, 0f)
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
                            val resumeSeconds = if (currentSecond > 1f) currentSecond else 0f
                            youTubePlayer.loadVideo(video.youtubeId, resumeSeconds)
                            val wv = findWebView(this@apply)
                            wv?.settings?.apply {
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = false
                                allowContentAccess = false
                            }
                            try {
                                val cm = android.webkit.CookieManager.getInstance()
                                cm.setAcceptCookie(true)
                                cm.setAcceptThirdPartyCookies(wv, false)
                            } catch (_: Exception) {}
                            webViewRef = wv
                            applySubtitles(subtitlesEnabled)
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentSecond = second
                            onProgressUpdate(second, videoDuration)
                        }

                        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                            videoDuration = duration
                            onProgressUpdate(currentSecond, duration)
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState,
                        ) {
                            playerState = state
                            if (state == PlayerConstants.PlayerState.PLAYING) {
                                applySubtitles(subtitlesEnabled)
                                queryAvailableQualities()
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
                                PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST ->
                                    "Invalid video playback request."
                                PlayerConstants.PlayerError.HTML_5_PLAYER ->
                                    "HTML5 player error encountered."
                                else -> "Video playback is currently unavailable. Please check your connection."
                            }
                            onPlaybackError?.invoke(true)
                        }
                    }, options)
                }

                playerViewRef = playerView

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
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = false
                    allowContentAccess = false
                }
                try {
                    val cm = android.webkit.CookieManager.getInstance()
                    cm.setAcceptCookie(true)
                    cm.setAcceptThirdPartyCookies(wv, false)
                } catch (_: Exception) {}
                webViewRef = wv
                container
            },
            onRelease = { container ->
                playerViewRef?.let { pv ->
                    try {
                        lifecycleOwner.lifecycle.removeObserver(pv)
                        pv.release()
                    } catch (_: Exception) {}
                }
                playerViewRef = null
                youTubePlayerRef = null
                webViewRef = null
                container.removeAllViews()
            },
            update = { _ -> },
            modifier = Modifier.fillMaxSize(),
        )

        // Playback Error Overlay (ONLY shown if YouTube reports a genuine playback error)
        if (playbackError != null) {
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
                        text = "Playback Notice",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = playbackError ?: "Video playback error occurred.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                playbackError = null
                                onPlaybackError?.invoke(false)
                                youTubePlayerRef?.loadVideo(video.youtubeId, 0f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        ) {
                            Text("Retry", color = Color.White)
                        }
                        if (onSkipNext != null) {
                            Button(
                                onClick = onSkipNext,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Next Video", fontWeight = FontWeight.Bold)
                            }
                        } else if (onFinishPlaylist != null) {
                            Button(
                                onClick = onFinishPlaylist,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Finish Playlist", fontWeight = FontWeight.Bold)
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

        // Custom Overlay Controls (Hidden when there is a playback error to keep error notice clean)
        if (showControls && playbackError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // In fullscreen, show top exit fullscreen button and video title
                if (isFullscreen) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .displayCutoutPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { onAttemptBack?.invoke() ?: onToggleFullscreen() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

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
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (isFullscreen) Modifier
                                .displayCutoutPadding()
                                .navigationBarsPadding()
                            else Modifier
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    // Full-width scrubbing / progress indicator
                    LinearProgressIndicator(
                        progress = { if (videoDuration > 0) (currentSecond / videoDuration).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = BrandPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Time display e.g. 01:23 / 05:10
                        Text(
                            text = "${formatTime(currentSecond)} / ${formatTime(videoDuration)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtitle / Closed Caption (CC) toggle button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (subtitlesEnabled) BrandPrimary.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.25f),
                                onClick = {
                                    onToggleSubtitles()
                                    toastMessage = if (!subtitlesEnabled) {
                                        if (parentPrefs.earlyReaderSubtitles) "Early Reader Karaoke CC ON" else "Subtitles ON (English)"
                                    } else {
                                        "Subtitles OFF"
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                                        contentDescription = if (subtitlesEnabled) "Disable Subtitles" else "Enable Subtitles",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Video Resolution / Quality button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentQuality != "auto" && currentQuality != "default") BrandPrimary.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.25f),
                                onClick = {
                                    queryAvailableQualities()
                                    showQualityDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.HighQuality,
                                        contentDescription = "Video Quality / Resolution",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Fullscreen toggle button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                onClick = { onToggleFullscreen() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = "Toggle Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
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

                        val qualityLabelsMap = mapOf(
                            "highres" to "4K Ultra HD (2160p)",
                            "hd1440" to "2K Quad HD (1440p)",
                            "hd1080" to "1080p (Full HD)",
                            "hd720" to "720p (HD)",
                            "large" to "480p (Standard)",
                            "medium" to "360p (Data Saver)",
                            "small" to "240p",
                            "tiny" to "144p",
                        )

                        val qualityOptions = if (availableQualities.isNotEmpty()) {
                            buildList {
                                add("default" to "Auto (Recommended)")
                                availableQualities.forEach { qKey ->
                                    if (qKey != "auto" && qKey != "default") {
                                        val label = qualityLabelsMap[qKey] ?: qKey.uppercase()
                                        add(qKey to label)
                                    }
                                }
                            }
                        } else {
                            listOf(
                                "default" to "Auto (Recommended)",
                                "hd1080" to "1080p (Full HD)",
                                "hd720" to "720p (HD)",
                                "large" to "480p (Standard)",
                                "medium" to "360p (Data Saver)",
                            )
                        }

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

