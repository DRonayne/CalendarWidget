import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("calendarwidget.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<LibrariesForLibs>()

android {
    buildFeatures {
        compose = true
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("config/compose-stability.conf"))
    // Opt-in recomposition audit artifacts: ./gradlew assembleRelease -PcomposeCompilerReports=true
    if (providers.gradleProperty("composeCompilerReports").isPresent) {
        metricsDestination = layout.buildDirectory.dir("compose-metrics").get()
        reportsDestination = layout.buildDirectory.dir("compose-reports").get()
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
