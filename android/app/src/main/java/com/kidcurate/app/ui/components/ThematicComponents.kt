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
import androidx.compose.material3.HorizontalDivider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
        val rawAvatar = avatar.trim().lowercase()
        val rawName = name.trim().lowercase()
        when {
            rawAvatar.contains("compass") || rawAvatar.contains("explorer") || rawAvatar.contains("🦁") || rawAvatar.contains("🧭") ->
                Triple(Icons.Rounded.Explore, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), Color(0xFFD97706))
            rawAvatar.contains("sparkle") || rawAvatar.contains("magic") || rawAvatar.contains("🦄") || rawAvatar.contains("✨") || rawAvatar.contains("🌟") ->
                Triple(Icons.Rounded.AutoAwesome, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF8B5CF6))
            rawAvatar.contains("rocket") || rawAvatar.contains("space") || rawAvatar.contains("🚀") ->
                Triple(Icons.Rounded.RocketLaunch, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), Color(0xFF0284C7))
            rawAvatar.contains("heart") || rawAvatar.contains("love") || rawAvatar.contains("kind") || rawAvatar.contains("❤️") || rawAvatar.contains("💖") ->
                Triple(Icons.Rounded.Favorite, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
            rawAvatar.contains("waves") || rawAvatar.contains("ocean") || rawAvatar.contains("water") || rawAvatar.contains("🐬") || rawAvatar.contains("🌊") ->
                Triple(Icons.Rounded.Waves, listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)), Color(0xFF0D9488))
            rawAvatar.contains("flame") || rawAvatar.contains("fire") || rawAvatar.contains("blaze") || rawAvatar.contains("🦊") || rawAvatar.contains("🔥") ->
                Triple(Icons.Rounded.LocalFireDepartment, listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C))
            rawAvatar.contains("shield") || rawAvatar.contains("guardian") || rawAvatar.contains("safe") || rawAvatar.contains("🐘") || rawAvatar.contains("🛡️") ->
                Triple(Icons.Rounded.Shield, listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A))
            rawAvatar.contains("palette") || rawAvatar.contains("artist") || rawAvatar.contains("paint") || rawAvatar.contains("🦋") || rawAvatar.contains("🎨") ->
                Triple(Icons.Rounded.Palette, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF9333EA))
            rawAvatar.contains("star") || rawAvatar.contains("⭐") ->
                Triple(Icons.Rounded.Star, listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)), Color(0xFFCA8A04))
            rawName.contains("compass") || rawName.contains("explorer") ->
                Triple(Icons.Rounded.Explore, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), Color(0xFFD97706))
            rawName.contains("sparkle") ->
                Triple(Icons.Rounded.AutoAwesome, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF8B5CF6))
            rawName.contains("rocket") ->
                Triple(Icons.Rounded.RocketLaunch, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), Color(0xFF0284C7))
            rawName.contains("heart") ->
                Triple(Icons.Rounded.Favorite, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
            rawName.contains("waves") ->
                Triple(Icons.Rounded.Waves, listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)), Color(0xFF0D9488))
            rawName.contains("flame") ->
                Triple(Icons.Rounded.LocalFireDepartment, listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C))
            rawName.contains("shield") ->
                Triple(Icons.Rounded.Shield, listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A))
            rawName.contains("palette") ->
                Triple(Icons.Rounded.Palette, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF9333EA))
            rawName.contains("star") ->
                Triple(Icons.Rounded.Star, listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)), Color(0xFFCA8A04))
            else -> {
                val hash = kotlin.math.abs(rawName.hashCode()) % 9
                when (hash) {
                    0 -> Triple(Icons.Rounded.RocketLaunch, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), Color(0xFF0284C7))
                    1 -> Triple(Icons.Rounded.AutoAwesome, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF8B5CF6))
                    2 -> Triple(Icons.Rounded.Explore, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), Color(0xFFD97706))
                    3 -> Triple(Icons.Rounded.Favorite, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
                    4 -> Triple(Icons.Rounded.Waves, listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)), Color(0xFF0D9488))
                    5 -> Triple(Icons.Rounded.LocalFireDepartment, listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C))
                    6 -> Triple(Icons.Rounded.Shield, listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A))
                    7 -> Triple(Icons.Rounded.Palette, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF9333EA))
                    else -> Triple(Icons.Rounded.Star, listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)), Color(0xFFCA8A04))
                }
            }
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
enum class ParentGateMode {
    PIN_UNLOCK,
    GOOGLE_LOGIN,
    SET_PIN,
}

/**
 * Parent Verification Dialog (Google-First Identity & PIN Gate)
 * Uses Google Sign-In for parent authentication and identity.
 * Uses 4-digit PIN for daily in-app unlock and child lock gates.
 */
@Composable
fun ParentPasswordDialog(
    title: String = "Parent Verification",
    subtitle: String = "Enter 4-digit Parent PIN",
    allowGoogleSignIn: Boolean = true,
    forceGoogleOnly: Boolean = false,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val appContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val parentPrefs = remember { ParentPreferences(appContext) }

    val initialMode = remember {
        if (forceGoogleOnly || (!parentPrefs.isParentLoggedIn && allowGoogleSignIn)) {
            ParentGateMode.GOOGLE_LOGIN
        } else if (parentPrefs.hasParentPin) {
            ParentGateMode.PIN_UNLOCK
        } else if (!allowGoogleSignIn || parentPrefs.isParentLoggedIn) {
            ParentGateMode.SET_PIN
        } else {
            ParentGateMode.GOOGLE_LOGIN
        }
    }

    var currentMode by remember { mutableStateOf(initialMode) }
    var pinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var showPinText by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isResettingPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account?.email ?: ""
            if (email.isNotBlank()) {
                isVerifying = true
                errorMessage = null
                val extractedName = account.displayName?.trim()?.ifBlank { null }
                    ?: account.givenName?.trim()?.ifBlank { null }
                    ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                parentPrefs.parentName = extractedName
                parentPrefs.parentEmail = email

                coroutineScope.launch {
                    try {
                        val payload = mutableMapOf<String, String>()
                        account.idToken?.let { if (it.isNotBlank()) payload["id_token"] = it }
                        payload["email"] = email
                        payload["name"] = extractedName
                        val resp = ApiClient.api.googleLogin(payload)
                        val token = resp["access_token"] as? String ?: ""
                        val refreshToken = resp["refresh_token"] as? String ?: ""
                        val serverName = (resp["name"] as? String)?.trim() ?: ""
                        if (serverName.isNotBlank() && serverName != "Parent") {
                            parentPrefs.parentName = serverName
                        }
                        if (token.isNotBlank()) {
                            parentPrefs.parentAuthToken = token
                            if (refreshToken.isNotBlank()) {
                                parentPrefs.parentRefreshToken = refreshToken
                            }
                            ApiClient.authToken = token
                            isVerifying = false
                            if (isResettingPin || !parentPrefs.hasParentPin) {
                                parentPrefs.parentPin = ""
                                newPinInput = ""
                                confirmPinInput = ""
                                isResettingPin = false
                                currentMode = ParentGateMode.SET_PIN
                            } else {
                                onSuccess()
                            }
                        } else {
                            isVerifying = false
                            errorMessage = "Server did not issue a session token."
                        }
                    } catch (e: Exception) {
                        isVerifying = false
                        errorMessage = "Login error: ${e.message}"
                    }
                }
            } else {
                errorMessage = "Google sign-in was cancelled."
            }
        } catch (e: ApiException) {
            errorMessage = "Google Sign-In failed (${e.statusCode}): ${e.message}"
        }
    }

    fun triggerGoogleSignIn() {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        val clientId = parentPrefs.googleClientId.trim()
        if (clientId.isNotBlank()) {
            gsoBuilder.requestIdToken(clientId)
        }
        val client = GoogleSignIn.getClient(appContext, gsoBuilder.build())
        client.signOut().addOnCompleteListener {
            googleLauncher.launch(client.signInIntent)
        }
    }

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
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when (currentMode) {
                            ParentGateMode.PIN_UNLOCK -> Icons.Rounded.Lock
                            ParentGateMode.SET_PIN -> Icons.Rounded.LockReset
                            ParentGateMode.GOOGLE_LOGIN -> Icons.Rounded.AccountCircle
                        },
                        contentDescription = "Parent Gate",
                        tint = BrandPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Title and Subtitle based on state
                Text(
                    text = when (currentMode) {
                        ParentGateMode.PIN_UNLOCK -> title
                        ParentGateMode.SET_PIN -> "Create 4-Digit In-App PIN"
                        ParentGateMode.GOOGLE_LOGIN -> if (isResettingPin) "Reset Parent PIN" else "Parent Account Sign In"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = when (currentMode) {
                        ParentGateMode.PIN_UNLOCK -> subtitle
                        ParentGateMode.SET_PIN -> "Choose a 4-digit PIN for quick daily in-app unlock and child safety."
                        ParentGateMode.GOOGLE_LOGIN -> if (isResettingPin) {
                            "Sign in with your Google account to verify your identity and reset your 4-digit PIN."
                        } else {
                            "Sign in with your Google account to set up child profiles and safe playlists."
                        }
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )

                // Mode-specific content
                when (currentMode) {
                    ParentGateMode.PIN_UNLOCK -> {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pinInput = it
                                    errorMessage = null
                                    if (it.length == 4) {
                                        if (it == parentPrefs.parentPin) {
                                            onSuccess()
                                        } else {
                                            errorMessage = "Incorrect 4-digit PIN."
                                        }
                                    }
                                }
                            },
                            label = { Text("Parent PIN") },
                            placeholder = { Text("••••") },
                            visualTransformation = if (showPinText) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPinText = !showPinText }) {
                                    Icon(
                                        if (showPinText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPinText) "Hide PIN" else "Show PIN",
                                        tint = if (showPinText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )

                        if (allowGoogleSignIn) {
                            Spacer(Modifier.height(10.dp))
                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    isResettingPin = true
                                    currentMode = ParentGateMode.GOOGLE_LOGIN
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Forgot PIN? Verify with Google",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary,
                                )
                            }
                        }
                    }

                    ParentGateMode.SET_PIN -> {
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                    newPinInput = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("New 4-digit PIN") },
                            placeholder = { Text("••••") },
                            visualTransformation = if (showPinText) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPinText = !showPinText }) {
                                    Icon(
                                        if (showPinText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPinText) "Hide PIN" else "Show PIN",
                                        tint = if (showPinText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                    confirmPinInput = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("Confirm 4-digit PIN") },
                            placeholder = { Text("••••") },
                            visualTransformation = if (showPinText) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPinText = !showPinText }) {
                                    Icon(
                                        if (showPinText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPinText) "Hide PIN" else "Show PIN",
                                        tint = if (showPinText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    ParentGateMode.GOOGLE_LOGIN -> {
                        Button(
                            onClick = {
                                errorMessage = null
                                triggerGoogleSignIn()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            enabled = !isVerifying,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Verifying with Google...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.AccountCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        }

                        if (isResettingPin) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    isResettingPin = false
                                    errorMessage = null
                                    currentMode = ParentGateMode.PIN_UNLOCK
                                }
                            ) {
                                Text(
                                    text = "Back to PIN",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
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

                // Bottom Action Buttons
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

                    when (currentMode) {
                        ParentGateMode.PIN_UNLOCK -> {
                            Button(
                                onClick = {
                                    if (pinInput == parentPrefs.parentPin) {
                                        onSuccess()
                                    } else {
                                        errorMessage = "Incorrect 4-digit PIN."
                                    }
                                },
                                enabled = pinInput.length == 4,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text("Unlock")
                            }
                        }

                        ParentGateMode.SET_PIN -> {
                            Button(
                                onClick = {
                                    if (newPinInput.length != 4) {
                                        errorMessage = "PIN must be exactly 4 digits."
                                    } else if (newPinInput != confirmPinInput) {
                                        errorMessage = "PINs do not match."
                                    } else {
                                        parentPrefs.parentPin = newPinInput
                                        onSuccess()
                                    }
                                },
                                enabled = newPinInput.length == 4 && confirmPinInput.length == 4,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text("Save PIN")
                            }
                        }

                        ParentGateMode.GOOGLE_LOGIN -> {
                            // Cancel already covers dismissing
                        }
                    }
                }
            }
        }
    }
}

