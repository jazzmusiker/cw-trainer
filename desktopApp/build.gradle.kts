import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

val appVersion = rootProject.file("version.txt").readText().trim()
require(appVersion.matches(Regex("V[0-9]+(\\.[0-9]+)*"))) {
    "version.txt muss das Format V1.0.0 verwenden"
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.cwtrainer.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "CWTrainer"
            packageVersion = appVersion.removePrefix("V")
        }
    }
}
