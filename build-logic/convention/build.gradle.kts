plugins {
    `kotlin-dsl`
}

group = "com.darach.calendarwidget.buildlogic"

dependencies {
    // Makes the version catalog's type-safe accessors available inside
    // precompiled script plugins (they are not generated there by default).
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.dependency.guard.gradle.plugin)
}
