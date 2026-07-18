package com.darach.calendarwidget.core.data.refresh

/**
 * How the rest of the app asks the widget layer for a refresh without a
 * compile-time edge onto :widget (implemented there, bound in :app).
 */
interface WidgetRefresher {
    fun requestRefresh(reason: RefreshReason)
}

enum class RefreshReason {
    CONFIG_CHANGED,
    CALENDAR_DATA_CHANGED,
    TIME_CHANGED,
    BOOT,
    ALARM,
    APP_OPENED,
    WIDGET_LIFECYCLE,
}
