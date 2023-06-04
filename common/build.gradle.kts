@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    alias(libs.plugins.kotest.multiplatform)
}


group = "net.leloubil"
version = "1.0-SNAPSHOT"

kotlin {
    android()
    jvm("desktop") {
        jvmToolchain(11)
    }
    sourceSets {
         val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(libs.logging)
                implementation(libs.kotlin.reflect)
                implementation(libs.kstatemachine)
                implementation(libs.kstatemachine.coroutines)
                implementation(libs.mordant)
                implementation(kotlin("reflect"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.mockk)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.junit5)
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

tasks.named<Test>("desktopTest") {
    outputs.upToDateWhen { false }
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
        @Suppress("UnstableApiUsage")
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin
    namespace = "net.leloubil.common"
}
