package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.ui.components.ConfettiCelebration
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.theme.*

@Composable
fun CompletionScreen(
    childName: String,
    childAvatar: String,
    videosWatched: Int,
    totalMinutes: Int,
    onGoHome: () -> Unit,
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        SoundHaptics.playCelebration(view, haptic)
    }

    BackHandler(onBack = onGoHome)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding(),
        contentAlignment = Alignment.Center,
    ) {
        ConfettiCelebration(isActive = true)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 16.dp),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(if (isLandscape) 16.dp else 28.dp),
        ) {
            // Trophy
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 60.dp else 90.dp)
                    .clip(CircleShape)
                    .background(BrandAccentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = BrandAccentDark,
                    modifier = Modifier.size(if (isLandscape) 36.dp else 54.dp),
                )
            }

            ThematicAvatar(name = childName, avatar = childAvatar, size = if (isLandscape) 48.dp else 64.dp, iconSize = if (isLandscape) 24.dp else 32.dp)

            Text(
                text = "All Done, $childName!",
                fontSize = if (isLandscape) 20.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "You've finished all the videos in today's playlist.",
                fontSize = if (isLandscape) 13.sp else 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(if (isLandscape) 2.dp else 8.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatBadgeLight(
                    icon = Icons.Rounded.PlayCircle,
                    value = "$videosWatched",
                    label = if (videosWatched == 1) "Video" else "Videos",
                    tint = MaterialTheme.colorScheme.primary,
                    container = MaterialTheme.colorScheme.primaryContainer,
                )
                StatBadgeLight(
                    icon = Icons.Rounded.Timer,
                    value = "$totalMinutes",
                    label = "Minutes",
                    tint = MaterialTheme.colorScheme.secondary,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                )
            }

            Spacer(Modifier.height(if (isLandscape) 2.dp else 8.dp))

            Button(
                onClick = {
                    SoundHaptics.playTactileClick(view, haptic)
                    onGoHome()
                },
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
            ) {
                Text(
                    text = "Back to Home",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun StatBadgeLight(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    container: Color,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BackgroundCard,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceCardBorder),
        shadowElevation = 3.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
