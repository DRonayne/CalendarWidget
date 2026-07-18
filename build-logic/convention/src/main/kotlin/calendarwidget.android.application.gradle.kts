import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.android.application")
    id("calendarwidget.quality")
}

val libs = the<LibrariesForLibs>()

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 36
        targetSdk = 37
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }

    lint {
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
