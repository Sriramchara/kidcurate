package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.ui.theme.*

@Composable
fun PinEntryScreen(
    childId: String,
    childName: String = "",
    childAvatar: String = "",
    onPinVerified: (String, String, String) -> Unit,
    onVerifyPin: suspend (String, String) -> Boolean,
    onParentUnlock: () -> Unit = {},
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)

    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var showParentGate by remember { mutableStateOf(false) }
    var hasFailedAttempt by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val compactLandscape = ResponsiveLayoutPolicy.isCompactLandscape(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
    )
    val isTablet = ResponsiveLayoutPolicy.isTablet(configuration.screenWidthDp, configuration.screenHeightDp)
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val useTwoPaneLayout = compactLandscape || (isLandscape && isTablet)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding(),
    ) {
        // Top Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(BackgroundCard),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
            )
        }

        if (useTwoPaneLayout) {
            // Two-pane landscape: left = avatar+text+pin boxes, right = number pad
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LEFT PANE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = 12.dp),
                ) {
                    if (isTablet) {
                        ThematicAvatar(name = childName, avatar = childAvatar, size = 72.dp, iconSize = 36.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text(
                        text = if (childName.isNotEmpty()) "Welcome back, $childName!" else "Welcome!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Enter your secret PIN to unlock today's session",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                    // PIN Boxes (Symmetrically Centered)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < pin.length
                            val digitChar = if (isFilled) pin[index].toString() else ""
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isFilled) BrandPrimary.copy(alpha = 0.12f) else BackgroundCard)
                                    .border(2.dp, if (hasFailedAttempt) BrandCoral else if (isFilled) BrandPrimary else SurfaceCardBorder, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isFilled) {
                                    if (showPin) {
                                        Text(text = digitChar, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (hasFailedAttempt) BrandCoral else BrandPrimary)
                                    } else {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (hasFailedAttempt) BrandCoral else BrandPrimary))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Centered Show/Hide PIN Toggle Pill
                    Surface(
                        onClick = { showPin = !showPin },
                        shape = RoundedCornerShape(16.dp),
                        color = if (showPin) BrandPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showPin) BrandPrimary else Color.Transparent),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                tint = if (showPin) BrandPrimary else TextSecondary,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = if (showPin) "Hide PIN" else "Show PIN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (showPin) BrandPrimary else TextSecondary,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.height(26.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (errorMessage != null) {
                            Text(text = errorMessage, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    TextButton(onClick = { showParentGate = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Forgot PIN? (Parent Unlock)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandPrimary)
                    }
                    if (isLoading) { CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(24.dp)) }
                }
                // RIGHT PANE — number pad
                Box(
                    modifier = Modifier.weight(0.55f).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    NumberPadLight(
                        buttonSize = if (isTablet) 60.dp else 46.dp,
                        rowSpacing = if (isTablet) 8.dp else 5.dp,
                        columnSpacing = if (isTablet) 14.dp else 10.dp,
                        onDigitPress = { digit -> if (!isLoading) { hasFailedAttempt = false; if (pin.length < 4) pin += digit } },
                        onDelete = { if (pin.isNotEmpty() && !isLoading) { hasFailedAttempt = false; pin = pin.dropLast(1) } },
                    )
                }
            }
        } else {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (compactLandscape) Arrangement.Top else Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (compactLandscape) 72.dp else 16.dp,
                    vertical = if (compactLandscape) 12.dp else 64.dp,
                ),
        ) {
            if (!compactLandscape) {
                ThematicAvatar(
                    name = childName,
                    avatar = childAvatar,
                    size = 84.dp,
                    iconSize = 42.dp,
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = if (childName.isNotEmpty()) "Welcome back, $childName!" else "Welcome!",
                fontSize = if (compactLandscape) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Text(
                text = "Enter your secret PIN to unlock today's session",
                fontSize = if (compactLandscape) 11.sp else 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = if (compactLandscape) 1.dp else 4.dp,
                    bottom = if (compactLandscape) 6.dp else 20.dp,
                ),
            )

            // 4 PIN Boxes (Symmetrically Centered over the Number Pad)
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val isFilled = index < pin.length
                    val digitChar = if (isFilled) pin[index].toString() else ""
                    Box(
                        modifier = Modifier
                            .size(if (compactLandscape) 36.dp else 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isFilled) BrandPrimary.copy(alpha = 0.12f) else BackgroundCard)
                            .border(
                                2.dp,
                                if (hasFailedAttempt) BrandCoral else if (isFilled) BrandPrimary else SurfaceCardBorder,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isFilled) {
                            if (showPin) {
                                Text(
                                    text = digitChar,
                                    fontSize = if (compactLandscape) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasFailedAttempt) BrandCoral else BrandPrimary,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(if (compactLandscape) 10.dp else 13.dp)
                                        .clip(CircleShape)
                                        .background(if (hasFailedAttempt) BrandCoral else BrandPrimary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Centered Eye Visibility Toggle Pill
            Surface(
                onClick = { showPin = !showPin },
                shape = RoundedCornerShape(16.dp),
                color = if (showPin) BrandPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (showPin) BrandPrimary else Color.Transparent),
                modifier = Modifier.height(28.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                        tint = if (showPin) BrandPrimary else TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (showPin) "Hide PIN" else "Show PIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (showPin) BrandPrimary else TextSecondary,
                    )
                }
            }

            // Fixed height error container and clear separation before Number Pad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactLandscape) 22.dp else 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = BrandCoral,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(if (compactLandscape) 4.dp else 16.dp))

            // Number pad
            NumberPadLight(
                buttonSize = if (compactLandscape) 42.dp else 76.dp,
                rowSpacing = if (compactLandscape) 4.dp else 10.dp,
                columnSpacing = if (compactLandscape) 10.dp else 16.dp,
                onDigitPress = { digit ->
                    if (!isLoading) {
                        hasFailedAttempt = false
                        if (pin.length < 4) {
                            pin += digit
                        }
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty() && !isLoading) {
                        hasFailedAttempt = false
                        pin = pin.dropLast(1)
                    }
                },
            )

            Spacer(modifier = Modifier.height(if (compactLandscape) 2.dp else 10.dp))

            TextButton(
                onClick = { showParentGate = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Forgot PIN? (Parent Unlock)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimary,
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    color = BrandPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        } // end else (single-pane portrait)

        if (showParentGate) {
            ParentPasswordDialog(
                title = "Parent Unlock",
                subtitle = "Enter 4-digit Parent PIN to unlock ${if (childName.isNotBlank()) "$childName's" else "this"} playlist",
                allowGoogleSignIn = false,
                onSuccess = {
                    showParentGate = false
                    onParentUnlock()
                },
                onDismiss = { showParentGate = false },
            )
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            val verified = onVerifyPin(childId, pin)
            if (!verified) {
                hasFailedAttempt = true
                // Retain digits so child/parent can see with eye toggle and delete one-by-one!
            } else {
                hasFailedAttempt = false
            }
        }
    }
}

@Composable
fun NumberPadLight(
    buttonSize: androidx.compose.ui.unit.Dp = 76.dp,
    rowSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    columnSpacing: androidx.compose.ui.unit.Dp = 16.dp,
    onDigitPress: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "delete"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        digits.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(columnSpacing)) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(buttonSize))
                    } else {
                        Surface(
                            onClick = {
                                SoundHaptics.playTactileClick(view, haptic)
                                if (digit == "delete") onDelete() else onDigitPress(digit)
                            },
                            shape = CircleShape,
                            color = BackgroundCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceCardBorder),
                            modifier = Modifier.size(buttonSize),
                            shadowElevation = 2.dp,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (digit == "delete") {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete last digit",
                                        tint = BrandCoral,
                                        modifier = Modifier.size(buttonSize * 0.36f),
                                    )
                                } else {
                                    Text(
                                        text = digit,
                                        fontSize = if (buttonSize < 60.dp) 18.sp else 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
