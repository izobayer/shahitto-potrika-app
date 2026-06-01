import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "bd.du.bangla.shahittopotrika"
    compileSdk = 35

    defaultConfig {
        applicationId = "bd.du.bangla.shahittopotrika"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "2.0.0"

        // Dynamically load Gemini key from system environment or local.properties
        val localProperties = Properties()
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { localProperties.load(it) }
        }
        val envKey = System.getenv("GOOGLE_AI_API_KEY") ?: ""
        val propKey = localProperties.getProperty("GOOGLE_AI_API_KEY") ?: ""
        val propKey2 = localProperties.getProperty("googleAiApiKey") ?: ""

        val apiKey = envKey.ifBlank { propKey }.ifBlank { propKey2 }

        buildConfigField("String", "GOOGLE_AI_API_KEY", "\"$apiKey\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.coil.compose)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    debugImplementation(libs.androidx.ui.tooling)
}
