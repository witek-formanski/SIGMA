@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.composePlugin)
    kotlin("plugin.serialization") version "1.9.10"
}

group = "pl.edu.mimuw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    testImplementation(kotlin("test"))
    testImplementation(compose.uiTest)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    val voyagerVersion = "1.1.0-beta02"
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.10")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "sigma.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "sigma"
            packageVersion = "1.0.0"
        }
    }
}

kotlin {
    jvmToolchain(22)
}