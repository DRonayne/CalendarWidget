package com.darach.calendarwidget.conformance

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/**
 * Enforces the module/layer rules that the build graph alone cannot express.
 * These tests must stay green from the first commit; new violations fail CI.
 */
class ArchitectureConformanceTest {
    @Test
    fun `core model is framework-free`() {
        Konsist
            .scopeFromModule("core/model")
            .files
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
            }
    }

    @Test
    fun `core domain is framework-free`() {
        Konsist
            .scopeFromModule("core/domain")
            .files
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
            }
    }

    @Test
    fun `core common is framework-free`() {
        Konsist
            .scopeFromModule("core/common")
            .files
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
            }
    }

    @Test
    fun `features do not depend on other features`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.moduleName.startsWith("feature/") }
            .assertFalse { file ->
                val ownFeature = file.moduleName.removePrefix("feature/").substringBefore('/')
                file.imports.any { import ->
                    import.name.startsWith("com.darach.calendarwidget.feature.") &&
                        !import.name.startsWith("com.darach.calendarwidget.feature.$ownFeature")
                }
            }
    }

    @Test
    fun `widget is only referenced from the app module`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.moduleName != "app" && it.moduleName != "widget" }
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("com.darach.calendarwidget.widget") }
            }
    }

    @Test
    fun `unit tests use JUnit 5 not JUnit 4`() {
        Konsist
            .scopeFromTest()
            .files
            .filter { it.path.contains("/src/test/") }
            .assertFalse { file ->
                file.imports.any { it.name == "org.junit.Test" || it.name.startsWith("junit.framework") }
            }
    }
}
