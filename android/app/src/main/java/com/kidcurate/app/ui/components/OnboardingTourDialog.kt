package com.kidcurate.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.ui.theme.*

data class ButtonGuideItem(
    val buttonName: String,
    val icon: ImageVector,
    val actionDescription: String,
)

data class TourStep(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val buttonGuides: List<ButtonGuideItem>,
)

val TOUR_STEPS = listOf(
    TourStep(
        stepNumber = 1,
        badge = "SCREEN 1: CHILD LOGIN & PIN PAD",
        title = "Who's Ready to Explore?",
        description = "Children tap their profile avatar and enter their secret 4-digit PIN to open their daily playlist.",
        icon = Icons.Rounded.Lock,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Child Avatar Card",
                icon = Icons.Rounded.Face,
                actionDescription = "Tap to open PIN pad and begin watching approved videos."
            ),
            ButtonGuideItem(
                buttonName = "Eye Toggle Icon",
                icon = Icons.Rounded.Visibility,
                actionDescription = "Reveals or hides entered PIN digits so typos can be verified."
            ),
            ButtonGuideItem(
                buttonName = "Parent Shield (Top Right)",
                icon = Icons.Rounded.AdminPanelSettings,
                actionDescription = "Opens Parent Zone using your 4-digit Parent PIN."
            ),
            ButtonGuideItem(
                buttonName = "Backspace",
                icon = Icons.AutoMirrored.Rounded.Backspace,
                actionDescription = "Deletes digits one by one so mistakes can be easily fixed."
            ),
        ),
    ),
    TourStep(
        stepNumber = 2,
        badge = "SCREEN 2: TODAY'S PLAYLIST",
        title = "Today's Curated Queue",
        description = "Distraction-free daily queue assigned by parents. Zero ads, no algorithmic autoplay, and no comments.",
        icon = Icons.Rounded.VideoLibrary,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Play All Button",
                icon = Icons.Rounded.PlayArrow,
                actionDescription = "Starts today's assigned videos in sequence from video 1."
            ),
            ButtonGuideItem(
                buttonName = "Child Lock (Lock/Unlock)",
                icon = Icons.Rounded.Lock,
                actionDescription = "Locks touch controls to prevent kids from skipping or exiting."
            ),
            ButtonGuideItem(
                buttonName = "Switch Profile",
                icon = Icons.AutoMirrored.Filled.Logout,
                actionDescription = "Quickly switch between brother and sister profiles without reloading."
            ),
        ),
    ),
    TourStep(
        stepNumber = 3,
        badge = "SCREEN 3: VIDEO PLAYER",
        title = "Safe Playback & Phonics",
        description = "Pure fullscreen entertainment locked strictly to parent-approved playlists.",
        icon = Icons.Rounded.SmartDisplay,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "CC Subtitles Button",
                icon = Icons.Rounded.ClosedCaption,
                actionDescription = "Toggles phonics karaoke subtitles with golden word highlight."
            ),
            ButtonGuideItem(
                buttonName = "Lock Controls Button",
                icon = Icons.Rounded.Lock,
                actionDescription = "Unlock controls using your 4-digit Parent PIN (stays right on video)."
            ),
            ButtonGuideItem(
                buttonName = "Next Video Button",
                icon = Icons.Rounded.SkipNext,
                actionDescription = "Advances to next video with 4-second star celebration overlay."
            ),
        ),
    ),
    TourStep(
        stepNumber = 4,
        badge = "SCREEN 4: PARENT ZONE",
        title = "Curation, Limits & History",
        description = "Complete parent control: search YouTube, enforce screen-time limits, and review history.",
        icon = Icons.Rounded.Settings,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Search Tab",
                icon = Icons.Rounded.Search,
                actionDescription = "Safe YouTube search with 1-tap duplicate check before adding."
            ),
            ButtonGuideItem(
                buttonName = "History Tab",
                icon = Icons.Rounded.History,
                actionDescription = "Real-time audit log of videos added, watched, and searched."
            ),
            ButtonGuideItem(
                buttonName = "Profiles Tab",
                icon = Icons.Rounded.Group,
                actionDescription = "Create or edit children's names, avatars, and 4-digit PINs."
            ),
            ButtonGuideItem(
                buttonName = "Kids Mode Button",
                icon = Icons.Rounded.ChildCare,
                actionDescription = "Instant 1-tap return back to child player."
            ),
        ),
    ),
)

@Composable
fun OnboardingTourDialog(
    onDismiss: () -> Unit,
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = TOUR_STEPS[currentStepIndex]
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val compactLandscape = ResponsiveLayoutPolicy.isCompactLandscape(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(if (compactLandscape) 0.96f else 0.94f)
                .fillMaxHeight(if (compactLandscape) 0.98f else 0.88f)
                .padding(vertical = if (compactLandscape) 4.dp else 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (compactLandscape) 12.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Header: Step Indicator & Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "Page ${step.stepNumber} of ${TOUR_STEPS.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Close Guide",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(if (compactLandscape) 4.dp else 10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Animated Slide Content
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState.stepNumber > initialState.stepNumber) {
                                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                            } else {
                                slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                            }
                        },
                        label = "TourSlideAnimation",
                    ) { targetStep ->
                        if (compactLandscape) {
                            // Two-column landscape layout: Left = step info, Right = control buttons guide
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.42f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .border(1.5.dp, BrandPrimary.copy(alpha = 0.35f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = targetStep.icon,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = targetStep.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimary,
                                        letterSpacing = 0.6.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = targetStep.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = targetStep.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(0.58f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "CONTROLS GUIDE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    targetStep.buttonGuides.forEach { guide ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = BrandPrimary,
                                                    modifier = Modifier.size(24.dp),
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = guide.icon,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp),
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = guide.buttonName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    Text(
                                                        text = guide.actionDescription,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 13.sp,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            // Glowing Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.5.dp, BrandPrimary.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = targetStep.icon,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(30.dp),
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = targetStep.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandPrimary,
                                letterSpacing = 0.8.sp,
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = targetStep.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = targetStep.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                            )

                            Spacer(Modifier.height(14.dp))

                            // Highlighted Buttons Guide Section
                            Text(
                                text = "BUTTONS & CONTROLS GUIDE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                targetStep.buttonGuides.forEach { guide ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            // Highlighted Button Badge
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = BrandPrimary,
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = guide.icon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = guide.buttonName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = guide.actionDescription,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 15.sp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        } // end non-compactLandscape else
                    }
                }

                Spacer(Modifier.height(if (compactLandscape) 6.dp else 14.dp))

                // Step Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TOUR_STEPS.indices.forEach { index ->
                        val isSelected = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 20.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }

                Spacer(Modifier.height(if (compactLandscape) 6.dp else 14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Previous", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < TOUR_STEPS.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(if (currentStepIndex > 0) 1f else 2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(
                            text = if (currentStepIndex == TOUR_STEPS.size - 1) "Got It!" else "Next Screen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}
