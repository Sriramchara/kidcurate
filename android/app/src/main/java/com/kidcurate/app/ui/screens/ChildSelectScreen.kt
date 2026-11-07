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
    var showParentGate by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
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

            // Brand Tag
            ThematicPill(
                icon = Icons.Rounded.VerifiedUser,
                label = "KidCurate • Safe & Ad-Free",
                tint = BrandPrimary,
                background = MaterialTheme.colorScheme.primaryContainer,
            )

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
                            text = if (isError) "Connection Notice" else "Welcome to KidCurate! 🎉",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isError) {
                                errorMessage ?: "Could not reach server. Check your connection."
                            } else {
                                "You haven't added any child profiles yet. Tap below to create your child's profile and start curating fun educational adventures!"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (isError) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text("Retry Connection", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { showParentGate = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create First Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Profile Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    itemsIndexed(profiles) { index, profile ->
                        GlassCard(
                            modifier = Modifier
                                .clickable {
                                    SoundHaptics.playPop(view, haptic)
                                    onSelectProfile(profile)
                                },
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                            ) {
                                ThematicAvatar(
                                    name = profile.name,
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
            subtitle = "Enter parent account password to manage settings",
            onSuccess = {
                showParentGate = false
                onOpenParentZone()
            },
            onDismiss = { showParentGate = false },
        )
    }

    if (showServerDialog) {
        val context = LocalContext.current
        val parentPrefs = remember { ParentPreferences.get(context) }
        var currentInput by remember { mutableStateOf(parentPrefs.serverUrl) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showServerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Rounded.SettingsEthernet,
                            contentDescription = null,
                            tint = BrandPrimary,
                        )
                        Text(
                            text = "Server Connection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Text(
                        text = "Connect via USB port forward or directly over your home Wi-Fi network.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Quick presets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Presets:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { currentInput = com.kidcurate.app.BuildConfig.API_BASE_URL },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            ) {
                                Text("Default Server", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { currentInput = "http://localhost:8000" },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            ) {
                                Text("Localhost (USB)", fontSize = 11.sp)
                            }
                        }
                    }

                    // Input field
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        label = { Text("API Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showServerDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val trimmed = currentInput.trim().trimEnd('/')
                                if (trimmed.isNotBlank()) {
                                    parentPrefs.serverUrl = trimmed
                                    com.kidcurate.app.data.ApiClient.updateBaseUrl(trimmed)
                                    onUpdateServerUrl(trimmed)
                                    showServerDialog = false
                                    onRetry()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier.weight(1.5f),
                        ) {
                            Text("Save & Connect", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    }
