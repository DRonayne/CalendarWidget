package com.darach.calendarwidget.widget.refresh

import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.DomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class RecordingCrashReporter : CrashReporter {
    val nonFatals = mutableListOf<Throwable>()
    val breadcrumbs = mutableListOf<String>()

    override fun recordNonFatal(throwable: Throwable) {
        nonFatals += throwable
    }

    override fun log(message: String) {
        breadcrumbs += message
    }
}

private class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()

    override fun track(event: AnalyticsEvent) {
        events += event
    }
}

class RefreshInstrumentationTest {
    private val crashReporter = RecordingCrashReporter()
    private val analytics = RecordingAnalytics()
    private val instrumentation = RefreshInstrumentation(crashReporter, analytics)

    @Test
    fun `failed with permission missing is tracked but not recorded as a non-fatal`() {
        instrumentation.failed(DomainException(DomainError.PermissionMissing))

        assertEquals(listOf(AnalyticsEvent.RefreshFailed("permission_missing")), analytics.events)
        assertTrue(crashReporter.nonFatals.isEmpty())
    }

    @Test
    fun `failed with provider unavailable is recorded as a non-fatal`() {
        val failure = DomainException(DomainError.ProviderUnavailable)

        instrumentation.failed(failure)

        assertEquals(listOf(AnalyticsEvent.RefreshFailed("provider_unavailable")), analytics.events)
        assertEquals(listOf(failure), crashReporter.nonFatals)
    }

    @Test
    fun `failed with query failed is recorded as a non-fatal carrying the reason`() {
        val failure = DomainException(DomainError.QueryFailed("cursor timeout"))

        instrumentation.failed(failure)

        assertEquals(listOf(AnalyticsEvent.RefreshFailed("query_failed")), analytics.events)
        assertEquals(listOf(failure), crashReporter.nonFatals)
    }

    @Test
    fun `unexpected is always tracked and recorded as a non-fatal`() {
        val unexpected = IllegalStateException("boom")

        instrumentation.unexpected(unexpected)

        assertEquals(listOf(AnalyticsEvent.RefreshFailed("unexpected")), analytics.events)
        assertEquals(listOf(unexpected), crashReporter.nonFatals)
    }
}
