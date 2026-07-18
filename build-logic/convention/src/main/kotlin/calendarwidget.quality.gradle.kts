import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("com.diffplug.spotless")
    id("org.jetbrains.kotlinx.kover")
}

val libs = the<LibrariesForLibs>()

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

kover {
    reports {
        filters {
            excludes {
                // DI wiring and Hilt/Dagger generated code carry no logic signal.
                packages("*.di")
                classes("*_Factory", "*_HiltModules*", "*Hilt_*", "*_Impl")
                annotatedBy("dagger.Module")
            }
        }
        verify {
            rule("line coverage ratchet") {
                // Ratchet: each module declares its floor via `kover.min.line`
                // in its own gradle.properties; the number only ever goes up.
                minBound(findProperty("kover.min.line")?.toString()?.toInt() ?: 0)
            }
        }
    }
}
