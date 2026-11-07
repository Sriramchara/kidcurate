package com.kidcurate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.kidcurate.app.data.ApiClient
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.theme.*

/**
 * Clean, thematic character avatar without raw emojis.
 * Matches child names and character badges to dedicated vibrant vector themes.
 */
@Composable
fun ThematicAvatar(
    name: String,
    avatar: String = "",
    size: Dp = 64.dp,
    iconSize: Dp = 32.dp,
    modifier: Modifier = Modifier,
) {
    val (icon, bgColors, ringColor) = remember(name, avatar) {
        val combined = (name.trim().lowercase() + " " + avatar.trim().lowercase()).trim()
        when {
            combined.contains("aarav") || combined.contains("🦁") || combined.contains("compass") || combined.contains("explorer") ->
                Triple(Icons.Rounded.Explore, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), BrandAccent)
            combined.contains("maya") || combined.contains("🦄") || combined.contains("sparkle") || combined.contains("magic") || combined.contains("🌟") ->
                Triple(Icons.Rounded.AutoAwesome, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), BrandViolet)
            combined.contains("leo") || combined.contains("🚀") || combined.contains("rocket") || combined.contains("space") ->
                Triple(Icons.Rounded.RocketLaunch, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), BrandSky)
            combined.contains("🐬") || combined.contains("ocean") || combined.contains("waves") ->
                Triple(Icons.Rounded.Waves, listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)), Color(0xFF0D9488))
            combined.contains("🦊") || combined.contains("flame") || combined.contains("blaze") ->
                Triple(Icons.Rounded.LocalFireDepartment, listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C))
            combined.contains("🐘") || combined.contains("shield") || combined.contains("guardian") ->
                Triple(Icons.Rounded.Shield, listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A))
            combined.contains("🦋") || combined.contains("palette") || combined.contains("artist") ->
                Triple(Icons.Rounded.Palette, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
            combined.contains("heart") || combined.contains("love") || combined.contains("kind") ->
                Triple(Icons.Rounded.Favorite, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
            combined.contains("star") ->
                Triple(Icons.Rounded.Star, listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)), Color(0xFFCA8A04))
            else ->
                Triple(Icons.Rounded.SentimentSatisfiedAlt, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), BrandAccent)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(bgColors))
            .border(2.5.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = ringColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * Stylized feature badge with vector icons.
 */
@Composable
fun ThematicPill(
    icon: ImageVector,
    label: String,
    tint: Color = BrandPrimary,
    background: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val effectiveBackground = if (background != Color.Unspecified) background else MaterialTheme.colorScheme.primaryContainer
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = effectiveBackground,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
    }
}

/**
 * Ambient Glassmorphic Aurora Mesh background for Compose screens.
 * Places soft, glowing ambient orbs (violet, amber, sky) behind translucent glass cards.
 */
@Composable
fun GlassBackgroundAura(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Top-left Violet / Indigo Ambient Orb
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandPrimary.copy(alpha = 0.32f),
                            BrandPrimary.copy(alpha = 0.08f),
                            Color.Transparent,
                        )
                    )
                )
        )
        // Top-right Warm Amber Ambient Orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandAccent.copy(alpha = 0.25f),
                            BrandAccent.copy(alpha = 0.05f),
                            Color.Transparent,
                        )
                    )
                )
        )
        // Bottom-center Cyan Ambient Orb
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandSky.copy(alpha = 0.22f),
                            BrandSky.copy(alpha = 0.04f),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

/**
 * Premium Frosted Glass Card container.
 * Features translucent fill, specular gradient border, and soft elevation shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    borderAlpha: Float = 0.25f,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBg = if (isDark) {
        Color(0xFF111827).copy(alpha = 0.72f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.88f)
    }

    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = 0.06f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFCBD5E1).copy(alpha = 0.85f),
                Color(0xFFE2E8F0).copy(alpha = 0.5f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 12.dp else 6.dp,
                shape = shape,
                spotColor = if (isDark) BrandPrimary.copy(alpha = 0.25f) else Color(0xFF64748B).copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(glassBg)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape),
        content = content
    )
}

/**
 * Parent Verification Dialog (Password Gate)
 * Allows parents to unlock child kiosk mode, exit, or manage playlists using their parent account password.
 */
@Composable
fun ParentPasswordDialog(
    title: String = "Parent Gate",
    subtitle: String = "Enter parent account password to continue",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val parentPrefs = remember { ParentPreferences(context) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var parentNameInput by remember { mutableStateOf("") }
    var parentEmailInput by remember { mutableStateOf(parentPrefs.parentEmail) }
    var parentPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Parent Gate",
                        tint = BrandPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = if (isRegisterMode) "Create Parent Account" else title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = if (isRegisterMode) "Set up your parent account to manage your children" else subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                )

                if (parentPrefs.parentEmail.isBlank()) {
                    // Sign In vs Create Account Toggle Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isRegisterMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    isRegisterMode = false
                                    errorMessage = null
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Sign In",
                                fontSize = 12.sp,
                                fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isRegisterMode) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRegisterMode) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    isRegisterMode = true
                                    errorMessage = null
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Create Account",
                                fontSize = 12.sp,
                                fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRegisterMode) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = parentNameInput,
                            onValueChange = {
                                parentNameInput = it
                                errorMessage = null
                            },
                            label = { Text("Parent Name") },
                            placeholder = { Text("e.g. Sriram") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = parentEmailInput,
                        onValueChange = {
                            parentEmailInput = it
                            errorMessage = null
                        },
                        label = { Text("Parent Account Email") },
                        placeholder = { Text("Enter account email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = parentPasswordInput,
                    onValueChange = {
                        parentPasswordInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isRegisterMode) "Create Password" else "Parent Account Password") },
                    placeholder = { Text(if (isRegisterMode) "At least 6 characters" else "Enter password") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (parentPrefs.parentEmail.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                parentPrefs.parentEmail = ""
                                parentPrefs.parentAuthToken = ""
                                ApiClient.authToken = ""
                                parentEmailInput = ""
                                isRegisterMode = false
                                errorMessage = null
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Text("Switch Account", fontSize = 11.sp, color = BrandPrimary)
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = BrandCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val emailToVerify = if (parentPrefs.parentEmail.isNotBlank()) {
                                parentPrefs.parentEmail.trim()
                            } else {
                                parentEmailInput.trim()
                            }

                            if (isRegisterMode && parentNameInput.isBlank()) {
                                errorMessage = "Please enter your name."
                                return@Button
                            }
                            if (emailToVerify.isBlank()) {
                                errorMessage = "Please enter parent email."
                                return@Button
                            }
                            if (parentPasswordInput.isBlank()) {
                                errorMessage = "Please enter password."
                                return@Button
                            }
                            if (isRegisterMode && parentPasswordInput.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@Button
                            }

                            isVerifying = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    if (isRegisterMode) {
                                        // Register new parent account
                                        val resp = ApiClient.api.registerParent(
                                            mapOf(
                                                "name" to parentNameInput.trim(),
                                                "email" to emailToVerify,
                                                "password" to parentPasswordInput.trim(),
                                            )
                                        )
                                        val token = resp["access_token"] as? String ?: ""
                                        if (token.isNotBlank()) {
                                            parentPrefs.parentAuthToken = token
                                            parentPrefs.parentEmail = emailToVerify
                                            ApiClient.authToken = token
                                            isVerifying = false
                                            onSuccess()
                                        } else {
                                            isVerifying = false
                                            errorMessage = "Registration completed. Please sign in."
                                        }
                                    } else {
                                        // Verify existing parent password
                                        val resp = ApiClient.api.verifyParentPassword(
                                            mapOf("email" to emailToVerify, "password" to parentPasswordInput.trim())
                                        )
                                        if (parentPrefs.parentEmail.isBlank()) {
                                            parentPrefs.parentEmail = emailToVerify
                                        }
                                        val token = resp["access_token"] as? String ?: ""
                                        if (token.isNotBlank()) {
                                            parentPrefs.parentAuthToken = token
                                            ApiClient.authToken = token
                                        }
                                        isVerifying = false
                                        onSuccess()
                                    }
                                } catch (e: HttpException) {
                                    isVerifying = false
                                    if (e.code() == 400) {
                                        errorMessage = "Email already registered. Switch to Sign In."
                                    } else if (e.code() == 401) {
                                        errorMessage = "Incorrect parent password. Please try again."
                                    } else {
                                        errorMessage = "Request failed (HTTP ${e.code()})."
                                    }
                                } catch (e: Exception) {
                                    isVerifying = false
                                    errorMessage = e.localizedMessage ?: "Unable to connect to server."
                                }
                            }
                        },
                        enabled = !isVerifying && parentPasswordInput.isNotBlank(),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(
                            if (isVerifying) "Processing..."
                            else if (isRegisterMode) "Create Account"
                            else "Unlock"
                        )
                    }
                }
            }
        }
    }
}

