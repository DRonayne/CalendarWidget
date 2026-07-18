package com.darach.calendarwidget.core.common.analytics

/**
 * Typed analytics facade. The app decides the sink (logcat in this build —
 * no analytics vendor ships in this project).
 */
interface Analytics {
    fun track(event: AnalyticsEvent)
}

sealed interface AnalyticsEvent {
    val name: String

    data object WidgetPlaced : AnalyticsEvent {
        override val name = "widget_placed"
    }

    data object WidgetRemoved : AnalyticsEvent {
        override val name = "widget_removed"
    }

    data object ConfigSaved : AnalyticsEvent {
        override val name = "config_saved"
    }

    data class RefreshCompleted(
        val durationMillis: Long,
    ) : AnalyticsEvent {
        override val name = "refresh_completed"
    }
}

object NoOpAnalytics : Analytics {
    override fun track(event: AnalyticsEvent) = Unit
}
