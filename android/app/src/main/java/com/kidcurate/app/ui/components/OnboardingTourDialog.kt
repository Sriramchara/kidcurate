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
        badge = "STEP 1: PARENT SIGN-IN & INDEPENDENT LOCK",
        title = "Google + Gmail OTP & Parent Lock",
        description = "First-time setup verifies your Gmail with a 6-digit OTP, sends a Welcome Note, and lets you choose your own Parent Lock.",
        icon = Icons.Rounded.VerifiedUser,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Parents Shield & Create First Profile",
                icon = Icons.Rounded.Shield,
                actionDescription = "Tap 'Parents' (top right) or 'Create First Profile' to open the Parent Gate."
            ),
            ButtonGuideItem(
                buttonName = "Continue with Google -> 6-Digit Gmail OTP",
                icon = Icons.Rounded.MarkEmailRead,
                actionDescription = "Select your Google account and enter the 6-digit verification code sent to your Gmail inbox."
            ),
            ButtonGuideItem(
                buttonName = "Choose Password, Pattern, or 4-Digit PIN",
                icon = Icons.Rounded.Pattern,
                actionDescription = "Set an independent Parent Password, 3x3 Pattern, or PIN separate from your phone lockscreen."
            ),
            ButtonGuideItem(
                buttonName = "Forgot Lock? Reset with Gmail OTP",
                icon = Icons.Rounded.LockReset,
                actionDescription = "Recover and reset your Parent Lock at any time using a 6-digit code sent to your Gmail."
            ),
        ),
    ),
    TourStep(
        stepNumber = 2,
        badge = "STEP 2: WHO'S WATCHING & CHILD PIN PAD",
        title = "Instant Play or Child PIN",
        description = "Children tap their avatar to start watching immediately, or enter an optional 4-digit Child PIN if enabled in Security.",
        icon = Icons.Rounded.Face,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Child Avatar Card (Play Button)",
                icon = Icons.Rounded.PlayArrow,
                actionDescription = "Opens the child's curated playlist immediately (or opens the 4-digit Child PIN pad when required)."
            ),
            ButtonGuideItem(
                buttonName = "Show PIN / Hide PIN Pill",
                icon = Icons.Rounded.Visibility,
                actionDescription = "Reveals or hides entered digits on the Child PIN pad so typos are easy to spot."
            ),
            ButtonGuideItem(
                buttonName = "Backspace Button",
                icon = Icons.AutoMirrored.Rounded.Backspace,
                actionDescription = "Deletes PIN digits one by one without clearing the entire entry."
            ),
            ButtonGuideItem(
                buttonName = "Forgot PIN? (Parent Unlock)",
                icon = Icons.Rounded.LockOpen,
                actionDescription = "Lets a parent unlock the child's playlist directly using the Parent Password, Pattern, or PIN."
            ),
        ),
    ),
    TourStep(
        stepNumber = 3,
        badge = "STEP 3: TODAY'S CURATED PLAYLIST",
        title = "Distraction-Free Daily Queue",
        description = "Only the videos you approved appear here — zero algorithmic recommendations, zero comments, and zero external links.",
        icon = Icons.Rounded.VideoLibrary,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Play All Button",
                icon = Icons.Rounded.PlayArrow,
                actionDescription = "Starts today's approved videos in sequence from the first video."
            ),
            ButtonGuideItem(
                buttonName = "Lock / Unlock (Child Lock)",
                icon = Icons.Rounded.Lock,
                actionDescription = "Pins the screen and locks navigation so children cannot exit or switch profiles without your Parent Lock."
            ),
            ButtonGuideItem(
                buttonName = "Video Cards & Progress Badges",
                icon = Icons.Rounded.SmartDisplay,
                actionDescription = "Tap any card in the queue to play that video and view its duration and completion badge."
            ),
            ButtonGuideItem(
                buttonName = "Back / Switch Profile",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                actionDescription = "Returns to 'Who's Watching?' to switch between sibling profiles (protected when Child Lock is active)."
            ),
        ),
    ),
    TourStep(
        stepNumber = 4,
        badge = "STEP 4: SAFE VIDEO PLAYER & FULLSCREEN LOCK",
        title = "Locked Playback & Karaoke Phonics",
        description = "Works in both portrait and edge-to-edge landscape fullscreen with strict Parent Lock protection.",
        icon = Icons.Rounded.SmartDisplay,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Padlock Button (Portrait & Landscape)",
                icon = Icons.Rounded.Lock,
                actionDescription = "Locks all player controls and timeline scrubbing. Unlocking requires your Parent Password, Pattern, or PIN."
            ),
            ButtonGuideItem(
                buttonName = "Left Back Arrow (While Locked)",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                actionDescription = "Even in landscape fullscreen, tapping the back arrow while locked prompts for your Parent Lock before exiting."
            ),
            ButtonGuideItem(
                buttonName = "CC ON / OFF (Karaoke Phonics)",
                icon = Icons.Rounded.ClosedCaption,
                actionDescription = "Toggles Early Reader subtitles with large bold text and synchronized golden word highlights."
            ),
            ButtonGuideItem(
                buttonName = "Fullscreen & Next Video Buttons",
                icon = Icons.Rounded.SkipNext,
                actionDescription = "Rotates between portrait and landscape fullscreen or advances to the next approved video."
            ),
        ),
    ),
    TourStep(
        stepNumber = 5,
        badge = "STEP 5: PARENT ZONE (ALL 5 TABS)",
        title = "Complete Curation & Safety Controls",
        description = "Manage child profiles, curate YouTube videos, set screen-time limits, and customize your security preferences.",
        icon = Icons.Rounded.Settings,
        buttonGuides = listOf(
            ButtonGuideItem(
                buttonName = "Profiles & Search Tabs",
                icon = Icons.Rounded.Search,
                actionDescription = "Create child profiles with avatars, search safe educational videos, or paste any YouTube URL directly."
            ),
            ButtonGuideItem(
                buttonName = "Videos & History Tabs",
                icon = Icons.Rounded.History,
                actionDescription = "Apply 1-tap Activity Packs, reorder videos with Up/Down arrows, and review Watch & Curation History."
            ),
            ButtonGuideItem(
                buttonName = "Security Tab: Lock, Limits & Curfew",
                icon = Icons.Rounded.Timer,
                actionDescription = "Switch between Password, Pattern, or PIN, toggle Fingerprint unlock, and set Daily Screen Time & Bedtime Curfew."
            ),
            ButtonGuideItem(
                buttonName = "Kids Mode Button (Top Right)",
                icon = Icons.Rounded.ChildCare,
                actionDescription = "Exits Parent Zone in 1 tap and returns safely to the child profile screen."
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
