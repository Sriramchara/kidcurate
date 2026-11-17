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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding(),
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            ThematicAvatar(
                name = childName,
                avatar = childAvatar,
                size = 84.dp,
                iconSize = 42.dp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (childName.isNotEmpty()) "Welcome back, $childName!" else "Welcome!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Text(
                text = "Enter your secret PIN to unlock today's session",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            // PIN Boxes with Eye Visibility Toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val isFilled = index < pin.length
                    val digitChar = if (isFilled) pin[index].toString() else ""
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFilled) BrandPrimary.copy(alpha = 0.12f) else BackgroundCard)
                            .border(
                                2.dp,
                                if (hasFailedAttempt) BrandCoral else if (isFilled) BrandPrimary else SurfaceCardBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isFilled) {
                            if (showPin) {
                                Text(
                                    text = digitChar,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasFailedAttempt) BrandCoral else BrandPrimary,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (hasFailedAttempt) BrandCoral else BrandPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = { showPin = !showPin },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (showPin) BrandPrimary.copy(alpha = 0.15f) else BackgroundCard)
                        .border(1.dp, SurfaceCardBorder, CircleShape),
                ) {
                    Icon(
                        imageVector = if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                        tint = if (showPin) BrandPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Error message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = BrandCoral,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            // Number pad
            NumberPadLight(
                onDigitPress = { digit ->
                    if (!isLoading) {
                        hasFailedAttempt = false
                        if (pin.length < 4) {
                            pin += digit
                        } else {
                            // If previously reached 4 and failed, replace with new first digit
                            pin = digit
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

            Spacer(modifier = Modifier.height(10.dp))

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
    onDigitPress: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        digits.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(76.dp))
                    } else {
                        Surface(
                            onClick = {
                                SoundHaptics.playTactileClick(view, haptic)
                                if (digit == "⌫") onDelete() else onDigitPress(digit)
                            },
                            shape = CircleShape,
                            color = BackgroundCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceCardBorder),
                            modifier = Modifier.size(76.dp),
                            shadowElevation = 2.dp,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = if (digit == "⌫") 22.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (digit == "⌫") BrandCoral else TextPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
