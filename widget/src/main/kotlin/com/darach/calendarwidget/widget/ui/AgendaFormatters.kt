package com.darach.calendarwidget.widget.ui

import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Pure formatting helpers, unit-testable without Android. */
internal object AgendaFormatters {
    private val time24 = DateTimeFormatter.ofPattern("HH:mm")
    private val time12 = DateTimeFormatter.ofPattern("h:mm a")

    fun dayHeader(
        date: LocalDate,
        today: LocalDate,
        locale: Locale,
    ): String {
        val name =
            when (date) {
                today -> "Today"
                today.plusDays(1) -> "Tomorrow"
                today.minusDays(1) -> "Yesterday"
                else -> date.format(DateTimeFormatter.ofPattern("EEEE", locale))
            }
        return "$name · ${date.format(DateTimeFormatter.ofPattern("d MMM", locale))}"
    }

    fun timeLabel(
        event: CalendarEvent,
        day: LocalDate,
        zone: ZoneId,
        use24Hour: Boolean,
    ): String {
        if (event.isAllDay) return "All day"
        val fmt = if (use24Hour) time24 else time12
        val start = event.startsAt.atZone(zone)
        val end = event.endsAt.atZone(zone)
        val startsToday = start.toLocalDate() == day
        val endsToday = end.toLocalDate() == day || isMidnightEnd(event.endsAt, day, zone)
        return when {
            startsToday && endsToday -> "${start.format(fmt)}\u0020\u2013\u0020${end.format(fmt)}"
            startsToday -> "${start.format(fmt)} →"
            endsToday -> "→ ${end.format(fmt)}"
            else -> "All day"
        }
    }

    /** An event ending at exactly local midnight after [day] still "ends" that day. */
    private fun isMidnightEnd(
        end: Instant,
        day: LocalDate,
        zone: ZoneId,
    ): Boolean = end == day.plusDays(1).atStartOfDay(zone).toInstant()
}
