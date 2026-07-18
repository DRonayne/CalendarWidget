package com.darach.calendarwidget.core.data.refresh

/**
 * Which widget instances currently exist, without a compile-time edge onto :widget
 * (implemented there, bound in :app) — same shape as [WidgetRefresher].
 */
interface PlacedWidgets {
    suspend fun ids(): List<Int>
}
