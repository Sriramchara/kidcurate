package com.kidcurate.app.ui.screens.parent

import android.app.Activity
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
import com.kidcurate.app.data.EmailOtpPolicy
import com.kidcurate.app.data.GoogleCredentialAuth
import com.kidcurate.app.data.ParentLockPolicy
import com.kidcurate.app.data.ParentLockType
import com.kidcurate.app.data.ParentSessionPolicy
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.ParentBiometricAuth
import androidx.fragment.app.FragmentActivity
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.components.ParentGateMode
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.PatternLockView
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
                                "Independent Parent Lock: Choose a 3x3 Pattern, 4-digit PIN, or Password separate from your phone's lockscreen to protect Parent Zone and Child Lock.",
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

        // Child Playback & Profile Access Card
        var requireChildPinState by remember { mutableStateOf(parentPrefs.requireChildPin) }
        var selectedVideoQuality by remember { mutableStateOf(parentPrefs.videoQuality) }

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
                    Icon(Icons.Rounded.SmartDisplay, contentDescription = null, tint = BrandPrimary)
                    Text("Child Access & Video Quality", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Configure instant profile access for toddlers and default streaming resolution.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                // Instant Tap-to-Play vs Child PIN Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Require Child Profile PIN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (requireChildPinState)
                                "Children must enter their 4-digit PIN to open their playlist."
                            else
                                "Instant Play: Children tap their avatar to start watching immediately (recommended for ages 3–6).",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = requireChildPinState,
                        onCheckedChange = {
                            requireChildPinState = it
                            parentPrefs.requireChildPin = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(12.dp))

                // Video Streaming Quality
                Text(
                    text = "Streaming Video Quality",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Text(
                    text = "Controls resolution without exposing technical settings inside the child player.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        "auto" to "Auto",
                        "hd1080" to "1080p",
                        "hd720" to "720p",
                        "large" to "480p",
                        "medium" to "360p",
                    ).forEach { (qualityKey, label) ->
                        val isSelected = selectedVideoQuality == qualityKey
                        OutlinedButton(
                            onClick = {
                                selectedVideoQuality = qualityKey
                                parentPrefs.videoQuality = qualityKey
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) BrandPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) BrandPrimary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandPrimary else TextSecondary,
                            )
                        }
                    }
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
        var showGmailOtpLoginDialog by remember { mutableStateOf(false) }
        var pendingGoogleOtpEmail by remember { mutableStateOf("") }
        var pendingGoogleOtpName by remember { mutableStateOf("") }
        var pendingGoogleOtpSent by remember { mutableStateOf(false) }
        var pendingGoogleOtpMessage by remember { mutableStateOf<String?>(null) }
        var pendingGoogleOtpPurpose by remember { mutableStateOf("login") }

        fun connectGoogleAccount() {
            val activity = appContext as? Activity
                ?: (appContext as? android.content.ContextWrapper)?.baseContext as? Activity
                ?: return
            scope.launch {
                isAccountLoading = true
                accountStatusMessage = null
                try {
                    val account = GoogleCredentialAuth.signIn(
                        activity = activity,
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
                    val serverName = (resp["name"] as? String)?.trim() ?: ""
                    val resolvedName = if (serverName.isNotBlank() && serverName != "Parent") serverName else account.name

                    if (EmailOtpPolicy.requiresGoogleFirstTimeOtp(resp)) {
                        pendingGoogleOtpEmail = email
                        pendingGoogleOtpName = resolvedName
                        pendingGoogleOtpSent = true
                        pendingGoogleOtpPurpose = "google_signup"
                        pendingGoogleOtpMessage = "Welcome, $resolvedName! Enter the 6-digit code sent to ${EmailOtpPolicy.maskEmail(email)}."
                        showGmailOtpLoginDialog = true
                        return@launch
                    }

                    val token = resp["access_token"] as? String ?: ""
                    val refreshToken = resp["refresh_token"] as? String ?: ""
                    if (resolvedName.isNotBlank() && resolvedName != "Parent") {
                        parentPrefs.parentName = resolvedName
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

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            pendingGoogleOtpEmail = parentPrefs.parentEmail
                            pendingGoogleOtpName = ""
                            pendingGoogleOtpSent = false
                            pendingGoogleOtpMessage = null
                            pendingGoogleOtpPurpose = "login"
                            showGmailOtpLoginDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrandPrimary.copy(alpha = 0.08f),
                        ),
                        enabled = !isAccountLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Rounded.MarkEmailRead,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Verify with Gmail OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandPrimary,
                            )
                        }
                    }
                }

                if (showGmailOtpLoginDialog) {
                    ParentPasswordDialog(
                        title = "Gmail OTP Verification",
                        subtitle = "Verify your Gmail address with a 6-digit code",
                        allowGoogleSignIn = true,
                        initialStartMode = ParentGateMode.EMAIL_OTP,
                        initialOtpEmail = pendingGoogleOtpEmail,
                        initialOtpName = pendingGoogleOtpName,
                        initialOtpSent = pendingGoogleOtpSent,
                        initialOtpMessage = pendingGoogleOtpMessage,
                        initialOtpPurpose = pendingGoogleOtpPurpose,
                        onSuccess = {
                            showGmailOtpLoginDialog = false
                            pendingGoogleOtpSent = false
                            pendingGoogleOtpMessage = null
                            currentToken = parentPrefs.parentAuthToken
                            accountStatusMessage = "Verified via Gmail OTP: ${parentPrefs.parentEmail}"
                            onPinUpdated()
                        },
                        onDismiss = {
                            showGmailOtpLoginDialog = false
                            pendingGoogleOtpSent = false
                            pendingGoogleOtpMessage = null
                        },
                    )
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
                                    val resp = ApiClient.api.deleteAccount()
                                    if (!resp.isSuccessful && resp.code() !in listOf(401, 404)) {
                                        throw HttpException(resp)
                                    }
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

        // Independent Parent Lock & Security Control Card
        var activeParentLockType by remember { mutableStateOf(parentPrefs.parentLockType) }
        var biometricEnabledState by remember { mutableStateOf(parentPrefs.parentBiometricEnabled) }
        val isHardwareBiometricAvailable = remember {
            ParentBiometricAuth.isHardwareBiometricAvailable(appContext)
        }
        var targetLockTypeForSetup by remember { mutableStateOf(parentPrefs.parentLockType) }
        var currentCredentialInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var newPasswordInput by remember { mutableStateOf("") }
        var confirmPasswordInput by remember { mutableStateOf("") }
        var draftPattern by remember { mutableStateOf<List<Int>?>(null) }
        var patternError by remember { mutableStateOf(false) }
        var patternResetKey by remember { mutableIntStateOf(0) }
        var isCurrentVerified by remember { mutableStateOf(false) }
        var pinErrorMessage by remember { mutableStateOf<String?>(null) }
        var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

        fun openLockSetupDialog(initialTargetType: ParentLockType) {
            targetLockTypeForSetup = initialTargetType
            currentCredentialInput = ""
            newPinInput = ""
            confirmPinInput = ""
            newPasswordInput = ""
            confirmPasswordInput = ""
            draftPattern = null
            patternError = false
            patternResetKey++
            isCurrentVerified = !parentPrefs.hasParentPin
            pinErrorMessage = null
            pinSuccessMessage = null
            showChangeParentPinDialog = true
        }

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
                    Text("Parent Lock & Security", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Your KidCurate Parent Lock is completely separate from your mobile phone's lockscreen password. Choose the lock style you prefer:",
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

                Spacer(Modifier.height(10.dp))

                // Independent Parent Lock Style Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    if (activeParentLockType == ParentLockType.PATTERN) Icons.Rounded.Pattern else Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                                Column {
                                    Text("Independent Parent Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Active: ${activeParentLockType.displayName} (Device lockscreen disabled)",
                                        fontSize = 11.sp,
                                        color = BrandPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { openLockSetupDialog(activeParentLockType) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Segmented Lock Style Selector: PIN | Pattern | Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ParentLockType.entries.forEach { lockType ->
                                val isSelected = activeParentLockType == lockType
                                OutlinedButton(
                                    onClick = { openLockSetupDialog(lockType) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) BrandPrimary else SurfaceCardBorder,
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = when (lockType) {
                                            ParentLockType.PIN -> "4-Digit PIN"
                                            ParentLockType.PATTERN -> "Pattern"
                                            ParentLockType.PASSWORD -> "Password"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) BrandPrimary else TextSecondary,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Hardware Biometric Unlock Toggle (Device PIN explicitly disabled)
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
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Unlock with Fingerprint", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isHardwareBiometricAvailable) {
                                        "Optional quick unlock using strong fingerprint sensor. Phone screen lock PIN is never accepted."
                                    } else {
                                        "No strong fingerprint sensor enrolled on this device."
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = biometricEnabledState && isHardwareBiometricAvailable,
                            enabled = isHardwareBiometricAvailable,
                            onCheckedChange = { enabled ->
                                biometricEnabledState = enabled
                                parentPrefs.parentBiometricEnabled = enabled
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandPrimary,
                            ),
                        )
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

        // Configure / Change Independent Parent Lock Dialog
        if (showChangeParentPinDialog) {
            Dialog(onDismissRequest = { showChangeParentPinDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val needsCurrentVerification = parentPrefs.hasParentPin && !isCurrentVerified
                        Icon(
                            if (targetLockTypeForSetup == ParentLockType.PATTERN) Icons.Rounded.Pattern else Icons.Rounded.LockReset,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (needsCurrentVerification) "Verify Current Parent Lock" else "Set Independent Parent Lock",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (needsCurrentVerification) {
                                "Confirm your current ${activeParentLockType.displayName} before choosing a new lock."
                            } else {
                                "Choose a Pattern, PIN, or Password separate from your mobile phone lockscreen."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))

                        if (needsCurrentVerification) {
                            // Step 1: Verify existing parent lock (supports PIN, Pattern, or Password)
                            when (activeParentLockType) {
                                ParentLockType.PATTERN -> {
                                    PatternLockView(
                                        maxSize = 216.dp,
                                        isError = patternError,
                                        resetKey = patternResetKey,
                                        helperText = "Draw your current Parent Pattern",
                                        onPatternStart = {
                                            patternError = false
                                            pinErrorMessage = null
                                        },
                                        onPatternComplete = { dots ->
                                            if (parentPrefs.verifyParentPattern(dots)) {
                                                isCurrentVerified = true
                                                pinErrorMessage = null
                                                patternError = false
                                                patternResetKey++
                                            } else {
                                                patternError = true
                                                pinErrorMessage = "Current Pattern is incorrect."
                                            }
                                        },
                                    )
                                }

                                ParentLockType.PIN -> {
                                    OutlinedTextField(
                                        value = currentCredentialInput,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                                currentCredentialInput = it
                                                pinErrorMessage = null
                                            }
                                        },
                                        label = { Text("Current 4-digit PIN") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                                ParentLockType.PASSWORD -> {
                                    OutlinedTextField(
                                        value = currentCredentialInput,
                                        onValueChange = {
                                            currentCredentialInput = it
                                            pinErrorMessage = null
                                        },
                                        label = { Text("Current Parent Password") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            if (biometricEnabledState && isHardwareBiometricAvailable) {
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        pinErrorMessage = null
                                        val fragmentActivity = appContext as? FragmentActivity
                                        if (fragmentActivity != null) {
                                            ParentBiometricAuth.authenticate(
                                                activity = fragmentActivity,
                                                title = "Verify Parent Identity",
                                                subtitle = "Confirm your fingerprint to authorize Parent Lock change.",
                                                negativeButtonText = "Cancel",
                                                onSuccess = {
                                                    isCurrentVerified = true
                                                    pinErrorMessage = null
                                                    patternResetKey++
                                                },
                                                onError = { err -> pinErrorMessage = err },
                                                onCancel = { pinErrorMessage = null },
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = BrandPrimary.copy(alpha = 0.08f),
                                    ),
                                    border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.35f)),
                                ) {
                                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Verify with Fingerprint", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandPrimary)
                                }
                            }

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
                                if (activeParentLockType != ParentLockType.PATTERN) {
                                    Button(
                                        onClick = {
                                            if (parentPrefs.verifyParentCredential(currentCredentialInput)) {
                                                isCurrentVerified = true
                                                pinErrorMessage = null
                                                patternResetKey++
                                            } else {
                                                pinErrorMessage = "Current ${activeParentLockType.displayName} is incorrect."
                                            }
                                        },
                                        enabled = currentCredentialInput.isNotBlank(),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    ) {
                                        Text("Continue", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Step 2: Choose new style and configure credential
                            if (parentPrefs.hasParentPin) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandSuccess.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Parent identity verified.", fontSize = 12.sp, color = BrandSuccess, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Style Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                ParentLockType.entries.forEach { lockType ->
                                    val isSelected = targetLockTypeForSetup == lockType
                                    OutlinedButton(
                                        onClick = {
                                            targetLockTypeForSetup = lockType
                                            pinErrorMessage = null
                                            draftPattern = null
                                            patternError = false
                                            patternResetKey++
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        ),
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) BrandPrimary else SurfaceCardBorder,
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = when (lockType) {
                                                ParentLockType.PIN -> "PIN"
                                                ParentLockType.PATTERN -> "Pattern"
                                                ParentLockType.PASSWORD -> "Password"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandPrimary else TextSecondary,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            when (targetLockTypeForSetup) {
                                ParentLockType.PATTERN -> {
                                    PatternLockView(
                                        maxSize = 216.dp,
                                        isError = patternError,
                                        resetKey = patternResetKey,
                                        helperText = if (draftPattern == null) {
                                            "Step 1 of 2: Draw new pattern (min ${ParentLockPolicy.MIN_PATTERN_DOTS} dots)"
                                        } else {
                                            "Step 2 of 2: Draw the same pattern again to confirm"
                                        },
                                        onPatternStart = {
                                            patternError = false
                                            pinErrorMessage = null
                                        },
                                        onPatternComplete = { dots ->
                                            if (draftPattern == null) {
                                                if (!ParentLockPolicy.isValidPattern(dots)) {
                                                    patternError = true
                                                    pinErrorMessage = "Connect at least ${ParentLockPolicy.MIN_PATTERN_DOTS} dots."
                                                } else {
                                                    draftPattern = dots
                                                    patternResetKey++
                                                    pinErrorMessage = null
                                                }
                                            } else {
                                                if (dots == draftPattern) {
                                                    parentPrefs.setParentPattern(dots)
                                                    parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                                    activeParentLockType = ParentLockType.PATTERN
                                                    pinSuccessMessage = "Parent Lock updated to 3x3 Pattern!"
                                                    showChangeParentPinDialog = false
                                                    onPinUpdated()
                                                } else {
                                                    patternError = true
                                                    pinErrorMessage = "Patterns did not match. Draw again to confirm."
                                                }
                                            }
                                        },
                                    )

                                    if (draftPattern != null) {
                                        TextButton(
                                            onClick = {
                                                draftPattern = null
                                                patternError = false
                                                pinErrorMessage = null
                                                patternResetKey++
                                            },
                                        ) {
                                            Text("Start Over / Redraw Pattern", fontSize = 12.sp, color = BrandPrimary)
                                        }
                                    }
                                }

                                ParentLockType.PIN -> {
                                    OutlinedTextField(
                                        value = newPinInput,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                                newPinInput = it
                                                pinErrorMessage = null
                                            }
                                        },
                                        label = { Text("New 4-digit PIN") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = confirmPinInput,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                                confirmPinInput = it
                                                pinErrorMessage = null
                                            }
                                        },
                                        label = { Text("Confirm New PIN") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                                ParentLockType.PASSWORD -> {
                                    OutlinedTextField(
                                        value = newPasswordInput,
                                        onValueChange = {
                                            newPasswordInput = it
                                            pinErrorMessage = null
                                        },
                                        label = { Text("New Password (min ${ParentLockPolicy.MIN_PASSWORD_LENGTH} chars)") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = confirmPasswordInput,
                                        onValueChange = {
                                            confirmPasswordInput = it
                                            pinErrorMessage = null
                                        },
                                        label = { Text("Confirm New Password") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

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
                                if (targetLockTypeForSetup != ParentLockType.PATTERN) {
                                    Button(
                                        onClick = {
                                            if (targetLockTypeForSetup == ParentLockType.PIN) {
                                                val err = ParentLockPolicy.validateCredential(ParentLockType.PIN, newPinInput)
                                                if (err != null) {
                                                    pinErrorMessage = err
                                                    return@Button
                                                }
                                                if (newPinInput != confirmPinInput) {
                                                    pinErrorMessage = "PINs do not match."
                                                    return@Button
                                                }
                                                parentPrefs.setParentCredential(ParentLockType.PIN, newPinInput)
                                                parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                                activeParentLockType = ParentLockType.PIN
                                                pinSuccessMessage = "Parent Lock updated to 4-Digit PIN!"
                                                showChangeParentPinDialog = false
                                                onPinUpdated()
                                            } else {
                                                val err = ParentLockPolicy.validateCredential(ParentLockType.PASSWORD, newPasswordInput)
                                                if (err != null) {
                                                    pinErrorMessage = err
                                                    return@Button
                                                }
                                                if (newPasswordInput != confirmPasswordInput) {
                                                    pinErrorMessage = "Passwords do not match."
                                                    return@Button
                                                }
                                                parentPrefs.setParentPassword(newPasswordInput)
                                                parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                                activeParentLockType = ParentLockType.PASSWORD
                                                pinSuccessMessage = "Parent Lock updated to Password!"
                                                showChangeParentPinDialog = false
                                                onPinUpdated()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    ) {
                                        Text(
                                            if (targetLockTypeForSetup == ParentLockType.PIN) "Save PIN" else "Save Password",
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
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
