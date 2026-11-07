package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Lock
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
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)

    var pin by remember { mutableStateOf("") }

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
                .padding(horizontal = 24.dp),
        ) {
            ThematicAvatar(
                name = childName,
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
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            // PIN dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val isFilled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) BrandPrimary else BackgroundCard)
                            .border(
                                2.dp,
                                if (isFilled) BrandPrimary else SurfaceCardBorder,
                                CircleShape
                            ),
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
                    if (pin.length < 4 && !isLoading) {
                        pin += digit
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty() && !isLoading) {
                        pin = pin.dropLast(1)
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = BrandPrimary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            val verified = onVerifyPin(childId, pin)
            if (!verified) {
                pin = ""
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
                            shape = CircleShape,
                            color = BackgroundCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceCardBorder),
                            modifier = Modifier
                                .size(76.dp)
                                .clickable {
                                    SoundHaptics.playTactileClick(view, haptic)
                                    if (digit == "⌫") onDelete() else onDigitPress(digit)
                                },
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
