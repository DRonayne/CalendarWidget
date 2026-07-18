import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("calendarwidget.android.library.compose")
    id("calendarwidget.hilt")
}

val libs = the<LibrariesForLibs>()

dependencies {
    "implementation"(project(":core:model"))
    "implementation"(project(":core:common"))
    "implementation"(project(":core:designsystem"))

    "implementation"(libs.androidx.hilt.navigation.compose)
    "implementation"(libs.androidx.hilt.lifecycle.viewmodel.compose)
    "implementation"(libs.androidx.lifecycle.runtime.compose)
    "implementation"(libs.androidx.lifecycle.viewmodel.compose)
    "implementation"(libs.androidx.navigation3.runtime)
    "implementation"(libs.kotlinx.collections.immutable)
}
