@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    kotlin("multiplatform") version "2.4.0"
}

repositories {
    mavenCentral()
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // No dependencies — pure FFI + builders
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }


}
