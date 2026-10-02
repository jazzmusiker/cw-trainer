plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

val appVersionFile = rootProject.file("version.txt")
val generatedAppVersionDir = layout.buildDirectory.dir("generated/appVersion/commonMain/kotlin")
val generateAppVersion by tasks.registering {
    inputs.file(appVersionFile)
    outputs.dir(generatedAppVersionDir)
    doLast {
        val appVersion = appVersionFile.readText().trim()
        require(appVersion.matches(Regex("V[0-9]+(\\.[0-9]+)*"))) {
            "version.txt muss das Format V1.0.0 verwenden"
        }
        val sourceFile = generatedAppVersionDir.get()
            .file("de/cwtrainer/app/AppVersion.kt")
            .asFile
        sourceFile.parentFile.mkdirs()
        sourceFile.writeText(
            """package de.cwtrainer.app

internal const val AppVersion = "$appVersion"
"""
        )
    }
}

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generatedAppVersionDir)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
        val androidMain by getting
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && name.contains("Kotlin")) {
        dependsOn(generateAppVersion)
    }
}

android {
    namespace = "de.cwtrainer.shared"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
