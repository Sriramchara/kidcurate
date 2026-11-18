package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kidcurate.app.data.ApiClient
import com.kidcurate.app.data.DbSummaryResponse
import com.kidcurate.app.data.GoogleCredentialAuth
import com.kidcurate.app.data.ParentSessionPolicy
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun SecurityTab(
    parentPrefs: ParentPreferences,
    onPinUpdated: () -> Unit = {},
    onThemeChanged: (String) -> Unit = {},
    onBackToChildMode: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBackToChildMode,
) {
    val appContext = LocalContext.current
    var selectedTheme by remember { mutableStateOf(parentPrefs.appTheme) }
    var showOnboardingTour by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .align(Alignment.TopCenter)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Parent Guide & FAQs Card
        var showGuideDetails by remember { mutableStateOf(false) }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuideDetails = !showGuideDetails },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = BrandPrimary)
                        Text("Parent Guide: How It Works", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        if (showGuideDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }

                if (showGuideDetails) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceCardBorder)
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Child Profiles: Go to 'Profiles' tab to add or edit children, change their character avatar, and view or change their 4-digit PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("2.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Add YouTube Videos: Go to 'Search' tab to find safe educational videos or paste ANY YouTube link to add it directly.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("3.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Manage Playlist: Go to 'Videos' tab to reorder videos with Up/Down buttons or delete unwanted videos.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Parent Master PIN: Set a 4-digit PIN for quick offline access or to bypass a forgotten child PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = { showOnboardingTour = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandPrimary)
                            Spacer(Modifier.width(6.dp))
                            Text("Replay Interactive App Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                    }
                }
            }
        }

        // App Theme Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = BrandPrimary)
                    Text("App Theme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose Light Theme, Dark Theme, or follow your device System setting.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (mode, label) ->
                        val isSelected = selectedTheme == mode
                        OutlinedButton(
                            onClick = {
                                selectedTheme = mode
                                parentPrefs.appTheme = mode
                                onThemeChanged(mode)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        // Daily Screen Time & Bedtime Curfew Card
        var screenTimeMinutes by remember { mutableIntStateOf(parentPrefs.dailyScreenTimeMinutes) }
        var curfewEnabled by remember { mutableStateOf(parentPrefs.curfewEnabled) }
        var curfewStartHour by remember { mutableIntStateOf(parentPrefs.curfewStartHour) }
        var curfewEndHour by remember { mutableIntStateOf(parentPrefs.curfewEndHour) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = BrandPrimary)
                    Text("Daily Screen Time & Bedtime Curfew", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Control how long your child can watch videos each day and set bedtime hours when video playback pauses.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Daily Screen Time Limit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(8.dp))

                val screenTimeOptions = listOf(
                    30 to "30 min",
                    45 to "45 min",
                    60 to "60 min",
                    90 to "90 min",
                    0 to "Unlimited",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    screenTimeOptions.forEach { (mins, label) ->
                        val isSelected = screenTimeMinutes == mins
                        OutlinedButton(
                            onClick = {
                                screenTimeMinutes = mins
                                parentPrefs.dailyScreenTimeMinutes = mins
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(14.dp))

                // Curfew Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = Color(0xFF6C5CE7))
                        Column {
                            Text("Bedtime Curfew Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (curfewEnabled) "Active (${if (curfewStartHour > 12) "${curfewStartHour - 12} PM" else "$curfewStartHour AM"} to ${if (curfewEndHour > 12) "${curfewEndHour - 12} PM" else "$curfewEndHour AM"})" else "Disabled",
                                fontSize = 12.sp,
                                color = if (curfewEnabled) Color(0xFF6C5CE7) else TextSecondary,
                            )
                        }
                    }
                    Switch(
                        checked = curfewEnabled,
                        onCheckedChange = {
                            curfewEnabled = it
                            parentPrefs.curfewEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                if (curfewEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Curfew Start Time (Bedtime):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(19 to "7 PM", 20 to "8 PM", 21 to "9 PM", 22 to "10 PM").forEach { (hr, label) ->
                            val isSelected = curfewStartHour == hr
                            OutlinedButton(
                                onClick = {
                                    curfewStartHour = hr
                                    parentPrefs.curfewStartHour = hr
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF6C5CE7).copy(alpha = 0.15f) else Color.Transparent,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6C5CE7) else SurfaceCardBorder,
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF6C5CE7) else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Curfew End Time (Wake Up):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(6 to "6 AM", 7 to "7 AM", 8 to "8 AM", 9 to "9 AM").forEach { (hr, label) ->
                            val isSelected = curfewEndHour == hr
                            OutlinedButton(
                                onClick = {
                                    curfewEndHour = hr
                                    parentPrefs.curfewEndHour = hr
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF6C5CE7).copy(alpha = 0.15f) else Color.Transparent,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6C5CE7) else SurfaceCardBorder,
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF6C5CE7) else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Early Reader Subtitles & Phonics Card
        var earlyReaderEnabled by remember { mutableStateOf(parentPrefs.earlyReaderSubtitles) }
        var defaultSubtitlesEnabled by remember { mutableStateOf(parentPrefs.subtitlesEnabled) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.ClosedCaption, contentDescription = null, tint = BrandPrimary)
                    Text("Reading & Subtitles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Reading support features to help children connect spoken words with written letters.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                // Karaoke Phonics Subtitles Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Early Reader Karaoke Phonics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Displays large, bold text with animated golden word-highlights synchronized to speech.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = earlyReaderEnabled,
                        onCheckedChange = {
                            earlyReaderEnabled = it
                            parentPrefs.earlyReaderSubtitles = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(10.dp))

                // Default Subtitles Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Subtitles by Default",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Automatically activate English subtitles when starting any video.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = defaultSubtitlesEnabled,
                        onCheckedChange = {
                            defaultSubtitlesEnabled = it
                            parentPrefs.subtitlesEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }
            }
        }

        // Parent Cloud Account Card (Google Sign-In)
        val scope = rememberCoroutineScope()
        var accountStatusMessage by remember { mutableStateOf<String?>(null) }
        var isAccountLoading by remember { mutableStateOf(false) }
        var currentToken by remember { mutableStateOf(parentPrefs.parentAuthToken) }

        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var isDeletingAccount by remember { mutableStateOf(false) }
        var showChangeParentPinDialog by remember { mutableStateOf(false) }

        fun connectGoogleAccount() {
            scope.launch {
                isAccountLoading = true
                accountStatusMessage = null
                try {
                    val account = GoogleCredentialAuth.signIn(
                        context = appContext,
                        serverClientId = parentPrefs.googleClientId.trim(),
                    )
                    val email = account.email
                    val savedPinMatchesAccount = ParentSessionPolicy.canReusePin(
                        hasPin = parentPrefs.hasParentPin,
                        pinOwnerEmail = parentPrefs.parentPinOwnerEmail,
                        currentAccountEmail = parentPrefs.parentEmail,
                        signingInEmail = email,
                    )
                    parentPrefs.parentName = account.name
                    parentPrefs.parentEmail = email

                    val resp = ApiClient.api.googleLogin(
                        mapOf(
                            "id_token" to account.idToken,
                            "email" to email,
                            "name" to account.name,
                        )
                    )
                    val token = resp["access_token"] as? String ?: ""
                    val refreshToken = resp["refresh_token"] as? String ?: ""
                    val serverName = (resp["name"] as? String)?.trim() ?: ""
                    if (serverName.isNotBlank() && serverName != "Parent") {
                        parentPrefs.parentName = serverName
                    }
                    if (token.isBlank()) {
                        accountStatusMessage = "Server did not issue an access token."
                        return@launch
                    }

                    parentPrefs.parentAuthToken = token
                    parentPrefs.parentRefreshToken = refreshToken
                    currentToken = token
                    ApiClient.authToken = token
                    accountStatusMessage = "Connected Google Account: $email"
                    if (!savedPinMatchesAccount) {
                        parentPrefs.parentPin = ""
                        parentPrefs.parentPinOwnerEmail = ""
                        showChangeParentPinDialog = true
                    } else {
                        parentPrefs.parentPinOwnerEmail = email
                    }
                } catch (e: Exception) {
                    accountStatusMessage = GoogleCredentialAuth.userMessage(e)
                } finally {
                    isAccountLoading = false
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = BrandPrimary)
                    Text("Parent Cloud Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sign in with your Google account to automatically synchronize child profiles, playlists, and settings across your devices.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(14.dp))

                if (currentToken.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        Icons.Rounded.AccountCircle,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Column {
                                        Text(
                                            text = "Connected Google Account",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandPrimary,
                                        )
                                        Text(
                                            text = if (parentPrefs.parentName.isNotBlank()) "${parentPrefs.parentName} (${parentPrefs.parentEmail})" else parentPrefs.parentEmail.ifBlank { "Parent Account" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary,
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            runCatching { GoogleCredentialAuth.signOut(appContext) }
                                            parentPrefs.clearParentSession()
                                            currentToken = ""
                                            ApiClient.authToken = ""
                                            accountStatusMessage = "Signed out. You can now connect a different Google account."
                                            onBackToChildMode()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = BrandPrimary.copy(alpha = 0.2f))
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Delete account & cloud data",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(
                                    onClick = { showDeleteAccountDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BrandCoral),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { connectGoogleAccount() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        enabled = !isAccountLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isAccountLoading) "Connecting to Google..." else "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (accountStatusMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        accountStatusMessage!!,
                        color = if (currentToken.isNotBlank()) BrandSuccess else BrandCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Delete Parent Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = BrandCoral)
                        Text("Delete Account & All Data?", fontWeight = FontWeight.Bold, color = BrandCoral)
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your parent account? This will erase all child profiles, playlists, watch history, and cloud data. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isDeletingAccount = true
                                try {
                                    ApiClient.api.deleteAccount()
                                    runCatching { GoogleCredentialAuth.signOut(appContext) }
                                    parentPrefs.clearAccountData()
                                    currentToken = ""
                                    ApiClient.authToken = ""
                                    showDeleteAccountDialog = false
                                    onAccountDeleted()
                                } catch (e: HttpException) {
                                    if (e.code() == 401 || e.code() == 404) {
                                        // The cloud account is already absent or the session no longer exists.
                                        runCatching { GoogleCredentialAuth.signOut(appContext) }
                                        parentPrefs.clearAccountData()
                                        currentToken = ""
                                        ApiClient.authToken = ""
                                        showDeleteAccountDialog = false
                                        onAccountDeleted()
                                    } else {
                                        accountStatusMessage = "Account deletion failed. Please try again."
                                    }
                                } catch (_: Exception) {
                                    accountStatusMessage = "Could not contact the server. Your account was not deleted."
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text(if (isDeletingAccount) "Deleting..." else "Yes, Delete Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Cloud Database Transparency & Audit Card
        var dbSummary by remember { mutableStateOf<DbSummaryResponse?>(null) }
        var isLoadingDbSummary by remember { mutableStateOf(false) }
        var dbSummaryError by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Storage, contentDescription = null, tint = BrandPrimary)
                    Text("Cloud Database Transparency", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Real-time verification of all records stored for your account in the cloud database:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                if (parentPrefs.isParentLoggedIn) {
                    if (dbSummary != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Database Engine:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dbSummary!!.databaseEngine, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Connected Account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dbSummary!!.parentEmail, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Child Profiles in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.childrenCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Playlists in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.playlistsCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Videos Stored in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.totalVideosCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Watch Sessions Logged:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.watchHistoryCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (dbSummaryError != null) {
                        Text(dbSummaryError!!, fontSize = 12.sp, color = BrandCoral, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            isLoadingDbSummary = true
                            dbSummaryError = null
                            scope.launch {
                                try {
                                    val summary = ApiClient.api.getDbSummary()
                                    dbSummary = summary
                                } catch (e: Exception) {
                                    dbSummaryError = "Failed to query database: ${e.message}"
                                } finally {
                                    isLoadingDbSummary = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingDbSummary,
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isLoadingDbSummary) "Querying Database..."
                            else if (dbSummary == null) "Inspect Cloud Database Records"
                            else "Refresh Database Metrics",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Text(
                        "You are currently offline or in local PIN mode. Connect a Google Account above to inspect cloud database records.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Parent Security & PIN Control Card
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var pinErrorMessage by remember { mutableStateOf<String?>(null) }
        var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = BrandPrimary)
                    Text("Security & PIN Control", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KidCurate provides dual-layer security for parent controls and individual child profiles:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                // Child Profile PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Rounded.Face, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Child Profile PINs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "To change a child's PIN, go to the 'Profiles' tab and tap 'Edit' on the child.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Parent Master PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Parent Master PIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Used for quick offline Parent Zone access: ••••",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                newPinInput = ""
                                confirmPinInput = ""
                                pinErrorMessage = null
                                pinSuccessMessage = null
                                showChangeParentPinDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (pinSuccessMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(pinSuccessMessage!!, color = BrandSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Legal & YouTube API Disclosures Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Gavel, contentDescription = null, tint = BrandPrimary)
                    Text("Legal & YouTube Disclosures", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KidCurate uses YouTube API Services to provide safe, parent-curated educational videos. By using this app, you agree to the YouTube Terms of Service and acknowledge Google's Privacy Policy.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )

                Spacer(Modifier.height(14.dp))

                val uriHandler = LocalUriHandler.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://kidcurate-api.onrender.com/privacy")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("Privacy Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://www.youtube.com/t/terms")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("YouTube ToS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://policies.google.com/privacy")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("Google Privacy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Change Parent PIN Dialog
        if (showChangeParentPinDialog) {
            Dialog(onDismissRequest = { showChangeParentPinDialog = false }) {
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
                        Icon(Icons.Rounded.LockReset, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Change Parent Master PIN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Set a 4-digit PIN for quick Parent Zone entry.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPinInput = it },
                            label = { Text("New 4-digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPinInput = it },
                            label = { Text("Confirm New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (pinErrorMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(pinErrorMessage!!, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showChangeParentPinDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (newPinInput.length != 4) {
                                        pinErrorMessage = "PIN must be exactly 4 digits."
                                        return@Button
                                    }
                                    if (newPinInput != confirmPinInput) {
                                        pinErrorMessage = "PINs do not match."
                                        return@Button
                                    }
                                    parentPrefs.parentPin = newPinInput
                                    parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                    pinSuccessMessage = "Parent Master PIN changed successfully!"
                                    showChangeParentPinDialog = false
                                    onPinUpdated()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text("Save PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showOnboardingTour) {
            OnboardingTourDialog(onDismiss = { showOnboardingTour = false })
        }
    }
    } // end Box tablet wrapper
}
