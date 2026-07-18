import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.android.library")
}

val libs = the<LibrariesForLibs>()

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        shaders = false
    }

    testOptions {
        targetSdk = 37
        unitTests.all { it.useJUnitPlatform() }
    }

    lint {
        targetSdk = 37
        warningsAsErrors = true
        abortOnError = true
        // Version freshness is Renovate's job, not lint's.
        disable += listOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    "testImplementation"(libs.junit.jupiter)
    "testImplementation"(libs.kotlinx.coroutines.test)
    "testImplementation"(libs.turbine)
    "testRuntimeOnly"(libs.junit.platform.launcher)
}
