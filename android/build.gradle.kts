

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group = "net.leloubil"
version = "1.0-SNAPSHOT"

repositories {
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "net.leloubil.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildTypes {
        getByName("release") {
            @param:Suppress("UnstableApiUsage")
            isMinifyEnabled = false
        }
    }
    namespace = "net.leloubil.android"
}
