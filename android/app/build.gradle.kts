import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

layout.buildDirectory.set(file("C:/tmp/kidcurate-build/app"))

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
        versionCode = 3
        versionName = "1.0.2"

        val apiUrl = (project.findProperty("API_BASE_URL") as? String) ?: "https://kidcurate-api.onrender.com"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")

        val googleClientId = (project.findProperty("GOOGLE_CLIENT_ID") as? String)
            ?: "179684236627-d7j0s15cvkj6li430jku6easqma5q81l.apps.googleusercontent.com"
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/kidcurate-release.jks")
            storePassword = requireNotNull(releaseStorePassword) {
                "Set KIDCURATE_STORE_PASSWORD or android/keystore.properties before building a release."
            }
            keyAlias = "kidcurate"
            keyPassword = requireNotNull(releaseKeyPassword) {
                "Set KIDCURATE_KEY_PASSWORD or android/keystore.properties before building a release."
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
