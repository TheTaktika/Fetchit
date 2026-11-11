plugins {
    kotlin("multiplatform") version "2.2.20"
    id("org.jetbrains.compose") version "1.12.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

group = "org.example"
version = "0.4.0-beta"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
            }
        }

        getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.window.styler)
                implementation(libs.jaudiotagger)
                implementation(libs.jackson.databind)
                implementation(libs.gson)
                implementation(libs.slf4j.api)
                implementation(libs.logback.classic)
            }
        }

        getByName("desktopTest") {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit)
                implementation(libs.testcontainers)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Test>("desktopTest") {
    useJUnitPlatform()
}

tasks.register<Exec>("killPreviousProcess") {
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        commandLine("taskkill", "/F", "/IM", "FetchIt.exe")
        isIgnoreExitValue = true
    } else {
        commandLine("echo", "Not on Windows, skipping taskkill")
    }
}

tasks.matching { it.name.startsWith("package") }.configureEach {
    dependsOn("killPreviousProcess")
}

compose.desktop {
    application {
        mainClass = "org.example.LauncherKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "FetchIt"
            packageVersion = "0.4.0"

            windows {
                console = false
                shortcut = true
                menu = true
                menuGroup = "FetchIt"
                dirChooser = true
                upgradeUuid = "a891cee0-2ca2-4ff9-8da6-dfe10d2a1703"
            }

            jvmArgs += listOf(
                "-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT",
                "-Djavax.net.ssl.trustStore=NUL"
            )
        }
    }
}