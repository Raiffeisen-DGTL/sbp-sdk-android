plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "raiffeisen.sbp.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "raiffeisen.sbp.sdk"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            keyAlias = "debug"
            keyPassword = "debugicerock"
            storeFile = file("signing/debug.jks")
            storePassword = "debugicerock"
        }

        getByName("debug") {
            keyAlias = "debug"
            keyPassword = "debugicerock"
            storeFile = file("signing/debug.jks")
            storePassword = "debugicerock"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
        }
    }
}

dependencies {
    implementation(project(":sdk"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
}