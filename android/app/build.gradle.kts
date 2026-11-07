plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
// Force IDE Sync prompt
android {
    namespace = "com.kidcurate.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kidcurate.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        val apiUrl = (project.findProperty("API_BASE_URL") as? String) ?: "http://localhost:8000"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/kidcurate-release.jks")
            storePassword = "kidcurate2026"
            keyAlias = "kidcurate"
            keyPassword = "kidcurate2026"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
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

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
