package com.darach.calendarwidget.core.common.analytics

/**
 * Typed analytics facade. The app decides the sink (Firebase Analytics when
 * a google-services.json shipped in the build; no-op otherwise).
 *
 * Privacy contract: events describe feature usage only — counts, durations,
 * settings values, error categories. Never calendar content, calendar or
 * account identity, free-text messages, or anything else that could identify
 * the user or their data.
 */
interface Analytics {
    fun track(event: AnalyticsEvent)
}

sealed interface AnalyticsEvent {
    val name: String

    /** Event parameters; values must be String, Boolean, Int, Long, or Double. */
    val params: Map<String, Any> get() = emptyMap()

    data object WidgetPlaced : AnalyticsEvent {
        override val name = "widget_placed"
    }

    data object WidgetRemoved : AnalyticsEvent {
        override val name = "widget_removed"
    }

    /** The saved configuration's shape — settings values, never calendar identity. */
    data class ConfigSaved(
        val daysAhead: Int,
        val includeYesterday: Boolean,
        val emptyDayBehavior: String,
        val textScale: String,
        val hideDeclined: Boolean,
        val hiddenCalendarCount: Int,
        val showAttendeePhotos: Boolean,
        val showAddButton: Boolean,
        val showEndTime: Boolean,
        val showDurationChip: Boolean,
        val isInstance: Boolean,
    ) : AnalyticsEvent {
        override val name = "config_saved"
        override val params
            get() =
                mapOf(
                    "days_ahead" to daysAhead,
                    "include_yesterday" to includeYesterday,
                    "empty_day_behavior" to emptyDayBehavior,
                    "text_scale" to textScale,
                    "hide_declined" to hideDeclined,
                    "hidden_calendar_count" to hiddenCalendarCount,
                    "show_attendee_photos" to showAttendeePhotos,
                    "show_add_button" to showAddButton,
                    "show_end_time" to showEndTime,
                    "show_duration_chip" to showDurationChip,
                    "is_instance" to isInstance,
                )
    }

    /** A save waited out the refresh timeout and the screen closed anyway. */
    data object ConfigSaveTimedOut : AnalyticsEvent {
        override val name = "config_save_timed_out"
    }

    data class RefreshCompleted(
        val durationMillis: Long,
        val widgetCount: Int,
    ) : AnalyticsEvent {
        override val name = "refresh_completed"
        override val params
            get() =
                mapOf(
                    "duration_ms" to durationMillis,
                    "widget_count" to widgetCount,
                )
    }

    /** A refresh pass failed; [errorType] is a fixed category, never a message. */
    data class RefreshFailed(
        val errorType: String,
    ) : AnalyticsEvent {
        override val name = "refresh_failed"
        override val params get() = mapOf("error_type" to errorType)
    }

    data class CalendarPermissionResult(
        val granted: Boolean,
    ) : AnalyticsEvent {
        override val name = "calendar_permission_result"
        override val params get() = mapOf("granted" to granted)
    }
}

object NoOpAnalytics : Analytics {
    override fun track(event: AnalyticsEvent) = Unit
}
