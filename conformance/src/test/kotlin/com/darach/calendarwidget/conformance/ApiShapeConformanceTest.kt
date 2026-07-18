package com.darach.calendarwidget.conformance

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Shape rules for the architecture standard: immutable UI state,
 * interface/impl repository split, ViewModel naming.
 */
class ApiShapeConformanceTest {
    @Test
    fun `UiState classes are immutable data classes`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.name.endsWith("UiState") }
            .assertTrue { cls ->
                cls.hasDataModifier && cls.annotations.any { it.name == "Immutable" }
            }
    }

    @Test
    fun `repository implementations are named after their interface`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.name.endsWith("RepositoryImpl") }
            .assertTrue { cls ->
                cls.parents().any { parent -> parent.name == cls.name.removeSuffix("Impl") }
            }
    }

    @Test
    fun `ViewModel subclasses carry the ViewModel suffix`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { cls -> cls.parents().any { it.name == "ViewModel" } }
            .assertTrue { it.name.endsWith("ViewModel") }
    }

    @Test
    fun `effect types are sealed`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.name.endsWith("Effect") }
            .assertTrue { it.hasSealedModifier }
    }
}
