plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

val appVersion = rootProject.file("version.txt").readText().trim()
require(appVersion.matches(Regex("V[0-9]+(\\.[0-9]+)*"))) {
    "version.txt muss das Format V1.0.0 verwenden"
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("androidx.activity:activity-compose:1.7.2")
            }
        }
    }
}

android {
    namespace = "de.cwtrainer.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        applicationId = "de.cwtrainer"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = appVersion.removePrefix("V")
    }
    buildTypes {
        getByName("release") {
            // Allows local release builds to be installed with adb without a private release keystore.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
