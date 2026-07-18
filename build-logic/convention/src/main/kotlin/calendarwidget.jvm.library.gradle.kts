import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.jetbrains.kotlin.jvm")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(17)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    "testImplementation"(libs.junit.jupiter)
    "testImplementation"(libs.kotlinx.coroutines.test)
    "testImplementation"(libs.turbine)
    "testRuntimeOnly"(libs.junit.platform.launcher)
}
