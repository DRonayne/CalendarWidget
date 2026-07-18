@file:UseSerializers(DayOfWeekSerializer::class)

package com.darach.calendarwidget.core.model

import com.darach.calendarwidget.core.model.serialization.DayOfWeekSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.DayOfWeek

/**
 * Per-widget-instance configuration. New fields must always carry defaults so
 * stored JSON from older versions keeps decoding (face-extensibility contract).
 */
@Serializable
data class WidgetConfig(
    val daysAhead: Int = DEFAULT_DAYS_AHEAD,
    val includeYesterday: Boolean = false,
    val backgroundOpacity: Float = 1f,
    val textScale: TextScale = TextScale.DEFAULT,
    val emptyDayBehavior: EmptyDayBehavior = EmptyDayBehavior.SKIP,
    val weekStartOverride: DayOfWeek? = null,
    val use24HourOverride: Boolean? = null,
    val hideDeclined: Boolean = true,
    val hiddenCalendarIds: Set<Long> = emptySet(),
    val showAttendeePhotos: Boolean = true,
    val showAddButton: Boolean = false,
) {
    companion object {
        const val DEFAULT_DAYS_AHEAD = 14
        const val MIN_DAYS_AHEAD = 1
        const val MAX_DAYS_AHEAD = 30
    }
}

@Serializable
enum class TextScale(
    val factor: Float,
) {
    SMALL(0.85f),
    DEFAULT(1f),
    LARGE(1.2f),
}

@Serializable
enum class EmptyDayBehavior {
    /** Days without events are omitted from the agenda. */
    SKIP,

    /** Days without events render a "nothing scheduled" placeholder row. */
    PLACEHOLDER,
}

/**
 * All widget configs: a global template that new instances inherit, plus
 * per-instance overrides keyed by appWidgetId.
 */
@Serializable
data class WidgetConfigStore(
    val global: WidgetConfig = WidgetConfig(),
    val byWidgetId: Map<Int, WidgetConfig> = emptyMap(),
) {
    fun configFor(appWidgetId: Int): WidgetConfig = byWidgetId[appWidgetId] ?: global
}
