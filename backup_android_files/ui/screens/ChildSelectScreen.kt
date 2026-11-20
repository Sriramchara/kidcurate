package com.kidcurate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.kidcurate.app.data.ChildProfileItem
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.GlassCard
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.components.ThematicPill
import com.kidcurate.app.ui.theme.*

@Composable
fun ChildSelectScreen(
    profiles: List<ChildProfileItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectProfile: (ChildProfileItem) -> Unit,
    onOpenParentZone: () -> Unit,
    onRetry: () -> Unit,
    onUpdateServerUrl: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var showParentGate by remember { mutableStateOf(false) }
    var showOnboardingTour by remember { mutableStateOf(!parentPrefs.hasSeenOnboardingTour) }
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        GlassBackgroundAura()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = if (isLandscape) 40.dp else 24.dp, vertical = if (isLandscape) 8.dp else 20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 12.dp))

            // Brand Tag & Discreet Parent Gate Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThematicPill(
                    icon = Icons.Rounded.VerifiedUser,
                    label = "KidCurate • Safe & Distraction-Free",
                    tint = BrandPrimary,
                    background = MaterialTheme.colorScheme.primaryContainer,
                )

                // Discreet, tasteful Parent Gate button at top corner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            SoundHaptics.playTactileClick(view, haptic)
                            showParentGate = true
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Shield,
                            contentDescription = "Parent Gate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Parents",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 16.dp))

            Text(
                text = "Who's Watching?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isLandscape) 22.sp else 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Tap your avatar to play!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (isLandscape) 13.sp else 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = if (isLandscape) 8.dp else 24.dp),
            )

            if (isLoading) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Loading profiles...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }
                }
            } else if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .padding(16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(if (isLandscape) 12.dp else 28.dp),
                        ) {
                        val isError = errorMessage != null
                        Icon(
                            if (isError) Icons.Rounded.CloudOff else Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(if (isLandscape) 36.dp else 52.dp),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isError) "Unable to Connect" else "Welcome to KidCurate",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isError) {
                                "Please check your internet connection and try again."
                            } else {
                                "Welcome! KidCurate is a safe, parent-controlled video player for kids. Create a profile, pick safe YouTube videos, and hand over the device with complete peace of mind."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        if (!isError) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "rocket", avatar = "rocket", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("1. Profile", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "palette", avatar = "palette", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("2. Add Videos", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "star", avatar = "star", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("3. Safe Play", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isError) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }

                            if (com.kidcurate.app.BuildConfig.DEBUG) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Target: ${parentPrefs.serverUrl.ifBlank { com.kidcurate.app.BuildConfig.API_BASE_URL }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Quick Server Switch:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("https://kidcurate-api.onrender.com/") },
                                        label = { Text("Cloud", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("http://192.168.0.107:8000/") },
                                        label = { Text("Wi-Fi PC", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("http://localhost:8000/") },
                                        label = { Text("USB", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { showParentGate = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create First Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                }
            } else if (profiles.size == 1) {
                // Single child profile: perfectly centered on screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val profile = profiles.first()
                    GlassCard(
                        modifier = Modifier
                            .widthIn(min = if (isTablet) 240.dp else 180.dp, max = if (isTablet) 300.dp else 220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                SoundHaptics.playPop(view, haptic)
                                onSelectProfile(profile)
                            },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (isLandscape) 14.dp else (if (isTablet) 36.dp else 28.dp), horizontal = 20.dp),
                        ) {
                            ThematicAvatar(
                                name = profile.name,
                                avatar = profile.avatar,
                                size = if (isTablet) (if (isLandscape) 88.dp else 112.dp) else (if (isLandscape) 64.dp else 88.dp),
                                iconSize = if (isTablet) (if (isLandscape) 44.dp else 56.dp) else (if (isLandscape) 32.dp else 44.dp),
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = profile.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 22.sp else 18.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Play",
                                        color = BrandPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Multiple profiles grid: responsive width capping for wide tablets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isTablet) 180.dp else 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .widthIn(max = 860.dp)
                            .fillMaxSize(),
                    ) {
                        itemsIndexed(profiles, key = { _, profile -> profile.id }) { index, profile ->
                            GlassCard(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        onSelectProfile(profile)
                                    },
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = if (isTablet) 28.dp else 24.dp, horizontal = 16.dp),
                                ) {
                                    ThematicAvatar(
                                        name = profile.name,
                                        avatar = profile.avatar,
                                        size = if (isTablet) 96.dp else 80.dp,
                                        iconSize = if (isTablet) 48.dp else 40.dp,
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = profile.name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isTablet) 20.sp else 18.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = "Play",
                                                color = BrandPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Icon(
                                                Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtle Parent Control shortcut at bottom (tasteful & non-distracting for kids)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = {
                        SoundHaptics.playTactileClick(view, haptic)
                        showParentGate = true
                    },
                    modifier = Modifier.padding(bottom = if (isLandscape) 4.dp else 12.dp),
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Parent Controls & Settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
    }

    if (showParentGate) {
        ParentPasswordDialog(
            title = if (parentPrefs.isParentLoggedIn) "Parent Verification" else "Parent Account Sign In",
            subtitle = if (parentPrefs.isParentLoggedIn) "Enter 4-digit Parent PIN" else "Sign in with Google to set up child profiles",
            forceGoogleOnly = !parentPrefs.isParentLoggedIn,
            onSuccess = {
                showParentGate = false
                onOpenParentZone()
            },
            onDismiss = { showParentGate = false },
        )
    }

    if (showOnboardingTour) {
        OnboardingTourDialog(
            onDismiss = {
                parentPrefs.hasSeenOnboardingTour = true
                showOnboardingTour = false
            }
        )
    }
}
