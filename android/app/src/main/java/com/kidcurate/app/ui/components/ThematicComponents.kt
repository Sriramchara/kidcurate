package com.kidcurate.app.ui.components

import android.app.Activity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.kidcurate.app.data.ApiClient
import com.kidcurate.app.data.ParentBiometricAuth
import androidx.fragment.app.FragmentActivity
import com.kidcurate.app.data.GoogleCredentialAuth
import com.kidcurate.app.data.ParentLockPolicy
import com.kidcurate.app.data.ParentLockType
import com.kidcurate.app.data.ParentSessionPolicy
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.theme.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.BorderStroke

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
            rawAvatar.contains("compass") || rawAvatar.contains("explorer") || rawAvatar.contains("lion") ->
                Triple(Icons.Rounded.Explore, listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), Color(0xFFD97706))
            rawAvatar.contains("sparkle") || rawAvatar.contains("magic") || rawAvatar.contains("unicorn") ->
                Triple(Icons.Rounded.AutoAwesome, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF8B5CF6))
            rawAvatar.contains("rocket") || rawAvatar.contains("space") ->
                Triple(Icons.Rounded.RocketLaunch, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), Color(0xFF0284C7))
            rawAvatar.contains("heart") || rawAvatar.contains("love") || rawAvatar.contains("kind") ->
                Triple(Icons.Rounded.Favorite, listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3)), Color(0xFFE11D48))
            rawAvatar.contains("waves") || rawAvatar.contains("ocean") || rawAvatar.contains("water") || rawAvatar.contains("dolphin") ->
                Triple(Icons.Rounded.Waves, listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)), Color(0xFF0D9488))
            rawAvatar.contains("flame") || rawAvatar.contains("fire") || rawAvatar.contains("blaze") || rawAvatar.contains("fox") ->
                Triple(Icons.Rounded.LocalFireDepartment, listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C))
            rawAvatar.contains("shield") || rawAvatar.contains("guardian") || rawAvatar.contains("safe") || rawAvatar.contains("elephant") ->
                Triple(Icons.Rounded.Shield, listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A))
            rawAvatar.contains("palette") || rawAvatar.contains("artist") || rawAvatar.contains("paint") || rawAvatar.contains("butterfly") ->
                Triple(Icons.Rounded.Palette, listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)), Color(0xFF9333EA))
            rawAvatar.contains("star") ->
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
 * Parent Verification Dialog (Independent Parent Lock Gate)
 * Supports parent-chosen independent lock styles (4-Digit PIN, 3x3 Pattern, or Password)
 * strictly decoupled from the mobile phone's device lockscreen credential.
 */
enum class ParentGateMode {
    CREDENTIAL_UNLOCK,
    GOOGLE_LOGIN,
    BIOMETRIC_VERIFY,
    SET_CREDENTIAL,
}

/**
 * Parent Verification Dialog (Google-First Identity & Independent Parent Lock Gate)
 * Uses Google Sign-In and optional hardware-only biometrics (never device lockscreen PIN)
 * for parent identity verification, and parent-chosen PIN / Pattern / Password for daily gates.
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
    val parentPrefs = remember { ParentPreferences.get(appContext) }
    val activeLockType = remember { parentPrefs.parentLockType }
    val isHardwareBiometricAvailable = remember {
        ParentBiometricAuth.isHardwareBiometricAvailable(appContext)
    }

    val effectiveUnlockSubtitle = remember(subtitle, activeLockType) {
        when (activeLockType) {
            ParentLockType.PIN -> subtitle
            ParentLockType.PATTERN -> subtitle
                .replace("Enter 4-digit Parent PIN", "Draw your Parent Pattern", ignoreCase = true)
                .replace("4-digit Parent PIN", "Parent Pattern", ignoreCase = true)
                .replace("Parent PIN", "Parent Pattern", ignoreCase = true)
            ParentLockType.PASSWORD -> subtitle
                .replace("Enter 4-digit Parent PIN", "Enter your Parent Password", ignoreCase = true)
                .replace("4-digit Parent PIN", "Parent Password", ignoreCase = true)
                .replace("Parent PIN", "Parent Password", ignoreCase = true)
        }
    }

    val initialMode = remember {
        val effectiveAllowGoogle = allowGoogleSignIn || !parentPrefs.hasParentPin
        if (forceGoogleOnly || (!parentPrefs.isParentLoggedIn && effectiveAllowGoogle)) {
            ParentGateMode.GOOGLE_LOGIN
        } else if (ParentSessionPolicy.canReusePin(
                hasPin = parentPrefs.hasParentPin,
                pinOwnerEmail = parentPrefs.parentPinOwnerEmail,
                currentAccountEmail = parentPrefs.parentEmail,
                signingInEmail = parentPrefs.parentEmail,
            )) {
            ParentGateMode.CREDENTIAL_UNLOCK
        } else if (isHardwareBiometricAvailable) {
            // Never let unauthenticated children see SET_CREDENTIAL directly.
            // Require hardware-only biometric (fingerprint/face) or Google sign-in first.
            ParentGateMode.BIOMETRIC_VERIFY
        } else if (effectiveAllowGoogle) {
            ParentGateMode.GOOGLE_LOGIN
        } else {
            ParentGateMode.SET_CREDENTIAL
        }
    }

    var currentMode by remember { mutableStateOf(initialMode) }
    var pinInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedSetupType by remember { mutableStateOf(parentPrefs.parentLockType) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var draftPattern by remember { mutableStateOf<List<Int>?>(null) }
    var patternError by remember { mutableStateOf(false) }
    var patternResetKey by remember { mutableIntStateOf(0) }
    var showSecretText by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isResettingCredential by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutRemainingSeconds by remember { mutableIntStateOf(parentPrefs.getPinLockoutRemainingSeconds()) }

    LaunchedEffect(lockoutRemainingSeconds) {
        if (lockoutRemainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            lockoutRemainingSeconds--
            if (lockoutRemainingSeconds == 0 && errorMessage?.contains("Locked for") == true) {
                errorMessage = null
            }
        }
    }

    fun handleFailedCredential(label: String) {
        val remainingSec = parentPrefs.getPinLockoutRemainingSeconds()
        if (remainingSec > 0) {
            lockoutRemainingSeconds = remainingSec
            errorMessage = "Too many failed attempts. Locked for ${remainingSec}s."
        } else {
            val attempts = parentPrefs.getFailedPinAttempts()
            val rem = (5 - attempts).coerceAtLeast(0)
            errorMessage = if (rem > 0) "Incorrect $label. $rem attempt(s) remaining." else "Incorrect $label."
        }
    }

    val hostActivity = appContext as? android.app.Activity
    if (com.kidcurate.app.BuildConfig.DEBUG) {
        val autoWalkthrough = hostActivity?.intent?.getStringExtra("test_auto_walkthrough")
        if (autoWalkthrough == "child_lock_check" && currentMode == ParentGateMode.CREDENTIAL_UNLOCK) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1200)
                pinInput = "9999"
                errorMessage = "Incorrect 4-digit PIN."
                kotlinx.coroutines.delay(2200)
                pinInput = "1234"
                errorMessage = null
                kotlinx.coroutines.delay(800)
                onSuccess()
            }
        }
    }

    fun triggerGoogleSignIn() {
        val activity = appContext as? Activity
            ?: (appContext as? android.content.ContextWrapper)?.baseContext as? Activity
            ?: return
        coroutineScope.launch {
            isVerifying = true
            errorMessage = null
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
                val token = resp["access_token"] as? String ?: ""
                val refreshToken = resp["refresh_token"] as? String ?: ""
                val serverName = (resp["name"] as? String)?.trim() ?: ""
                if (serverName.isNotBlank() && serverName != "Parent") {
                    parentPrefs.parentName = serverName
                }
                if (token.isBlank()) {
                    errorMessage = "Server did not issue a session token."
                    return@launch
                }

                parentPrefs.parentAuthToken = token
                parentPrefs.parentRefreshToken = refreshToken
                ApiClient.authToken = token
                if (isResettingCredential || !savedPinMatchesAccount) {
                    parentPrefs.parentPin = ""
                    parentPrefs.parentPinOwnerEmail = ""
                    newPinInput = ""
                    confirmPinInput = ""
                    newPasswordInput = ""
                    confirmPasswordInput = ""
                    draftPattern = null
                    patternError = false
                    patternResetKey++
                    isResettingCredential = false
                    currentMode = ParentGateMode.SET_CREDENTIAL
                } else {
                    parentPrefs.parentPinOwnerEmail = email
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage = GoogleCredentialAuth.userMessage(e)
            } finally {
                isVerifying = false
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val compactHeight = ResponsiveLayoutPolicy.isCompactLandscape(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.56f else 0.92f)
                .widthIn(max = 520.dp)
                .padding(if (compactHeight) 8.dp else 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = (configuration.screenHeightDp * 0.9f).dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(if (compactHeight) 12.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(if (compactHeight) 40.dp else 56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when (currentMode) {
                            ParentGateMode.CREDENTIAL_UNLOCK ->
                                if (activeLockType == ParentLockType.PATTERN) Icons.Rounded.Pattern else Icons.Rounded.Lock
                            ParentGateMode.SET_CREDENTIAL -> Icons.Rounded.LockReset
                            ParentGateMode.BIOMETRIC_VERIFY -> Icons.Rounded.Fingerprint
                            ParentGateMode.GOOGLE_LOGIN -> Icons.Rounded.AccountCircle
                        },
                        contentDescription = "Parent Gate",
                        tint = BrandPrimary,
                        modifier = Modifier.size(if (compactHeight) 22.dp else 28.dp),
                    )
                }

                Spacer(Modifier.height(if (compactHeight) 6.dp else 14.dp))

                // Title and Subtitle based on state
                Text(
                    text = when (currentMode) {
                        ParentGateMode.CREDENTIAL_UNLOCK -> title
                        ParentGateMode.SET_CREDENTIAL -> "Create Independent Parent Lock"
                        ParentGateMode.BIOMETRIC_VERIFY -> "Parent Biometric Verification"
                        ParentGateMode.GOOGLE_LOGIN -> if (isResettingCredential) "Reset Parent Lock" else "Parent Account Sign In"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = when (currentMode) {
                        ParentGateMode.CREDENTIAL_UNLOCK -> effectiveUnlockSubtitle
                        ParentGateMode.SET_CREDENTIAL -> "Choose a Pattern, PIN, or Password separate from your phone's lockscreen."
                        ParentGateMode.BIOMETRIC_VERIFY -> "Confirm your fingerprint or face ID to verify you are the parent."
                        ParentGateMode.GOOGLE_LOGIN -> if (isResettingCredential) {
                            "Sign in with your Google account to verify your identity and reset your Parent Lock."
                        } else {
                            "Sign in with your Google account to set up child profiles and safe playlists."
                        }
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = if (compactHeight) 2.dp else 4.dp,
                        bottom = if (compactHeight) 8.dp else 16.dp,
                    ),
                )

                // Mode-specific content
                when (currentMode) {
                    ParentGateMode.CREDENTIAL_UNLOCK -> {
                        when (activeLockType) {
                            ParentLockType.PATTERN -> {
                                PatternLockView(
                                    maxSize = if (compactHeight) 180.dp else 230.dp,
                                    enabled = !isVerifying && lockoutRemainingSeconds == 0,
                                    isError = patternError,
                                    resetKey = patternResetKey,
                                    helperText = if (lockoutRemainingSeconds > 0) {
                                        "Locked for ${lockoutRemainingSeconds}s"
                                    } else {
                                        ""
                                    },
                                    onPatternStart = {
                                        patternError = false
                                        errorMessage = null
                                    },
                                    onPatternComplete = { dots ->
                                        if (lockoutRemainingSeconds > 0) return@PatternLockView
                                        if (dots.size < ParentLockPolicy.MIN_PATTERN_DOTS) {
                                            patternError = true
                                            errorMessage = "Connect at least ${ParentLockPolicy.MIN_PATTERN_DOTS} dots."
                                        } else if (parentPrefs.verifyParentPattern(dots)) {
                                            patternError = false
                                            lockoutRemainingSeconds = 0
                                            onSuccess()
                                        } else {
                                            patternError = true
                                            handleFailedCredential("Pattern")
                                        }
                                    },
                                )
                            }

                            ParentLockType.PIN -> {
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = {
                                        if (lockoutRemainingSeconds > 0) return@OutlinedTextField
                                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                            pinInput = it
                                            errorMessage = null
                                            if (it.length == 4) {
                                                if (parentPrefs.verifyParentPin(it)) {
                                                    lockoutRemainingSeconds = 0
                                                    onSuccess()
                                                } else {
                                                    handleFailedCredential("PIN")
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isVerifying && lockoutRemainingSeconds == 0,
                                    label = {
                                        Text(if (lockoutRemainingSeconds > 0) "Locked (${lockoutRemainingSeconds}s)" else "Parent PIN")
                                    },
                                    placeholder = { Text("••••") },
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide PIN" else "Show PIN",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }

                            ParentLockType.PASSWORD -> {
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = {
                                        if (lockoutRemainingSeconds > 0) return@OutlinedTextField
                                        passwordInput = it
                                        errorMessage = null
                                    },
                                    enabled = !isVerifying && lockoutRemainingSeconds == 0,
                                    label = {
                                        Text(if (lockoutRemainingSeconds > 0) "Locked (${lockoutRemainingSeconds}s)" else "Parent Password")
                                    },
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide Password" else "Show Password",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }

                        // Optional direct strong fingerprint unlock (opt-in only, never phone screen lock PIN)
                        if (parentPrefs.parentBiometricEnabled && isHardwareBiometricAvailable && lockoutRemainingSeconds == 0) {
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    errorMessage = null
                                    val fragmentActivity = appContext as? FragmentActivity
                                    if (fragmentActivity != null) {
                                        ParentBiometricAuth.authenticate(
                                            activity = fragmentActivity,
                                            title = title,
                                            subtitle = "Confirm your fingerprint to unlock.",
                                            negativeButtonText = "Use ${activeLockType.displayName}",
                                            onSuccess = { onSuccess() },
                                            onError = { err -> errorMessage = err },
                                            onCancel = { errorMessage = null },
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BrandPrimary.copy(alpha = 0.08f),
                                ),
                                border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.35f)),
                            ) {
                                Icon(
                                    Icons.Rounded.Fingerprint,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Unlock with Fingerprint",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary,
                                )
                            }
                        }

                        // Single clean parent-only recovery path via Google Account
                        if (allowGoogleSignIn) {
                            val lockLabel = when (activeLockType) {
                                ParentLockType.PIN -> "PIN"
                                ParentLockType.PATTERN -> "Pattern"
                                ParentLockType.PASSWORD -> "Password"
                            }

                            Spacer(Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    isResettingCredential = true
                                    currentMode = ParentGateMode.GOOGLE_LOGIN
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "Forgot $lockLabel? Reset with Google",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ParentGateMode.BIOMETRIC_VERIFY -> {
                        Button(
                            onClick = {
                                errorMessage = null
                                val fragmentActivity = appContext as? FragmentActivity
                                if (fragmentActivity != null) {
                                    ParentBiometricAuth.authenticate(
                                        activity = fragmentActivity,
                                        title = "Parent Biometric Verification",
                                        subtitle = "Confirm fingerprint or face ID to create your independent Parent Lock.",
                                        negativeButtonText = "Cancel",
                                        onSuccess = {
                                            errorMessage = null
                                            currentMode = ParentGateMode.SET_CREDENTIAL
                                        },
                                        onError = { err -> errorMessage = err },
                                        onCancel = { errorMessage = null },
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        ) {
                            Icon(Icons.Rounded.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Verify Fingerprint / Face ID", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (allowGoogleSignIn) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    currentMode = ParentGateMode.GOOGLE_LOGIN
                                }
                            ) {
                                Text(
                                    text = "Or verify with Google Sign-In",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ParentGateMode.SET_CREDENTIAL -> {
                        // Lock Style Selector Row: PIN | Pattern | Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ParentLockType.entries.forEach { lockType ->
                                val isSelected = selectedSetupType == lockType
                                OutlinedButton(
                                    onClick = {
                                        selectedSetupType = lockType
                                        errorMessage = null
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
                                        if (isSelected) BrandPrimary else MaterialTheme.colorScheme.outline,
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = when (lockType) {
                                            ParentLockType.PIN -> "PIN"
                                            ParentLockType.PATTERN -> "Pattern"
                                            ParentLockType.PASSWORD -> "Password"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        when (selectedSetupType) {
                            ParentLockType.PATTERN -> {
                                PatternLockView(
                                    maxSize = if (compactHeight) 170.dp else 220.dp,
                                    isError = patternError,
                                    resetKey = patternResetKey,
                                    helperText = if (draftPattern == null) {
                                        "Step 1 of 2: Draw new pattern (min ${ParentLockPolicy.MIN_PATTERN_DOTS} dots)"
                                    } else {
                                        "Step 2 of 2: Draw the same pattern again to confirm"
                                    },
                                    onPatternStart = {
                                        patternError = false
                                        errorMessage = null
                                    },
                                    onPatternComplete = { dots ->
                                        if (draftPattern == null) {
                                            if (!ParentLockPolicy.isValidPattern(dots)) {
                                                patternError = true
                                                errorMessage = "Connect at least ${ParentLockPolicy.MIN_PATTERN_DOTS} dots."
                                            } else {
                                                draftPattern = dots
                                                patternResetKey++
                                                errorMessage = null
                                            }
                                        } else {
                                            if (dots == draftPattern) {
                                                parentPrefs.setParentPattern(dots)
                                                parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                                onSuccess()
                                            } else {
                                                patternError = true
                                                errorMessage = "Patterns did not match. Draw again to confirm."
                                            }
                                        }
                                    },
                                )

                                if (draftPattern != null) {
                                    TextButton(
                                        onClick = {
                                            draftPattern = null
                                            patternError = false
                                            errorMessage = null
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
                                            errorMessage = null
                                        }
                                    },
                                    label = { Text("New 4-digit PIN") },
                                    placeholder = { Text("••••") },
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide PIN" else "Show PIN",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
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
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide PIN" else "Show PIN",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }

                            ParentLockType.PASSWORD -> {
                                OutlinedTextField(
                                    value = newPasswordInput,
                                    onValueChange = {
                                        newPasswordInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("New Password (min ${ParentLockPolicy.MIN_PASSWORD_LENGTH} chars)") },
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide Password" else "Show Password",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmPasswordInput,
                                    onValueChange = {
                                        confirmPasswordInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("Confirm Password") },
                                    visualTransformation = if (showSecretText) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showSecretText = !showSecretText }) {
                                            Icon(
                                                if (showSecretText) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = if (showSecretText) "Hide Password" else "Show Password",
                                                tint = if (showSecretText) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }
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

                        if (isResettingCredential) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    isResettingCredential = false
                                    errorMessage = null
                                    currentMode = ParentGateMode.CREDENTIAL_UNLOCK
                                }
                            ) {
                                Text(
                                    text = "Back to ${activeLockType.displayName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (com.kidcurate.app.BuildConfig.DEBUG) {
                            Spacer(Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    parentPrefs.parentName = "Test Parent"
                                    parentPrefs.parentEmail = "cleanparent@test.com"
                                    parentPrefs.parentAuthToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2OGVlMDMyNi00Yzg2LTQwNmUtYmRmNi0wNjYxM2EwYmEyYmUiLCJlbWFpbCI6ImNsZWFucGFyZW50QHRlc3QuY29tIiwidHlwZSI6ImFjY2VzcyIsImV4cCI6MTc5NTY5NTA4N30.gsRcdYIUtjfDVQtUWdKQo3PWGIeb_GqmRvuV3KNwQss"
                                    parentPrefs.setParentCredential(ParentLockType.PIN, "1234")
                                    parentPrefs.parentPinOwnerEmail = "cleanparent@test.com"
                                    com.kidcurate.app.data.ApiClient.authToken = parentPrefs.parentAuthToken
                                    onSuccess()
                                }
                            ) {
                                Text(
                                    text = "Debug: Quick Sign-In (Test Parent)",
                                    fontSize = 12.sp,
                                    color = BrandPrimary,
                                    fontWeight = FontWeight.Bold,
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

                Spacer(Modifier.height(if (compactHeight) 8.dp else 16.dp))

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
                        ParentGateMode.CREDENTIAL_UNLOCK -> {
                            if (activeLockType == ParentLockType.PIN) {
                                Button(
                                    onClick = {
                                        if (lockoutRemainingSeconds > 0) return@Button
                                        if (parentPrefs.verifyParentPin(pinInput)) {
                                            lockoutRemainingSeconds = 0
                                            onSuccess()
                                        } else {
                                            handleFailedCredential("PIN")
                                        }
                                    },
                                    enabled = pinInput.length == 4 && lockoutRemainingSeconds == 0,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                ) {
                                    Text("Unlock")
                                }
                            } else if (activeLockType == ParentLockType.PASSWORD) {
                                Button(
                                    onClick = {
                                        if (lockoutRemainingSeconds > 0) return@Button
                                        if (parentPrefs.verifyParentPassword(passwordInput)) {
                                            lockoutRemainingSeconds = 0
                                            onSuccess()
                                        } else {
                                            handleFailedCredential("Password")
                                        }
                                    },
                                    enabled = passwordInput.isNotBlank() && lockoutRemainingSeconds == 0,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                ) {
                                    Text("Unlock")
                                }
                            }
                        }

                        ParentGateMode.SET_CREDENTIAL -> {
                            if (selectedSetupType == ParentLockType.PIN) {
                                Button(
                                    onClick = {
                                        val validationErr = ParentLockPolicy.validateCredential(ParentLockType.PIN, newPinInput)
                                        if (validationErr != null) {
                                            errorMessage = validationErr
                                        } else if (newPinInput != confirmPinInput) {
                                            errorMessage = "PINs do not match."
                                        } else {
                                            parentPrefs.setParentCredential(ParentLockType.PIN, newPinInput)
                                            parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
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
                            } else if (selectedSetupType == ParentLockType.PASSWORD) {
                                Button(
                                    onClick = {
                                        val validationErr = ParentLockPolicy.validateCredential(ParentLockType.PASSWORD, newPasswordInput)
                                        if (validationErr != null) {
                                            errorMessage = validationErr
                                        } else if (newPasswordInput != confirmPasswordInput) {
                                            errorMessage = "Passwords do not match."
                                        } else {
                                            parentPrefs.setParentPassword(newPasswordInput)
                                            parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
                                            onSuccess()
                                        }
                                    },
                                    enabled = newPasswordInput.length >= ParentLockPolicy.MIN_PASSWORD_LENGTH && confirmPasswordInput.isNotEmpty(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                ) {
                                    Text("Save Password")
                                }
                            }
                        }

                        ParentGateMode.GOOGLE_LOGIN,
                        ParentGateMode.BIOMETRIC_VERIFY -> {
                            // Cancel already covers dismissing
                        }
                    }
                }
            }
        }
    }
}

