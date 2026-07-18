package com.darach.calendarwidget.widget.ui

import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Pure formatting helpers, unit-testable without Android. */
internal object AgendaFormatters {
    private const val MINUTES_PER_HOUR = 60L
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

    /**
     * Start time, plus end time when [showEndTime] is on. Falls back to [startTimeLabel]
     * alone for all-day events and multi-day events already "Ongoing", where there's no
     * clean range to show.
     */
    fun timeLabel(
        event: CalendarEvent,
        day: LocalDate,
        zone: ZoneId,
        use24Hour: Boolean,
        showEndTime: Boolean,
    ): String {
        val start = startTimeLabel(event, day, zone, use24Hour)
        if (!showEndTime || event.isAllDay) return start
        val startZoned = event.startsAt.atZone(zone)
        if (startZoned.toLocalDate() != day) return start
        val fmt = if (use24Hour) time24 else time12
        return "$start - ${event.endsAt.atZone(zone).format(fmt)}"
    }

    /**
     * Compact duration label for the duration chip, e.g. "50mins", "1hr", "2hrs", "2h30m".
     * Null for all-day events and multi-day events already "Ongoing", where duration isn't
     * meaningful to show inline.
     */
    fun durationLabel(
        event: CalendarEvent,
        day: LocalDate,
        zone: ZoneId,
    ): String? {
        if (event.isAllDay || event.startsAt.atZone(zone).toLocalDate() != day) return null
        val minutes = Duration.between(event.startsAt, event.endsAt).toMinutes()
        if (minutes <= 0) return null
        val hours = minutes / MINUTES_PER_HOUR
        val remainder = minutes % MINUTES_PER_HOUR
        return when {
            minutes < MINUTES_PER_HOUR -> "${minutes}min${if (minutes == 1L) "" else "s"}"
            remainder == 0L -> "${hours}hr${if (hours == 1L) "" else "s"}"
            else -> "${hours}h${remainder}m"
        }
    }
}
