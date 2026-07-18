import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("calendarwidget.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<LibrariesForLibs>()

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    "implementation"(platform(libs.androidx.compose.bom))
    "implementation"(libs.androidx.compose.ui)
    "implementation"(libs.androidx.compose.material3)
    "implementation"(libs.androidx.compose.ui.tooling.preview)
    "debugImplementation"(libs.androidx.compose.ui.tooling)
    "androidTestImplementation"(platform(libs.androidx.compose.bom))
}
