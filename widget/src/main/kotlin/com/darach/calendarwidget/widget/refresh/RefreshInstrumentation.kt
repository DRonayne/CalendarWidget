package com.darach.calendarwidget.widget.refresh

import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.domainError
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash breadcrumbs, analytics, and non-fatals for the refresh pipeline.
 * Analytics reports shape only — counts, durations, fixed error categories,
 * never event content. Crashlytics non-fatals carry the full exception, so
 * the "why" behind a failure is diagnosable without touching user data.
 */
@Singleton
class RefreshInstrumentation
    @Inject
    constructor(
        private val crashReporter: CrashReporter,
        private val analytics: Analytics,
    ) {
        fun started(widgetCount: Int) {
            crashReporter.log("agenda refresh started: $widgetCount widget(s)")
        }

        /**
         * A recoverable per-widget refresh failure: breadcrumbed and tracked
         * by category, and recorded as a non-fatal unless it's an expected
         * pre-grant state rather than a defect.
         */
        fun failed(failure: Throwable) {
            val category = errorType(failure)
            crashReporter.log("agenda refresh failed: $category")
            analytics.track(AnalyticsEvent.RefreshFailed(category))
            if (failure.domainError() != DomainError.PermissionMissing) {
                crashReporter.recordNonFatal(failure)
            }
        }

        /** A bug that escaped the guarded pipeline: breadcrumbed, tracked, and always a non-fatal. */
        fun unexpected(throwable: Throwable) {
            crashReporter.recordNonFatal(throwable)
            crashReporter.log("agenda refresh failed: $ERROR_UNEXPECTED")
            analytics.track(AnalyticsEvent.RefreshFailed(ERROR_UNEXPECTED))
        }

        fun completed(
            duration: Duration,
            widgetCount: Int,
        ) {
            analytics.track(
                AnalyticsEvent.RefreshCompleted(
                    durationMillis = duration.toMillis(),
                    widgetCount = widgetCount,
                ),
            )
        }

        /** A fixed error category for analytics — Crashlytics gets the full exception instead. */
        private fun errorType(failure: Throwable): String =
            when (failure.domainError()) {
                DomainError.PermissionMissing -> "permission_missing"
                DomainError.ProviderUnavailable -> "provider_unavailable"
                is DomainError.QueryFailed -> "query_failed"
                null -> ERROR_UNEXPECTED
            }

        private companion object {
            const val ERROR_UNEXPECTED = "unexpected"
        }
    }
