package com.kidcurate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.kidcurate.app.data.PlaylistData
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

    // When child lock is active, hardware back button prompts for Parent Password
    BackHandler {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        GlassBackgroundAura()
        if (isLoading) {
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
                onBack = {
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
                onBack = {
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
            )
        }

        // Parent Unlock Dialog
        if (showParentPasswordDialog) {
            ParentPasswordDialog(
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Top Header: Child avatar, name, Lock & Switch buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ThematicAvatar(
                            name = childName,
                            avatar = childAvatar,
                            size = 48.dp,
                            iconSize = 24.dp
                        )
                        Column {
                            Text(
                                text = "Hi $childName!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Choose a video to watch",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Action buttons: Child Lock + Switch Profile
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Lock toggle indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChildLockActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { onToggleLock() },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                    text = if (isChildLockActive) "Locked" else "Unlock",
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Switch Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
        item {
            Text(
                text = "Videos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        // YouTube-style Video Cards Feed
        itemsIndexed(playlist.items) { index, video ->
            YouTubeVideoCard(
                video = video,
                index = index + 1,
                onClick = { onStartPlaying(index) },
            )
        }
    }
}

@Composable
fun YouTubeVideoCard(
    video: VideoItem,
    index: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
    ) {
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
                    model = video.thumbnailUrl,
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
                            text = "$min:${"%02d".format(sec)}",
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
            modifier = Modifier.fillMaxWidth(),
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
