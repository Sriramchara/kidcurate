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

