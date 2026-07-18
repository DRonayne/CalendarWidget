package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.WidgetConfig
import java.time.LocalDate

/** The day range an agenda covers: [start] inclusive, [dayCount] days long. */
data class AgendaWindow(
    val start: LocalDate,
    val dayCount: Int,
) {
    val endExclusive: LocalDate get() = start.plusDays(dayCount.toLong())

    fun days(): List<LocalDate> = List(dayCount) { start.plusDays(it.toLong()) }

    companion object {
        fun from(
            config: WidgetConfig,
            today: LocalDate,
        ): AgendaWindow {
            val start = if (config.includeYesterday) today.minusDays(1) else today
            val extra = if (config.includeYesterday) 1 else 0
            return AgendaWindow(start = start, dayCount = config.daysAhead + extra)
        }
    }
}
