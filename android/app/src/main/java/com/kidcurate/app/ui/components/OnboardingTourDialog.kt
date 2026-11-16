package com.kidcurate.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.kidcurate.app.ui.theme.*

data class TourStep(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlights: List<String>,
)

val TOUR_STEPS = listOf(
    TourStep(
        stepNumber = 1,
        badge = "WELCOME TO KIDCURATE",
        title = "Safe Learning for Kids",
        description = "Transform YouTube into an ad-free, wholesome learning environment. Children can only explore videos and playlists that you personally approve.",
        icon = Icons.Rounded.SmartDisplay,
        highlights = listOf(
            "Zero random autoplay or comments",
            "Strict child safety pre-filtering",
            "Wholesome educational topics",
        ),
    ),
    TourStep(
        stepNumber = 2,
        badge = "STEP 1: PROFILES",
        title = "Personalized Child Profiles",
        description = "Create individual profiles for each child with age-appropriate filtering, customized space avatars, and their own 4-digit login PIN.",
        icon = Icons.Rounded.Face,
        highlights = listOf(
            "Individual avatars and names",
            "Custom child 4-digit PIN",
            "Age-targeted video recommendations",
        ),
    ),
    TourStep(
        stepNumber = 3,
        badge = "STEP 2: CURATION",
        title = "Parent Curated Playlists",
        description = "Search YouTube with live suggestions or paste ANY direct YouTube video link. Videos are automatically verified before adding.",
        icon = Icons.Rounded.VideoLibrary,
        highlights = listOf(
            "Real-time type-ahead search suggestions",
            "Direct YouTube link pasting",
            "One-click 100% duplicate prevention",
        ),
    ),
    TourStep(
        stepNumber = 4,
        badge = "STEP 3: SECURITY & LIMITS",
        title = "Screen Time & Kiosk Lock",
        description = "Enforce daily watch timers and bedtime curfews. Kiosk lock prevents kids from exiting or altering settings without your Parent PIN.",
        icon = Icons.Rounded.Timer,
        highlights = listOf(
            "Customizable daily screen limits",
            "Automatic bedtime curfew blackout",
            "Fast 1-second Parent PIN unlock",
        ),
    ),
)

@Composable
fun OnboardingTourDialog(
    onDismiss: () -> Unit,
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = TOUR_STEPS[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                            text = "Step ${step.stepNumber} of ${TOUR_STEPS.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Skip Tour",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Large Glowing Icon Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.5.dp, BrandPrimary.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = targetStep.icon,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(36.dp),
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = targetStep.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandPrimary,
                            letterSpacing = 1.sp,
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = targetStep.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = targetStep.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                        )

                        Spacer(Modifier.height(16.dp))

                        // Highlight checklist cards
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                targetStep.highlights.forEach { highlight ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandSuccess,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = highlight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Step Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TOUR_STEPS.indices.forEach { index ->
                        val isSelected = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 22.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

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
                            Text("Back", fontWeight = FontWeight.SemiBold)
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
                            text = if (currentStepIndex == TOUR_STEPS.size - 1) "Get Started" else "Next",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
