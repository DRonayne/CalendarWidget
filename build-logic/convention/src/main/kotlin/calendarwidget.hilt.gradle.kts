import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val libs = the<LibrariesForLibs>()

dependencies {
    "implementation"(libs.hilt.android)
    "ksp"(libs.hilt.compiler)
}
