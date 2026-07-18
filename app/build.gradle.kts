import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    id("calendarwidget.android.application.compose")
    id("calendarwidget.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dependency.guard)
    alias(libs.plugins.baselineprofile)
    // Applied unconditionally (unlike google-services/crashlytics below) so the typed
    // `firebaseAppDistribution { }` DSL is available — Kotlin DSL only generates typed
    // accessors for plugins declared statically here. It's inert unless its upload task
    // actually runs, which requires Firebase credentials no contributor build has by default.
    alias(libs.plugins.firebase.appdistribution)
}

// google-services/crashlytics activate only when the (gitignored) google-services.json is
// present, so a fresh clone without Firebase access still builds.
val firebaseConfigured = file("google-services.json").exists()
if (firebaseConfigured) {
    apply(
        plugin =
            libs.plugins.google.services
                .get()
                .pluginId,
    )
    apply(
        plugin =
            libs.plugins.firebase.crashlytics
                .get()
                .pluginId,
    )
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}

android {
    namespace = "com.darach.calendarwidget"

    defaultConfig {
        applicationId = "com.darach.calendarwidget"
        // version.txt is owned by release-please; versionCode derives from it
        // (pre-release suffixes like -alpha are ignored for the code).
        val semver = rootProject.file("version.txt").readText().trim()
        val (major, minor, patch) = semver.substringBefore('-').split('.').map(String::toInt)
        versionCode = major * 10_000 + minor * 100 + patch
        versionName = semver
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        // Populated from ~/.gradle/gradle.properties (local) or env (CI);
        // falls back to debug signing so unsigned environments still build.
        val storeFilePath =
            providers.gradleProperty("CALENDARWIDGET_STORE_FILE").orNull
                ?: System.getenv("CALENDARWIDGET_STORE_FILE")
        if (storeFilePath != null) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = providers.gradleProperty("CALENDARWIDGET_STORE_PASSWORD").orNull
                    ?: System.getenv("CALENDARWIDGET_STORE_PASSWORD")
                keyAlias = providers.gradleProperty("CALENDARWIDGET_KEY_ALIAS").orNull
                    ?: System.getenv("CALENDARWIDGET_KEY_ALIAS")
                keyPassword = providers.gradleProperty("CALENDARWIDGET_KEY_PASSWORD").orNull
                    ?: System.getenv("CALENDARWIDGET_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // "release-testers" is a Firebase App Distribution tester *group* — managed in the
            // Firebase console/CLI, never as emails in this file or git history. appId is
            // auto-detected from google-services.json; auth via GOOGLE_APPLICATION_CREDENTIALS
            // (CI) or `firebase login` (local). Uploading requires Firebase setup either way.
            firebaseAppDistribution {
                groups = "release-testers"
            }
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":feature:calendars"))
    implementation(project(":feature:settings"))
    implementation(project(":widget"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.work.runtime.ktx)
    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.junit)
}
