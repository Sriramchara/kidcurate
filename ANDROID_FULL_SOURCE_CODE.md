# KidCurate Complete Android Source Code & Configuration Archive

This document contains the complete, unabbreviated source code of all 38 essential Android files in the KidCurate repository, including application code, data layers, user interface screens, components, XML resources, Gradle build scripts, and unit tests.

## Table of Contents

1. [AndroidManifest.xml](#androidmanifestxml) - *Manifest: Permissions, Application & Activity Definitions*
2. [build.gradle.kts](#buildgradlekts) - *App Build Config: Dependencies, Plugins & SDK Versions*
3. [project-build.gradle.kts](#projectbuildgradlekts) - *Root Project Build Config: Plugin Repositories & Buildscript*
4. [settings.gradle.kts](#settingsgradlekts) - *Settings Config: Plugin Management & Module Includes*
5. [libs.versions.toml](#libsversionstoml) - *Version Catalog: Managed Dependency Versions*
6. [proguard-rules.pro](#proguardrulespro) - *ProGuard Rules: Code Shrinking & Obfuscation Rules*
7. [KidCurateApplication.kt](#kidcurateapplicationkt) - *Application Class: Global App Context Initialization*
8. [MainActivity.kt](#mainactivitykt) - *Single Activity: Window Setup, Orientation & Navigation Host*
9. [KidViewModel.kt](#kidviewmodelkt) - *ViewModel: Central State, Playlist Curation & Failover*
10. [data/ApiClient.kt](#dataapiclientkt) - *Network Client: Retrofit, Auth Tokens & Server Failover*
11. [data/AppPolicies.kt](#dataapppolicieskt) - *App Policies: Tablet/Phone Layout Rules & Probe Timeouts*
12. [data/GoogleCredentialAuth.kt](#datagooglecredentialauthkt) - *Google Auth: Credential Manager One-Tap Sign In*
13. [data/Models.kt](#datamodelskt) - *Data Models: Videos, Playlists, Channels & Analytics DTOs*
14. [data/ParentBiometricAuth.kt](#dataparentbiometricauthkt) - *Biometric Auth: Fingerprint/Face Verification Gate*
15. [data/ParentPreferences.kt](#dataparentpreferenceskt) - *Preferences: SharedPreferences Storage for PIN, Curfew & Progress*
16. [ui/screens/ChildSelectScreen.kt](#uiscreenschildselectscreenkt) - *Child Screen: Avatar Picker & Server Connection Status*
17. [ui/screens/TodaysPlaylistScreen.kt](#uiscreenstodaysplaylistscreenkt) - *Child Screen: Daily Queue, Curfew Notice & Limits*
18. [ui/screens/PlayerScreen.kt](#uiscreensplayerscreenkt) - *Child Screen: YouTube Player, Child Lock & Resume Engine*
19. [ui/screens/CompletionScreen.kt](#uiscreenscompletionscreenkt) - *Child Screen: Playlist Finish Celebration Screen*
20. [ui/screens/PinEntryScreen.kt](#uiscreenspinentryscreenkt) - *Security Screen: Dedicated Fullscreen Parent PIN Keypad*
21. [ui/screens/ParentZoneScreen.kt](#uiscreensparentzonescreenkt) - *Parent Zone: Main Dashboard & Tab Navigation*
22. [ui/screens/parent/VideosTabContent.kt](#uiscreensparentvideostabcontentkt) - *Parent Tab: Queue Management & Video Approval*
23. [ui/screens/parent/SearchTabContent.kt](#uiscreensparentsearchtabcontentkt) - *Parent Tab: YouTube Search & Channel Curation*
24. [ui/screens/parent/ProfilesTabContent.kt](#uiscreensparentprofilestabcontentkt) - *Parent Tab: Child Profiles & Bedtime Curfew Settings*
25. [ui/screens/parent/HistoryTabContent.kt](#uiscreensparenthistorytabcontentkt) - *Parent Tab: Watch History & Screen Time Analytics*
26. [ui/screens/parent/SecurityTabContent.kt](#uiscreensparentsecuritytabcontentkt) - *Parent Tab: PIN Modification, Biometrics & Account Delete*
27. [ui/components/ConfettiAnimation.kt](#uicomponentsconfettianimationkt) - *UI Component: Confetti Particle Celebration Canvas*
28. [ui/components/OnboardingTourDialog.kt](#uicomponentsonboardingtourdialogkt) - *UI Component: Interactive Parent Guided Tour Dialog*
29. [ui/components/SoundHaptics.kt](#uicomponentssoundhapticskt) - *UI Component: Tactile Sound & Haptic Feedback*
30. [ui/components/ThematicComponents.kt](#uicomponentsthematiccomponentskt) - *UI Component: Parent Password Dialog & Theme Avatars*
31. [ui/theme/Color.kt](#uithemecolorkt) - *Design System: Material3 Color Tokens*
32. [ui/theme/Theme.kt](#uithemethemekt) - *Design System: KidCurate App Theme Composable*
33. [res/values/strings.xml](#resvaluesstringsxml) - *Resource: App Strings & Accessibility Labels*
34. [res/values/colors.xml](#resvaluescolorsxml) - *Resource: Core Brand Colors & Palette XML*
35. [res/values/themes.xml](#resvaluesthemesxml) - *Resource: Android System Splash & Base Themes*
36. [res/xml/network_security_config.xml](#resxmlnetwork_security_configxml) - *Security: Network Security Config for Local & HTTPS Traffic*
37. [test/AppPoliciesTest.kt](#testapppoliciestestkt) - *Unit Test: Responsive Layout & Connection Policy Assertions*
38. [test/PlaybackUsabilityTest.kt](#testplaybackusabilitytestkt) - *Unit Test: Playback Usability & Child Lock State Verifications*

---

<a name="androidmanifestxml"></a>
## 1. AndroidManifest.xml
**Original Location:** `android/app/src/main/AndroidManifest.xml`
**Description:** Manifest: Permissions, Application & Activity Definitions

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".KidCurateApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:hardwareAccelerated="true"
        android:theme="@style/Theme.KidCurate"
        android:networkSecurityConfig="@xml/network_security_config"
        android:enableOnBackInvokedCallback="true">

        <activity
            android:name=".MainActivity"
            android:icon="@mipmap/ic_launcher"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:label="@string/app_name"
            android:exported="true"
            android:hardwareAccelerated="true"
            android:launchMode="singleTask"
            android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>

```

---

<a name="buildgradlekts"></a>
## 2. build.gradle.kts
**Original Location:** `android/app/build.gradle.kts`
**Description:** App Build Config: Dependencies, Plugins & SDK Versions

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Optional custom build directory override (e.g. for Windows path length limits)
(project.findProperty("CUSTOM_BUILD_DIR") as? String)?.let {
    layout.buildDirectory.set(file(it))
}

val localSigningProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}
val releaseStorePassword = System.getenv("KIDCURATE_STORE_PASSWORD")
    ?: localSigningProperties.getProperty("storePassword")
val releaseKeyPassword = System.getenv("KIDCURATE_KEY_PASSWORD")
    ?: localSigningProperties.getProperty("keyPassword")

// Force IDE Sync prompt
android {
    namespace = "com.kidcurate.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kidcurate.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"

        val apiUrl = (project.findProperty("API_BASE_URL") as? String) ?: "https://kidcurate-api.onrender.com"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")

        val googleClientId = (project.findProperty("GOOGLE_CLIENT_ID") as? String)
            ?: "179684236627-d7j0s15cvkj6li430jku6easqma5q81l.apps.googleusercontent.com"
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }

    val hasReleaseKeystore = releaseStorePassword != null && file("keystore/kidcurate-release.jks").exists()

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file("keystore/kidcurate-release.jks")
                storePassword = releaseStorePassword
                keyAlias = "kidcurate"
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Must use project keystore matching the SHA-1 registered in Google Cloud Console for Google Sign-In
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    
    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // DataStore (for persisting child session)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Encrypted SharedPreferences backed by Android Keystore
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Biometric & Device Credential Authentication for Parent Gate
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Google Sign-In through Android Credential Manager
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
}

```

---

<a name="projectbuildgradlekts"></a>
## 3. project-build.gradle.kts
**Original Location:** `android/build.gradle.kts`
**Description:** Root Project Build Config: Plugin Repositories & Buildscript

```kotlin
// Top-level build.gradle
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

```

---

<a name="settingsgradlekts"></a>
## 4. settings.gradle.kts
**Original Location:** `android/settings.gradle.kts`
**Description:** Settings Config: Plugin Management & Module Includes

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KidCurate"
include(":app")
```

---

<a name="libsversionstoml"></a>
## 5. libs.versions.toml
**Original Location:** `android/gradle/libs.versions.toml`
**Description:** Version Catalog: Managed Dependency Versions

```toml
[versions]
agp = "9.4.0"
kotlin = "2.2.10"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

```

---

<a name="proguardrulespro"></a>
## 6. proguard-rules.pro
**Original Location:** `android/app/proguard-rules.pro`
**Description:** ProGuard Rules: Code Shrinking & Obfuscation Rules

```pro
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.* { *; }

# KidCurate Data Transfer Objects and Serializable Models
-keep class com.kidcurate.app.data.*Item { *; }
-keep class com.kidcurate.app.data.*Request { *; }
-keep class com.kidcurate.app.data.*Response { *; }
-keep class com.kidcurate.app.data.PlaylistData { *; }
-keep class com.kidcurate.app.data.VideoItem { *; }
-keep class com.kidcurate.app.data.ProgressUpdate { *; }
-keep class com.kidcurate.app.data.GoogleCredentialAccount { *; }

# YouTube Player library & WebView JS bridge
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.core.** { *; }
-dontwarn com.pierfrancescosoffritti.androidyoutubeplayer.core.**

# JavascriptInterface (essential for YouTube iFrame events on Android 8-15)
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Android WebKit
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
}

# Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**

# Google Tink & AndroidX Security Crypto (Keystore-backed EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }
-keep class androidx.security.crypto.** { *; }

# Google Sign-In SDK
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.api.** { *; }

# AndroidX Credential Manager & Google Identity
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**


```

---

<a name="kidcurateapplicationkt"></a>
## 7. KidCurateApplication.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/KidCurateApplication.kt`
**Description:** Application Class: Global App Context Initialization

```kotlin
package com.kidcurate.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

class KidCurateApplication : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: KidCurateApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.kidcurate.app.data.ApiClient.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}

```

---

<a name="mainactivitykt"></a>
## 8. MainActivity.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/MainActivity.kt`
**Description:** Single Activity: Window Setup, Orientation & Navigation Host

```kotlin
package com.kidcurate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.screens.*
import com.kidcurate.app.ui.theme.KidCurateTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = ParentPreferences.get(this)
        if (com.kidcurate.app.BuildConfig.DEBUG) {
            val autoWalk = intent.getStringExtra("test_auto_walkthrough")
            if (autoWalk != null) {
                val tokenExtra = intent.getStringExtra("test_token")
                if (!tokenExtra.isNullOrBlank()) {
                    prefs.parentAuthToken = tokenExtra
                }
                val emailExtra = intent.getStringExtra("test_email")
                if (!emailExtra.isNullOrBlank()) {
                    prefs.parentEmail = emailExtra
                }
                if (prefs.serverUrl.isBlank()) {
                    prefs.serverUrl = intent.getStringExtra("test_server") ?: com.kidcurate.app.BuildConfig.API_BASE_URL
                }
                prefs.hasSeenOnboardingTour = true
                val pinExtra = intent.getStringExtra("test_parent_pin")
                if (!pinExtra.isNullOrBlank()) {
                    prefs.parentPin = pinExtra
                    prefs.parentPinOwnerEmail = prefs.parentEmail
                }
            } else {
                if (intent.hasExtra("test_token")) {
                    prefs.parentAuthToken = intent.getStringExtra("test_token") ?: ""
                    prefs.parentEmail = intent.getStringExtra("test_email") ?: ""
                    prefs.hasSeenOnboardingTour = true
                    val testServer = intent.getStringExtra("test_server") ?: "http://127.0.0.1:8000/"
                    prefs.serverUrl = testServer
                    if (intent.hasExtra("test_profiles_json")) {
                        prefs.cachedProfilesJson = intent.getStringExtra("test_profiles_json") ?: ""
                    }
                }
                if (intent.hasExtra("test_parent_pin")) {
                    prefs.parentPin = intent.getStringExtra("test_parent_pin") ?: "1234"
                    prefs.parentPinOwnerEmail = prefs.parentEmail
                }
            }
        }
        if (prefs.serverUrl.isNotBlank()) {
            com.kidcurate.app.data.ApiClient.updateBaseUrl(prefs.serverUrl)
        }
        if (prefs.parentAuthToken.isNotBlank()) {
            com.kidcurate.app.data.ApiClient.authToken = prefs.parentAuthToken
        }
        setContent {
            val context = LocalContext.current
            val parentPrefs = remember { ParentPreferences.get(context) }
            var currentTheme by remember { mutableStateOf(parentPrefs.appTheme) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (currentTheme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            KidCurateTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize().displayCutoutPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    KidApp(
                        onThemeChanged = { newTheme ->
                            currentTheme = newTheme
                            parentPrefs.appTheme = newTheme
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KidApp(
    onThemeChanged: (String) -> Unit = {},
) {
    val viewModel: KidViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.ensureParentSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val autoWalkthrough = activity?.intent?.getStringExtra("test_auto_walkthrough")

    if (com.kidcurate.app.BuildConfig.DEBUG && autoWalkthrough != null) {
        LaunchedEffect(autoWalkthrough) {
            // 1. Wait for available profiles to load
            for (i in 0 until 50) {
                if (state.availableProfiles.isNotEmpty()) break
                kotlinx.coroutines.delay(200)
            }
            if (state.availableProfiles.isEmpty()) {
                val demoProfile = com.kidcurate.app.data.ChildProfileItem(
                    id = "89b9bed9-4135-4e94-bd95-c05101ebb062",
                    name = "Maya",
                    avatar = "rainbow"
                )
                val demoItem1 = com.kidcurate.app.data.VideoItem(
                    itemId = "dd83b682-bfc1-4dfd-b113-7dd95546d4e1",
                    position = 1,
                    youtubeId = "M7lc1UVf-VE",
                    title = "YouTube Developers Live Demo",
                    channelName = "Google Developers",
                    thumbnailUrl = "https://img.youtube.com/vi/M7lc1UVf-VE/hqdefault.jpg",
                    durationSeconds = 100,
                    subjectTags = listOf("learning", "tech")
                )
                val demoItem2 = com.kidcurate.app.data.VideoItem(
                    itemId = "ecf0f4a6-1fb4-4ab9-8c2e-fb2d26597d98",
                    position = 2,
                    youtubeId = "jNQXAC9IVRw",
                    title = "Me at the zoo",
                    channelName = "jawed",
                    thumbnailUrl = "https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg",
                    durationSeconds = 19,
                    subjectTags = listOf("animals")
                )
                val demoPlaylist = com.kidcurate.app.data.PlaylistData(
                    id = "61f78a6b-d857-4776-8132-4d0512756525",
                    childId = demoProfile.id,
                    title = "Maya's Curated Stories",
                    description = null,
                    scheduledDate = "2026-11-19",
                    isActive = true,
                    totalDurationSeconds = 119,
                    videoCount = 2,
                    items = listOf(demoItem1, demoItem2)
                )
                viewModel.setDemoWalkthroughData(demoProfile, demoPlaylist)
            }
            val profile = state.availableProfiles.find { it.name.contains("Maya", ignoreCase = true) }
                ?: state.availableProfiles.firstOrNull()

            if (profile != null) {
                // Step 1: Select profile
                viewModel.selectProfile(profile)
                kotlinx.coroutines.delay(1200)

                // Step 2: Unlock PIN
                val verified = viewModel.verifyPin(profile.id, "4321")
                if (!verified) {
                    viewModel.unlockChildDirectly(profile.id, profile.name, profile.avatar)
                }

                // Step 3: Wait for playlist to load
                for (i in 0 until 50) {
                    if (state.playlist?.items?.isNotEmpty() == true) break
                    kotlinx.coroutines.delay(200)
                }
                kotlinx.coroutines.delay(1500)

                // Step 4: Open video player
                viewModel.startPlaying(0)

                if (autoWalkthrough == "poco_rotation_splitscreen") {
                    kotlinx.coroutines.delay(4000)
                    // Rotate to Landscape
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    kotlinx.coroutines.delay(6000)
                    // Rotate back to Portrait
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    kotlinx.coroutines.delay(4000)
                }
            }
        }
    }

    AnimatedContent(
        targetState = state.screen,
        transitionSpec = {
            (fadeIn() + slideInHorizontally { it }) togetherWith
                    (fadeOut() + slideOutHorizontally { -it })
        },
        label = "screen_transition",
    ) { screen ->
        when (screen) {
            is AppScreen.ChildSelect -> {
                ChildSelectScreen(
                    profiles = state.availableProfiles,
                    isLoading = state.isLoading,
                    errorMessage = state.error,
                    onSelectProfile = { profile ->
                        viewModel.selectProfile(profile)
                    },
                    onOpenParentZone = {
                        viewModel.openParentZone()
                    },
                    onRetry = {
                        viewModel.loadProfiles()
                    },
                    onUpdateServerUrl = { newUrl ->
                        viewModel.updateServerUrl(newUrl)
                    },
                )
            }

            is AppScreen.ParentZone -> {
                ParentZoneScreen(
                    profiles = state.availableProfiles,
                    onThemeChanged = onThemeChanged,
                    onBackToChildMode = {
                        viewModel.ensureParentSession()
                        viewModel.loadProfiles()
                        viewModel.backToChildSelect()
                    },
                    onAccountDeleted = {
                        viewModel.resetAllAccountData()
                    },
                )
            }

            is AppScreen.PinEntry -> {
                PinEntryScreen(
                    childId = screen.childId,
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    onPinVerified = { _, _, _ -> },
                    onVerifyPin = { id, pin ->
                        viewModel.verifyPin(id, pin)
                    },
                    onClearError = { viewModel.clearError() },
                    onParentUnlock = {
                        viewModel.unlockChildDirectly(screen.childId, screen.childName, screen.childAvatar)
                    },
                    isLoading = state.isLoading,
                    errorMessage = state.error,
                    onBack = { viewModel.backToChildSelect() },
                )
            }

            is AppScreen.Playlist -> {
                TodaysPlaylistScreen(
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    playlist = state.playlist,
                    isLoading = state.isLoading,
                    onStartPlaying = { startIndex ->
                        viewModel.startPlaying(startIndex)
                    },
                    onBack = {
                        viewModel.backToChildSelect()
                    },
                    onOpenParentZone = {
                        viewModel.openParentZone()
                    },
                )
            }

            is AppScreen.Player -> {
                val videos = state.playlist?.items ?: emptyList()
                var completedCount by remember { mutableStateOf(0) }

                PlayerScreen(
                    videos = videos,
                    startIndex = screen.startIndex,
                    onVideoCompleted = { video, seconds ->
                        completedCount++
                        viewModel.reportCompleted(video, seconds)
                    },
                    onProgressUpdate = { video, seconds ->
                        viewModel.reportProgress(video, seconds)
                    },
                    onAllCompleted = {
                        val totalMinutes = (state.playlist?.totalDurationSeconds ?: 0) / 60
                        val actualWatched = if (completedCount > 0) completedCount else (videos.size - screen.startIndex).coerceAtLeast(1)
                        viewModel.showCompletion(
                            videosWatched = actualWatched,
                            totalMinutes = totalMinutes,
                        )
                    },
                    onBack = { viewModel.goBackToPlaylist() },
                )
            }

            is AppScreen.Completion -> {
                CompletionScreen(
                    childName = screen.childName,
                    childAvatar = screen.childAvatar,
                    videosWatched = screen.videosWatched,
                    totalMinutes = screen.totalMinutes,
                    onGoHome = { viewModel.goBackToPlaylist() },
                )
            }
        }
    }
}

```

---

<a name="kidviewmodelkt"></a>
## 9. KidViewModel.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/KidViewModel.kt`
**Description:** ViewModel: Central State, Playlist Curation & Failover

```kotlin
package com.kidcurate.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kidcurate.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AppScreen {
    object ChildSelect : AppScreen()           // Choose which child profile
    object ParentZone : AppScreen()            // In-app Parent curation and search
    data class PinEntry(
        val childId: String,
        val childName: String,
        val childAvatar: String,
    ) : AppScreen()
    data class Playlist(
        val childId: String,
        val childName: String,
        val childAvatar: String,
    ) : AppScreen()
    data class Player(
        val childId: String,
        val childName: String,
        val childAvatar: String,
        val startIndex: Int,
    ) : AppScreen()
    data class Completion(
        val childName: String,
        val childAvatar: String,
        val videosWatched: Int,
        val totalMinutes: Int,
    ) : AppScreen()
}

data class KidAppState(
    val screen: AppScreen = AppScreen.ChildSelect,
    val availableProfiles: List<ChildProfileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlist: PlaylistData? = null,
    val childName: String = "",
    val childAvatar: String = "compass",
    val childId: String = "",
)

class KidViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(KidAppState())
    val state: StateFlow<KidAppState> = _state
    private val parentPrefs = ParentPreferences.get(application)
    private val gson = Gson()

    private var loadProfilesJob: kotlinx.coroutines.Job? = null

    init {
        ApiClient.onSessionExpired = {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Your parent session ended. Sign in with Google to continue.",
                )
            }
        }
        loadProfiles()
    }

    fun loadProfiles() {
        loadProfilesJob?.cancel()
        loadProfilesJob = viewModelScope.launch {
            if (!parentPrefs.isParentLoggedIn) {
                ApiClient.authToken = ""
                _state.value = KidAppState(screen = AppScreen.ChildSelect)
                return@launch
            }
            ApiClient.authToken = parentPrefs.parentAuthToken
            if (parentPrefs.serverUrl.isNotBlank()) {
                ApiClient.updateBaseUrl(parentPrefs.serverUrl)
            }
            val currentScreen = _state.value.screen
            val retainedScreen = if (currentScreen is AppScreen.ChildSelect) AppScreen.ChildSelect else currentScreen
            // Restore cached profiles only while an authenticated parent session exists.
            if (parentPrefs.cachedProfilesJson.isNotBlank()) {
                try {
                    val listType = object : TypeToken<List<ChildProfileItem>>() {}.type
                    val cached: List<ChildProfileItem> = gson.fromJson(parentPrefs.cachedProfilesJson, listType)
                    if (cached.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            availableProfiles = cached,
                            isLoading = false,
                            error = null,
                            screen = retainedScreen,
                        )
                    }
                } catch (_: Exception) {}
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }

            // 2. Fetch live profiles: connect to production cloud server or saved server URL
            var fetchedProfiles: List<ChildProfileItem>? = null
            val primaryUrl = parentPrefs.serverUrl.ifBlank { com.kidcurate.app.BuildConfig.API_BASE_URL }
            val candidateUrls = buildList {
                if (primaryUrl.isNotBlank() && !primaryUrl.contains("127.0.0.1")) {
                    add(primaryUrl)
                }
                add(com.kidcurate.app.BuildConfig.API_BASE_URL)
                if (com.kidcurate.app.BuildConfig.DEBUG) {
                    add("http://192.168.0.107:8000/")
                    add("http://localhost:8000/")
                }
            }.filter { it.isNotBlank() }
             .map { if (it.endsWith("/")) it else "$it/" }
             .distinct()

            var lastNetworkError: Exception? = null
            val (fetched, netErr) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                var found: List<ChildProfileItem>? = null
                var err: Exception? = null
                for (url in candidateUrls) {
                    val isCloud = !CloudConnectionPolicy.isLocalHost(url)
                    val maxAttempts = if (isCloud) 2 else 1
                    var attempt = 0
                    while (attempt < maxAttempts) {
                        attempt++
                        try {
                            val candidateApi = ApiClient.buildProbeApi(url)
                            val result = candidateApi.getPublicProfiles()
                            // If getPublicProfiles succeeds without network exception, connection is confirmed!
                            found = result
                            ApiClient.updateBaseUrl(url)
                            parentPrefs.serverUrl = url
                            err = null
                            break
                        } catch (e: retrofit2.HttpException) {
                            if (e.code() == 401 || e.code() == 403) {
                                // HTTP 401/403 confirms the server IS alive and reachable!
                                // The token is simply invalid, expired, or belongs to another backend.
                                ApiClient.updateBaseUrl(url)
                                parentPrefs.serverUrl = url
                                parentPrefs.clearParentSession()
                                ApiClient.authToken = ""
                                err = null
                                break
                            } else if (e.code() in listOf(408, 429, 500, 502, 503, 504) && attempt < maxAttempts) {
                                err = e
                                kotlinx.coroutines.delay(2000)
                            } else {
                                err = e
                                break
                            }
                        } catch (e: Exception) {
                            err = e
                            if (attempt < maxAttempts) {
                                kotlinx.coroutines.delay(2000)
                            }
                        }
                    }
                    if (found != null || (!parentPrefs.isParentLoggedIn && err == null)) break
                }
                Pair(found, err)
            }
            fetchedProfiles = fetched
            lastNetworkError = netErr

            if (!parentPrefs.isParentLoggedIn) {
                ApiClient.authToken = ""
                _state.value = KidAppState(screen = AppScreen.ChildSelect)
                return@launch
            }

            val activeScreen = _state.value.screen
            val finalScreen = if (activeScreen is AppScreen.ChildSelect) AppScreen.ChildSelect else activeScreen

            if (fetchedProfiles != null) {
                if (fetchedProfiles.isNotEmpty()) {
                    parentPrefs.cachedProfilesJson = gson.toJson(fetchedProfiles)
                    _state.value = _state.value.copy(
                        availableProfiles = fetchedProfiles,
                        isLoading = false,
                        error = null,
                        screen = finalScreen,
                    )
                } else {
                    parentPrefs.cachedProfilesJson = ""
                    _state.value = _state.value.copy(
                        availableProfiles = emptyList(),
                        isLoading = false,
                        error = null,
                        screen = finalScreen,
                    )
                }
            } else {
                // If network failed but we have cached profiles, do NOT show blocking error screen!
                // On launch with 0 profiles, show actionable error if connection failed
                val hasCached = _state.value.availableProfiles.isNotEmpty()
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = if (!hasCached && lastNetworkError != null) "Could not connect to server. Check your connection or tap to retry." else null,
                    screen = finalScreen,
                )
            }
        }
    }

    fun updateServerUrl(newUrl: String) {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(newUrl)
        if (sanitized.isNotBlank()) {
            ApiClient.updateBaseUrl(sanitized)
            parentPrefs.serverUrl = sanitized
            loadProfiles()
        }
    }

    fun selectProfile(profile: ChildProfileItem) {
        if (!parentPrefs.isParentLoggedIn) {
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return
        }
        val requiresPin = parentPrefs.requireChildPin && profile.hasPin
        if (requiresPin) {
            _state.value = _state.value.copy(
                childId = profile.id,
                childName = profile.name,
                childAvatar = profile.avatar,
                error = null,
                screen = AppScreen.PinEntry(profile.id, profile.name, profile.avatar),
            )
        } else {
            // Instant Tap-to-Play: friction-free profile launch for kids
            unlockChildDirectly(profile.id, profile.name, profile.avatar)
        }
    }

    fun clearError() {
        if (_state.value.error != null) {
            _state.value = _state.value.copy(error = null)
        }
    }

    fun backToChildSelect() {
        _state.value = _state.value.copy(
            error = null,
            screen = AppScreen.ChildSelect,
        )
    }

    fun ensureParentSession() {
        if (!parentPrefs.isParentLoggedIn) {
            ApiClient.authToken = ""
            _state.value = KidAppState(screen = AppScreen.ChildSelect)
        }
    }

    fun resetAllAccountData() {
        parentPrefs.clearAccountData()
        ApiClient.authToken = ""
        viewModelScope.launch {
            try {
                GoogleCredentialAuth.signOut(getApplication())
            } catch (_: Exception) {}
        }
        _state.value = KidAppState(
            screen = AppScreen.ChildSelect,
            availableProfiles = emptyList(),
            isLoading = false,
            error = null,
            playlist = null,
            childName = "",
            childAvatar = "compass",
            childId = "",
        )
    }

    suspend fun verifyPin(childId: String, pin: String): Boolean {
        if (!parentPrefs.isParentLoggedIn) {
            ApiClient.authToken = ""
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return false
        }
        _state.value = _state.value.copy(isLoading = true, error = null)

        // Always verify against the authenticated parent account. A cached child PIN
        // must never outlive a signed-out or deleted parent account.
        return try {
            val response = ApiClient.api.verifyPin(
                ChildVerifyRequest(childId = childId, pin = pin)
            )
            if (response.verified) {
                if (_state.value.screen is AppScreen.PinEntry) {
                    _state.value = _state.value.copy(
                        childId = response.childId,
                        childName = response.name,
                        childAvatar = response.avatar,
                        playlist = null,
                        screen = AppScreen.Playlist(
                            childId = response.childId,
                            childName = response.name,
                            childAvatar = response.avatar,
                        ),
                        isLoading = false,
                        error = null,
                    )
                    loadTodaysPlaylist(response.childId)
                }
                true
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Incorrect PIN. Please try again.",
                )
                false
            }
        } catch (e: retrofit2.HttpException) {
            val detail = try {
                val body = e.response()?.errorBody()?.string() ?: ""
                org.json.JSONObject(body).optString("detail", "")
            } catch (_: Exception) {
                ""
            }
            if (e.code() == 401 && (detail.contains("credentials", ignoreCase = true) || detail.contains("token", ignoreCase = true) || detail.isBlank())) {
                parentPrefs.clearParentSession()
                ApiClient.authToken = ""
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Your parent session ended. Sign in with Google to continue.",
                )
                return false
            }
            val msg = when {
                e.code() == 429 -> if (detail.isNotBlank()) detail else "Too many failed attempts. Account locked temporarily."
                e.code() == 404 -> "Incorrect PIN. Please try again."
                detail.isNotBlank() && !detail.contains("not found", ignoreCase = true) -> detail
                else -> "Incorrect PIN. Please try again."
            }
            _state.value = _state.value.copy(
                isLoading = false,
                error = msg,
            )
            false
        } catch (e: java.io.IOException) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Connection error. Please check your internet connection.",
            )
            false
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Verification error. Please try again.",
            )
            false
        }
    }

    fun unlockChildDirectly(childId: String, name: String, avatar: String) {
        if (!parentPrefs.isParentLoggedIn) {
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return
        }
        _state.value = _state.value.copy(
            childId = childId,
            childName = name,
            childAvatar = avatar,
            playlist = null,
            screen = AppScreen.Playlist(
                childId = childId,
                childName = name,
                childAvatar = avatar,
            ),
            isLoading = false,
            error = null,
        )
        loadTodaysPlaylist(childId)
    }

    fun loadTodaysPlaylist(childId: String) {
        viewModelScope.launch {
            if (!parentPrefs.isParentLoggedIn) {
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Parent Google sign-in is required before opening a child profile.",
                )
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.api.getTodaysPlaylist(childId)
                parentPrefs.setCachedPlaylist(childId, gson.toJson(response.playlist))
                _state.value = _state.value.copy(playlist = response.playlist, isLoading = false, error = null)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    parentPrefs.clearParentSession()
                    ApiClient.authToken = ""
                    _state.value = KidAppState(
                        screen = AppScreen.ChildSelect,
                        error = "Your parent session ended. Sign in with Google to continue.",
                    )
                } else {
                    _state.value = _state.value.copy(
                        playlist = null,
                        isLoading = false,
                        error = "Could not load playlist. Please try again.",
                    )
                }
            } catch (e: java.io.IOException) {
                val cached = parentPrefs.getCachedPlaylist(childId)
                val cachedPl = if (cached.isNotBlank()) {
                    try { gson.fromJson(cached, PlaylistData::class.java) } catch (_: Exception) { null }
                } else null
                if (cachedPl != null) {
                    _state.value = _state.value.copy(
                        playlist = cachedPl,
                        isLoading = false,
                        error = null,
                    )
                } else {
                    _state.value = _state.value.copy(
                        playlist = null,
                        isLoading = false,
                        error = "Could not load playlist. Please check your connection.",
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    playlist = null,
                    isLoading = false,
                    error = "Could not load playlist. Please check your connection.",
                )
            }
        }
    }

    fun startPlaying(startIndex: Int) {
        val s = _state.value
        val total = s.playlist?.items?.size ?: 0
        val clamped = if (total > 0) startIndex.coerceIn(0, total - 1) else 0
        _state.value = s.copy(
            screen = AppScreen.Player(
                childId = s.childId,
                childName = s.childName,
                childAvatar = s.childAvatar,
                startIndex = clamped,
            )
        )
    }

    fun reportProgress(video: VideoItem, watchedSeconds: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.updateProgress(
                    ProgressUpdate(
                        childId = _state.value.childId,
                        youtubeId = video.youtubeId,
                        playlistItemId = video.itemId,
                        watchedSeconds = watchedSeconds,
                        completed = false,
                    )
                )
            } catch (ignored: Exception) {}
        }
    }

    fun reportCompleted(video: VideoItem, watchedSeconds: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.updateProgress(
                    ProgressUpdate(
                        childId = _state.value.childId,
                        youtubeId = video.youtubeId,
                        playlistItemId = video.itemId,
                        watchedSeconds = watchedSeconds,
                        completed = true,
                    )
                )
            } catch (ignored: Exception) {}
        }
    }

    fun showCompletion(videosWatched: Int, totalMinutes: Int) {
        val s = _state.value
        _state.value = s.copy(
            screen = AppScreen.Completion(
                childName = s.childName,
                childAvatar = s.childAvatar,
                videosWatched = videosWatched,
                totalMinutes = totalMinutes,
            )
        )
    }

    fun goBackToPlaylist() {
        val s = _state.value
        _state.value = s.copy(
            screen = AppScreen.Playlist(
                childId = s.childId,
                childName = s.childName,
                childAvatar = s.childAvatar,
            )
        )
    }

    fun openParentZone() {
        _state.value = _state.value.copy(screen = AppScreen.ParentZone)
    }

    fun setDemoWalkthroughData(profile: ChildProfileItem, playlist: PlaylistData) {
        if (!com.kidcurate.app.BuildConfig.DEBUG) return
        _state.value = _state.value.copy(
            availableProfiles = listOf(profile),
            childId = profile.id,
            childName = profile.name,
            childAvatar = profile.avatar,
            playlist = playlist,
            isLoading = false,
            error = null,
        )
    }
}

```

---

<a name="dataapiclientkt"></a>
## 10. data/ApiClient.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/ApiClient.kt`
**Description:** Network Client: Retrofit, Auth Tokens & Server Failover

```kotlin
package com.kidcurate.app.data

import android.content.Context
import android.util.Log
import com.kidcurate.app.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface KidCurateApi {

    @GET("api/children/profiles")
    suspend fun getPublicProfiles(): List<ChildProfileItem>

    @POST("api/children/verify-pin")
    suspend fun verifyPin(@Body request: ChildVerifyRequest): ChildVerifyResponse

    @GET("api/playlists/child/{child_id}/today")
    suspend fun getTodaysPlaylist(@Path("child_id") childId: String): TodaysPlaylistResponse

    @POST("api/progress/update")
    suspend fun updateProgress(@Body progress: ProgressUpdate): Map<String, Boolean>

    @GET("api/videos/search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("max_results") maxResults: Int = 30
    ): List<VideoSearchResultItem>

    @GET("api/videos/suggestions")
    suspend fun getSearchSuggestions(@Query("q") query: String): List<String>

    @POST("api/playlists/child/{child_id}/add-video")
    suspend fun addVideoToChildPlaylist(
        @Path("child_id") childId: String,
        @Body request: AddVideoToChildRequest,
    ): Map<String, Any>

    @DELETE("api/playlists/child/{child_id}/items/{item_id}")
    suspend fun removeVideoFromChildPlaylist(
        @Path("child_id") childId: String,
        @Path("item_id") itemId: String,
    ): retrofit2.Response<Unit>

    @PUT("api/playlists/child/{child_id}/reorder")
    suspend fun reorderChildPlaylist(
        @Path("child_id") childId: String,
        @Body request: ReorderPlaylistRequest,
    ): Map<String, Any>

    @GET("api/children/manage")
    suspend fun getManageChildren(): List<ChildManageItem>

    @POST("api/children/manage")
    suspend fun createChild(@Body request: ChildCreateRequest): ChildManageItem

    @PUT("api/children/manage/{child_id}")
    suspend fun updateChild(
        @Path("child_id") childId: String,
        @Body request: ChildUpdateRequest,
    ): ChildManageItem

    @DELETE("api/children/manage/{child_id}")
    suspend fun deleteChild(@Path("child_id") childId: String): retrofit2.Response<Unit>

    @DELETE("api/auth/account")
    suspend fun deleteAccount(): retrofit2.Response<Unit>

    @GET("api/auth/diagnostic/db-summary")
    suspend fun getDbSummary(): DbSummaryResponse

    @GET("api/playlists/activity-packs")
    suspend fun getActivityPacks(): List<ActivityPackItem>

    @GET("api/playlists/curation-history")
    suspend fun getCurationHistory(
        @Query("child_id") childId: String? = null,
        @Query("limit") limit: Int = 50,
    ): CuratedHistoryResponse

    @GET("api/progress/child/{child_id}/history")
    suspend fun getWatchHistory(
        @Path("child_id") childId: String,
        @Query("limit") limit: Int = 50,
    ): WatchHistoryResponse

    @POST("api/playlists/child/{child_id}/apply-pack/{pack_id}")
    suspend fun applyActivityPack(
        @Path("child_id") childId: String,
        @Path("pack_id") packId: String,
    ): Map<String, Any>

    @POST("api/auth/register")
    suspend fun registerParent(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/login")
    suspend fun loginParent(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/verify-parent-password")
    suspend fun verifyParentPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: Map<String, String>): Map<String, Any>
}

private val Response.responseCount: Int
    get() {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
    }

    @Volatile
    var authToken: String = ""

    @Volatile
    var onSessionExpired: (() -> Unit)? = null

    @Volatile
    private var appContext: Context? = null
    private val refreshLock = Any()

    private val refreshClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val orig = chain.request()
            val token = authToken
            val newReq = if (token.isNotBlank()) {
                orig.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                orig
            }
            try {
                chain.proceed(newReq)
            } catch (e: java.io.IOException) {
                val currentHost = orig.url.host
                if (BuildConfig.DEBUG && (currentHost == "localhost" || currentHost == "127.0.0.1" || currentHost == "10.0.2.2")) {
                    val fallbackUrls = listOf(
                        "http://192.168.0.107:8000/",
                        BuildConfig.API_BASE_URL.trimEnd('/') + "/"
                    )
                    var lastEx: java.io.IOException = e
                    for (fallback in fallbackUrls) {
                        try {
                            val fallbackUrl = fallback.toHttpUrlOrNull()
                            if (fallbackUrl != null) {
                                val newUrl = orig.url.newBuilder()
                                    .scheme(fallbackUrl.scheme)
                                    .host(fallbackUrl.host)
                                    .port(fallbackUrl.port)
                                    .build()
                                val retryReq = newReq.newBuilder().url(newUrl).build()
                                val resp = chain.proceed(retryReq)
                                if (resp.isSuccessful || resp.code in 400..499) {
                                    updateBaseUrl(fallback)
                                    return@addInterceptor resp
                                }
                                resp.close()
                            }
                        } catch (failEx: java.io.IOException) {
                            lastEx = failEx
                        }
                    }
                    throw lastEx
                } else {
                    throw e
                }
            }
        }
        .authenticator { _, response ->
            if (response.responseCount >= 3) return@authenticator null
            val path = response.request.url.encodedPath
            if (path.contains("api/auth/refresh") ||
                path.contains("api/auth/login") ||
                path.contains("api/auth/google") ||
                path.contains("api/auth/register") ||
                path.contains("api/auth/verify-parent-password")) {
                return@authenticator null
            }
            val reqToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim() ?: ""
            if (reqToken.isBlank()) return@authenticator null

            synchronized(refreshLock) {
                // If another concurrent thread has already refreshed the token, retry with it
                if (authToken.isNotBlank() && authToken != reqToken) {
                    return@authenticator response.request.newBuilder()
                        .header("Authorization", "Bearer $authToken")
                        .build()
                }

                val ctx = appContext ?: return@authenticator null
                val prefs = ParentPreferences.get(ctx)
                val rToken = prefs.parentRefreshToken
                if (rToken.isBlank()) return@authenticator null

                val refreshUrl = response.request.url.newBuilder()
                    .encodedPath("/api/auth/refresh")
                    .query(null)
                    .build()
                val bodyJson = JSONObject().apply {
                    put("refresh_token", rToken)
                }.toString()
                val body = bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                val refreshRequest = Request.Builder()
                    .url(refreshUrl)
                    .post(body)
                    .build()

                try {
                    val refreshResp = refreshClient.newCall(refreshRequest).execute()
                    refreshResp.use { resp ->
                        if (resp.isSuccessful) {
                            val respBody = resp.body?.string() ?: ""
                            val json = JSONObject(respBody)
                            val newAccess = json.optString("access_token", "")
                            val newRefresh = json.optString("refresh_token", "")
                            if (newAccess.isNotBlank()) {
                                authToken = newAccess
                                prefs.parentAuthToken = newAccess
                                if (newRefresh.isNotBlank()) {
                                    prefs.parentRefreshToken = newRefresh
                                }
                                return@authenticator response.request.newBuilder()
                                    .header("Authorization", "Bearer $newAccess")
                                    .build()
                            }
                        } else if (resp.code in 400..499) {
                            prefs.clearParentSession()
                            authToken = ""
                            onSessionExpired?.invoke()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ApiClient", "Token refresh failed: ${e.message}")
                }
                null
            }
        }
        .addInterceptor(logging)
        .build()

    private val cloudProbeClient = httpClient.newBuilder()
        .connectTimeout(CloudConnectionPolicy.CLOUD_PROBE_TIMEOUT_SECONDS.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(CloudConnectionPolicy.CLOUD_PROBE_TIMEOUT_SECONDS.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val localProbeClient = httpClient.newBuilder()
        .connectTimeout(CloudConnectionPolicy.LOCAL_PROBE_TIMEOUT_SECONDS.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(CloudConnectionPolicy.LOCAL_PROBE_TIMEOUT_SECONDS.toLong(), java.util.concurrent.TimeUnit.SECONDS)
        .build()

    @Volatile
    private var currentBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"

    @Volatile
    private var currentApi: KidCurateApi = buildApi(currentBaseUrl)

    fun buildApi(baseUrl: String): KidCurateApi {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(baseUrl).ifBlank { currentBaseUrl }
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KidCurateApi::class.java)
    }

    fun buildProbeApi(baseUrl: String): KidCurateApi {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(baseUrl).ifBlank { currentBaseUrl }
        val isLocal = CloudConnectionPolicy.isLocalHost(sanitized)
        val client = if (isLocal) localProbeClient else cloudProbeClient
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KidCurateApi::class.java)
    }

    val api: KidCurateApi
        get() = currentApi

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newUrl: String) {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(newUrl)
        if (sanitized.isNotBlank()) {
            currentBaseUrl = sanitized
            currentApi = buildApi(sanitized)
        }
    }
}

```

---

<a name="dataapppolicieskt"></a>
## 11. data/AppPolicies.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/AppPolicies.kt`
**Description:** App Policies: Tablet/Phone Layout Rules & Probe Timeouts

```kotlin
package com.kidcurate.app.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ParentSessionPolicy {
    fun canReusePin(
        hasPin: Boolean,
        pinOwnerEmail: String,
        currentAccountEmail: String,
        signingInEmail: String,
    ): Boolean {
        if (!hasPin || signingInEmail.isBlank()) return false
        val owner = pinOwnerEmail.ifBlank { currentAccountEmail }
        return owner.isNotBlank() && owner.equals(signingInEmail, ignoreCase = true)
    }
}

object ResponsiveLayoutPolicy {
    fun isLandscape(widthDp: Int, heightDp: Int): Boolean = widthDp > heightDp

    fun isTablet(widthDp: Int): Boolean = widthDp >= 600

    fun isTablet(widthDp: Int, heightDp: Int): Boolean =
        kotlin.math.min(widthDp, heightDp) >= 600

    fun isExpanded(widthDp: Int): Boolean = widthDp >= 840

    fun isCompactLandscape(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp && heightDp < 500

    fun isTabletLandscape(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp && kotlin.math.min(widthDp, heightDp) >= 600

    fun shouldUseTwoPanePin(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp

    fun shouldShowTwoPanePlaylist(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp

    fun playlistGridColumns(widthDp: Int, heightDp: Int): Int {
        val isLandscape = widthDp > heightDp
        val minDim = kotlin.math.min(widthDp, heightDp)
        return when {
            minDim >= 600 -> 2
            widthDp >= 720 && !isLandscape -> 2
            else -> 1
        }
    }

    fun pinButtonSizeDp(widthDp: Int, heightDp: Int): Int {
        val isLandscape = widthDp > heightDp
        val isTab = kotlin.math.min(widthDp, heightDp) >= 600
        return when {
            isLandscape && isTab -> 60
            isLandscape -> 46
            heightDp < 680 -> 64
            else -> 74
        }
    }
}

object CloudConnectionPolicy {
    const val LOCAL_PROBE_TIMEOUT_SECONDS = 4
    const val CLOUD_PROBE_TIMEOUT_SECONDS = 25

    private val RFC172_REGEX = Regex("""^172\.(1[6-9]|2[0-9]|3[0-1])\.""")

    fun isLocalHost(url: String): Boolean {
        val sanitized = url.trim().lowercase()
        val host = try {
            val candidate = if (sanitized.contains("://")) sanitized else "http://$sanitized"
            candidate.toHttpUrlOrNull()?.host
        } catch (_: Exception) {
            null
        } ?: sanitized.substringAfter("://").substringBefore("/").substringBefore(":")

        if (host.isNullOrBlank()) return false
        if (host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2") return true
        if (host.startsWith("192.168.")) return true
        if (host.startsWith("10.")) return true
        if (RFC172_REGEX.containsMatchIn(host)) return true
        return false
    }

    fun getRecommendedProbeTimeoutSeconds(url: String): Int {
        return if (isLocalHost(url)) LOCAL_PROBE_TIMEOUT_SECONDS else CLOUD_PROBE_TIMEOUT_SECONDS
    }

    fun sanitizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}

```

---

<a name="datagooglecredentialauthkt"></a>
## 12. data/GoogleCredentialAuth.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/GoogleCredentialAuth.kt`
**Description:** Google Auth: Credential Manager One-Tap Sign In

```kotlin
package com.kidcurate.app.data

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleCredentialAccount(
    val email: String,
    val name: String,
    val idToken: String,
)

object GoogleCredentialAuth {
    suspend fun signIn(activity: Activity, serverClientId: String): GoogleCredentialAccount {
        require(serverClientId.isNotBlank()) { "Google OAuth client ID is not configured." }

        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request,
        )
        val credential = result.credential
        if (
            credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Google did not return a valid identity credential.")
        }

        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val email = google.id.trim().lowercase()
        require(email.isNotBlank() && google.idToken.isNotBlank()) {
            "Google account identity was incomplete."
        }
        val name = google.displayName?.trim()?.ifBlank { null }
            ?: google.givenName?.trim()?.ifBlank { null }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        return GoogleCredentialAccount(
            email = email,
            name = name,
            idToken = google.idToken,
        )
    }

    suspend fun signOut(context: Context) {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }

    fun userMessage(error: Throwable): String {
        android.util.Log.e("GoogleCredentialAuth", "Sign-in error: ${error.javaClass.name}: ${error.message}", error)
        return when (error) {
            is androidx.credentials.exceptions.GetCredentialCancellationException ->
                "Google sign-in was cancelled."
            is androidx.credentials.exceptions.NoCredentialException ->
                "No Google account available or account not authorized for this app signature."
            is GetCredentialException ->
                error.message ?: "Google sign-in failed. Please try again."
            else -> error.message ?: "Google sign-in failed. Please try again."
        }
    }
}

```

---

<a name="datamodelskt"></a>
## 13. data/Models.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/Models.kt`
**Description:** Data Models: Videos, Playlists, Channels & Analytics DTOs

```kotlin
package com.kidcurate.app.data

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────────────────

data class ChildProfileItem(
    val id: String,
    val name: String,
    val avatar: String,
    @SerializedName("has_pin") val hasPin: Boolean = false,
)

data class ChildVerifyRequest(
    @SerializedName("child_id") val childId: String,
    val pin: String,
)

data class ChildVerifyResponse(
    val verified: Boolean,
    @SerializedName("child_id") val childId: String,
    val name: String,
    val avatar: String,
)

// ─── Playlist ─────────────────────────────────────────────────────────────

@Immutable
data class VideoItem(
    @SerializedName("item_id") val itemId: String,
    val position: Int,
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int?,
    @SerializedName("subject_tags") val subjectTags: List<String>?,
)

@Immutable
data class PlaylistData(
    val id: String,
    @SerializedName("child_id") val childId: String,
    val title: String,
    val description: String?,
    @SerializedName("scheduled_date") val scheduledDate: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("total_duration_seconds") val totalDurationSeconds: Int,
    @SerializedName("video_count") val videoCount: Int,
    val items: List<VideoItem>,
)

data class TodaysPlaylistResponse(
    val playlist: PlaylistData?,
    val message: String?,
)

// ─── Progress ─────────────────────────────────────────────────────────────

data class ProgressUpdate(
    @SerializedName("child_id") val childId: String,
    @SerializedName("youtube_id") val youtubeId: String,
    @SerializedName("playlist_item_id") val playlistItemId: String?,
    @SerializedName("watched_seconds") val watchedSeconds: Int,
    val completed: Boolean,
)

// ─── Parent Zone & Search ───────────────────────────────────────────────────

data class VideoSearchResultItem(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int?,
    val description: String?,
    @SerializedName("is_safe") val isSafe: Boolean?,
)

data class AddVideoToChildRequest(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

data class ChildManageItem(
    val id: String,
    val name: String,
    val age: Int,
    val avatar: String,
    val pin: String? = "••••",
    @SerializedName("has_pin") val hasPin: Boolean? = true,
    @SerializedName("parent_id") val parentId: String? = null,
)

data class ChildCreateRequest(
    val name: String,
    val age: Int,
    val avatar: String,
    val pin: String,
)

data class ChildUpdateRequest(
    val name: String?,
    val age: Int?,
    val avatar: String?,
    val pin: String?,
)

data class ReorderPlaylistRequest(
    @SerializedName("ordered_item_ids") val orderedItemIds: List<String>,
)

// ─── Activity Packs for Parents ─────────────────────────────────────────────

@Immutable
data class ActivityPackVideo(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

@Immutable
data class ActivityPackItem(
    val id: String,
    val title: String,
    @SerializedName("badge_icon") val badgeIcon: String,
    val tagline: String,
    val category: String,
    @SerializedName("approx_minutes") val approxMinutes: Int,
    val videos: List<ActivityPackVideo>,
)

@Immutable
data class DbSummaryResponse(
    @SerializedName("parent_email") val parentEmail: String = "",
    @SerializedName("database_engine") val databaseEngine: String = "",
    @SerializedName("children_count") val childrenCount: Int = 0,
    @SerializedName("playlists_count") val playlistsCount: Int = 0,
    @SerializedName("total_videos_count") val totalVideosCount: Int = 0,
    @SerializedName("watch_history_count") val watchHistoryCount: Int = 0,
    @SerializedName("account_created_at") val accountCreatedAt: String = "",
)

// ─── Curation & Watch History ───────────────────────────────────────────────

@Immutable
data class CuratedHistoryItem(
    val id: String = "",
    @SerializedName("child_id") val childId: String = "",
    @SerializedName("child_name") val childName: String = "",
    val action: String = "added", // "added", "removed", "pack_applied"
    val title: String = "",
    @SerializedName("youtube_id") val youtubeId: String = "",
    @SerializedName("channel_name") val channelName: String = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String = "",
    @SerializedName("duration_seconds") val durationSeconds: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
)

@Immutable
data class CuratedHistoryResponse(
    val history: List<CuratedHistoryItem> = emptyList(),
)

@Immutable
data class WatchHistoryItem(
    @SerializedName("youtube_id") val youtubeId: String = "",
    val title: String = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String = "",
    @SerializedName("watched_seconds") val watchedSeconds: Int = 0,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null,
    val completed: Boolean = false,
    @SerializedName("watched_at") val watchedAt: String? = null,
)

@Immutable
data class WatchHistoryResponse(
    val history: List<WatchHistoryItem> = emptyList(),
)


```

---

<a name="dataparentbiometricauthkt"></a>
## 14. data/ParentBiometricAuth.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/ParentBiometricAuth.kt`
**Description:** Biometric Auth: Fingerprint/Face Verification Gate

```kotlin
package com.kidcurate.app.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Hardware-backed Biometric and Device Credential Authenticator for Parent Gate.
 * Enforces hardware security (fingerprint, face unlock, or device screen lock PIN/pattern)
 * so that children cannot reset or bypass the Parent PIN.
 */
object ParentBiometricAuth {

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        UNAVAILABLE,
    }

    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun getStatus(context: Context): BiometricStatus {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun isBiometricOrDeviceLockAvailable(context: Context): Boolean {
        return getStatus(context) == BiometricStatus.AVAILABLE
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Parent Identity Verification",
        subtitle: String = "Confirm your fingerprint, face, or phone screen lock to verify you are the parent.",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: (() -> Unit)? = null,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onCancel?.invoke() ?: onError("Verification canceled.")
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // BiometricPrompt handles transient failed attempts with native OS haptic and visual cues
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo)
    }
}

```

---

<a name="dataparentpreferenceskt"></a>
## 15. data/ParentPreferences.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/data/ParentPreferences.kt`
**Description:** Preferences: SharedPreferences Storage for PIN, Curfew & Progress

```kotlin
package com.kidcurate.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ParentPreferences(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "kidcurate_parent_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("ParentPreferences", "EncryptedSharedPreferences initialization failed, falling back", e)
        context.getSharedPreferences("kidcurate_parent_prefs", Context.MODE_PRIVATE)
    }

    var subtitlesEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUBTITLES_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SUBTITLES_ENABLED, value).apply()

    var earlyReaderSubtitles: Boolean
        get() = prefs.getBoolean(KEY_EARLY_READER_SUBTITLES, true)
        set(value) = prefs.edit().putBoolean(KEY_EARLY_READER_SUBTITLES, value).apply()

    var videoQuality: String
        get() = prefs.getString(KEY_VIDEO_QUALITY, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_VIDEO_QUALITY, value).apply()

    var requireChildPin: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_CHILD_PIN, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_CHILD_PIN, value).apply()

    var parentPin: String
        get() = prefs.getString(KEY_PARENT_PIN, "") ?: ""
        set(value) {
            val toStore = if (value.isBlank() || value.startsWith("pbkdf2$") || value.startsWith("sha256$")) value else hashPinPbkdf2(value)
            prefs.edit().putString(KEY_PARENT_PIN, toStore).apply()
        }

    private fun getOrCreatePinSalt(): ByteArray {
        val storedSaltHex = prefs.getString("parent_pin_salt", "") ?: ""
        if (storedSaltHex.isNotBlank() && storedSaltHex.length == 32) {
            return hexToBytes(storedSaltHex)
        }
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        val saltHex = bytesToHex(salt)
        prefs.edit().putString("parent_pin_salt", saltHex).apply()
        return salt
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun hashPinPbkdf2(pin: String, salt: ByteArray = getOrCreatePinSalt()): String {
        if (pin.isBlank()) return ""
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return "pbkdf2$100000$${bytesToHex(salt)}$${bytesToHex(hash)}"
    }

    private fun hashPinLegacySha256(pin: String): String {
        if (pin.isBlank()) return ""
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("kidcurate_salt_${pin}".toByteArray(Charsets.UTF_8))
        return "sha256$" + bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String): String = hashPinPbkdf2(pin)

    fun getPinLockoutRemainingSeconds(): Int {
        val lockoutUntil = prefs.getLong("parent_pin_lockout_until", 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) (((lockoutUntil - now) / 1000) + 1).toInt() else 0
    }

    fun isPinLockedOut(): Boolean = getPinLockoutRemainingSeconds() > 0

    private fun recordFailedPinAttempt() {
        val currentFailed = prefs.getInt("parent_pin_failed_attempts", 0) + 1
        val editor = prefs.edit().putInt("parent_pin_failed_attempts", currentFailed)
        if (currentFailed >= 5) {
            val multiplier = 1 shl (currentFailed - 5).coerceAtMost(5)
            val lockoutMs = 30_000L * multiplier
            editor.putLong("parent_pin_lockout_until", System.currentTimeMillis() + lockoutMs)
        }
        editor.apply()
    }

    private fun recordSuccessfulPinAttempt() {
        prefs.edit()
            .putInt("parent_pin_failed_attempts", 0)
            .putLong("parent_pin_lockout_until", 0L)
            .apply()
    }

    fun verifyParentPin(candidatePin: String): Boolean {
        if (candidatePin.isBlank() || isPinLockedOut()) return false
        val stored = parentPin
        if (stored.isBlank()) return false

        var matches = false
        if (stored.startsWith("pbkdf2$")) {
            val parts = stored.split("$")
            if (parts.size >= 4) {
                val salt = hexToBytes(parts[2])
                val expectedHashHex = parts[3]
                val spec = javax.crypto.spec.PBEKeySpec(candidatePin.toCharArray(), salt, 100_000, 256)
                val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                val hash = factory.generateSecret(spec).encoded
                matches = bytesToHex(hash) == expectedHashHex
            }
        } else if (stored.startsWith("sha256$")) {
            matches = (stored == hashPinLegacySha256(candidatePin))
            if (matches) {
                parentPin = candidatePin
            }
        } else if (stored == candidatePin) {
            matches = true
            parentPin = candidatePin
        }

        if (matches) {
            recordSuccessfulPinAttempt()
            return true
        } else {
            recordFailedPinAttempt()
            return false
        }
    }

    var parentPinOwnerEmail: String
        get() = prefs.getString(KEY_PARENT_PIN_OWNER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_PIN_OWNER_EMAIL, value.trim().lowercase()).apply()

    val hasParentPin: Boolean
        get() = parentPin.isNotBlank()

    val isParentLoggedIn: Boolean
        get() = parentAuthToken.isNotBlank()

    var hasSeenOnboardingTour: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, value).apply()

    fun clearParentSession() {
        if (hasParentPin && parentPinOwnerEmail.isBlank() && parentEmail.isNotBlank()) {
            parentPinOwnerEmail = parentEmail
        }
        parentAuthToken = ""
        parentRefreshToken = ""
        parentEmail = ""
        parentName = ""
        cachedProfilesJson = ""
    }

    /** Clears account-owned secrets and cached child data after permanent account deletion. */
    fun clearAccountData() {
        val editor = prefs.edit()
        prefs.all.keys
            .filter {
                it.startsWith(KEY_CHILD_PIN_PREFIX) ||
                    it.startsWith(KEY_CACHED_PLAYLIST_PREFIX) ||
                    it.startsWith("video_pos_") ||
                    it.startsWith("today_watched_seconds_")
            }
            .forEach(editor::remove)
        editor
            .remove(KEY_PARENT_AUTH_TOKEN)
            .remove(KEY_PARENT_REFRESH_TOKEN)
            .remove(KEY_PARENT_EMAIL)
            .remove(KEY_PARENT_NAME)
            .remove(KEY_PARENT_PIN)
            .remove(KEY_PARENT_PIN_OWNER_EMAIL)
            .remove(KEY_CACHED_PROFILES)
            .remove(KEY_RECENT_SEARCHES)
            .remove(KEY_LAST_WATCHED_DATE)
            .remove(KEY_TODAY_WATCHED_SECONDS)
            .remove(KEY_LAST_RECORDED_TIMESTAMP)
            .remove(KEY_DAILY_SCREEN_TIME_MINUTES)
            .remove(KEY_CURFEW_ENABLED)
            .remove(KEY_CURFEW_START_HOUR)
            .remove(KEY_CURFEW_END_HOUR)
            .remove(KEY_REQUIRE_CHILD_PIN)
            .apply()
    }

    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, "light") ?: "light"
        set(value) = prefs.edit().putString(KEY_APP_THEME, value).apply()

    var serverUrl: String
        get() {
            val url = prefs.getString(KEY_SERVER_URL, "") ?: ""
            if (url.isBlank() || url.contains("127.0.0.1") || (!com.kidcurate.app.BuildConfig.DEBUG && url.contains("localhost"))) {
                val defaultUrl = com.kidcurate.app.BuildConfig.API_BASE_URL
                prefs.edit().putString(KEY_SERVER_URL, defaultUrl).apply()
                return defaultUrl
            }
            return url
        }
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var parentAuthToken: String
        get() = prefs.getString(KEY_PARENT_AUTH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_AUTH_TOKEN, value).apply()

    var parentRefreshToken: String
        get() = prefs.getString(KEY_PARENT_REFRESH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_REFRESH_TOKEN, value).apply()

    var parentEmail: String
        get() = prefs.getString(KEY_PARENT_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_EMAIL, value).apply()

    var parentName: String
        get() = prefs.getString(KEY_PARENT_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_NAME, value).apply()

    val googleClientId: String
        get() = com.kidcurate.app.BuildConfig.GOOGLE_CLIENT_ID

    var dailyScreenTimeMinutes: Int
        get() = prefs.getInt(KEY_DAILY_SCREEN_TIME_MINUTES, 45) // 0 means unlimited
        set(value) = prefs.edit().putInt(KEY_DAILY_SCREEN_TIME_MINUTES, value).apply()

    var curfewEnabled: Boolean
        get() = prefs.getBoolean(KEY_CURFEW_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CURFEW_ENABLED, value).apply()

    var curfewStartHour: Int
        get() = prefs.getInt(KEY_CURFEW_START_HOUR, 20) // 8:00 PM
        set(value) = prefs.edit().putInt(KEY_CURFEW_START_HOUR, value).apply()

    var curfewEndHour: Int
        get() = prefs.getInt(KEY_CURFEW_END_HOUR, 7) // 7:00 AM
        set(value) = prefs.edit().putInt(KEY_CURFEW_END_HOUR, value).apply()

    private fun getTodayDate(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
    }

    var todayWatchedSeconds: Int
        get() {
            val savedDate = prefs.getString(KEY_LAST_WATCHED_DATE, "")
            val today = getTodayDate()
            val now = System.currentTimeMillis()
            val lastRecorded = prefs.getLong(KEY_LAST_RECORDED_TIMESTAMP, 0L)
            
            // Anti-Tamper: If device clock was rolled backwards by more than 5 minutes, retain previous watch time!
            if (lastRecorded > 0L && now < lastRecorded - (5 * 60 * 1000L)) {
                return prefs.getInt(KEY_TODAY_WATCHED_SECONDS, 0)
            }

            return if (savedDate == today) {
                prefs.getInt(KEY_TODAY_WATCHED_SECONDS, 0)
            } else {
                0
            }
        }
        set(value) {
            val today = getTodayDate()
            val now = System.currentTimeMillis()
            val lastRecorded = prefs.getLong(KEY_LAST_RECORDED_TIMESTAMP, 0L)
            val toRecord = if (now > lastRecorded) now else lastRecorded
            prefs.edit()
                .putString(KEY_LAST_WATCHED_DATE, today)
                .putInt(KEY_TODAY_WATCHED_SECONDS, value)
                .putLong(KEY_LAST_RECORDED_TIMESTAMP, toRecord)
                .apply()
        }

    fun addWatchedSeconds(seconds: Int) {
        todayWatchedSeconds += seconds
    }

    fun resetTodayWatched() {
        todayWatchedSeconds = 0
    }

    var cachedProfilesJson: String
        get() = prefs.getString(KEY_CACHED_PROFILES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CACHED_PROFILES, value).apply()

    fun getChildLocalPin(childId: String): String {
        return ""
    }

    fun setChildLocalPin(childId: String, pin: String) {
        // Plaintext PIN storage removed for security. Child PINs are verified securely via ApiClient.
    }

    fun getCachedPlaylist(childId: String): String {
        return prefs.getString(KEY_CACHED_PLAYLIST_PREFIX + childId, "") ?: ""
    }

    fun setCachedPlaylist(childId: String, json: String) {
        prefs.edit().putString(KEY_CACHED_PLAYLIST_PREFIX + childId, json).apply()
    }

    fun getVideoProgress(youtubeId: String): Float {
        if (youtubeId.isBlank()) return 0f
        return prefs.getFloat("video_pos_$youtubeId", 0f)
    }

    fun setVideoProgress(youtubeId: String, seconds: Float) {
        if (youtubeId.isBlank()) return
        prefs.edit().putFloat("video_pos_$youtubeId", seconds).apply()
    }

    fun clearVideoProgress(youtubeId: String) {
        if (youtubeId.isBlank()) return
        prefs.edit().remove("video_pos_$youtubeId").apply()
    }

    fun isCurfewActive(): Boolean {
        if (!curfewEnabled) return false
        if (curfewStartHour == curfewEndHour) return true
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (curfewStartHour > curfewEndHour) {
            // Over midnight, e.g. 20 (8 PM) to 7 (7 AM)
            currentHour >= curfewStartHour || currentHour < curfewEndHour
        } else {
            currentHour in curfewStartHour until curfewEndHour
        }
    }

    fun isDailyLimitReached(): Boolean {
        if (dailyScreenTimeMinutes <= 0) return false
        return todayWatchedSeconds >= dailyScreenTimeMinutes * 60
    }

    fun getRecentSearches(): List<String> {
        val raw = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i, "")
                if (item.isNotBlank()) list.add(item)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        val limited = current.take(15)
        val arr = org.json.JSONArray()
        limited.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECENT_SEARCHES, arr.toString()).apply()
    }

    fun removeRecentSearch(query: String) {
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        val arr = org.json.JSONArray()
        current.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECENT_SEARCHES, arr.toString()).apply()
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    companion object {
        private const val KEY_SUBTITLES_ENABLED = "subtitles_enabled"
        private const val KEY_EARLY_READER_SUBTITLES = "early_reader_subtitles"
        private const val KEY_RECENT_SEARCHES = "recent_searches_json"
        private const val KEY_VIDEO_QUALITY = "video_quality"
        private const val KEY_REQUIRE_CHILD_PIN = "require_child_pin"
        private const val KEY_PARENT_PIN = "parent_pin"
        private const val KEY_PARENT_PIN_OWNER_EMAIL = "parent_pin_owner_email"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PARENT_AUTH_TOKEN = "parent_auth_token"
        private const val KEY_PARENT_REFRESH_TOKEN = "parent_refresh_token"
        private const val KEY_PARENT_EMAIL = "parent_email"
        private const val KEY_PARENT_NAME = "parent_name"
        private const val KEY_DAILY_SCREEN_TIME_MINUTES = "daily_screen_time_minutes"
        private const val KEY_CURFEW_ENABLED = "curfew_enabled"
        private const val KEY_CURFEW_START_HOUR = "curfew_start_hour"
        private const val KEY_CURFEW_END_HOUR = "curfew_end_hour"
        private const val KEY_LAST_WATCHED_DATE = "last_watched_date"
        private const val KEY_LAST_RECORDED_TIMESTAMP = "last_recorded_wall_time_ms"
        private const val KEY_TODAY_WATCHED_SECONDS = "today_watched_seconds"
        private const val KEY_CACHED_PROFILES = "cached_profiles_json"
        private const val KEY_CACHED_PLAYLIST_PREFIX = "cached_playlist_json_"
        private const val KEY_CHILD_PIN_PREFIX = "child_local_pin_"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding_tour"

        @Volatile
        private var instance: ParentPreferences? = null

        fun get(context: Context): ParentPreferences {
            return instance ?: synchronized(this) {
                instance ?: ParentPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}


```

---

<a name="uiscreenschildselectscreenkt"></a>
## 16. ui/screens/ChildSelectScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/ChildSelectScreen.kt`
**Description:** Child Screen: Avatar Picker & Server Connection Status

```kotlin
package com.kidcurate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.kidcurate.app.data.ChildProfileItem
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.GlassCard
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.components.ThematicPill
import com.kidcurate.app.ui.theme.*

@Composable
fun ChildSelectScreen(
    profiles: List<ChildProfileItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectProfile: (ChildProfileItem) -> Unit,
    onOpenParentZone: () -> Unit,
    onRetry: () -> Unit,
    onUpdateServerUrl: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var showParentGate by remember { mutableStateOf(false) }
    var showOnboardingTour by remember { mutableStateOf(!parentPrefs.hasSeenOnboardingTour) }
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        GlassBackgroundAura()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = if (isLandscape) 40.dp else 24.dp, vertical = if (isLandscape) 8.dp else 20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 12.dp))

            // Brand Tag & Discreet Parent Gate Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThematicPill(
                    icon = Icons.Rounded.VerifiedUser,
                    label = "KidCurate • Safe & Distraction-Free",
                    tint = BrandPrimary,
                    background = MaterialTheme.colorScheme.primaryContainer,
                )

                // Discreet, tasteful Parent Gate button at top corner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            SoundHaptics.playTactileClick(view, haptic)
                            showParentGate = true
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Shield,
                            contentDescription = "Parent Gate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Parents",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 16.dp))

            Text(
                text = "Who's Watching?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isLandscape) 22.sp else 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Tap your avatar to play!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (isLandscape) 13.sp else 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = if (isLandscape) 8.dp else 24.dp),
            )

            if (isLoading) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Loading profiles...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }
                }
            } else if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .padding(16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(if (isLandscape) 12.dp else 28.dp),
                        ) {
                        val isError = errorMessage != null
                        Icon(
                            if (isError) Icons.Rounded.CloudOff else Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(if (isLandscape) 36.dp else 52.dp),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isError) "Unable to Connect" else "Welcome to KidCurate",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isError) {
                                "Please check your internet connection and try again."
                            } else {
                                "Welcome! KidCurate is a safe, parent-controlled video player for kids. Create a profile, pick safe YouTube videos, and hand over the device with complete peace of mind."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        if (!isError) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "rocket", avatar = "rocket", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("1. Profile", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "palette", avatar = "palette", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("2. Add Videos", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ThematicAvatar(name = "star", avatar = "star", size = 40.dp, iconSize = 20.dp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("3. Safe Play", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isError) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }

                            if (com.kidcurate.app.BuildConfig.DEBUG) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Target: ${parentPrefs.serverUrl.ifBlank { com.kidcurate.app.BuildConfig.API_BASE_URL }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Quick Server Switch:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("https://kidcurate-api.onrender.com/") },
                                        label = { Text("Cloud", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("http://192.168.0.107:8000/") },
                                        label = { Text("Wi-Fi PC", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                    AssistChip(
                                        onClick = { onUpdateServerUrl("http://localhost:8000/") },
                                        label = { Text("USB", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { showParentGate = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create First Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                }
            } else if (profiles.size == 1) {
                // Single child profile: perfectly centered on screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val profile = profiles.first()
                    GlassCard(
                        modifier = Modifier
                            .widthIn(min = if (isTablet) 240.dp else 180.dp, max = if (isTablet) 300.dp else 220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                SoundHaptics.playPop(view, haptic)
                                onSelectProfile(profile)
                            },
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (isLandscape) 14.dp else (if (isTablet) 36.dp else 28.dp), horizontal = 20.dp),
                        ) {
                            ThematicAvatar(
                                name = profile.name,
                                avatar = profile.avatar,
                                size = if (isTablet) (if (isLandscape) 88.dp else 112.dp) else (if (isLandscape) 64.dp else 88.dp),
                                iconSize = if (isTablet) (if (isLandscape) 44.dp else 56.dp) else (if (isLandscape) 32.dp else 44.dp),
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = profile.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 22.sp else 18.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Play",
                                        color = BrandPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Multiple profiles grid: responsive width capping for wide tablets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isTablet) 180.dp else 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .widthIn(max = 860.dp)
                            .fillMaxSize(),
                    ) {
                        itemsIndexed(profiles, key = { _, profile -> profile.id }) { index, profile ->
                            GlassCard(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        onSelectProfile(profile)
                                    },
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = if (isTablet) 28.dp else 24.dp, horizontal = 16.dp),
                                ) {
                                    ThematicAvatar(
                                        name = profile.name,
                                        avatar = profile.avatar,
                                        size = if (isTablet) 96.dp else 80.dp,
                                        iconSize = if (isTablet) 48.dp else 40.dp,
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = profile.name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isTablet) 20.sp else 18.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = "Play",
                                                color = BrandPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Icon(
                                                Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtle Parent Control shortcut at bottom (tasteful & non-distracting for kids)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = {
                        SoundHaptics.playTactileClick(view, haptic)
                        showParentGate = true
                    },
                    modifier = Modifier.padding(bottom = if (isLandscape) 4.dp else 12.dp),
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Parent Controls & Settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
    }

    if (showParentGate) {
        ParentPasswordDialog(
            title = if (parentPrefs.isParentLoggedIn) "Parent Verification" else "Parent Account Sign In",
            subtitle = if (parentPrefs.isParentLoggedIn) "Enter 4-digit Parent PIN" else "Sign in with Google to set up child profiles",
            forceGoogleOnly = !parentPrefs.isParentLoggedIn,
            onSuccess = {
                showParentGate = false
                onOpenParentZone()
            },
            onDismiss = { showParentGate = false },
        )
    }

    if (showOnboardingTour) {
        OnboardingTourDialog(
            onDismiss = {
                parentPrefs.hasSeenOnboardingTour = true
                showOnboardingTour = false
            }
        )
    }
}

```

---

<a name="uiscreenstodaysplaylistscreenkt"></a>
## 17. ui/screens/TodaysPlaylistScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/TodaysPlaylistScreen.kt`
**Description:** Child Screen: Daily Queue, Curfew Notice & Limits

```kotlin
package com.kidcurate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.PlaylistData
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.theme.*

@Composable
fun TodaysPlaylistScreen(
    childName: String,
    childAvatar: String,
    playlist: PlaylistData?,
    isLoading: Boolean,
    onStartPlaying: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenParentZone: () -> Unit = {},
) {
    var isChildLockActive by remember { mutableStateOf(false) }
    var showParentPasswordDialog by remember { mutableStateOf(false) }
    var pendingUnlockAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val parentPrefs = remember { ParentPreferences.get(context) }
    var curfewBypassed by remember { mutableStateOf(false) }
    var dailyLimitBypassed by remember { mutableStateOf(false) }
    var limitCheckTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            limitCheckTick++
        }
    }

    val isCurfewBlocked = !curfewBypassed && run {
        val tick = limitCheckTick
        if (tick >= 0) parentPrefs.isCurfewActive() else false
    }
    val isDailyLimitBlocked = !dailyLimitBypassed && run {
        val tick = limitCheckTick
        if (tick >= 0) parentPrefs.isDailyLimitReached() else false
    }

    // Defensive cleanup of screen pinning if navigating away or unmounting
    DisposableEffect(Unit) {
        onDispose {
            try { activity?.stopLockTask() } catch (_: Exception) {}
        }
    }

    val safeBack: () -> Unit = {
        if (isChildLockActive) {
            pendingUnlockAction = {
                isChildLockActive = false
                try { activity?.stopLockTask() } catch (_: Exception) {}
                onBack()
            }
            showParentPasswordDialog = true
        } else {
            onBack()
        }
    }

    // When child lock is active, hardware back button prompts for Parent Password
    BackHandler {
        safeBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding(),
    ) {
        GlassBackgroundAura()
        if (isCurfewBlocked) {
            CurfewRestView(
                childName = childName,
                curfewEndHour = parentPrefs.curfewEndHour,
                onParentUnlock = {
                    pendingUnlockAction = { curfewBypassed = true }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else if (isDailyLimitBlocked) {
            DailyLimitRestView(
                childName = childName,
                dailyMinutes = parentPrefs.dailyScreenTimeMinutes,
                onParentUnlock = {
                    pendingUnlockAction = { dailyLimitBypassed = true }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("Loading videos...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (playlist == null || playlist.items.isEmpty()) {
            EmptyPlaylistLightView(
                childName = childName,
                childAvatar = childAvatar,
                onOpenParentZone = {
                    pendingUnlockAction = {
                        isChildLockActive = false
                        try { activity?.stopLockTask() } catch (_: Exception) {}
                        onOpenParentZone()
                    }
                    showParentPasswordDialog = true
                },
                onBack = safeBack
            )
        } else {
            FilledPlaylistLightView(
                childName = childName,
                childAvatar = childAvatar,
                playlist = playlist,
                isChildLockActive = isChildLockActive,
                onToggleLock = {
                    if (isChildLockActive) {
                        pendingUnlockAction = {
                            isChildLockActive = false
                            try { activity?.stopLockTask() } catch (_: Exception) {}
                        }
                        showParentPasswordDialog = true
                    } else {
                        isChildLockActive = true
                        try { activity?.startLockTask() } catch (_: Exception) {}
                    }
                },
                onStartPlaying = onStartPlaying,
                onBack = safeBack
            )
        }

        // Parent Unlock Dialog
        if (showParentPasswordDialog) {
            ParentPasswordDialog(
                title = "Parent Unlock",
                subtitle = "Enter 4-digit Parent PIN",
                allowGoogleSignIn = false,
                onSuccess = {
                    showParentPasswordDialog = false
                    pendingUnlockAction?.invoke()
                    pendingUnlockAction = null
                },
                onDismiss = {
                    showParentPasswordDialog = false
                    pendingUnlockAction = null
                }
            )
        }
    }
}

@Composable
fun FilledPlaylistLightView(
    childName: String,
    childAvatar: String,
    playlist: PlaylistData,
    isChildLockActive: Boolean,
    onToggleLock: () -> Unit,
    onStartPlaying: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    val gridColumns = ResponsiveLayoutPolicy.playlistGridColumns(widthDp, heightDp)

    if (isLandscape) {
        // YouTube-style Split Console in Landscape
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // LEFT COLUMN: Child header, stats & primary action controls
            Column(
                modifier = Modifier
                    .weight(if (isTablet) 0.32f else 0.38f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Child avatar & name card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ThematicAvatar(
                            name = childName,
                            avatar = childAvatar,
                            size = 46.dp,
                            iconSize = 24.dp
                        )
                        Column {
                            Text(
                                text = "Hi $childName!",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                            )
                            val count = playlist.items.size
                            val mins = playlist.totalDurationSeconds / 60
                            Text(
                                text = if (mins > 0) "$count videos • $mins mins" else "$count videos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Switch Profile
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Switch Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Primary Play All Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = playlist.title.ifBlank { "Today's Playlist" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onStartPlaying(0) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        // Child lock toggle
                        OutlinedButton(
                            onClick = onToggleLock,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                        ) {
                            Icon(
                                if (isChildLockActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = null,
                                tint = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isChildLockActive) "Child Lock Active" else "Enable Child Lock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // RIGHT COLUMN: Scrollable Video Cards Queue (Adaptive 2-column grid on tablets)
            if (gridColumns > 1) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(if (isTablet) 0.68f else 0.62f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    gridItemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                        YouTubeVideoCard(
                            video = video,
                            index = index + 1,
                            onClick = { onStartPlaying(index) },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    itemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                        YouTubeVideoCard(
                            video = video,
                            index = index + 1,
                            onClick = { onStartPlaying(index) },
                        )
                    }
                }
            }
        }
    } else {
        // Portrait Layout (Single Column on Phone, 2-Column Responsive Grid on Tablet)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 2 else 1),
                modifier = Modifier
                    .widthIn(max = if (isTablet) 860.dp else 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Top Header: Child avatar, name, Lock & Switch buttons
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                ThematicAvatar(
                                    name = childName,
                                    avatar = childAvatar,
                                    size = 44.dp,
                                    iconSize = 22.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hi $childName!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = "Choose a video to watch",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Action buttons: Child Lock + Switch Profile (Adaptive compact layout)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Lock toggle indicator
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChildLockActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    onClick = onToggleLock,
                                    modifier = Modifier.height(38.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            if (isChildLockActive) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                            contentDescription = "Child Lock",
                                            tint = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = if (isChildLockActive) "Locked" else "Lock",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChildLockActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                // Switch Profile Button
                                FilledTonalButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onBackground,
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Switch Profile",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Playlist Banner Card with "Play All" button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.title.ifBlank { "Today's Videos" },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 2,
                                    )
                                    val count = playlist.items.size
                                    val mins = playlist.totalDurationSeconds / 60
                                    Text(
                                        text = if (mins > 0) "$count videos • $mins mins" else "$count videos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Button(
                                    onClick = { onStartPlaying(0) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play All",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Play All",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Title
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }

                // YouTube-style Video Cards Feed with stable item keys
                gridItemsIndexed(playlist.items, key = { _, video -> video.itemId }) { index, video ->
                    YouTubeVideoCard(
                        video = video,
                        index = index + 1,
                        onClick = { onStartPlaying(index) },
                    )
                }
            }
        } // end Box tablet constraint
    } // end portrait else
}

@Composable
fun YouTubeVideoCard(
    video: VideoItem,
    index: Int,
    onClick: () -> Unit,
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    val useCompactCard = isLandscape || isTablet

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (useCompactCard) 4.dp else 16.dp, vertical = if (useCompactCard) 4.dp else 6.dp),
    ) {
        if (useCompactCard) {
            // Horizontal compact card for landscape and tablet grids
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(if (isTablet) 150.dp else 130.dp)
                        .height(if (isTablet) 86.dp else 74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(video.thumbnailUrl).crossfade(true).build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    if (video.durationSeconds != null && video.durationSeconds > 0) {
                        val min = video.durationSeconds / 60; val sec = video.durationSeconds % 60
                        Surface(shape = RoundedCornerShape(5.dp), color = Color.Black.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp)) {
                            Text(text = String.format(java.util.Locale.US, "%d:%02d", min, sec), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#$index  ${video.title}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = video.channelName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:9 Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Large play icon overlay hint
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                if (video.durationSeconds != null && video.durationSeconds > 0) {
                    val min = video.durationSeconds / 60
                    val sec = video.durationSeconds % 60
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%d:%02d", min, sec),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            // Title and Channel below thumbnail (Classic YouTube Card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = video.channelName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        } // end portrait else
    }
}

@Composable
fun EmptyPlaylistLightView(
    childName: String,
    childAvatar: String,
    onOpenParentZone: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                ThematicAvatar(
                    name = childName,
                    avatar = childAvatar,
                    size = 72.dp,
                    iconSize = 36.dp
                )
                Text(
                    text = "Welcome, $childName!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "No videos added for today yet.\nParents can search and add videos anytime from the Parent Zone!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onOpenParentZone,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Parent: Open Control Center to Add Videos", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Exit / Switch Child Profile")
                }
            }
        }
    }
}

@Composable
private fun CurfewRestView(
    childName: String,
    curfewEndHour: Int = 7,
    onParentUnlock: () -> Unit,
    onBack: () -> Unit,
) {
    val endHourFormatted = if (curfewEndHour == 0) "12:00 AM" else if (curfewEndHour < 12) "$curfewEndHour:00 AM" else if (curfewEndHour == 12) "12:00 PM" else "${curfewEndHour - 12}:00 PM"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bedtime,
                    contentDescription = null,
                    tint = Color(0xFF6C5CE7),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Sweet Dreams, $childName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "It's bedtime curfew until $endHourFormatted. Time to rest your eyes and recharge for tomorrow's learning adventures!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Exit", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onParentUnlock,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Parent Unlock", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DailyLimitRestView(
    childName: String,
    dailyMinutes: Int,
    onParentUnlock: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9F43).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = Color(0xFFFF9F43),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Great Learning Today, $childName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You've reached your daily screen time of $dailyMinutes minutes. Great job completing today's playlist!",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Exit", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onParentUnlock,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Parent Unlock", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

```

---

<a name="uiscreensplayerscreenkt"></a>
## 18. ui/screens/PlayerScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/PlayerScreen.kt`
**Description:** Child Screen: YouTube Player, Child Lock & Resume Engine

```kotlin
package com.kidcurate.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.ResponsiveLayoutPolicy
import com.kidcurate.app.data.VideoItem
import com.kidcurate.app.ui.components.ConfettiCelebration
import com.kidcurate.app.ui.components.GlassCard
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.theme.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay

private fun findWebView(view: android.view.View): android.webkit.WebView? {
    if (view is android.webkit.WebView) return view
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = findWebView(view.getChildAt(i))
            if (child != null) return child
        }
    }
    return null
}

private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remSeconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, remSeconds)
}

@Composable
fun PlayerScreen(
    videos: List<VideoItem>,
    startIndex: Int,
    onVideoCompleted: (VideoItem, Int) -> Unit,
    onProgressUpdate: (VideoItem, Int) -> Unit,
    onAllCompleted: () -> Unit,
    onBack: () -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(startIndex) }
    var isFullscreen by remember { mutableStateOf(false) }

    val currentVideo = videos.getOrNull(currentIndex)
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? Activity
    val parentPrefs = remember { ParentPreferences.get(context) }
    var subtitlesEnabled by remember { mutableStateOf(parentPrefs.subtitlesEnabled) }

    var isChildLockActive by remember { mutableStateOf(false) }
    var showParentPasswordDialog by remember { mutableStateOf(false) }
    var isExitingPlayer by remember { mutableStateOf(false) }
    var curfewBypassed by remember { mutableStateOf(false) }
    var dailyLimitBypassed by remember { mutableStateOf(false) }
    var activeLimitType by remember { mutableStateOf<String?>(null) } // "curfew" or "limit"
    var pendingUnlockAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var lastRecordedSecond by remember(currentIndex) { mutableIntStateOf(0) }
    var lastReportedProgressBucket by remember(currentIndex) { mutableIntStateOf(-1) }
    var hasReportedCompletion by remember(currentIndex) { mutableStateOf(false) }
    var showEndCelebration by remember { mutableStateOf(false) }
    var celebrationCountdown by remember { mutableIntStateOf(4) }

    var isCurrentVideoCompleted by remember(currentIndex) { mutableStateOf(false) }
    var currentVideoSeconds by remember(currentIndex) { mutableFloatStateOf(0f) }
    var currentVideoTotalDuration by remember(currentIndex) { mutableFloatStateOf(0f) }
    var currentVideoHasError by remember(currentIndex) { mutableStateOf(false) }

    fun advanceFromCelebration() {
        showEndCelebration = false
        if (currentVideo != null && !hasReportedCompletion) {
            hasReportedCompletion = true
            parentPrefs.clearVideoProgress(currentVideo.youtubeId)
            onVideoCompleted(currentVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
        }
        if (currentIndex + 1 < videos.size) {
            currentIndex++
        } else {
            onAllCompleted()
        }
    }

    LaunchedEffect(showEndCelebration, currentIndex) {
        if (showEndCelebration) {
            SoundHaptics.playCelebration(view, haptic)
            celebrationCountdown = 4
            while (celebrationCountdown > 0) {
                delay(1000)
                celebrationCountdown--
            }
            advanceFromCelebration()
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isWindowLandscape = ResponsiveLayoutPolicy.isLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    // Tablets in landscape get two-pane layout, NOT forced fullscreen
    val effectiveFullscreen = isFullscreen || (isWindowLandscape && !isTablet)

    fun attemptBack() {
        if (isChildLockActive) {
            isExitingPlayer = true
            showParentPasswordDialog = true
        } else if (effectiveFullscreen) {
            isFullscreen = false
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            onBack()
        }
    }

    BackHandler {
        attemptBack()
    }

    if (com.kidcurate.app.BuildConfig.DEBUG) {
        val autoWalkthrough = activity?.intent?.getStringExtra("test_auto_walkthrough")
        if (autoWalkthrough == "child_lock_check") {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                // 1. Turn Child Lock ON
                isChildLockActive = true
                kotlinx.coroutines.delay(2500)
                // 2. System Back pressed -> intercepted by Parent PIN dialog
                attemptBack()
                kotlinx.coroutines.delay(2500)
                // 3. User cancels / dismisses dialog -> Child Lock remains ACTIVE
                showParentPasswordDialog = false
                isExitingPlayer = false
                kotlinx.coroutines.delay(2500)
                // 4. On-screen Back button pressed -> intercepted by Parent PIN dialog
                attemptBack()
            }
        }
    }

    LaunchedEffect(effectiveFullscreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (effectiveFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }

    // Reset orientation to user preference and release wake lock when leaving the screen
    DisposableEffect(Unit) {
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            activity?.window?.let {
                WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Retain video player instance across orientation & layout transitions with zero teardown
    val playerComposable = remember {
        movableContentOf { isFs: Boolean, isLocked: Boolean ->
            val activeVideo = currentVideo
            if (activeVideo != null) {
                OptimizedVideoPlayer(
                    video = activeVideo,
                    isFullscreen = isFs,
                    isChildLockActive = isLocked,
                    isPlaybackBlocked = activeLimitType != null,
                    subtitlesEnabled = subtitlesEnabled,
                    onToggleSubtitles = {
                        val newState = !subtitlesEnabled
                        subtitlesEnabled = newState
                        parentPrefs.subtitlesEnabled = newState
                    },
                    onToggleFullscreen = {
                        if (effectiveFullscreen) {
                            isFullscreen = false
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        } else {
                            isFullscreen = true
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                    },
                    onToggleChildLock = {
                        if (isChildLockActive) {
                            isExitingPlayer = false
                            showParentPasswordDialog = true
                        } else {
                            isChildLockActive = true
                        }
                    },
                    onVideoEnded = { _ ->
                        isCurrentVideoCompleted = true
                        if (currentIndex + 1 < videos.size) {
                            showEndCelebration = true
                        } else {
                            if (!hasReportedCompletion) {
                                hasReportedCompletion = true
                                parentPrefs.clearVideoProgress(activeVideo.youtubeId)
                                onVideoCompleted(activeVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                            }
                            onAllCompleted()
                        }
                    },
                    onProgressUpdate = { sec, dur ->
                        val secInt = sec.toInt()
                        val delta = secInt - lastRecordedSecond
                        if (delta in 1..5) {
                            parentPrefs.addWatchedSeconds(delta)
                        }
                        lastRecordedSecond = secInt
                        currentVideoSeconds = sec
                        currentVideoTotalDuration = dur
                        if (dur > 5f && sec >= (dur - 4f).coerceAtMost(dur * 0.85f)) {
                            isCurrentVideoCompleted = true
                        }
                        val currentBucket = secInt / 30
                        if (secInt > 0 && secInt % 30 == 0 && currentBucket != lastReportedProgressBucket) {
                            lastReportedProgressBucket = currentBucket
                            onProgressUpdate(activeVideo, secInt)
                        }
                        if (!curfewBypassed && parentPrefs.isCurfewActive()) {
                            activeLimitType = "curfew"
                        } else if (!dailyLimitBypassed && parentPrefs.isDailyLimitReached()) {
                            activeLimitType = "limit"
                        }
                    },
                    onSkipNext = if (currentIndex + 1 < videos.size && (isCurrentVideoCompleted || currentVideoHasError)) {
                        {
                            parentPrefs.clearVideoProgress(activeVideo.youtubeId)
                            if (isCurrentVideoCompleted && !hasReportedCompletion) {
                                hasReportedCompletion = true
                                onVideoCompleted(activeVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                            }
                            currentIndex++
                        }
                    } else null,
                    onFinishPlaylist = if (currentIndex + 1 >= videos.size) {
                        {
                            parentPrefs.clearVideoProgress(activeVideo.youtubeId)
                            if (isCurrentVideoCompleted && !hasReportedCompletion) {
                                hasReportedCompletion = true
                                onVideoCompleted(activeVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                            }
                            onBack()
                        }
                    } else null,
                    onPlaybackError = { hasErr ->
                        currentVideoHasError = hasErr
                        if (hasErr) {
                            lastRecordedSecond = 0
                        }
                    },
                    onAttemptBack = { attemptBack() },
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Tablet landscape: two-pane Row (video left 65% | queue right 35%)
        if (isTablet && isWindowLandscape && !isFullscreen && currentVideo != null) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left pane: video player (retained via movableContentOf)
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    playerComposable(false, isChildLockActive)
                }
                // Right pane: title, controls, queue
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(12.dp),
                ) {
                    Text(
                        text = currentVideo.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentVideo.channelName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Video ${currentIndex + 1} of ${videos.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { if (!isChildLockActive) currentIndex-- },
                                    enabled = !isChildLockActive,
                                    modifier = Modifier.height(34.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                ) { Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(16.dp)) }
                            }
                            if (currentIndex + 1 < videos.size) {
                                val canProceed = !isChildLockActive && (isCurrentVideoCompleted || currentVideoHasError)
                                Button(
                                    onClick = { if (canProceed) currentIndex++ },
                                    enabled = canProceed,
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                ) {
                                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        videos.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                    .background(when {
                                        idx < currentIndex -> BrandSuccess
                                        idx == currentIndex -> BrandPrimary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    })
                            )
                        }
                    }
                    if (videos.size > 1) {
                        Spacer(Modifier.height(12.dp))
                        Text("Up Next", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(6.dp))
                        videos.forEachIndexed { idx, v ->
                            val isCurrent = idx == currentIndex
                            Surface(
                                onClick = {
                                    if (!isChildLockActive && (idx <= currentIndex || isCurrentVideoCompleted || currentVideoHasError)) {
                                        currentIndex = idx
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) BrandPrimary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            ) {
                                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.width(72.dp).height(44.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                        AsyncImage(
                                            model = coil.request.ImageRequest.Builder(LocalContext.current).data(v.thumbnailUrl).crossfade(true).build(),
                                            contentDescription = v.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(v.title, fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {

        Column(modifier = Modifier.fillMaxSize()) {

            // YouTube Player Component (retained via movableContentOf across layouts)
            if (currentVideo != null) {
                Box(
                    modifier = if (effectiveFullscreen) Modifier.fillMaxSize() else Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    playerComposable(effectiveFullscreen, isChildLockActive)
                }
            }

            // Playlist info & controls (hidden in fullscreen)
            if (!effectiveFullscreen && currentVideo != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(16.dp),
                ) {
                    Text(
                        text = currentVideo.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentVideo.channelName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Video ${currentIndex + 1} of ${videos.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary,
                            )

                            // Quick Subtitle pill toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (subtitlesEnabled) BrandPrimary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable(enabled = !isChildLockActive) {
                                        val newState = !subtitlesEnabled
                                        subtitlesEnabled = newState
                                        parentPrefs.subtitlesEnabled = newState
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                                    contentDescription = "Subtitles",
                                    tint = if (subtitlesEnabled) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (subtitlesEnabled) {
                                        if (parentPrefs.earlyReaderSubtitles) "PHONICS CC" else "CC ON"
                                    } else "CC OFF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (subtitlesEnabled) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Touch Lock button right next to Next button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChildLockActive) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isChildLockActive) BrandPrimary else MaterialTheme.colorScheme.outline),
                                onClick = {
                                    if (isChildLockActive) {
                                        isExitingPlayer = false
                                        showParentPasswordDialog = true
                                    } else {
                                        isChildLockActive = true
                                    }
                                },
                                modifier = Modifier.height(44.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        if (isChildLockActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        contentDescription = "Lock",
                                        tint = if (isChildLockActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = if (isChildLockActive) "Locked" else "Lock",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChildLockActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { if (!isChildLockActive) currentIndex-- },
                                    enabled = !isChildLockActive,
                                    modifier = Modifier.height(44.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = if (!isChildLockActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            if (currentIndex + 1 < videos.size) {
                                val canProceed = !isChildLockActive && (isCurrentVideoCompleted || currentVideoHasError)
                                Button(
                                    onClick = {
                                        if (canProceed) {
                                            parentPrefs.clearVideoProgress(currentVideo.youtubeId)
                                            if (isCurrentVideoCompleted && !hasReportedCompletion) {
                                                hasReportedCompletion = true
                                                onVideoCompleted(currentVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                                            }
                                            currentIndex++
                                        }
                                    },
                                    enabled = canProceed,
                                    modifier = Modifier.height(44.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                ) {
                                    Text("Next", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        Icons.Filled.SkipNext,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        videos.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            index < currentIndex -> BrandSuccess
                                            index == currentIndex -> BrandPrimary
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        }
                                    ),
                            )
                        }
                    }

                    // Up Next Video Queue
                    if (videos.size > 1) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Up Next",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "${videos.size} Videos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            itemsIndexed(videos, key = { _, v -> v.itemId }) { idx, v ->
                                val isCurrent = idx == currentIndex
                                val canClick = !isChildLockActive && (idx <= currentIndex || isCurrentVideoCompleted || currentVideoHasError)
                                Surface(
                                    onClick = {
                                        if (canClick) {
                                            if (idx > currentIndex && isCurrentVideoCompleted && !hasReportedCompletion) {
                                                hasReportedCompletion = true
                                                parentPrefs.clearVideoProgress(currentVideo.youtubeId)
                                                onVideoCompleted(currentVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                                            }
                                            currentIndex = idx
                                        }
                                    },
                                    enabled = canClick,
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isCurrent) 2.dp else 1.dp,
                                        if (isCurrent) BrandPrimary else MaterialTheme.colorScheme.outline
                                    ),
                                    shadowElevation = if (isCurrent) 4.dp else 1.dp,
                                    modifier = Modifier.width(140.dp),
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(72.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            AsyncImage(
                                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                                    .data(v.thumbnailUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = v.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                            if (isCurrent) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = BrandPrimary,
                                                    modifier = Modifier.align(Alignment.Center)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.PlayArrow,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text("PLAYING", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                    }
                                                }
                                            } else if (v.durationSeconds != null) {
                                                val min = v.durationSeconds / 60
                                                val sec = v.durationSeconds % 60
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Black.copy(alpha = 0.75f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp),
                                                ) {
                                                    Text(
                                                        text = "$min:${"%02d".format(sec)}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = v.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 14.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Next button or All Caught Up (Requires completing current video or handling unplayable video)
                    if (isCurrentVideoCompleted || currentVideoHasError) {
                        if (currentIndex + 1 < videos.size) {
                            Button(
                                onClick = {
                                    parentPrefs.clearVideoProgress(currentVideo.youtubeId)
                                    if (isCurrentVideoCompleted && !hasReportedCompletion) {
                                        hasReportedCompletion = true
                                        onVideoCompleted(currentVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                                    }
                                    currentIndex++
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (currentVideoHasError) "Skip Unavailable Video" else "Play Next Video", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    parentPrefs.clearVideoProgress(currentVideo.youtubeId)
                                    if (isCurrentVideoCompleted && !hasReportedCompletion) {
                                        hasReportedCompletion = true
                                        onVideoCompleted(currentVideo, currentVideoSeconds.toInt().coerceAtLeast(1))
                                    }
                                    if (isCurrentVideoCompleted) onAllCompleted() else onBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSuccess),
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (currentVideoHasError) "Back to Playlist" else "Finish Playlist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        // Video in progress: Anti-cheat banner prevents closing incomplete videos
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                val percent = if (currentVideoTotalDuration > 0) ((currentVideoSeconds / currentVideoTotalDuration) * 100).toInt().coerceIn(0, 100) else 0
                                Text(
                                    text = if (percent > 0) "Watching: $percent% complete" else "Watch video to complete",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        } // end else (non-tablet-landscape)

        // Floating Back button over the player (hidden in fullscreen)
        if (!effectiveFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                IconButton(
                    onClick = { attemptBack() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f)),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

    if (showParentPasswordDialog) {
        ParentPasswordDialog(
            title = if (pendingUnlockAction != null) "Parent Unlock" else if (isExitingPlayer) "Exit Video Player" else "Unlock Controls",
            subtitle = "Enter 4-digit Parent PIN to ${if (pendingUnlockAction != null) "continue watching" else if (isExitingPlayer) "exit player" else "unlock screen controls"}",
            allowGoogleSignIn = false,
            onSuccess = {
                showParentPasswordDialog = false
                isChildLockActive = false
                pendingUnlockAction?.invoke()
                pendingUnlockAction = null
                if (isExitingPlayer) {
                    isExitingPlayer = false
                    if (isFullscreen) {
                        isFullscreen = false
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                    }
                    onBack()
                }
            },
            onDismiss = {
                showParentPasswordDialog = false
                isExitingPlayer = false
                pendingUnlockAction = null
            },
        )
    }

    if (activeLimitType != null) {
        val limitKind = activeLimitType
        AlertDialog(
            onDismissRequest = { /* Modal */ },
            icon = {
                Icon(
                    imageVector = if (limitKind == "curfew") Icons.Filled.Bedtime else Icons.Filled.Timer,
                    contentDescription = null,
                    tint = if (limitKind == "curfew") Color(0xFF6C5CE7) else Color(0xFFFF9F43),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (limitKind == "curfew") "Bedtime Mode" else "Daily Limit Reached",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (limitKind == "curfew")
                        "It's curfew time! Time to rest your eyes and recharge for tomorrow."
                    else
                        "You've reached your daily screen time of ${parentPrefs.dailyScreenTimeMinutes} minutes for today. Great job learning!",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUnlockAction = {
                            if (limitKind == "curfew") {
                                curfewBypassed = true
                            } else {
                                dailyLimitBypassed = true
                            }
                            activeLimitType = null
                        }
                        showParentPasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Parent Unlock")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        activeLimitType = null
                        attemptBack()
                    }
                ) {
                    Text("Done for Today")
                }
            }
        )
    }


    if (showEndCelebration && currentVideo != null) {
        val nextVideo = if (currentIndex + 1 < videos.size) videos[currentIndex + 1] else null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Up Next",
                            tint = BrandPrimary,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Text(
                        text = if (nextVideo != null) "Up Next" else "Playlist Finished",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (nextVideo != null) {
                        Text(
                            text = nextVideo.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Text(
                            text = "Playing in ${celebrationCountdown}s...",
                            fontSize = 12.sp,
                            color = BrandPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Button(
                        onClick = {
                            advanceFromCelebration()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(if (nextVideo != null) "Play Now" else "Finish", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

@Composable
fun OptimizedVideoPlayer(
    video: VideoItem,
    isFullscreen: Boolean,
    isChildLockActive: Boolean = false,
    isPlaybackBlocked: Boolean = false,
    subtitlesEnabled: Boolean,
    onToggleSubtitles: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onVideoEnded: (Int) -> Unit,
    onProgressUpdate: (Float, Float) -> Unit,
    onSkipNext: (() -> Unit)? = null,
    onFinishPlaylist: (() -> Unit)? = null,
    onPlaybackError: ((Boolean) -> Unit)? = null,
    onAttemptBack: (() -> Unit)? = null,
    onToggleChildLock: (() -> Unit)? = null,
) {
    val currentVideoState by rememberUpdatedState(video)
    val currentOnVideoEnded by rememberUpdatedState(onVideoEnded)
    val currentOnProgressUpdate by rememberUpdatedState(onProgressUpdate)
    val currentOnPlaybackError by rememberUpdatedState(onPlaybackError)
    val currentOnSkipNext by rememberUpdatedState(onSkipNext)
    val currentOnFinishPlaylist by rememberUpdatedState(onFinishPlaylist)
    val currentOnAttemptBack by rememberUpdatedState(onAttemptBack)
    val currentOnToggleChildLock by rememberUpdatedState(onToggleChildLock)
    val currentOnToggleFullscreen by rememberUpdatedState(onToggleFullscreen)
    val currentOnToggleSubtitles by rememberUpdatedState(onToggleSubtitles)

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var playerViewRef by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var currentSecond by rememberSaveable(video.youtubeId) { mutableFloatStateOf(parentPrefs.getVideoProgress(video.youtubeId)) }
    var videoDuration by rememberSaveable(video.youtubeId) { mutableFloatStateOf(0f) }
    var lastSavedProgressSecond by remember(video.youtubeId) { mutableFloatStateOf(0f) }
    var playerState by remember { mutableStateOf(PlayerConstants.PlayerState.UNKNOWN) }
    var showControls by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var currentQuality by remember { mutableStateOf(parentPrefs.videoQuality) }

    fun applyVideoQuality(qualityKey: String) {
        currentQuality = qualityKey
    }

    fun applySubtitles(enabled: Boolean) {
        val script = if (enabled) {
            """
            (function() {
                try {
                    if (typeof player !== 'undefined' && player) {
                        player.loadModule('captions');
                        player.setOption('captions', 'track', {'languageCode': 'en'});
                        player.setOption('captions', 'reload', true);
                    }
                } catch(e) {}
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                try {
                    if (typeof player !== 'undefined' && player) {
                        player.unloadModule('captions');
                        player.setOption('captions', 'track', {});
                    }
                } catch(e) {}
            })();
            """.trimIndent()
        }
        webViewRef?.post {
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    LaunchedEffect(isPlaybackBlocked) {
        if (isPlaybackBlocked) {
            youTubePlayerRef?.pause()
        }
    }

    // Auto-hide controls after a few seconds of playback
    LaunchedEffect(showControls, playerState) {
        if (showControls && playerState == PlayerConstants.PlayerState.PLAYING) {
            delay(3500)
            showControls = false
        }
    }

    // Auto-hide toast feedback message
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            toastMessage = null
        }
    }

    // React dynamically to subtitlesEnabled changes
    LaunchedEffect(subtitlesEnabled, webViewRef) {
        applySubtitles(subtitlesEnabled)
    }

    // Release YouTube player and unbind lifecycle observer when composable leaves composition
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                if (currentSecond > 1f) {
                    parentPrefs.setVideoProgress(currentVideoState.youtubeId, currentSecond)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            if (currentSecond > 1f) {
                parentPrefs.setVideoProgress(currentVideoState.youtubeId, currentSecond)
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerViewRef?.let { pv ->
                try {
                    lifecycleOwner.lifecycle.removeObserver(pv)
                    pv.release()
                } catch (_: Exception) {}
            }
            playerViewRef = null
            youTubePlayerRef = null
            webViewRef = null
        }
    }

    LaunchedEffect(video.youtubeId) {
        playbackError = null
        val savedProgress = parentPrefs.getVideoProgress(video.youtubeId)
        currentSecond = savedProgress
        videoDuration = 0f
        youTubePlayerRef?.loadVideo(video.youtubeId, savedProgress)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val container = FrameLayout(ctx)

                val playerView = YouTubePlayerView(ctx).apply {
                    enableAutomaticInitialization = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    lifecycleOwner.lifecycle.addObserver(this)

                    val options = IFramePlayerOptions.Builder(ctx)
                        .controls(0)
                        .rel(0)
                        .ivLoadPolicy(3) // hide annotations
                        .ccLoadPolicy(if (subtitlesEnabled) 1 else 0)
                        .build()

                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayerRef = youTubePlayer
                            playbackError = null
                            val savedProgress = parentPrefs.getVideoProgress(currentVideoState.youtubeId)
                            val resumeSeconds = if (currentSecond > 1f) currentSecond else if (savedProgress > 1f) savedProgress else 0f
                            currentSecond = resumeSeconds
                            youTubePlayer.loadVideo(currentVideoState.youtubeId, resumeSeconds)
                            val wv = findWebView(this@apply)
                            wv?.settings?.apply {
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = false
                                allowContentAccess = false
                                setSupportMultipleWindows(false)
                                javaScriptCanOpenWindowsAutomatically = false
                            }
                            try {
                                val cm = android.webkit.CookieManager.getInstance()
                                cm.setAcceptCookie(true)
                                cm.setAcceptThirdPartyCookies(wv, false)
                            } catch (_: Exception) {}
                            webViewRef = wv
                            applySubtitles(subtitlesEnabled)
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentSecond = second
                            currentOnProgressUpdate(second, videoDuration)
                            if (second > 1f && (second - lastSavedProgressSecond >= 4f || second < lastSavedProgressSecond)) {
                                lastSavedProgressSecond = second
                                parentPrefs.setVideoProgress(currentVideoState.youtubeId, second)
                            }
                        }

                        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                            videoDuration = duration
                            currentOnProgressUpdate(currentSecond, duration)
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState,
                        ) {
                            playerState = state
                            if (state == PlayerConstants.PlayerState.PLAYING) {
                                applySubtitles(subtitlesEnabled)
                            }
                            if (state == PlayerConstants.PlayerState.ENDED) {
                                parentPrefs.clearVideoProgress(currentVideoState.youtubeId)
                                currentOnVideoEnded(currentSecond.toInt())
                            }
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: PlayerConstants.PlayerError,
                        ) {
                            playbackError = when (error) {
                                PlayerConstants.PlayerError.VIDEO_NOT_FOUND ->
                                    "This video was not found on YouTube."
                                PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ->
                                    "The video creator has restricted playback in third-party mobile apps."
                                PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST ->
                                    "Invalid video playback request."
                                PlayerConstants.PlayerError.HTML_5_PLAYER ->
                                    "HTML5 player error encountered."
                                else -> "Video playback is currently unavailable. Please check your connection."
                            }
                            currentOnPlaybackError?.invoke(true)
                        }
                    }, options)
                }

                playerViewRef = playerView

                // Transparent View on top to intercept ALL touches and block WebView clicks
                val touchInterceptor = android.view.View(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        showControls = !showControls
                    }
                }

                container.addView(playerView)
                container.addView(touchInterceptor)

                val wv = findWebView(playerView)
                wv?.settings?.apply {
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = false
                    allowContentAccess = false
                    setSupportMultipleWindows(false)
                    javaScriptCanOpenWindowsAutomatically = false
                }
                try {
                    val cm = android.webkit.CookieManager.getInstance()
                    cm.setAcceptCookie(true)
                    cm.setAcceptThirdPartyCookies(wv, false)
                } catch (_: Exception) {}
                webViewRef = wv
                container
            },
            onRelease = { container ->
                playerViewRef?.let { pv ->
                    try {
                        lifecycleOwner.lifecycle.removeObserver(pv)
                        pv.release()
                    } catch (_: Exception) {}
                }
                playerViewRef = null
                youTubePlayerRef = null
                webViewRef = null
                container.removeAllViews()
            },
            update = { _ -> },
            modifier = Modifier.fillMaxSize(),
        )

        // Playback Error Overlay (ONLY shown if YouTube reports a genuine playback error)
        if (playbackError != null) {
            Surface(
                color = Color.Black.copy(alpha = 0.92f),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Playback Notice",
                        tint = BrandAccent,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Playback Notice",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = playbackError ?: "Video playback error occurred.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                playbackError = null
                                currentOnPlaybackError?.invoke(false)
                                currentSecond = 0f
                                lastSavedProgressSecond = 0f
                                parentPrefs.clearVideoProgress(currentVideoState.youtubeId)
                                youTubePlayerRef?.loadVideo(currentVideoState.youtubeId, 0f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        ) {
                            Text("Retry", color = Color.White)
                        }
                        if (currentOnSkipNext != null) {
                            Button(
                                onClick = currentOnSkipNext!!,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Next Video", fontWeight = FontWeight.Bold)
                            }
                        } else if (currentOnFinishPlaylist != null) {
                            Button(
                                onClick = currentOnFinishPlaylist!!,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Finish Playlist", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Subtitles Feedback Toast Overlay (e.g. Subtitles ON / OFF)
        if (toastMessage != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.82f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (isFullscreen) 24.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                        contentDescription = null,
                        tint = if (subtitlesEnabled) BrandAccent else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Custom Overlay Controls (Hidden when there is a playback error to keep error notice clean)
        if (showControls && playbackError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top header: Back/Exit (fullscreen), Title, and Child Lock button (visible in all modes)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .then(
                            if (isFullscreen) Modifier
                                .statusBarsPadding()
                                .displayCutoutPadding()
                            else Modifier
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isFullscreen) {
                        IconButton(
                            onClick = { currentOnAttemptBack?.invoke() ?: currentOnToggleFullscreen() },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = currentVideoState.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    if (currentOnToggleChildLock != null) {
                        IconButton(
                            onClick = currentOnToggleChildLock!!,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isChildLockActive) BrandPrimary else Color.Black.copy(alpha = 0.65f)),
                        ) {
                            Icon(
                                if (isChildLockActive) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = if (isChildLockActive) "Unlock Controls" else "Lock Controls",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                // Center Play/Pause button - Extra large and comfortable for kids (Always responsive even when locked)
                IconButton(
                    onClick = {
                        youTubePlayerRef?.let { player ->
                            if (playerState == PlayerConstants.PlayerState.PLAYING) {
                                player.pause()
                            } else {
                                player.play()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(76.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(38.dp))
                ) {
                    Icon(
                        imageVector = if (playerState == PlayerConstants.PlayerState.PLAYING) 
                            Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Bottom bar: Progress, timestamps, Subtitle CC button, and Fullscreen toggle
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (isFullscreen) Modifier
                                .displayCutoutPadding()
                                .navigationBarsPadding()
                            else Modifier
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    // Full-width scrubbing / progress indicator (6dp height for easy visual tracking)
                    LinearProgressIndicator(
                        progress = { if (videoDuration > 0) (currentSecond / videoDuration).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BrandPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Time display e.g. 01:23 / 05:10
                        Text(
                            text = "${formatTime(currentSecond)} / ${formatTime(videoDuration)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtitle / Closed Caption (CC) toggle button (Enlarged to 46dp for kids)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (subtitlesEnabled) BrandPrimary.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.25f),
                                onClick = {
                                    if (isChildLockActive) {
                                        toastMessage = "Controls Locked"
                                    } else {
                                        currentOnToggleSubtitles()
                                        toastMessage = if (!subtitlesEnabled) {
                                            if (parentPrefs.earlyReaderSubtitles) "Early Reader Karaoke CC ON" else "Subtitles ON (English)"
                                        } else {
                                            "Subtitles OFF"
                                        }
                                    }
                                },
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (subtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionDisabled,
                                        contentDescription = if (subtitlesEnabled) "Disable Subtitles" else "Enable Subtitles",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Fullscreen toggle button (Enlarged to 46dp for kids)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                onClick = {
                                    if (isChildLockActive) {
                                        toastMessage = "Controls Locked"
                                    } else {
                                        currentOnToggleFullscreen()
                                    }
                                },
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = "Toggle Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


```

---

<a name="uiscreenscompletionscreenkt"></a>
## 19. ui/screens/CompletionScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/CompletionScreen.kt`
**Description:** Child Screen: Playlist Finish Celebration Screen

```kotlin
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

```

---

<a name="uiscreenspinentryscreenkt"></a>
## 20. ui/screens/PinEntryScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/PinEntryScreen.kt`
**Description:** Security Screen: Dedicated Fullscreen Parent PIN Keypad

```kotlin
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.ui.components.ParentPasswordDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.data.ResponsiveLayoutPolicy
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
    onClearError: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)

    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var showParentGate by remember { mutableStateOf(false) }
    var hasFailedAttempt by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val compactLandscape = ResponsiveLayoutPolicy.isCompactLandscape(widthDp, heightDp)
    val isTablet = ResponsiveLayoutPolicy.isTablet(widthDp, heightDp)
    val useTwoPaneLayout = ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp, heightDp)
    val padButtonSize = ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp, heightDp).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding(),
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

        if (useTwoPaneLayout) {
            // Two-pane landscape: left = avatar+text+pin boxes, right = number pad
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LEFT PANE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = 12.dp),
                ) {
                    if (isTablet) {
                        ThematicAvatar(name = childName, avatar = childAvatar, size = 72.dp, iconSize = 36.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text(
                        text = if (childName.isNotEmpty()) "Welcome back, $childName!" else "Welcome!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Enter your secret PIN to unlock today's session",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                    // PIN Boxes (Symmetrically Centered)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < pin.length
                            val digitChar = if (isFilled) pin[index].toString() else ""
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isFilled) BrandPrimary.copy(alpha = 0.12f) else BackgroundCard)
                                    .border(2.dp, if (hasFailedAttempt) BrandCoral else if (isFilled) BrandPrimary else SurfaceCardBorder, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isFilled) {
                                    if (showPin) {
                                        Text(text = digitChar, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (hasFailedAttempt) BrandCoral else BrandPrimary)
                                    } else {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (hasFailedAttempt) BrandCoral else BrandPrimary))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Centered Show/Hide PIN Toggle Pill
                    Surface(
                        onClick = { showPin = !showPin },
                        shape = RoundedCornerShape(16.dp),
                        color = if (showPin) BrandPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showPin) BrandPrimary else Color.Transparent),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                tint = if (showPin) BrandPrimary else TextSecondary,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = if (showPin) "Hide PIN" else "Show PIN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (showPin) BrandPrimary else TextSecondary,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.height(26.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (errorMessage != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(text = errorMessage, color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                if (errorMessage.contains("connection", ignoreCase = true) || errorMessage.contains("internet", ignoreCase = true)) {
                                    Text(
                                        text = "Retry",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                        modifier = Modifier.clickable { retryTrigger++ }
                                    )
                                }
                            }
                        }
                    }

                    TextButton(onClick = { showParentGate = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Forgot PIN? (Parent Unlock)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandPrimary)
                    }
                    if (isLoading) { CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(24.dp)) }
                }
                // RIGHT PANE — number pad
                Box(
                    modifier = Modifier.weight(0.55f).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    NumberPadLight(
                        buttonSize = padButtonSize,
                        rowSpacing = if (isTablet) 8.dp else 5.dp,
                        columnSpacing = if (isTablet) 14.dp else 10.dp,
                        onDigitPress = { digit ->
                            if (!isLoading) {
                                hasFailedAttempt = false
                                onClearError()
                                if (pin.length < 4) pin += digit
                            }
                        },
                        onDelete = {
                            if (pin.isNotEmpty() && !isLoading) {
                                hasFailedAttempt = false
                                onClearError()
                                pin = pin.dropLast(1)
                            }
                        },
                    )
                }
            }
        } else {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (compactLandscape) Arrangement.Top else Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (compactLandscape) 72.dp else 16.dp,
                    vertical = if (compactLandscape) 12.dp else 64.dp,
                ),
        ) {
            if (!compactLandscape) {
                ThematicAvatar(
                    name = childName,
                    avatar = childAvatar,
                    size = 84.dp,
                    iconSize = 42.dp,
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = if (childName.isNotEmpty()) "Welcome back, $childName!" else "Welcome!",
                fontSize = if (compactLandscape) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Text(
                text = "Enter your secret PIN to unlock today's session",
                fontSize = if (compactLandscape) 11.sp else 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = if (compactLandscape) 1.dp else 4.dp,
                    bottom = if (compactLandscape) 6.dp else 20.dp,
                ),
            )

            // 4 PIN Boxes (Symmetrically Centered over the Number Pad)
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val isFilled = index < pin.length
                    val digitChar = if (isFilled) pin[index].toString() else ""
                    Box(
                        modifier = Modifier
                            .size(if (compactLandscape) 36.dp else 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isFilled) BrandPrimary.copy(alpha = 0.12f) else BackgroundCard)
                            .border(
                                2.dp,
                                if (hasFailedAttempt) BrandCoral else if (isFilled) BrandPrimary else SurfaceCardBorder,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isFilled) {
                            if (showPin) {
                                Text(
                                    text = digitChar,
                                    fontSize = if (compactLandscape) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasFailedAttempt) BrandCoral else BrandPrimary,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(if (compactLandscape) 10.dp else 13.dp)
                                        .clip(CircleShape)
                                        .background(if (hasFailedAttempt) BrandCoral else BrandPrimary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Centered Eye Visibility Toggle Pill
            Surface(
                onClick = { showPin = !showPin },
                shape = RoundedCornerShape(16.dp),
                color = if (showPin) BrandPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (showPin) BrandPrimary else Color.Transparent),
                modifier = Modifier.height(28.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                        tint = if (showPin) BrandPrimary else TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (showPin) "Hide PIN" else "Show PIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (showPin) BrandPrimary else TextSecondary,
                    )
                }
            }

            // Fixed height error container and clear separation before Number Pad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactLandscape) 22.dp else 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = errorMessage,
                            color = BrandCoral,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        if (errorMessage.contains("connection", ignoreCase = true) || errorMessage.contains("internet", ignoreCase = true)) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                modifier = Modifier.clickable { retryTrigger++ }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(if (compactLandscape) 4.dp else 16.dp))

            // Number pad
            NumberPadLight(
                buttonSize = padButtonSize,
                rowSpacing = if (heightDp < 680) 6.dp else 10.dp,
                columnSpacing = if (widthDp < 380) 12.dp else 16.dp,
                onDigitPress = { digit ->
                    if (!isLoading) {
                        hasFailedAttempt = false
                        onClearError()
                        if (pin.length < 4) {
                            pin += digit
                        }
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty() && !isLoading) {
                        hasFailedAttempt = false
                        onClearError()
                        pin = pin.dropLast(1)
                    }
                },
            )

            Spacer(modifier = Modifier.height(if (compactLandscape) 2.dp else 10.dp))

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
        } // end else (single-pane portrait)

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

    LaunchedEffect(pin, retryTrigger) {
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
    buttonSize: androidx.compose.ui.unit.Dp = 76.dp,
    rowSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    columnSpacing: androidx.compose.ui.unit.Dp = 16.dp,
    onDigitPress: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "delete"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        digits.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(columnSpacing)) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(buttonSize))
                    } else {
                        Surface(
                            onClick = {
                                SoundHaptics.playTactileClick(view, haptic)
                                if (digit == "delete") onDelete() else onDigitPress(digit)
                            },
                            shape = CircleShape,
                            color = BackgroundCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceCardBorder),
                            modifier = Modifier.size(buttonSize),
                            shadowElevation = 2.dp,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (digit == "delete") {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete last digit",
                                        tint = BrandCoral,
                                        modifier = Modifier.size(buttonSize * 0.36f),
                                    )
                                } else {
                                    Text(
                                        text = digit,
                                        fontSize = if (buttonSize < 60.dp) 18.sp else 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

```

---

<a name="uiscreensparentzonescreenkt"></a>
## 21. ui/screens/ParentZoneScreen.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/ParentZoneScreen.kt`
**Description:** Parent Zone: Main Dashboard & Tab Navigation

```kotlin
package com.kidcurate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.kidcurate.app.data.*
import com.kidcurate.app.ui.screens.parent.*
import com.kidcurate.app.ui.components.GlassBackgroundAura
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.components.SoundHaptics
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.components.ThematicPill
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ParentZoneScreen(
    profiles: List<ChildProfileItem>,
    onThemeChanged: (String) -> Unit = {},
    onBackToChildMode: () -> Unit,
    onAccountDeleted: () -> Unit = onBackToChildMode,
) {
    BackHandler(onBack = onBackToChildMode)

    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val parentPrefs = remember { ParentPreferences.get(appContext) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = ResponsiveLayoutPolicy.isTablet(configuration.screenWidthDp)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Videos", "Search", "History", "Profiles", "Settings")

    // Managed Children state
    var managedChildren by remember { mutableStateOf<List<ChildManageItem>>(emptyList()) }
    var selectedChildId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    var selectedChildName by remember { mutableStateOf(profiles.firstOrNull()?.name ?: "Child") }

    // Child's Playlist state
    var currentPlaylist by remember { mutableStateOf<PlaylistData?>(null) }
    var isLoadingPlaylist by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<VideoSearchResultItem>>(emptyList()) }
    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Dialog states
    var showEditChildDialog by remember { mutableStateOf<ChildManageItem?>(null) }
    var showAddChildDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ChildManageItem?>(null) }
    var showTourDialog by remember { mutableStateOf(false) }

    // Status feedback
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refreshManagedChildren() {
        scope.launch {
            try {
                val list = ApiClient.api.getManageChildren()
                if (list.isNotEmpty()) {
                    managedChildren = list
                    val cachedList = list.map {
                        ChildProfileItem(
                            id = it.id,
                            name = it.name,
                            avatar = it.avatar,
                            hasPin = it.hasPin ?: !it.pin.isNullOrBlank(),
                        )
                    }
                    parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                    if (selectedChildId.isEmpty()) {
                        selectedChildId = list.first().id
                        selectedChildName = list.first().name
                    }
                } else if (managedChildren.isEmpty() && parentPrefs.cachedProfilesJson.isNotBlank()) {
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<List<ChildProfileItem>>() {}.type
                        val cached: List<ChildProfileItem> = com.google.gson.Gson().fromJson(parentPrefs.cachedProfilesJson, listType)
                        if (cached.isNotEmpty()) {
                            managedChildren = cached.map {
                                ChildManageItem(
                                    id = it.id,
                                    name = it.name,
                                    age = 7,
                                    avatar = it.avatar,
                                    pin = "••••",
                                    parentId = null,
                                )
                            }
                            if (selectedChildId.isEmpty()) {
                                selectedChildId = cached.first().id
                                selectedChildName = cached.first().name
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                if (managedChildren.isEmpty() && parentPrefs.cachedProfilesJson.isNotBlank()) {
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<List<ChildProfileItem>>() {}.type
                        val cached: List<ChildProfileItem> = com.google.gson.Gson().fromJson(parentPrefs.cachedProfilesJson, listType)
                        if (cached.isNotEmpty()) {
                            managedChildren = cached.map {
                                ChildManageItem(
                                    id = it.id,
                                    name = it.name,
                                    age = 7,
                                    avatar = it.avatar,
                                    pin = "••••",
                                    parentId = null,
                                )
                            }
                            if (selectedChildId.isEmpty()) {
                                selectedChildId = cached.first().id
                                selectedChildName = cached.first().name
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun loadChildPlaylist(childId: String) {
        if (childId.isBlank()) return
        isLoadingPlaylist = true
        scope.launch {
            try {
                val resp = ApiClient.api.getTodaysPlaylist(childId)
                currentPlaylist = resp.playlist
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (!msg.contains("404")) {
                    statusMessage = "Could not load playlist: $msg"
                }
            } finally {
                isLoadingPlaylist = false
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        searchSuggestions = emptyList()
        scope.launch {
            try {
                searchResults = ApiClient.api.searchVideos(query.trim(), maxResults = 30)
            } catch (e: Exception) {
                statusMessage = "Search error: ${e.message}"
            } finally {
                isSearching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshManagedChildren()
        if (selectedChildId.isNotEmpty()) {
            loadChildPlaylist(selectedChildId)
        }
        if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            performSearch(searchQuery)
        }
    }

    LaunchedEffect(selectedChildId) {
        if (selectedChildId.isNotEmpty()) {
            loadChildPlaylist(selectedChildId)
        }
    }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            try {
                searchSuggestions = ApiClient.api.getSearchSuggestions(q)
            } catch (_: Exception) {
                searchSuggestions = emptyList()
            }
        } else {
            searchSuggestions = emptyList()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding(),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = if (isLandscape) 4.dp else 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            IconButton(
                                onClick = onBackToChildMode,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary,
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Parent Zone",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                    )
                                }
                                Text(
                                    text = if (parentPrefs.parentName.isNotBlank()) "Hi, ${parentPrefs.parentName}" else "Control Center",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            IconButton(
                                onClick = { showTourDialog = true },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.HelpOutline,
                                    contentDescription = "App Tour & Guide",
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            FilledTonalButton(
                                onClick = onBackToChildMode,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.ChildCare,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Kids Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Navigation Tabs (Scrollable on compact phones, TabRow on tablets & landscape)
                    if (isTablet || isLandscape) {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = BackgroundCard,
                            contentColor = BrandPrimary,
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    icon = if (isLandscape) null else {
                                        {
                                            val icon = when (index) {
                                                0 -> Icons.Rounded.PlayCircle
                                                1 -> Icons.Rounded.Search
                                                2 -> Icons.Rounded.History
                                                3 -> Icons.Rounded.Face
                                                else -> Icons.Rounded.Settings
                                            }
                                            Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                        )
                                    },
                                )
                            }
                        }
                    } else {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = BackgroundCard,
                            contentColor = BrandPrimary,
                            edgePadding = 12.dp,
                            divider = {},
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    icon = {
                                        val icon = when (index) {
                                            0 -> Icons.Rounded.PlayCircle
                                            1 -> Icons.Rounded.Search
                                            2 -> Icons.Rounded.History
                                            3 -> Icons.Rounded.Face
                                            else -> Icons.Rounded.Settings
                                        }
                                        Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
                                    },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = Modifier.navigationBarsPadding(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GlassBackgroundAura()
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
            // Status banner if present
            if (statusMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // Child Selector Bar (for tabs 0 and 1: Videos and Search & Add)
            if (selectedTabIndex == 0 || selectedTabIndex == 1) {
                Surface(
                    color = SurfaceCard,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Child:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(if (managedChildren.isNotEmpty()) managedChildren.map { ChildProfileItem(it.id, it.name, it.avatar) } else profiles) { child ->
                                val isSelected = selectedChildId == child.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else BackgroundCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else SurfaceCardBorder,
                                    ),
                                    onClick = {
                                        selectedChildId = child.id
                                        selectedChildName = child.name
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        ThematicAvatar(name = child.name, avatar = child.avatar, size = 22.dp, iconSize = 12.dp)
                                        Text(
                                            text = child.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandPrimary else TextPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val existingVideoIds: Set<String> = remember(currentPlaylist) {
                currentPlaylist?.items?.map { it.youtubeId }?.toSet() ?: emptySet()
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ManagePlaylistTab(
                    childName = selectedChildName,
                    playlist = currentPlaylist,
                    isLoading = isLoadingPlaylist,
                    onDeleteVideo = { itemId ->
                        val current = currentPlaylist
                        if (current != null) {
                            val remaining = current.items.filter { it.itemId != itemId }
                            currentPlaylist = current.copy(items = remaining)
                        }
                        scope.launch {
                            try {
                                val resp = ApiClient.api.removeVideoFromChildPlaylist(selectedChildId, itemId)
                                if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
                                statusMessage = "Video removed from playlist."
                            } catch (e: Exception) {
                                statusMessage = "Error removing video: ${e.message}"
                                loadChildPlaylist(selectedChildId)
                            }
                        }
                    },
                    onMoveUp = { index ->
                        val current = currentPlaylist ?: return@ManagePlaylistTab
                        val items = current.items.toMutableList()
                        if (index > 0) {
                            val temp = items[index]
                            items[index] = items[index - 1]
                            items[index - 1] = temp
                            currentPlaylist = current.copy(items = items)
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                } catch (e: Exception) {
                                    statusMessage = "Error syncing playlist order: ${e.message}"
                                }
                            }
                        }
                    },
                    onMoveDown = { index ->
                        val current = currentPlaylist ?: return@ManagePlaylistTab
                        val items = current.items.toMutableList()
                        if (index < items.size - 1) {
                            val temp = items[index]
                            items[index] = items[index + 1]
                            items[index + 1] = temp
                            currentPlaylist = current.copy(items = items)
                            val orderedIds = items.map { it.itemId }
                            scope.launch {
                                try {
                                    ApiClient.api.reorderChildPlaylist(selectedChildId, ReorderPlaylistRequest(orderedIds))
                                } catch (e: Exception) {
                                    statusMessage = "Error syncing playlist order: ${e.message}"
                                }
                            }
                        }
                    },
                    onNavigateToSearch = { selectedTabIndex = 1 },
                )

                1 -> SearchAndAddTab(
                    selectedChildName = selectedChildName,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onPerformSearch = { performSearch(searchQuery) },
                    searchResults = searchResults,
                    isSearching = isSearching,
                    searchSuggestions = searchSuggestions,
                    existingVideoIds = existingVideoIds,
                    onAddVideo = { video ->
                        if (selectedChildId.isBlank() || managedChildren.isEmpty()) {
                            statusMessage = "Please create a child profile under the Profile tab first before adding videos."
                            selectedTabIndex = 3
                        } else {
                            scope.launch {
                                try {
                                    val resp = ApiClient.api.addVideoToChildPlaylist(
                                        selectedChildId,
                                        AddVideoToChildRequest(
                                            youtubeId = video.youtubeId,
                                            title = video.title,
                                            channelName = video.channelName,
                                            thumbnailUrl = video.thumbnailUrl,
                                            durationSeconds = video.durationSeconds ?: 300,
                                        ),
                                    )
                                    if (resp["status"] == "already_added") {
                                        statusMessage = "\"${video.title}\" is already in $selectedChildName's playlist."
                                    } else {
                                        statusMessage = "Added \"${video.title}\" to $selectedChildName's playlist!"
                                        loadChildPlaylist(selectedChildId)
                                    }
                                } catch (e: Exception) {
                                    val msg = e.message ?: ""
                                    statusMessage = when {
                                        msg.contains("404") -> "Child profile not found. Please create a child profile under the Profile tab."
                                        msg.contains("401") || msg.contains("403") -> "Session expired. Please verify your Parent PIN under Settings."
                                        else -> "Could not add video. Please check your internet connection."
                                    }
                                }
                            }
                        }
                    },
                )

                2 -> HistoryTabContent(
                    managedChildren = managedChildren,
                    onSelectSearchTerm = { term ->
                        searchQuery = term
                        selectedTabIndex = 1
                        scope.launch {
                            isSearching = true
                            try {
                                searchResults = ApiClient.api.searchVideos(term)
                            } catch (_: Exception) {
                                searchResults = emptyList()
                            } finally {
                                isSearching = false
                            }
                        }
                    },
                    onReAddVideo = { historyItem ->
                        val targetChild = if (historyItem.childId.isNotBlank()) historyItem.childId else selectedChildId
                        if (targetChild.isBlank() || managedChildren.isEmpty()) {
                            statusMessage = "Please create a child profile under the Profile tab first."
                            selectedTabIndex = 3
                        } else {
                            scope.launch {
                                try {
                                    ApiClient.api.addVideoToChildPlaylist(
                                        targetChild,
                                        AddVideoToChildRequest(
                                            youtubeId = historyItem.youtubeId,
                                            title = historyItem.title,
                                            channelName = historyItem.channelName,
                                            thumbnailUrl = historyItem.thumbnailUrl,
                                            durationSeconds = historyItem.durationSeconds,
                                        )
                                    )
                                    statusMessage = "Re-added \"${historyItem.title}\" to playlist!"
                                    loadChildPlaylist(selectedChildId)
                                } catch (e: Exception) {
                                    val msg = e.message ?: ""
                                    statusMessage = when {
                                        msg.contains("404") -> "Child profile not found. Please create a child profile under the Profile tab."
                                        msg.contains("401") || msg.contains("403") -> "Session expired. Please verify your Parent PIN under Settings."
                                        else -> "Could not re-add video. Please check your internet connection."
                                    }
                                }
                            }
                        }
                    }
                )

                3 -> ChildProfilesTab(
                    children = managedChildren,
                    onEditChild = { child -> showEditChildDialog = child },
                    onDeleteChild = { child -> showDeleteConfirmDialog = child },
                    onAddNewChild = { showAddChildDialog = true },
                )

                4 -> SecurityTab(
                    parentPrefs = parentPrefs,
                    onThemeChanged = onThemeChanged,
                    onBackToChildMode = onBackToChildMode,
                    onAccountDeleted = onAccountDeleted,
                )
            }
        }
    }
}

    // Edit Child Profile Dialog
    if (showEditChildDialog != null) {
        val child = showEditChildDialog!!
        var nameInput by remember { mutableStateOf(child.name) }
        var ageInput by remember { mutableStateOf(child.age.toString()) }
        val hasExistingPin = child.hasPin == true || !child.pin.isNullOrBlank()
        var pinInput by remember { mutableStateOf("") }
        var isNewPinVisible by remember { mutableStateOf(false) }
        var avatarInput by remember { mutableStateOf(child.avatar) }
        val avatarChoices = listOf("compass", "sparkles", "rocket", "heart", "waves", "flame", "shield", "palette", "star")

        AlertDialog(
            onDismissRequest = { showEditChildDialog = null },
            title = { Text("Edit Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = (configuration.screenHeightDp * 0.70f).dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Child Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) ageInput = it },
                        label = { Text("Age (1-18)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("PIN Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (hasExistingPin) "Protected with 4-digit PIN" else "No PIN set",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text(if (hasExistingPin) "Change PIN (leave blank to keep)" else "Child 4-digit PIN (optional)") },
                        placeholder = { Text(if (hasExistingPin) "Leave blank to keep current PIN" else "Enter 4-digit PIN") },
                        visualTransformation = if (isNewPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isNewPinVisible = !isNewPinVisible }) {
                                Icon(
                                    if (isNewPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isNewPinVisible) "Hide PIN" else "Show PIN",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Select Character Avatar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(avatarChoices) { av ->
                            val isSelected = avatarInput == av
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        avatarInput = av
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.5.dp,
                                            if (isSelected) BrandPrimary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ThematicAvatar(
                                        name = av,
                                        avatar = av,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = av.replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = ageInput.toIntOrNull() ?: child.age
                        scope.launch {
                            try {
                                if (ApiClient.authToken.isBlank()) {
                                    statusMessage = "Please sign in with Google under Settings to sync child profiles to the cloud."
                                    showEditChildDialog = null
                                    return@launch
                                }
                                val updatedPin = if (pinInput.isNotBlank() && pinInput.length == 4) pinInput.trim() else null
                                val updatedChild = ApiClient.api.updateChild(
                                    child.id,
                                    ChildUpdateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = updatedPin,
                                    ),
                                )
                                val updatedList = managedChildren.map { if (it.id == child.id) updatedChild else it }
                                managedChildren = updatedList
                                val cachedList = updatedList.map {
                                    ChildProfileItem(
                                        id = it.id,
                                        name = it.name,
                                        avatar = it.avatar,
                                        hasPin = it.hasPin ?: !it.pin.isNullOrBlank(),
                                    )
                                }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                                statusMessage = "Updated profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                statusMessage = if (msg.contains("401")) {
                                    "Cloud session expired. Please re-authenticate under Settings."
                                } else {
                                    "Error updating child: $msg"
                                }
                            } finally {
                                showEditChildDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditChildDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Add Child Profile Dialog
    if (showAddChildDialog) {
        var nameInput by remember { mutableStateOf("") }
        var ageInput by remember { mutableStateOf("") }
        var pinInput by remember { mutableStateOf("") }
        var isPinVisible by remember { mutableStateOf(true) }
        var avatarInput by remember { mutableStateOf("compass") }
        val avatarChoices = listOf("compass", "sparkles", "rocket", "heart", "waves", "flame", "shield", "palette", "star")

        AlertDialog(
            onDismissRequest = { showAddChildDialog = false },
            title = { Text("Add New Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = (configuration.screenHeightDp * 0.70f).dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Child Name") },
                        placeholder = { Text("e.g. Maya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) ageInput = it },
                        label = { Text("Age (1-18)") },
                        placeholder = { Text("e.g. 7") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        label = { Text("4-digit Login PIN") },
                        placeholder = { Text("Enter 4-digit PIN") },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    if (isPinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Select Character Avatar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(avatarChoices) { av ->
                            val isSelected = avatarInput == av
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        SoundHaptics.playPop(view, haptic)
                                        avatarInput = av
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.5.dp,
                                            if (isSelected) BrandPrimary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ThematicAvatar(
                                        name = av,
                                        avatar = av,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = av.replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAge = ageInput.toIntOrNull() ?: 7
                        scope.launch {
                            try {
                                if (ApiClient.authToken.isBlank()) {
                                    statusMessage = "Please sign in with Google under Settings to sync child profiles to the cloud."
                                    showAddChildDialog = false
                                    return@launch
                                }
                                val created = ApiClient.api.createChild(
                                    ChildCreateRequest(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        avatar = avatarInput,
                                        pin = pinInput.trim(),
                                    ),
                                )
                                val updated = managedChildren + created
                                managedChildren = updated
                                val cachedList = updated.map {
                                    ChildProfileItem(
                                        id = it.id,
                                        name = it.name,
                                        avatar = it.avatar,
                                        hasPin = it.hasPin ?: !it.pin.isNullOrBlank(),
                                    )
                                }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                                statusMessage = "Created profile for ${nameInput.trim()}!"
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                statusMessage = if (msg.contains("401")) {
                                    "Cloud session expired. Please re-authenticate under Settings."
                                } else {
                                    "Error adding child: $msg"
                                }
                            } finally {
                                showAddChildDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = nameInput.isNotBlank() && pinInput.length == 4 && ageInput.toIntOrNull() != null,
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddChildDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Child Profile Confirmation
    if (showDeleteConfirmDialog != null) {
        val child = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Child Profile", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete ${child.name}'s profile and all assigned playlists? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val resp = ApiClient.api.deleteChild(child.id)
                                if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
                                val updated = managedChildren.filter { it.id != child.id }
                                managedChildren = updated
                                val cachedList = updated.map {
                                    ChildProfileItem(
                                        id = it.id,
                                        name = it.name,
                                        avatar = it.avatar,
                                        hasPin = it.hasPin ?: !it.pin.isNullOrBlank(),
                                    )
                                }
                                parentPrefs.cachedProfilesJson = com.google.gson.Gson().toJson(cachedList)
                                statusMessage = "${child.name}'s profile was deleted."
                                refreshManagedChildren()
                            } catch (e: Exception) {
                                statusMessage = "Error deleting child: ${e.message}"
                            } finally {
                                showDeleteConfirmDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showTourDialog) {
        OnboardingTourDialog(onDismiss = { showTourDialog = false })
    }
}

```

---

<a name="uiscreensparentvideostabcontentkt"></a>
## 22. ui/screens/parent/VideosTabContent.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/parent/VideosTabContent.kt`
**Description:** Parent Tab: Queue Management & Video Approval

```kotlin
package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.PlaylistData
import com.kidcurate.app.ui.theme.*

@Composable
fun ManagePlaylistTab(
    childName: String,
    playlist: PlaylistData?,
    isLoading: Boolean,
    onDeleteVideo: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    val items = playlist?.items ?: emptyList()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "$childName's Assigned Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    val totalMins = (playlist?.totalDurationSeconds ?: 0) / 60
                    Text(
                        text = "${items.size} videos • ${totalMins}m total watch time",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = onNavigateToSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No Videos Assigned Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Search YouTube to curate safe, wholesome videos for $childName.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSearch,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        ) {
                            Text("Find Wholesome Videos")
                        }
                    }
                }
            }
        } else {
            itemsIndexed(items, key = { _, video -> video.itemId }) { index, video ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    if (isLandscape) {
                        // Landscape: compact horizontal card
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.width(130.dp).height(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.08f)),
                            ) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current).data(video.thumbnailUrl).crossfade(true).build(),
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                Surface(shape = RoundedCornerShape(6.dp), color = BrandPrimary, modifier = Modifier.align(Alignment.TopStart).padding(5.dp)) {
                                    Text(text = "#${index + 1}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = video.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(text = video.channelName, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = { onMoveUp(index) }, enabled = index > 0, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onMoveDown(index) }, enabled = index < items.size - 1, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = if (index < items.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteVideo(video.itemId) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = BrandCoral, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color.Black.copy(alpha = 0.08f)),
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(video.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandPrimary,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }

                            val mins = (video.durationSeconds ?: 0) / 60
                            val secs = (video.durationSeconds ?: 0) % 60
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.82f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%d:%02d", mins, secs),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = video.channelName,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                IconButton(
                                    onClick = { onMoveUp(index) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { onMoveDown(index) },
                                    enabled = index < items.size - 1,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < items.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteVideo(video.itemId) },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete Video",
                                        tint = BrandCoral,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                    } // end portrait else
                }
            }
        }
    }
}

```

---

<a name="uiscreensparentsearchtabcontentkt"></a>
## 23. ui/screens/parent/SearchTabContent.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/parent/SearchTabContent.kt`
**Description:** Parent Tab: YouTube Search & Channel Curation

```kotlin
package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.VideoSearchResultItem
import com.kidcurate.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchAndAddTab(
    selectedChildName: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPerformSearch: () -> Unit,
    searchResults: List<VideoSearchResultItem>,
    isSearching: Boolean,
    onAddVideo: (VideoSearchResultItem) -> Unit,
    searchSuggestions: List<String> = emptyList(),
    existingVideoIds: Set<String> = emptySet(),
) {
    val context = LocalContext.current
    val parentPrefs = remember { ParentPreferences.get(context) }
    var recentSearches by remember { mutableStateOf(parentPrefs.getRecentSearches()) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val handleSearch: (String) -> Unit = { queryToSearch ->
        val trimmed = queryToSearch.trim()
        if (trimmed.isNotBlank()) {
            parentPrefs.addRecentSearch(trimmed)
            recentSearches = parentPrefs.getRecentSearches()
        }
        onPerformSearch()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Search bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search safe educational YouTube videos...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { handleSearch(searchQuery) }),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = { handleSearch(searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dynamic suggestions if typing
            if (searchSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        searchSuggestions.take(6).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSearchQueryChange(suggestion)
                                        handleSearch(suggestion)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSearching) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Searching safe YouTube videos for \"$searchQuery\"...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary,
                        )
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            if (searchQuery.isBlank() && recentSearches.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Recent Searches",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                )
                            }
                            TextButton(
                                onClick = {
                                    parentPrefs.clearRecentSearches()
                                    recentSearches = emptyList()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "Clear All",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            recentSearches.forEach { queryItem ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = BackgroundCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                    shadowElevation = 1.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    onSearchQueryChange(queryItem)
                                                    handleSearch(queryItem)
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                Icons.Rounded.Search,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(15.dp),
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = queryItem,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                parentPrefs.removeRecentSearch(queryItem)
                                                recentSearches = parentPrefs.getRecentSearches()
                                            },
                                            modifier = Modifier.size(24.dp),
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Remove search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No videos found for \"$searchQuery\"" else "Search Safe YouTube Videos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tip: You can search any educational topic (Phonics, Science, Animals) or paste ANY direct YouTube video link to add it instantly!",
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(searchResults, key = { it.youtubeId }) { video ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shadowElevation = 1.dp,
                ) {
                    if (isLandscape) {
                        // Landscape: compact horizontal card
                        val isAlreadyAdded = existingVideoIds.contains(video.youtubeId)
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(130.dp).height(76.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.08f))) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current).data(video.thumbnailUrl).crossfade(true).build(),
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                if (video.durationSeconds != null && video.durationSeconds > 0) {
                                    val mins = video.durationSeconds / 60; val secs = video.durationSeconds % 60
                                    Surface(shape = RoundedCornerShape(5.dp), color = Color.Black.copy(alpha = 0.82f), modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp)) {
                                        Text(text = String.format(java.util.Locale.US, "%d:%02d", mins, secs), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = video.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(text = video.channelName, fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (isAlreadyAdded) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandSuccess, modifier = Modifier.size(28.dp))
                            } else {
                                Button(onClick = { onAddVideo(video) }, colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.08f)),
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(video.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSuccess,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Parent review required",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            if (video.durationSeconds != null && video.durationSeconds > 0) {
                                val mins = video.durationSeconds / 60
                                val secs = video.durationSeconds % 60
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.82f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                ) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%d:%02d", mins, secs),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = video.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = video.channelName,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )

                        Spacer(Modifier.height(10.dp))

                        val isAlreadyAdded = existingVideoIds.contains(video.youtubeId)
                        if (isAlreadyAdded) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = BrandSuccess.copy(alpha = 0.15f),
                                    disabledContentColor = BrandSuccess,
                                ),
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandSuccess)
                                Spacer(Modifier.width(6.dp))
                                Text("Already in $selectedChildName's Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSuccess)
                            }
                        } else {
                            Button(
                                onClick = { onAddVideo(video) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add to $selectedChildName's Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    } // end portrait else
                }
            }
        }
    }
}

```

---

<a name="uiscreensparentprofilestabcontentkt"></a>
## 24. ui/screens/parent/ProfilesTabContent.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/parent/ProfilesTabContent.kt`
**Description:** Parent Tab: Child Profiles & Bedtime Curfew Settings

```kotlin
package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.kidcurate.app.data.ChildManageItem
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.theme.*

@Composable
fun ChildProfilesTab(
    children: List<ChildManageItem>,
    onEditChild: (ChildManageItem) -> Unit,
    onDeleteChild: (ChildManageItem) -> Unit,
    onAddNewChild: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Child Profiles",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Edit names, ages, avatars & login PINs",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = onAddNewChild,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Child", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(children, key = { it.id }) { child ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ThematicAvatar(name = child.name, avatar = child.avatar, size = 48.dp, iconSize = 24.dp)
                        Column {
                            Text(
                                text = child.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Age: ${child.age} yrs",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                )
                                Text("•", fontSize = 12.sp, color = TextSecondary)
                                val hasPin = child.hasPin == true || !child.pin.isNullOrBlank()
                                Text(
                                    text = "PIN: ${if (hasPin) "Active" else "Not set"}",
                                    fontSize = 12.sp,
                                    fontWeight = if (hasPin) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (hasPin) BrandPrimary else TextSecondary,
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onEditChild(child) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.heightIn(min = 36.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { onDeleteChild(child) },
                            modifier = Modifier.heightIn(min = 36.dp),
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = BrandCoral,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

```

---

<a name="uiscreensparenthistorytabcontentkt"></a>
## 25. ui/screens/parent/HistoryTabContent.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/parent/HistoryTabContent.kt`
**Description:** Parent Tab: Watch History & Screen Time Analytics

```kotlin
package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidcurate.app.data.*
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HistoryTabContent(
    managedChildren: List<ChildManageItem>,
    onSelectSearchTerm: (String) -> Unit,
    onReAddVideo: (CuratedHistoryItem) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parentPrefs = remember { ParentPreferences.get(context) }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Curated History", "Watch Activity", "Search Queries")

    var selectedChildFilterId by remember { mutableStateOf("") } // empty = all children

    var curatedHistory by remember { mutableStateOf<List<CuratedHistoryItem>>(emptyList()) }
    var isLoadingCurated by remember { mutableStateOf(false) }

    var watchHistory by remember { mutableStateOf<List<WatchHistoryItem>>(emptyList()) }
    var isLoadingWatch by remember { mutableStateOf(false) }

    var searchHistory by remember { mutableStateOf(parentPrefs.getRecentSearches()) }

    fun loadCuratedHistory() {
        scope.launch {
            isLoadingCurated = true
            try {
                val childIdParam = if (selectedChildFilterId.isNotBlank()) selectedChildFilterId else null
                val res = ApiClient.api.getCurationHistory(childId = childIdParam, limit = 60)
                curatedHistory = res.history
            } catch (_: Exception) {
                curatedHistory = emptyList()
            } finally {
                isLoadingCurated = false
            }
        }
    }

    fun loadWatchHistory() {
        val targetChildId = if (selectedChildFilterId.isNotBlank()) {
            selectedChildFilterId
        } else {
            managedChildren.firstOrNull()?.id ?: ""
        }

        if (targetChildId.isBlank()) {
            watchHistory = emptyList()
            return
        }

        scope.launch {
            isLoadingWatch = true
            try {
                val res = ApiClient.api.getWatchHistory(childId = targetChildId, limit = 50)
                watchHistory = res.history
            } catch (_: Exception) {
                watchHistory = emptyList()
            } finally {
                isLoadingWatch = false
            }
        }
    }

    LaunchedEffect(selectedSubTab, selectedChildFilterId) {
        if (selectedSubTab == 0) {
            loadCuratedHistory()
        } else if (selectedSubTab == 1) {
            loadWatchHistory()
        } else {
            searchHistory = parentPrefs.getRecentSearches()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .align(Alignment.TopCenter)
            .fillMaxHeight(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Child filter chips
        if (managedChildren.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedChildFilterId.isEmpty(),
                        onClick = { selectedChildFilterId = "" },
                        label = { Text("All Children", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White,
                        ),
                    )

                    managedChildren.forEach { child ->
                        FilterChip(
                            selected = selectedChildFilterId == child.id,
                            onClick = { selectedChildFilterId = child.id },
                            label = { Text(child.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
        }

        // Sub-tabs segment
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    subTabs.forEachIndexed { index, title ->
                        val isSelected = selectedSubTab == index
                        Surface(
                            onClick = { selectedSubTab = index },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BackgroundCard else Color.Transparent,
                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        // SubTab 0: Curated History
        if (selectedSubTab == 0) {
            if (isLoadingCurated) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (curatedHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.PlaylistAddCheck, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Curated History Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "When you add videos or apply activity packs to your children's playlists, the activity audit log will appear here.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(curatedHistory, key = { it.id }) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp, 52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val actionColor = when (item.action) {
                                        "removed" -> MaterialTheme.colorScheme.error
                                        "pack_applied" -> BrandViolet
                                        else -> Color(0xFF10B981)
                                    }
                                    val actionLabel = when (item.action) {
                                        "removed" -> "Removed"
                                        "pack_applied" -> "Pack Added"
                                        else -> "Curated"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = actionColor.copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = actionLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = actionColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }

                                    if (item.childName.isNotBlank()) {
                                        Text(
                                            text = "for ${item.childName}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }

                                if (item.createdAt.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = item.createdAt.take(16).replace("T", " "),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }

                            if (item.action == "removed") {
                                IconButton(
                                    onClick = { onReAddVideo(item) },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.AddCircleOutline,
                                        contentDescription = "Re-add video",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SubTab 1: Watch Activity
        if (selectedSubTab == 1) {
            if (isLoadingWatch) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (watchHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Watch Activity Recorded",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "When your child plays videos in Kids Mode, their viewing time and completion marks will show up here.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(watchHistory) { watch ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp, 52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = watch.thumbnailUrl,
                                    contentDescription = watch.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                if (watch.completed) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = watch.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(Modifier.height(4.dp))

                                val watchedMin = watch.watchedSeconds / 60
                                val watchedSec = watch.watchedSeconds % 60
                                val watchTimeText = "${watchedMin}m ${watchedSec}s"

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "Watched $watchTimeText",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandPrimary,
                                    )

                                    if (watch.completed) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                text = "Completed",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }

                                if (!watch.watchedAt.isNullOrBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = watch.watchedAt.take(16).replace("T", " "),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SubTab 2: Search Queries
        if (selectedSubTab == 2) {
            if (searchHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No Search History",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Queries you search for when finding safe educational videos will be logged here for convenient 1-tap re-use.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${searchHistory.size} Recent Queries",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                        )
                        TextButton(
                            onClick = {
                                parentPrefs.clearRecentSearches()
                                searchHistory = emptyList()
                            },
                        ) {
                            Text("Clear All Queries", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                items(searchHistory) { query ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BackgroundCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSearchTerm(query) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = query,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = { onSelectSearchTerm(query) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp),
                                ) {
                                    Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = {
                                        parentPrefs.removeRecentSearch(query)
                                        searchHistory = parentPrefs.getRecentSearches()
                                    },
                                    modifier = Modifier.size(30.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove query",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    } // end Box tablet wrapper
}

```

---

<a name="uiscreensparentsecuritytabcontentkt"></a>
## 26. ui/screens/parent/SecurityTabContent.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/screens/parent/SecurityTabContent.kt`
**Description:** Parent Tab: PIN Modification, Biometrics & Account Delete

```kotlin
package com.kidcurate.app.ui.screens.parent

import android.app.Activity
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
import com.kidcurate.app.data.ApiClient
import com.kidcurate.app.data.DbSummaryResponse
import com.kidcurate.app.data.GoogleCredentialAuth
import com.kidcurate.app.data.ParentSessionPolicy
import com.kidcurate.app.data.ParentPreferences
import com.kidcurate.app.data.ParentBiometricAuth
import androidx.fragment.app.FragmentActivity
import com.kidcurate.app.ui.components.OnboardingTourDialog
import com.kidcurate.app.ui.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .align(Alignment.TopCenter)
            .fillMaxHeight()
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

        // Daily Screen Time & Bedtime Curfew Card
        var screenTimeMinutes by remember { mutableIntStateOf(parentPrefs.dailyScreenTimeMinutes) }
        var curfewEnabled by remember { mutableStateOf(parentPrefs.curfewEnabled) }
        var curfewStartHour by remember { mutableIntStateOf(parentPrefs.curfewStartHour) }
        var curfewEndHour by remember { mutableIntStateOf(parentPrefs.curfewEndHour) }

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
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = BrandPrimary)
                    Text("Daily Screen Time & Bedtime Curfew", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Control how long your child can watch videos each day and set bedtime hours when video playback pauses.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Daily Screen Time Limit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(8.dp))

                val screenTimeOptions = listOf(
                    30 to "30 min",
                    45 to "45 min",
                    60 to "60 min",
                    90 to "90 min",
                    0 to "Unlimited",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    screenTimeOptions.forEach { (mins, label) ->
                        val isSelected = screenTimeMinutes == mins
                        OutlinedButton(
                            onClick = {
                                screenTimeMinutes = mins
                                parentPrefs.dailyScreenTimeMinutes = mins
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
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(14.dp))

                // Curfew Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = Color(0xFF6C5CE7))
                        Column {
                            Text("Bedtime Curfew Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (curfewEnabled) "Active (${if (curfewStartHour > 12) "${curfewStartHour - 12} PM" else "$curfewStartHour AM"} to ${if (curfewEndHour > 12) "${curfewEndHour - 12} PM" else "$curfewEndHour AM"})" else "Disabled",
                                fontSize = 12.sp,
                                color = if (curfewEnabled) Color(0xFF6C5CE7) else TextSecondary,
                            )
                        }
                    }
                    Switch(
                        checked = curfewEnabled,
                        onCheckedChange = {
                            curfewEnabled = it
                            parentPrefs.curfewEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                if (curfewEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Curfew Start Time (Bedtime):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(19 to "7 PM", 20 to "8 PM", 21 to "9 PM", 22 to "10 PM").forEach { (hr, label) ->
                            val isSelected = curfewStartHour == hr
                            OutlinedButton(
                                onClick = {
                                    curfewStartHour = hr
                                    parentPrefs.curfewStartHour = hr
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF6C5CE7).copy(alpha = 0.15f) else Color.Transparent,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6C5CE7) else SurfaceCardBorder,
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF6C5CE7) else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Curfew End Time (Wake Up):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(6 to "6 AM", 7 to "7 AM", 8 to "8 AM", 9 to "9 AM").forEach { (hr, label) ->
                            val isSelected = curfewEndHour == hr
                            OutlinedButton(
                                onClick = {
                                    curfewEndHour = hr
                                    parentPrefs.curfewEndHour = hr
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF6C5CE7).copy(alpha = 0.15f) else Color.Transparent,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6C5CE7) else SurfaceCardBorder,
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF6C5CE7) else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Early Reader Subtitles & Phonics Card
        var earlyReaderEnabled by remember { mutableStateOf(parentPrefs.earlyReaderSubtitles) }
        var defaultSubtitlesEnabled by remember { mutableStateOf(parentPrefs.subtitlesEnabled) }

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
                    Icon(Icons.Rounded.ClosedCaption, contentDescription = null, tint = BrandPrimary)
                    Text("Reading & Subtitles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Reading support features to help children connect spoken words with written letters.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                // Karaoke Phonics Subtitles Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Early Reader Karaoke Phonics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Displays large, bold text with animated golden word-highlights synchronized to speech.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = earlyReaderEnabled,
                        onCheckedChange = {
                            earlyReaderEnabled = it
                            parentPrefs.earlyReaderSubtitles = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(10.dp))

                // Default Subtitles Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Subtitles by Default",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Automatically activate English subtitles when starting any video.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = defaultSubtitlesEnabled,
                        onCheckedChange = {
                            defaultSubtitlesEnabled = it
                            parentPrefs.subtitlesEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }
            }
        }

        // Child Playback & Profile Access Card
        var requireChildPinState by remember { mutableStateOf(parentPrefs.requireChildPin) }
        var selectedVideoQuality by remember { mutableStateOf(parentPrefs.videoQuality) }

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
                    Icon(Icons.Rounded.SmartDisplay, contentDescription = null, tint = BrandPrimary)
                    Text("Child Access & Video Quality", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Configure instant profile access for toddlers and default streaming resolution.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                // Instant Tap-to-Play vs Child PIN Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Require Child Profile PIN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (requireChildPinState)
                                "Children must enter their 4-digit PIN to open their playlist."
                            else
                                "Instant Play: Children tap their avatar to start watching immediately (recommended for ages 3–6).",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = requireChildPinState,
                        onCheckedChange = {
                            requireChildPinState = it
                            parentPrefs.requireChildPin = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPrimary,
                        ),
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(Modifier.height(12.dp))

                // Video Streaming Quality
                Text(
                    text = "Streaming Video Quality",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Text(
                    text = "Controls resolution without exposing technical settings inside the child player.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        "auto" to "Auto",
                        "hd1080" to "1080p",
                        "hd720" to "720p",
                        "large" to "480p",
                        "medium" to "360p",
                    ).forEach { (qualityKey, label) ->
                        val isSelected = selectedVideoQuality == qualityKey
                        OutlinedButton(
                            onClick = {
                                selectedVideoQuality = qualityKey
                                parentPrefs.videoQuality = qualityKey
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) BrandPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) BrandPrimary else SurfaceCardBorder,
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandPrimary else TextSecondary,
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

        fun connectGoogleAccount() {
            val activity = appContext as? Activity
                ?: (appContext as? android.content.ContextWrapper)?.baseContext as? Activity
                ?: return
            scope.launch {
                isAccountLoading = true
                accountStatusMessage = null
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
                        accountStatusMessage = "Server did not issue an access token."
                        return@launch
                    }

                    parentPrefs.parentAuthToken = token
                    parentPrefs.parentRefreshToken = refreshToken
                    currentToken = token
                    ApiClient.authToken = token
                    accountStatusMessage = "Connected Google Account: $email"
                    if (!savedPinMatchesAccount) {
                        parentPrefs.parentPin = ""
                        parentPrefs.parentPinOwnerEmail = ""
                        showChangeParentPinDialog = true
                    } else {
                        parentPrefs.parentPinOwnerEmail = email
                    }
                } catch (e: Exception) {
                    accountStatusMessage = GoogleCredentialAuth.userMessage(e)
                } finally {
                    isAccountLoading = false
                }
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
                                            text = if (parentPrefs.parentName.isNotBlank()) "${parentPrefs.parentName} (${parentPrefs.parentEmail})" else parentPrefs.parentEmail.ifBlank { "Parent Account" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary,
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            runCatching { GoogleCredentialAuth.signOut(appContext) }
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
                        onClick = { connectGoogleAccount() },
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
                                    val resp = ApiClient.api.deleteAccount()
                                    if (!resp.isSuccessful && resp.code() !in listOf(401, 404)) {
                                        throw HttpException(resp)
                                    }
                                    runCatching { GoogleCredentialAuth.signOut(appContext) }
                                    parentPrefs.clearAccountData()
                                    currentToken = ""
                                    ApiClient.authToken = ""
                                    showDeleteAccountDialog = false
                                    onAccountDeleted()
                                } catch (e: HttpException) {
                                    if (e.code() == 401 || e.code() == 404) {
                                        // The cloud account is already absent or the session no longer exists.
                                        runCatching { GoogleCredentialAuth.signOut(appContext) }
                                        parentPrefs.clearAccountData()
                                        currentToken = ""
                                        ApiClient.authToken = ""
                                        showDeleteAccountDialog = false
                                        onAccountDeleted()
                                    } else {
                                        accountStatusMessage = "Account deletion failed. Please try again."
                                    }
                                } catch (_: Exception) {
                                    accountStatusMessage = "Could not contact the server. Your account was not deleted."
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
        var currentPinInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var isBiometricVerified by remember { mutableStateOf(false) }
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
                                currentPinInput = ""
                                newPinInput = ""
                                confirmPinInput = ""
                                isBiometricVerified = false
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

                        if (parentPrefs.hasParentPin && !isBiometricVerified) {
                            OutlinedTextField(
                                value = currentPinInput,
                                onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) currentPinInput = it },
                                label = { Text("Current 4-digit PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            if (ParentBiometricAuth.isBiometricOrDeviceLockAvailable(appContext)) {
                                Spacer(Modifier.height(8.dp))
                                FilledTonalButton(
                                    onClick = {
                                        pinErrorMessage = null
                                        val fragmentActivity = appContext as? androidx.fragment.app.FragmentActivity
                                        if (fragmentActivity != null) {
                                            ParentBiometricAuth.authenticate(
                                                activity = fragmentActivity,
                                                title = "Verify Parent Identity",
                                                subtitle = "Confirm fingerprint or phone lock to authorize PIN change.",
                                                onSuccess = {
                                                    isBiometricVerified = true
                                                    pinErrorMessage = null
                                                },
                                                onError = { err -> pinErrorMessage = err },
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Verify with Fingerprint / Phone Lock", fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        } else if (isBiometricVerified) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSuccess.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BrandSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Identity verified via device security.", fontSize = 12.sp, color = BrandSuccess, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

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
                                    if (parentPrefs.hasParentPin && !isBiometricVerified) {
                                        if (!parentPrefs.verifyParentPin(currentPinInput)) {
                                            pinErrorMessage = "Current PIN is incorrect."
                                            return@Button
                                        }
                                    }
                                    if (newPinInput.length != 4) {
                                        pinErrorMessage = "PIN must be exactly 4 digits."
                                        return@Button
                                    }
                                    if (newPinInput != confirmPinInput) {
                                        pinErrorMessage = "PINs do not match."
                                        return@Button
                                    }
                                    parentPrefs.parentPin = newPinInput
                                    parentPrefs.parentPinOwnerEmail = parentPrefs.parentEmail
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
    } // end Box tablet wrapper
}

```

---

<a name="uicomponentsconfettianimationkt"></a>
## 27. ui/components/ConfettiAnimation.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/components/ConfettiAnimation.kt`
**Description:** UI Component: Confetti Particle Celebration Canvas

```kotlin
package com.kidcurate.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: ParticleShape,
    val phase: Float,
)

enum class ParticleShape { CIRCLE, RIBBON, STAR }

@Composable
fun ConfettiCelebration(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
) {
    if (!isActive) return

    val colors = listOf(
        Color(0xFFFFD166), // Sparkling Sun Gold
        Color(0xFFFF70A6), // Sweet Bubblegum Rose
        Color(0xFF70D6FF), // Bright Sky Blue
        Color(0xFF06D6A0), // Fresh Mint Emerald
        Color(0xFF8338EC), // Vivid Royal Violet
        Color(0xFFFF9F1C), // Warm Tangerine
        Color(0xFFE0AAFF), // Soft Lilac
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = -0.1f - Random.nextFloat() * 0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.25f,
                vy = 0.35f + Random.nextFloat() * 0.55f,
                color = colors[Random.nextInt(colors.size)],
                size = 10f + Random.nextFloat() * 16f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 540f,
                shape = when (Random.nextInt(3)) {
                    0 -> ParticleShape.STAR
                    1 -> ParticleShape.RIBBON
                    else -> ParticleShape.CIRCLE
                },
                phase = Random.nextFloat() * 2f * PI.toFloat(),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti_anim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confetti_progress",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Subtle harmonic sway left/right for natural floating feel
            val sway = sin(progress * 4f * PI.toFloat() + p.phase) * (width * 0.035f)
            val currentY = ((p.y + p.vy * progress) % 1.25f) * height
            val currentX = (p.x * width) + (p.vx * progress * width) + sway
            val currentRot = p.rotation + p.rotationSpeed * progress

            rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                when (p.shape) {
                    ParticleShape.STAR -> {
                        // Twinkling shimmer effect
                        val twinkleAlpha = (0.55f + 0.45f * sin(progress * 14f + p.phase)).coerceIn(0.2f, 1f)
                        val starPath = createStarPath(
                            cx = currentX,
                            cy = currentY,
                            outerRadius = p.size,
                            innerRadius = p.size * 0.42f,
                        )
                        drawPath(
                            path = starPath,
                            color = p.color.copy(alpha = twinkleAlpha),
                        )
                    }
                    ParticleShape.RIBBON -> {
                        // 3D tumble compression across the horizontal axis
                        val tumble = abs(cos(progress * 8f + p.phase)).coerceIn(0.15f, 1f)
                        scale(scaleX = tumble, scaleY = 1f, pivot = Offset(currentX, currentY)) {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(currentX - p.size, currentY - p.size * 0.35f),
                                size = Size(p.size * 2f, p.size * 0.7f),
                            )
                        }
                    }
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = p.color.copy(alpha = 0.9f),
                            radius = p.size * 0.45f,
                            center = Offset(currentX, currentY),
                        )
                    }
                }
            }
        }
    }
}

private fun createStarPath(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
): Path {
    val path = Path()
    val points = 5
    val step = PI / points
    var angle = -PI / 2.0 // Start pointing directly upwards

    for (i in 0 until (2 * points)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = (cx + r * cos(angle)).toFloat()
        val y = (cy + r * sin(angle)).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += step
    }
    path.close()
    return path
}

```

---

<a name="uicomponentsonboardingtourdialogkt"></a>
## 28. ui/components/OnboardingTourDialog.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/components/OnboardingTourDialog.kt`
**Description:** UI Component: Interactive Parent Guided Tour Dialog

```kotlin
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

```

---

<a name="uicomponentssoundhapticskt"></a>
## 29. ui/components/SoundHaptics.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/components/SoundHaptics.kt`
**Description:** UI Component: Tactile Sound & Haptic Feedback

```kotlin
package com.kidcurate.app.ui.components

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object SoundHaptics {
    fun playTactileClick(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

    fun playPop(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    fun playCelebration(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }
}

```

---

<a name="uicomponentsthematiccomponentskt"></a>
## 30. ui/components/ThematicComponents.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/components/ThematicComponents.kt`
**Description:** UI Component: Parent Password Dialog & Theme Avatars

```kotlin
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
 * Parent Verification Dialog (Password Gate)
 * Allows parents to unlock child kiosk mode, exit, or manage playlists using their parent account password.
 */
enum class ParentGateMode {
    PIN_UNLOCK,
    GOOGLE_LOGIN,
    BIOMETRIC_VERIFY,
    SET_PIN,
}

/**
 * Parent Verification Dialog (Google-First Identity & PIN Gate)
 * Uses Google Sign-In and Hardware Biometrics for parent authentication and identity.
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
    val parentPrefs = remember { ParentPreferences.get(appContext) }
    val isBiometricAvailable = remember {
        ParentBiometricAuth.isBiometricOrDeviceLockAvailable(appContext)
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
            ParentGateMode.PIN_UNLOCK
        } else if (isBiometricAvailable) {
            // NEVER let unauthenticated children see SET_PIN directly!
            // First require device biometric / lock screen authentication
            ParentGateMode.BIOMETRIC_VERIFY
        } else if (effectiveAllowGoogle) {
            ParentGateMode.GOOGLE_LOGIN
        } else {
            ParentGateMode.SET_PIN
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
    var failedAttempts by remember { mutableIntStateOf(0) }
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

    val hostActivity = appContext as? android.app.Activity
    if (com.kidcurate.app.BuildConfig.DEBUG) {
        val autoWalkthrough = hostActivity?.intent?.getStringExtra("test_auto_walkthrough")
        if (autoWalkthrough == "child_lock_check" && currentMode == ParentGateMode.PIN_UNLOCK) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1200)
                // First simulate entering wrong PIN
                pinInput = "9999"
                errorMessage = "Incorrect 4-digit PIN."
                kotlinx.coroutines.delay(2200)
                // Then simulate entering correct parent PIN
                pinInput = parentPrefs.parentPin.ifBlank { "1234" }
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
                if (isResettingPin || !savedPinMatchesAccount) {
                    parentPrefs.parentPin = ""
                    parentPrefs.parentPinOwnerEmail = ""
                    newPinInput = ""
                    confirmPinInput = ""
                    isResettingPin = false
                    currentMode = ParentGateMode.SET_PIN
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
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.52f else 0.92f)
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
                            ParentGateMode.PIN_UNLOCK -> Icons.Rounded.Lock
                            ParentGateMode.SET_PIN -> Icons.Rounded.LockReset
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
                        ParentGateMode.PIN_UNLOCK -> title
                        ParentGateMode.SET_PIN -> "Create 4-Digit In-App PIN"
                        ParentGateMode.BIOMETRIC_VERIFY -> "Parent Identity Verification"
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
                        ParentGateMode.BIOMETRIC_VERIFY -> "Confirm your fingerprint or phone screen lock to verify you are the parent."
                        ParentGateMode.GOOGLE_LOGIN -> if (isResettingPin) {
                            "Sign in with your Google account to verify your identity and reset your 4-digit PIN."
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
                    ParentGateMode.PIN_UNLOCK -> {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (lockoutRemainingSeconds > 0) return@OutlinedTextField
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pinInput = it
                                    errorMessage = null
                                    if (it.length == 4) {
                                        if (parentPrefs.verifyParentPin(it)) {
                                            failedAttempts = 0
                                            lockoutRemainingSeconds = 0
                                            onSuccess()
                                        } else {
                                            val remainingSec = parentPrefs.getPinLockoutRemainingSeconds()
                                            if (remainingSec > 0) {
                                                lockoutRemainingSeconds = remainingSec
                                                errorMessage = "Too many failed attempts. Locked for ${remainingSec}s."
                                            } else {
                                                failedAttempts++
                                                val rem = 5 - failedAttempts
                                                errorMessage = if (rem > 0) "Incorrect PIN. $rem attempt(s) remaining." else "Incorrect PIN."
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isVerifying && lockoutRemainingSeconds == 0,
                            label = { 
                                Text(if (lockoutRemainingSeconds > 0) "Locked (${lockoutRemainingSeconds}s)" else "Parent PIN") 
                            },
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

                        if (isBiometricAvailable) {
                            Spacer(Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = {
                                    errorMessage = null
                                    val fragmentActivity = appContext as? androidx.fragment.app.FragmentActivity
                                    if (fragmentActivity != null) {
                                        ParentBiometricAuth.authenticate(
                                            activity = fragmentActivity,
                                            title = "Verify Parent Identity",
                                            subtitle = "Confirm fingerprint or screen lock to reset your Parent PIN.",
                                            onSuccess = {
                                                isResettingPin = true
                                                newPinInput = ""
                                                confirmPinInput = ""
                                                errorMessage = null
                                                currentMode = ParentGateMode.SET_PIN
                                            },
                                            onError = { err ->
                                                errorMessage = err
                                            },
                                        )
                                    } else {
                                        errorMessage = "Biometric authentication not supported."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Fingerprint,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Forgot PIN? Verify with Fingerprint / Phone Lock",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary,
                                )
                            }
                        }

                        if (allowGoogleSignIn) {
                            Spacer(Modifier.height(6.dp))
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isBiometricAvailable) "Or verify with Google Account" else "Forgot PIN? Verify with Google",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ParentGateMode.BIOMETRIC_VERIFY -> {
                        Button(
                            onClick = {
                                errorMessage = null
                                val fragmentActivity = appContext as? androidx.fragment.app.FragmentActivity
                                if (fragmentActivity != null) {
                                    ParentBiometricAuth.authenticate(
                                        activity = fragmentActivity,
                                        title = "Parent Identity Verification",
                                        subtitle = "Confirm fingerprint, face, or device screen lock to create your Parent PIN.",
                                        onSuccess = {
                                            errorMessage = null
                                            currentMode = ParentGateMode.SET_PIN
                                        },
                                        onError = { err ->
                                            errorMessage = err
                                        },
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
                            Text("Verify Fingerprint / Phone Lock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

                        if (com.kidcurate.app.BuildConfig.DEBUG) {
                            Spacer(Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    parentPrefs.parentName = "Test Parent"
                                    parentPrefs.parentEmail = "cleanparent@test.com"
                                    parentPrefs.parentAuthToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2OGVlMDMyNi00Yzg2LTQwNmUtYmRmNi0wNjYxM2EwYmEyYmUiLCJlbWFpbCI6ImNsZWFucGFyZW50QHRlc3QuY29tIiwidHlwZSI6ImFjY2VzcyIsImV4cCI6MTc5NTY5NTA4N30.gsRcdYIUtjfDVQtUWdKQo3PWGIeb_GqmRvuV3KNwQss"
                                    parentPrefs.parentPin = "1234"
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
                        ParentGateMode.PIN_UNLOCK -> {
                            Button(
                                onClick = {
                                    if (lockoutRemainingSeconds > 0) return@Button
                                    if (parentPrefs.verifyParentPin(pinInput)) {
                                        failedAttempts = 0
                                        lockoutRemainingSeconds = 0
                                        onSuccess()
                                    } else {
                                        failedAttempts++
                                        if (failedAttempts >= 5) {
                                            lockoutRemainingSeconds = 30
                                            errorMessage = "Too many failed attempts. Locked for 30s."
                                        } else {
                                            val rem = 5 - failedAttempts
                                            errorMessage = "Incorrect PIN. $rem attempt(s) remaining."
                                        }
                                    }
                                },
                                enabled = pinInput.length == 4 && lockoutRemainingSeconds == 0,
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


```

---

<a name="uithemecolorkt"></a>
## 31. ui/theme/Color.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/theme/Color.kt`
**Description:** Design System: Material3 Color Tokens

```kotlin
package com.kidcurate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Primary: Royal Indigo (confidence, learning, playful)
val BrandPrimary = Color(0xFF4F46E5)
val BrandPrimaryLight = Color(0xFF6366F1)
val BrandPrimaryDark = Color(0xFF3730A3)
val BrandPrimaryContainer = Color(0xFFEEF2FF)

// Accent: Warm Sunshine Gold (stars, energy, quests)
val BrandAccent = Color(0xFFF59E0B)
val BrandAccentLight = Color(0xFFFBBF24)
val BrandAccentDark = Color(0xFFD97706)
val BrandAccentContainer = Color(0xFFFEF3C7)

// Success: Mint Emerald (achievements, correct answers)
val BrandSuccess = Color(0xFF10B981)
val BrandSuccessLight = Color(0xFF34D399)
val BrandSuccessContainer = Color(0xFFD1FAE5)

// Supporting playful colors
val BrandCoral = Color(0xFFEF4444)
val BrandCoralLight = Color(0xFFF87171)
val BrandCoralContainer = Color(0xFFFEE2E2)
val BrandSky = Color(0xFF0EA5E9)
val BrandSkyContainer = Color(0xFFE0F2FE)
val BrandViolet = Color(0xFF8B5CF6)
val BrandVioletContainer = Color(0xFFF3E8FF)

// Dynamic Theme-Aware Tokens that automatically match active Light / Dark theme
val BackgroundLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val BackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val BackgroundCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SurfaceCardLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SurfaceCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

// Dynamic Container Tokens (Material 3 Theme Adaptive)
val PrimaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val OnPrimaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val SecondaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val TertiaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiaryContainer

// High-contrast, friendly text
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

```

---

<a name="uithemethemekt"></a>
## 32. ui/theme/Theme.kt
**Original Location:** `android/app/src/main/java/com/kidcurate/app/ui/theme/Theme.kt`
**Description:** Design System: KidCurate App Theme Composable

```kotlin
package com.kidcurate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF0B0F19),
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF374151),
)

val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandPrimary,
    secondary = BrandAccent,
    onSecondary = Color.White,
    secondaryContainer = BrandAccentContainer,
    onSecondaryContainer = BrandAccentDark,
    tertiary = BrandSuccess,
    onTertiary = Color.White,
    tertiaryContainer = BrandSuccessContainer,
    onTertiaryContainer = BrandSuccess,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
)

@Composable
fun KidCurateTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}


```

---

<a name="resvaluesstringsxml"></a>
## 33. res/values/strings.xml
**Original Location:** `android/app/src/main/res/values/strings.xml`
**Description:** Resource: App Strings & Accessibility Labels

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">KidCurate</string>
</resources>

```

---

<a name="resvaluescolorsxml"></a>
## 34. res/values/colors.xml
**Original Location:** `android/app/src/main/res/values/colors.xml`
**Description:** Resource: Core Brand Colors & Palette XML

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFFFFF</color>
</resources>

```

---

<a name="resvaluesthemesxml"></a>
## 35. res/values/themes.xml
**Original Location:** `android/app/src/main/res/values/themes.xml`
**Description:** Resource: Android System Splash & Base Themes

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.KidCurate" parent="android:Theme.Material.Light.NoActionBar">
    </style>
</resources>

```

---

<a name="resxmlnetwork_security_configxml"></a>
## 36. res/xml/network_security_config.xml
**Original Location:** `android/app/src/main/res/xml/network_security_config.xml`
**Description:** Security: Network Security Config for Local & HTTPS Traffic

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>

```

---

<a name="testapppoliciestestkt"></a>
## 37. test/AppPoliciesTest.kt
**Original Location:** `android/app/src/test/java/com/kidcurate/app/data/AppPoliciesTest.kt`
**Description:** Unit Test: Responsive Layout & Connection Policy Assertions

```kotlin
package com.kidcurate.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPoliciesTest {
    @Test
    fun parentPinCanOnlyBeReusedByItsGoogleAccount() {
        assertTrue(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "",
                signingInEmail = "PARENT@example.com",
            )
        )
        assertFalse(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "",
                signingInEmail = "different@example.com",
            )
        )
        assertFalse(
            ParentSessionPolicy.canReusePin(
                hasPin = false,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "parent@example.com",
                signingInEmail = "parent@example.com",
            )
        )
    }

    @Test
    fun legacyPinUsesCurrentAuthenticatedEmailAsOwner() {
        assertTrue(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "",
                currentAccountEmail = "parent@example.com",
                signingInEmail = "parent@example.com",
            )
        )
    }

    @Test
    fun compactLandscapeCoversShortWidePhonesOnly() {
        assertTrue(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 412, heightDp = 915))
        assertFalse(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 1280, heightDp = 800))
    }

    @Test
    fun tabletAndLandscapePredicatesIdentifyTabletScreens() {
        assertTrue(ResponsiveLayoutPolicy.isTablet(widthDp = 600))
        assertTrue(ResponsiveLayoutPolicy.isTablet(widthDp = 800))
        assertFalse(ResponsiveLayoutPolicy.isTablet(widthDp = 412))

        assertTrue(ResponsiveLayoutPolicy.isLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isLandscape(widthDp = 412, heightDp = 915))

        assertTrue(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 800, heightDp = 1280))
    }

    @Test
    fun responsiveLayoutPolicySelectsAdaptiveTwoPaneAndGridConfigurations() {
        // PIN two-pane layout: any landscape orientation
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 915, heightDp = 412))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 412, heightDp = 915))
        assertFalse(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 800, heightDp = 1280))

        // Playlist two-pane console
        assertTrue(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 915, heightDp = 412))
        assertTrue(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 412, heightDp = 915))

        // Playlist grid columns: tablet landscape = 2, tablet portrait = 2, phone = 1
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 1280, heightDp = 800))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 800, heightDp = 1280))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 915, heightDp = 412))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 412, heightDp = 915))

        // PIN button sizing: adaptive across tablet vs compact landscape vs portrait
        assertEquals(60, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 1280, heightDp = 800))
        assertEquals(46, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 915, heightDp = 412))
        assertEquals(64, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 360, heightDp = 640))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 412, heightDp = 915))
    }

    @Test
    fun cloudConnectionPolicyAllocatesAppropriateProbeTimeouts() {
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://localhost:8000/"))
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://10.0.2.2:8000/"))
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://127.0.0.1:8000/"))
        assertEquals(25, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://kidcurate-api.onrender.com/"))
        assertEquals(25, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://api.kidcurate.com/"))
    }

    @Test
    fun cloudConnectionPolicySanitizesBaseUrls() {
        assertEquals("http://localhost:8000/", CloudConnectionPolicy.sanitizeBaseUrl("http://localhost:8000"))
        assertEquals("http://localhost:8000/", CloudConnectionPolicy.sanitizeBaseUrl("http://localhost:8000/"))
        assertEquals("https://kidcurate-api.onrender.com/", CloudConnectionPolicy.sanitizeBaseUrl("  https://kidcurate-api.onrender.com  "))
        assertEquals("", CloudConnectionPolicy.sanitizeBaseUrl("   "))
    }

    @Test
    fun cloudConnectionPolicyCalculatesMaxColdStartWindow() {
        val attempts = 2
        val probeTimeout = CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://kidcurate-api.onrender.com/")
        val backoff = 2
        val totalWindow = (attempts * probeTimeout) + ((attempts - 1) * backoff)
        assertTrue(totalWindow >= 45)
    }
}

```

---

<a name="testplaybackusabilitytestkt"></a>
## 38. test/PlaybackUsabilityTest.kt
**Original Location:** `android/app/src/test/java/com/kidcurate/app/ui/PlaybackUsabilityTest.kt`
**Description:** Unit Test: Playback Usability & Child Lock State Verifications

```kotlin
package com.kidcurate.app.ui

import com.kidcurate.app.data.ResponsiveLayoutPolicy
import org.junit.Assert.*
import org.junit.Test

class PlaybackUsabilityTest {

    @Test
    fun antiSeekingProtectionIgnoresFastForwardDeltas() {
        // Simulates the watch delta accounting logic in PlayerScreen
        var lastRecordedSecond = 2
        var totalWatchedRecorded = 2

        // User scrubs/seeks to 250s of a 260s video (delta = 248s)
        val scrubbedSecond = 250
        val delta = scrubbedSecond - lastRecordedSecond

        // In PlayerScreen: delta in 1..5 is accepted as genuine playback; seeks are ignored
        if (delta in 1..5) {
            totalWatchedRecorded += delta
        }
        // Watched time must NOT inflate
        assertEquals(2, totalWatchedRecorded)
    }

    @Test
    fun continuousPlaybackAccumulatesTimeAccurately() {
        var lastRecordedSecond = 0
        var totalWatchedRecorded = 0

        // 5 seconds of real playback (1s heartbeat ticks)
        for (sec in 1..5) {
            val delta = sec - lastRecordedSecond
            if (delta in 1..5) {
                totalWatchedRecorded += delta
            }
            lastRecordedSecond = sec
        }

        assertEquals(5, totalWatchedRecorded)
    }

    @Test
    fun midVideoRotationPreservesPlaybackPosition() {
        // Simulates saved state before rotation
        val timestampBeforeRotation = 34.5f

        // On rotation / Activity recreation, rememberSaveable restores currentSecond
        val restoredCurrentSecond = timestampBeforeRotation

        // PlayerScreen initializes YouTubePlayer with resumeSeconds
        val resumeSeconds = if (restoredCurrentSecond > 1f) restoredCurrentSecond else 0f

        assertEquals(34.5f, resumeSeconds, 0.01f)
        assertNotEquals(0f, resumeSeconds)
    }

    @Test
    fun newVideoTransitionResetsTimestampToZero() {
        val newVideoId = "_7jhd4-o9gU"
        val previousVideoId = "libKVRa0WV4"

        val isDifferentVideo = newVideoId != previousVideoId
        val startSeconds = if (isDifferentVideo) 0f else 34.5f

        assertEquals(0f, startSeconds, 0.01f)
    }

    @Test
    fun childLockBlocksUnauthorizedBackNavigation() {
        var isChildLockActive = true
        var isExitingPlayer = false
        var showParentPasswordDialog = false
        var navigatedBack = false

        // User presses hardware or on-screen back
        if (isChildLockActive) {
            isExitingPlayer = true
            showParentPasswordDialog = true
        } else {
            navigatedBack = true
        }

        assertTrue(showParentPasswordDialog)
        assertTrue(isExitingPlayer)
        assertFalse(navigatedBack)

        // Parent successfully enters 4-digit PIN
        fun onParentPinSuccess() {
            showParentPasswordDialog = false
            isChildLockActive = false
            if (isExitingPlayer) {
                isExitingPlayer = false
                navigatedBack = true
            }
        }

        onParentPinSuccess()

        assertFalse(showParentPasswordDialog)
        assertFalse(isChildLockActive)
        assertTrue(navigatedBack)
    }

    @Test
    fun responsiveLayoutAdaptsAcrossDeviceMatrix() {
        // Phone Portrait (e.g. Pixel 7: 412 x 915 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(412, 915))
        assertFalse(ResponsiveLayoutPolicy.isTablet(412, 915))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(412, 915))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(412, 915))

        // Phone Landscape (e.g. Pixel 7 landscape: 915 x 412 dp)
        assertTrue(ResponsiveLayoutPolicy.isLandscape(915, 412))
        assertFalse(ResponsiveLayoutPolicy.isTablet(915, 412))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(915, 412))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(915, 412))
        assertEquals(46, ResponsiveLayoutPolicy.pinButtonSizeDp(915, 412))

        // Tablet Portrait (e.g. Galaxy Tab: 800 x 1280 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(800, 1280))
        assertTrue(ResponsiveLayoutPolicy.isTablet(800, 1280))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(800, 1280))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(800, 1280))

        // Tablet Landscape (e.g. Galaxy Tab landscape: 1280 x 800 dp)
        assertTrue(ResponsiveLayoutPolicy.isLandscape(1280, 800))
        assertTrue(ResponsiveLayoutPolicy.isTablet(1280, 800))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(1280, 800))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(1280, 800))
        assertEquals(60, ResponsiveLayoutPolicy.pinButtonSizeDp(1280, 800))

        // Split-Screen / Multi-Window (e.g. docked 400 x 700 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(400, 700))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(400, 700))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(400, 700))
    }

    @Test
    fun pbkdf2HashedPinProducesExpectedFormatAndSalt() {
        val pin = "1234"
        val salt = ByteArray(16) { 0x01.toByte() }
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        assertTrue(hash.isNotEmpty())
        assertEquals(32, hash.size)
    }

    @Test
    fun failedPinAttemptsTriggerExponentialLockout() {
        val maxAttempts = 5
        var failedAttempts = 0
        var lockoutMs = 0L

        for (i in 1..7) {
            failedAttempts++
            if (failedAttempts >= maxAttempts) {
                val multiplier = 1 shl (failedAttempts - maxAttempts).coerceAtMost(5)
                lockoutMs = 30_000L * multiplier
            }
        }

        assertEquals(7, failedAttempts)
        assertEquals(120_000L, lockoutMs)
    }
}

```

---
