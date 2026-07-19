package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Buckets provider events into per-day agenda groups.
 *
 * - A timed event appears on every local day it overlaps inside the window.
 * - All-day events are day-ranged in UTC, per the CalendarContract contract.
 * - Within a day: all-day events first, then by start, end, title.
 * - Empty days are skipped or kept as placeholders per config.
 */
class BuildAgendaUseCase
    @Inject
    constructor() {
        operator fun invoke(
            events: List<CalendarEvent>,
            window: AgendaWindow,
            zone: ZoneId,
            emptyDayBehavior: EmptyDayBehavior,
        ): List<AgendaDay> {
            val buckets = window.days().associateWith { mutableListOf<CalendarEvent>() }
            for (event in events) {
                for (day in event.localDays(zone, window)) {
                    buckets[day]?.add(event)
                }
            }
            return buckets
                .mapNotNull { (date, dayEvents) ->
                    when {
                        dayEvents.isNotEmpty() -> AgendaDay(date, dayEvents.sortedWith(DAY_ORDER))
                        emptyDayBehavior == EmptyDayBehavior.PLACEHOLDER -> AgendaDay(date, emptyList())
                        else -> null
                    }
                }.sortedBy(AgendaDay::date)
        }

        /**
         * The local dates this event occupies within [window]. Clipped to the
         * window bounds: instance BEGIN/END carry the event's real span, so a
         * long or malformed (far-future end) event must not enumerate every
         * day of that span here.
         */
        private fun CalendarEvent.localDays(
            zone: ZoneId,
            window: AgendaWindow,
        ): List<LocalDate> {
            val effectiveZone = if (isAllDay) ZoneOffset.UTC else zone
            val firstDay = maxOf(startsAt.atZone(effectiveZone).toLocalDate(), window.start)
            // An event ending exactly at midnight does not occupy the day it ends on.
            val lastInstant = if (endsAt > startsAt) endsAt.minusMillis(1) else endsAt
            val lastDay = minOf(lastInstant.atZone(effectiveZone).toLocalDate(), window.endExclusive.minusDays(1))
            return generateSequence(firstDay) { it.plusDays(1) }
                .takeWhile { !it.isAfter(lastDay) }
                .toList()
        }

        private companion object {
            val DAY_ORDER: Comparator<CalendarEvent> =
                compareByDescending(CalendarEvent::isAllDay)
                    .thenBy(CalendarEvent::startsAt)
                    .thenBy(CalendarEvent::endsAt)
                    .thenBy(CalendarEvent::title)
        }
    }
