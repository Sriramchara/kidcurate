package com.kidcurate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.PlaylistData
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.theme.*

@Composable
fun TodaysPlaylistScreen(
    childName: String,
    childAvatar: String,
    playlist: PlaylistData?,
    isLoading: Boolean,
    onStartPlaying: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenParentZone: () -> Unit = {},
) {
    var isChildLockActive by remember { mutableStateOf(false) }
    var showParentPasswordDialog by remember { mutableStateOf(false) }
    var pendingUnlockAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val parentPrefs = remember { ParentPreferences.get(context) }
    var curfewBypassed by remember { mutableStateOf(false) }
    var dailyLimitBypassed by remember { mutableStateOf(false) }
    var limitCheckTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            limitCheckTick++
        }
    }

    val isCurfewBlocked = !curfewBypassed && run {
        val tick = limitCheckTick
        if (tick >= 0) parentPrefs.isCurfewActive() else false
    }
    val isDailyLimitBlocked = !dailyLimitBypassed && run {
        val tick = limitCheckTick
        if (tick >= 0) parentPrefs.isDailyLimitReached() else false
    }

    // Defensive cleanup of screen pinning if navigating away or unmounting
    DisposableEffect(Unit) {
        onDispose {
            try { activity?.stopLockTask() } catch (_: Exception) {}
        }
    }

    val safeBack: () -> Unit = {
        if (isChildLockActive) {
            pendingUnlockAction = {
                isChildLockActive = false
                try { activity?.stopLockTask() } catch (_: Exception) {}
                onBack()
            }
            showParentPasswordDialog = true
        } else {
            onBack()
        }
    }

    // When child lock is active, hardware back button prompts for Parent Password
    BackHandler {
        safeBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding(),
    ) {
        GlassBackgroundAura()
        if (isCurfewBlocked) {
            CurfewRestView(
                childName = childName,
                curfewEndHour = parentPrefs.curfewEndHour,
                onParentUnlock = {
                    pendingUnlockAction = { curfewBypassed = true }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else if (isDailyLimitBlocked) {
            DailyLimitRestView(
                childName = childName,
                dailyMinutes = parentPrefs.dailyScreenTimeMinutes,
                onParentUnlock = {
                    pendingUnlockAction = { dailyLimitBypassed = true }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("Loading videos...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (playlist == null || playlist.items.isEmpty()) {
            EmptyPlaylistLightView(
                childName = childName,
                childAvatar = childAvatar,
                onOpenParentZone = {
                    pendingUnlockAction = {
                        isChildLockActive = false
                        try { activity?.stopLockTask() } catch (_: Exception) {}
                        onOpenParentZone()
                    }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else {
            FilledPlaylistLightView(
                childName = childName,
                childAvatar = childAvatar,
                playlist = playlist,
                isChildLockActive = isChildLockActive,
                onToggleLock = {
                    if (isChildLockActive) {
                        pendingUnlockAction = {
                            isChildLockActive = false
                            try { activity?.stopLockTask() } catch (_: Exception) {}
                        }
                        showParentPasswordDialog = true
                    } else {
                        isChildLockActive = true
                        try { activity?.startLockTask() } catch (_: Exception) {}
                    }
                },
                onStartPlaying = onStartPlaying,
                onBack = safeBack
            )
        }

        // Parent Unlock Dialog
        if (showParentPasswordDialog) {
            ParentPasswordDialog(
                title = "Parent Unlock",
                subtitle = "Enter 4-digit Parent PIN",
                allowGoogleSignIn = false,
                onSuccess = {
                    showParentPasswordDialog = false
                    pendingUnlockAction?.invoke()
                    pendingUnlockAction = null
                },
                onDismiss = {
                    showParentPasswordDialog = false
                    pendingUnlockAction = null
                }
            )
        }
    }
}

@Composable
fun FilledPlaylistLightView(
    childName: String,
    childAvatar: String,
    playlist: PlaylistData,
    isChildLockActive: Boolean,
    onToggleLock: () -> Unit,
    onStartPlaying: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    val gridColumns = ResponsiveLayoutPolicy.playlistGridColumns(widthDp, heightDp)

    if (isLandscape) {
        // YouTube-style Split Console in Landscape
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // LEFT COLUMN: Child header, stats & primary action controls
            Column(
                modifier = Modifier
                    .weight(if (isTablet) 0.32f else 0.38f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Child avatar & name card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ThematicAvatar(
                            name = childName,
                            avatar = childAvatar,
                            size = 46.dp,
                            iconSize = 24.dp
                        )
                        Column {
                            Text(
                                text = "Hi $childName!",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                            )
                            val count = playlist.items.size
                            val mins = playlist.totalDurationSeconds / 60
                            Text(
                                text = if (mins > 0) "$count videos • $mins mins" else "$count videos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Switch Profile
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Switch Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Primary Play All Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = playlist.title.ifBlank { "Today's Playlist" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onStartPlaying(0) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        // Child lock toggle
                        OutlinedButton(
                            onClick = onToggleLock,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                        ) {
                            Icon(
                                if (isChildLockActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = null,
                                tint = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isChildLockActive) "Child Lock Active" else "Enable Child Lock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // RIGHT COLUMN: Scrollable Video Cards Queue (Adaptive 2-column grid on tablets)
            if (gridColumns > 1) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(if (isTablet) 0.68f else 0.62f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    gridItemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                        YouTubeVideoCard(
                            video = video,
                            index = index + 1,
                            onClick = { onStartPlaying(index) },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    itemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                        YouTubeVideoCard(
                            video = video,
                            index = index + 1,
                            onClick = { onStartPlaying(index) },
                        )
                    }
                }
            }
        }
    } else {
        // Portrait Layout (Single Column on Phone, 2-Column Responsive Grid on Tablet)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 2 else 1),
                modifier = Modifier
                    .widthIn(max = if (isTablet) 860.dp else 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Top Header: Child avatar, name, Lock & Switch buttons
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                ThematicAvatar(
                                    name = childName,
                                    avatar = childAvatar,
                                    size = 44.dp,
                                    iconSize = 22.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hi $childName!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = "Choose a video to watch",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Action buttons: Child Lock + Switch Profile (Adaptive compact layout)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Lock toggle indicator
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChildLockActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    onClick = onToggleLock,
                                    modifier = Modifier.height(38.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            if (isChildLockActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                            contentDescription = "Child Lock",
                                            tint = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = if (isChildLockActive) "Locked" else "Lock",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                // Switch Profile Button
                                FilledTonalButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onBackground,
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Switch Profile",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Playlist Banner Card with "Play All" button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.title.ifBlank { "Today's Videos" },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 2,
                                    )
                                    val count = playlist.items.size
                                    val mins = playlist.totalDurationSeconds / 60
                                    Text(
                                        text = if (mins > 0) "$count videos • $mins mins" else "$count videos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Button(
                                    onClick = { onStartPlaying(0) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play All",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Play All",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Title
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }

                // YouTube-style Video Cards Feed with stable item keys
                gridItemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                    YouTubeVideoCard(
                        video = video,
                        index = index + 1,
                        onClick = { onStartPlaying(index) },
                    )
                }
            }
        } // end Box tablet constraint
    } // end portrait else
}

@Composable
fun YouTubeVideoCard(
    video: VideoItem,
    index: Int,
    onClick: () -> Unit,
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    val useCompactCard = isLandscape || isTablet

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (useCompactCard) 4.dp else 16.dp, vertical = if (useCompactCard) 4.dp else 6.dp),
    ) {
        if (useCompactCard) {
            // Horizontal compact card for landscape and tablet grids
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(if (isTablet) 150.dp else 130.dp)
                        .height(if (isTablet) 86.dp else 74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(video.thumbnailUrl).crossfade(true).build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    if (video.durationSeconds != null && video.durationSeconds > 0) {
                        val min = video.durationSeconds / 60; val sec = video.durationSeconds % 60
                        Surface(shape = RoundedCornerShape(5.dp), color = Color.Black.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp)) {
                            Text(text = String.format(java.util.Locale.US, "%d:%02d", min, sec), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#$index  ${video.title}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = video.channelName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:9 Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Large play icon overlay hint
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                if (video.durationSeconds != null && video.durationSeconds > 0) {
                    val min = video.durationSeconds / 60
                    val sec = video.durationSeconds % 60
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%d:%02d", min, sec),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            // Title and Channel below thumbnail (Classic YouTube Card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = video.channelName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        } // end portrait else
    }
}

@Composable
fun EmptyPlaylistLightView(
    childName: String,
    childAvatar: String,
    onOpenParentZone: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                ThematicAvatar(
                    name = childName,
                    avatar = childAvatar,
                    size = 72.dp,
                    iconSize = 36.dp
                )
                Text(
                    text = "Welcome, $childName!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "No videos added for today yet.\nParents can search and add videos anytime from the Parent Zone!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onOpenParentZone,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Parent: Open Control Center to Add Videos", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Exit / Switch Child Profile")
                }
            }
        }
    }
}

@Composable
private fun CurfewRestView(
    childName: String,
    curfewEndHour: Int = 7,
    onParentUnlock: () -> Unit,
    onBack: () -> Unit,
) {
    val endHourFormatted = if (curfewEndHour == 0) "12:00 AM" else if (curfewEndHour < 12) "$curfewEndHour:00 AM" else if (curfewEndHour == 12) "12:00 PM" else "${curfewEndHour - 12}:00 PM"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bedtime,
                    contentDescription = null,
                    tint = Color(0xFF6C5CE7),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Sweet Dreams, $childName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "It's bedtime curfew until $endHourFormatted. Time to rest your eyes and recharge for tomorrow's learning adventures!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Exit", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onParentUnlock,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Parent Unlock", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DailyLimitRestView(
    childName: String,
    dailyMinutes: Int,
    onParentUnlock: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9F43).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = Color(0xFFFF9F43),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Great Learning Today, $childName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You've reached your daily screen time of $dailyMinutes minutes. Great job completing today's playlist!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Exit", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onParentUnlock,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Parent Unlock", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
