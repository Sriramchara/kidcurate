package com.kidcurate.app.ui.screens.parent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kidcurate.app.data.ApiClient
import com.kidcurate.app.data.DbSummaryResponse
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SecurityTab(
    parentPrefs: ParentPreferences,
    onPinUpdated: () -> Unit = {},
    onThemeChanged: (String) -> Unit = {},
    onBackToChildMode: () -> Unit = {},
    onAccountDeleted: () -> Unit = onBackToChildMode,
) {
    val appContext = LocalContext.current
    var selectedTheme by remember { mutableStateOf(parentPrefs.appTheme) }
    var showOnboardingTour by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Parent Guide & FAQs Card
        var showGuideDetails by remember { mutableStateOf(false) }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuideDetails = !showGuideDetails },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = BrandPrimary)
                        Text("Parent Guide: How It Works", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        if (showGuideDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                }

                if (showGuideDetails) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceCardBorder)
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Child Profiles: Go to 'Profiles' tab to add or edit children, change their character avatar, and view or change their 4-digit PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("2.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Add YouTube Videos: Go to 'Search' tab to find safe educational videos or paste ANY YouTube link to add it directly.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("3.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Manage Playlist: Go to 'Videos' tab to reorder videos with Up/Down buttons or delete unwanted videos.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4.", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                            Text(
                                "Parent Master PIN: Set a 4-digit PIN for quick offline access or to bypass a forgotten child PIN.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = { showOnboardingTour = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandPrimary)
                            Spacer(Modifier.width(6.dp))
                            Text("Replay Interactive App Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                    }
                }
            }
        }

        // App Theme Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = BrandPrimary)
                    Text("App Theme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Choose Light Theme, Dark Theme, or follow your device System setting.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (mode, label) ->
                        val isSelected = selectedTheme == mode
                        OutlinedButton(
                            onClick = {
                                selectedTheme = mode
                                parentPrefs.appTheme = mode
                                onThemeChanged(mode)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        // Parent Cloud Account Card (Google Sign-In)
        val scope = rememberCoroutineScope()
        var accountStatusMessage by remember { mutableStateOf<String?>(null) }
        var isAccountLoading by remember { mutableStateOf(false) }
        var currentToken by remember { mutableStateOf(parentPrefs.parentAuthToken) }

        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var isDeletingAccount by remember { mutableStateOf(false) }
        var showChangeParentPinDialog by remember { mutableStateOf(false) }

        val googleLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email ?: ""
                if (email.isNotBlank()) {
                    isAccountLoading = true
                    accountStatusMessage = null
                    scope.launch {
                        try {
                            val payload = mutableMapOf<String, String>()
                            account.idToken?.let { if (it.isNotBlank()) payload["id_token"] = it }
                            payload["email"] = email
                            payload["name"] = account.displayName ?: email.substringBefore("@")
                            val resp = ApiClient.api.googleLogin(payload)
                            val token = resp["access_token"] as? String ?: ""
                            val refreshToken = resp["refresh_token"] as? String ?: ""
                            if (token.isNotBlank()) {
                                parentPrefs.parentAuthToken = token
                                if (refreshToken.isNotBlank()) {
                                    parentPrefs.parentRefreshToken = refreshToken
                                }
                                currentToken = token
                                ApiClient.authToken = token
                                parentPrefs.parentEmail = email
                                accountStatusMessage = "Connected Google Account: $email"
                                if (!parentPrefs.hasParentPin) {
                                    showChangeParentPinDialog = true
                                }
                            } else {
                                accountStatusMessage = "Server did not issue an access token."
                            }
                        } catch (e: Exception) {
                            accountStatusMessage = "Login failed: ${e.message}"
                        } finally {
                            isAccountLoading = false
                        }
                    }
                } else {
                    accountStatusMessage = "Google sign-in was cancelled."
                }
            } catch (e: ApiException) {
                accountStatusMessage = "Google Sign-In failed (${e.statusCode}): ${e.message}"
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = BrandPrimary)
                    Text("Parent Cloud Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sign in with your Google account to automatically synchronize child profiles, playlists, and settings across your devices.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(14.dp))

                if (currentToken.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        Icons.Rounded.AccountCircle,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Column {
                                        Text(
                                            text = "Connected Google Account",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandPrimary,
                                        )
                                        Text(
                                            text = parentPrefs.parentEmail.ifBlank { "Parent Account" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary,
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
                                        GoogleSignIn.getClient(appContext, gso).signOut().addOnCompleteListener {
                                            parentPrefs.clearParentSession()
                                            currentToken = ""
                                            ApiClient.authToken = ""
                                            accountStatusMessage = "Signed out. You can now connect a different Google account."
                                            onBackToChildMode()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = BrandPrimary.copy(alpha = 0.2f))
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Delete account & cloud data",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(
                                    onClick = { showDeleteAccountDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BrandCoral),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                            val clientId = parentPrefs.googleClientId.trim()
                            if (clientId.isNotBlank()) {
                                gsoBuilder.requestIdToken(clientId)
                            }
                            val client = GoogleSignIn.getClient(appContext, gsoBuilder.build())
                            googleLauncher.launch(client.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        enabled = !isAccountLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isAccountLoading) "Connecting to Google..." else "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (accountStatusMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        accountStatusMessage!!,
                        color = if (currentToken.isNotBlank()) BrandSuccess else BrandCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Delete Parent Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = BrandCoral)
                        Text("Delete Account & All Data?", fontWeight = FontWeight.Bold, color = BrandCoral)
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your parent account? This will erase all child profiles, playlists, watch history, and cloud data. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isDeletingAccount = true
                                try {
                                    ApiClient.api.deleteAccount()
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
                                    GoogleSignIn.getClient(appContext, gso).signOut().addOnCompleteListener {
                                        parentPrefs.clearParentSession()
                                        currentToken = ""
                                        ApiClient.authToken = ""
                                        showDeleteAccountDialog = false
                                        onAccountDeleted()
                                    }
                                } catch (e: Exception) {
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
                                    GoogleSignIn.getClient(appContext, gso).signOut().addOnCompleteListener {
                                        parentPrefs.clearParentSession()
                                        currentToken = ""
                                        ApiClient.authToken = ""
                                        showDeleteAccountDialog = false
                                        onAccountDeleted()
                                    }
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text(if (isDeletingAccount) "Deleting..." else "Yes, Delete Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeletingAccount,
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Cloud Database Transparency & Audit Card
        var dbSummary by remember { mutableStateOf<DbSummaryResponse?>(null) }
        var isLoadingDbSummary by remember { mutableStateOf(false) }
        var dbSummaryError by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Storage, contentDescription = null, tint = BrandPrimary)
                    Text("Cloud Database Transparency", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Real-time verification of all records stored for your account in the cloud database:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                if (parentPrefs.isParentLoggedIn) {
                    if (dbSummary != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Database Engine:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dbSummary!!.databaseEngine, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Connected Account:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dbSummary!!.parentEmail, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Child Profiles in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.childrenCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Playlists in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.playlistsCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Videos Stored in DB:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.totalVideosCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Watch Sessions Logged:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dbSummary!!.watchHistoryCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (dbSummaryError != null) {
                        Text(dbSummaryError!!, fontSize = 12.sp, color = BrandCoral, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            isLoadingDbSummary = true
                            dbSummaryError = null
                            scope.launch {
                                try {
                                    val summary = ApiClient.api.getDbSummary()
                                    dbSummary = summary
                                } catch (e: Exception) {
                                    dbSummaryError = "Failed to query database: ${e.message}"
                                } finally {
                                    isLoadingDbSummary = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingDbSummary,
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isLoadingDbSummary) "Querying Database..."
                            else if (dbSummary == null) "Inspect Cloud Database Records"
                            else "Refresh Database Metrics",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Text(
                        "You are currently offline or in local PIN mode. Connect a Google Account above to inspect cloud database records.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Parent Security & PIN Control Card
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var pinErrorMessage by remember { mutableStateOf<String?>(null) }
        var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = BrandPrimary)
                    Text("Security & PIN Control", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KidCurate provides dual-layer security for parent controls and individual child profiles:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(12.dp))

                // Child Profile PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Rounded.Face, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Child Profile PINs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "To change a child's PIN, go to the 'Profiles' tab and tap 'Edit' on the child.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Parent Master PIN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Parent Master PIN", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Used for quick offline Parent Zone access: ••••",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                newPinInput = ""
                                confirmPinInput = ""
                                pinErrorMessage = null
                                pinSuccessMessage = null
                                showChangeParentPinDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (pinSuccessMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(pinSuccessMessage!!, color = BrandSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Legal & YouTube API Disclosures Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Gavel, contentDescription = null, tint = BrandPrimary)
                    Text("Legal & YouTube Disclosures", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "KidCurate uses YouTube API Services to provide safe, parent-curated educational videos. By using this app, you agree to the YouTube Terms of Service and acknowledge Google's Privacy Policy.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )

                Spacer(Modifier.height(14.dp))

                val uriHandler = LocalUriHandler.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://kidcurate-api.onrender.com/privacy")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("Privacy Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://www.youtube.com/t/terms")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("YouTube ToS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://policies.google.com/privacy")
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        Text("Google Privacy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Change Parent PIN Dialog
        if (showChangeParentPinDialog) {
            Dialog(onDismissRequest = { showChangeParentPinDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.LockReset, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Change Parent Master PIN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Set a 4-digit PIN for quick Parent Zone entry.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPinInput = it },
                            label = { Text("New 4-digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPinInput = it },
                            label = { Text("Confirm New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (pinErrorMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(pinErrorMessage!!, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showChangeParentPinDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (newPinInput.length != 4) {
                                        pinErrorMessage = "PIN must be exactly 4 digits."
                                        return@Button
                                    }
                                    if (newPinInput != confirmPinInput) {
                                        pinErrorMessage = "PINs do not match."
                                        return@Button
                                    }
                                    parentPrefs.parentPin = newPinInput
                                    pinSuccessMessage = "Parent Master PIN changed successfully!"
                                    showChangeParentPinDialog = false
                                    onPinUpdated()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text("Save PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showOnboardingTour) {
            OnboardingTourDialog(onDismiss = { showOnboardingTour = false })
        }
    }
}
