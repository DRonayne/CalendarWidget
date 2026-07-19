package com.darach.calendarwidget.widget.refresh

import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.common.crash.CrashReporter
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash breadcrumbs and analytics for the refresh pipeline. Reports shape
 * only — counts, durations, fixed error categories — never event content.
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

        fun failed(errorType: String) {
            crashReporter.log("agenda refresh failed: $errorType")
            analytics.track(AnalyticsEvent.RefreshFailed(errorType))
        }

        fun unexpected(throwable: Throwable) {
            crashReporter.recordNonFatal(throwable)
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
    }
