package com.kidcurate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val parentPrefs = remember { ParentPreferences(context) }
    var showParentGate by remember { mutableStateOf(false) }
    var showOnboardingTour by remember { mutableStateOf(!parentPrefs.hasSeenOnboardingTour) }
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

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
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Brand Tag & Server Settings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThematicPill(
                    icon = Icons.Rounded.VerifiedUser,
                    label = "KidCurate • Safe & Distraction-Free",
                    tint = BrandPrimary,
                    background = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Who's ready to explore?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Pick your profile to jump into today's fun adventure!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp),
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
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp),
                    ) {
                        val isError = errorMessage != null
                        Icon(
                            if (isError) Icons.Rounded.CloudOff else Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(52.dp),
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
                        Spacer(modifier = Modifier.height(20.dp))
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
                            .widthIn(min = 180.dp, max = 220.dp)
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
                                .padding(vertical = 28.dp, horizontal = 20.dp),
                        ) {
                            ThematicAvatar(
                                name = profile.name,
                                avatar = profile.avatar,
                                size = 88.dp,
                                iconSize = 44.dp,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = profile.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
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
                // Multiple profiles grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                            ) {
                                ThematicAvatar(
                                    name = profile.name,
                                    avatar = profile.avatar,
                                    size = 80.dp,
                                    iconSize = 40.dp,
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = profile.name,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Zone Button
            OutlinedButton(
                onClick = {
                    SoundHaptics.playTactileClick(view, haptic)
                    showParentGate = true
                },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BrandPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Parent Zone (Add Videos & Settings)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
    }

    if (showParentGate) {
        ParentPasswordDialog(
            title = "Parent Verification",
            subtitle = "Sign in with Google or enter your 4-digit Parent PIN",
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
