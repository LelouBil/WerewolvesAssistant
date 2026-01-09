import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.vlcSetup)
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    config.setFrom("$rootDir/composeApp/config/detekt.yml")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }


    compilerOptions{
        freeCompilerArgs.set(listOf("-Xcontext-parameters","-Xconsistent-data-class-copy-visibility"))
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
//            implementation(compose.material3)
            implementation(libs.bundles.unstyled)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.bundles.androidx.multiplatform)
            implementation(libs.bundles.arrow)
            implementation(libs.bundles.kotlinxEcosystem)
            implementation(libs.bundles.koin)
            implementation(libs.klogging)
            implementation(libs.mediaPlayer)
        }
        commonTest.dependencies {
            implementation(libs.bundles.kotest)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }

        // KSP Common sourceSet
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

    }
}



ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

android {
    namespace = "net.leloubil.werewolvesassistant"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.leloubil.werewolvesassistant"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    fun myksp(dep: Any) {
        add("kspCommonMainMetadata", dep)
//        add("kspAndroid", dep)
//        add("kspIosArm64", dep)
//        add("kspIosSimulatorArm64", dep)
    }
    myksp(libs.arrow.optics.ksp)
    myksp(libs.koin.ksp)
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


val desktopAssetsDir = rootDir.resolve("assets")
compose.desktop {
    application {
        mainClass = "net.leloubil.werewolvesassistant.MainKt"
        jvmArgs += "--add-opens=java.base/java.nio=ALL-UNNAMED"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.leloubil.werewolvesassistant"
            packageVersion = "1.0.0"
            appResourcesRootDir = desktopAssetsDir
        }
    }
}

tasks.withType<JavaExec> {
    systemProperty("compose.application.resources.dir", file("appResources").absolutePath)
}
vlcSetup {
    vlcVersion = "3.0.21"
    shouldCompressVlcFiles = true
    shouldIncludeAllVlcFiles = false
    pathToCopyVlcLinuxFilesTo = desktopAssetsDir.resolve("linux-x64/")
    pathToCopyVlcMacosFilesTo = desktopAssetsDir.resolve("macos-arm64/")
    pathToCopyVlcWindowsFilesTo =  desktopAssetsDir.resolve("windows-x64/")
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}
