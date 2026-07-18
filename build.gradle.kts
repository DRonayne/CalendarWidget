// Root build file. Shared configuration lives in build-logic convention plugins;
// modules apply them via `id("calendarwidget.*")`.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.kover)
}

// Aggregated coverage report across every module (`:koverHtmlReport` / `:koverXmlReport`).
dependencies {
    kover(project(":app"))
    kover(project(":widget"))
    kover(project(":feature:settings"))
    kover(project(":feature:calendars"))
    kover(project(":core:model"))
    kover(project(":core:common"))
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":core:designsystem"))
    kover(project(":conformance"))
}
