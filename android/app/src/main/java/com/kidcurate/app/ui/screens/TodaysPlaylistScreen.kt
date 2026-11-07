package com.kidcurate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.GlassCard
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.components.ThematicPill
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
    var parentOverrideActive by remember { mutableStateOf(false) }

    val isCurfew = !parentOverrideActive && parentPrefs.isCurfewActive()
    val isDailyLimit = !parentOverrideActive && parentPrefs.isDailyLimitReached()

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
                    Text("Loading today's adventure...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (isCurfew) {
            CurfewLightView(
                childName = childName,
                curfewStartHour = parentPrefs.curfewStartHour,
                curfewEndHour = parentPrefs.curfewEndHour,
                onParentOverride = {
                    pendingUnlockAction = { parentOverrideActive = true }
                    showParentPasswordDialog = true
                },
                onBack = onBack,
            )
        } else if (isDailyLimit) {
            DailyLimitLightView(
                childName = childName,
                minutesUsed = parentPrefs.todayWatchedSeconds / 60,
                dailyLimitMinutes = parentPrefs.dailyScreenTimeMinutes,
                onParentOverride = {
                    pendingUnlockAction = { parentOverrideActive = true }
                    showParentPasswordDialog = true
                },
                onBack = onBack,
            )
        } else if (playlist == null || playlist.items.isEmpty()) {
            EmptyPlaylistLightView(
                childName = childName,
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
                playlist = playlist,
                isChildLockActive = isChildLockActive,
                onToggleLock = {
                    if (isChildLockActive) {
                        // Unlocking: Ask for Password and stay on this screen
                        pendingUnlockAction = {
                            isChildLockActive = false
                            try { activity?.stopLockTask() } catch (_: Exception) {}
                        }
                        showParentPasswordDialog = true
                    } else {
                        // Locking: Activate lock mode and pin screen
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
                },
            )
        }

        // Parent Gate Dialog to unlock
        if (showParentPasswordDialog) {
            ParentPasswordDialog(
                title = "Parent Authorization",
                subtitle = "Enter parent account password to unlock settings",
                onSuccess = {
                    showParentPasswordDialog = false
                    pendingUnlockAction?.invoke()
                    pendingUnlockAction = null
                },
                onDismiss = {
                    showParentPasswordDialog = false
                    pendingUnlockAction = null
                },
            )
        }
    }
}

@Composable
fun FilledPlaylistLightView(
    childName: String,
    playlist: PlaylistData,
    isChildLockActive: Boolean,
    onToggleLock: () -> Unit,
    onStartPlaying: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var viewMode by remember { mutableStateOf("quest") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Top Header
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
                        ThematicAvatar(name = childName, size = 48.dp, iconSize = 24.dp)
                        Column {
                            Text(
                                text = "Hi $childName!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Today's Fun Adventure",
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

                Spacer(Modifier.height(18.dp))

                // Hero Learning Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        ThematicPill(
                            icon = Icons.Rounded.RocketLaunch,
                            label = "TODAY'S QUEST",
                            tint = BrandPrimary,
                            background = MaterialTheme.colorScheme.primaryContainer,
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = playlist.title.ifBlank { "Daily Fun Adventure" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Stats pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val count = playlist.items.size
                            ThematicPill(
                                icon = Icons.Rounded.PlayCircle,
                                label = if (count == 1) "1 Video" else "$count Videos",
                                tint = BrandPrimary,
                                background = MaterialTheme.colorScheme.primaryContainer,
                            )
                            val minutes = playlist.totalDurationSeconds / 60
                            ThematicPill(
                                icon = Icons.Rounded.Timer,
                                label = if (minutes > 0) "$minutes min" else "11 min",
                                tint = BrandAccentDark,
                                background = BrandAccentContainer,
                            )
                            ThematicPill(
                                icon = Icons.Rounded.VerifiedUser,
                                label = "100% Safe",
                                tint = BrandSuccess,
                                background = BrandSuccessContainer,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Big Start Button
                        Button(
                            onClick = { onStartPlaying(0) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Color.White,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Start Adventure Now! 🚀",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        // Section Title & View Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (viewMode == "quest") "Adventure Quest Map" else "Today's Fun Picks",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "${playlist.items.size} Steps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Quest vs List Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (viewMode == "quest") BrandPrimary else Color.Transparent,
                        modifier = Modifier.clickable { viewMode = "quest" },
                    ) {
                        Text(
                            text = "Quest",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "quest") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (viewMode == "list") BrandPrimary else Color.Transparent,
                        modifier = Modifier.clickable { viewMode = "list" },
                    ) {
                        Text(
                            text = "List",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "list") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        if (viewMode == "quest") {
            itemsIndexed(playlist.items) { index, video ->
                QuestAdventureMilestone(
                    video = video,
                    index = index + 1,
                    isLast = index == playlist.items.size - 1,
                    onClick = { onStartPlaying(index) },
                )
            }

            item {
                // Final Trophy Milestone
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color(0xFFFFB800), Color(0xFFF59E0B))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStartPlaying(0) },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFFFB800).copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, Color(0xFFFFB800).copy(alpha = 0.4f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    "Grand Finish: Earn Daily Star!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    "Complete watches & spark your brain!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        } else {
            // Classic Video list items
            itemsIndexed(playlist.items) { index, video ->
                VideoListItemLight(
                    video = video,
                    index = index + 1,
                    onClick = { onStartPlaying(index) },
                )
            }
        }

        // How Today Works Section
        item {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = "KidCurate Sanctuary Guarantee",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        RoadmapStep(
                            icon = Icons.Rounded.VerifiedUser,
                            step = "Shield",
                            title = "100% Vulgarity-Free",
                            desc = "Strictly filtered against bad language, violence, adult jokes, and creepy brainrot tropes.",
                            tint = BrandSuccess,
                            container = BrandSuccessContainer,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        RoadmapStep(
                            icon = Icons.Rounded.Favorite,
                            step = "Moral",
                            title = "Heartwarming Values",
                            desc = "Wholesome stories teaching kindness, honesty, sharing, good manners, and innocent fun.",
                            tint = Color(0xFFE11D48),
                            container = Color(0xFFFFE4E6),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        RoadmapStep(
                            icon = Icons.Rounded.Lock,
                            step = "Zero Ads",
                            title = "No Ads or Algorithms",
                            desc = "No external links, no comment sections, and no algorithmic autoplay traps.",
                            tint = BrandPrimary,
                            container = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Safety Assurance Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSuccessContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BrandSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = BrandSuccess,
                            modifier = Modifier.size(24.dp),
                        )
                        Column {
                            Text(
                                text = "100% Parent Controlled",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "Curated safely from the Parent Zone.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapStep(
    icon: ImageVector,
    step: String,
    title: String,
    desc: String,
    tint: Color,
    container: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = step,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun VideoListItemLight(video: VideoItem, index: Int, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = BrandPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp),
                )
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (video.durationSeconds != null) {
                    val min = video.durationSeconds / 60
                    val sec = video.durationSeconds % 60
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                    ) {
                        Text(
                            text = "$min:${"%02d".format(sec)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = video.channelName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Play action badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = BrandPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
fun QuestAdventureMilestone(
    video: VideoItem,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Stepping Stone Column with connecting trail
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp),
        ) {
            // Milestone Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(BrandPrimary, BrandViolet)
                        )
                    )
                    .shadow(elevation = 4.dp, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$index",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }

            // Connecting Trail Line to next stepping stone
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(90.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(BrandPrimary.copy(alpha = 0.8f), BrandViolet.copy(alpha = 0.3f))
                            )
                        )
                )
            }
        }

        // Adventure Milestone Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = BrandPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(26.dp),
                    )
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (video.durationSeconds != null) {
                        val min = video.durationSeconds / 60
                        val sec = video.durationSeconds % 60
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp),
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

                // Info & Action
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(bottom = 2.dp),
                    ) {
                        Text(
                            text = "Story #$index",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Text(
                        text = video.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = video.channelName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Play circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPlaylistLightView(
    childName: String,
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
                ThematicAvatar(name = childName, size = 72.dp, iconSize = 36.dp)
                Text(
                    text = "Welcome, $childName!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "No playlist chosen for today yet.\nParents can choose or curate today's wholesome stories anytime in the Parent Zone!",
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
                    Text("Parent: Choose Stories Now 🛡️", fontWeight = FontWeight.Bold)
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
fun CurfewLightView(
    childName: String,
    curfewStartHour: Int,
    curfewEndHour: Int,
    onParentOverride: () -> Unit,
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
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandVioletContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = "Bedtime",
                        tint = BrandViolet,
                        modifier = Modifier.size(36.dp),
                    )
                }

                Text(
                    text = "Bedtime Curfew Active",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "KidCurate is resting for the night so $childName can get great sleep. Wholesome stories will unlock in the morning!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Exit / Switch Profile", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onParentOverride,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Parent Override (Enter PIN)", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DailyLimitLightView(
    childName: String,
    minutesUsed: Int,
    dailyLimitMinutes: Int,
    onParentOverride: () -> Unit,
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
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandAccentContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "Limit Reached",
                        tint = BrandAccentDark,
                        modifier = Modifier.size(36.dp),
                    )
                }

                Text(
                    text = "Daily Learning Goal Reached!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Awesome work, $childName! You reached your $dailyLimitMinutes-minute daily learning goal ($minutesUsed min total). Rest your eyes and play!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Exit / Switch Profile", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onParentOverride,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Parent Add Time / Override", fontSize = 13.sp)
                }
            }
        }
    }
}
