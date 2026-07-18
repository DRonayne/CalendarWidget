package com.darach.calendarwidget.widget.ui

import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Pure formatting helpers, unit-testable without Android. */
internal object AgendaFormatters {
    private val time24 = DateTimeFormatter.ofPattern("HH:mm")
    private val time12 = DateTimeFormatter.ofPattern("h:mm a")

    fun dayHeader(
        date: LocalDate,
        today: LocalDate,
        locale: Locale,
    ): String =
        if (date == today) {
            "TODAY"
        } else {
            date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale)).uppercase(locale)
        }

    /** Start time only; multi-day events already in progress read "Ongoing". */
    fun startTimeLabel(
        event: CalendarEvent,
        day: LocalDate,
        zone: ZoneId,
        use24Hour: Boolean,
    ): String {
        if (event.isAllDay) return "All day"
        val start = event.startsAt.atZone(zone)
        val fmt = if (use24Hour) time24 else time12
        return if (start.toLocalDate() == day) start.format(fmt) else "Ongoing"
    }
}
