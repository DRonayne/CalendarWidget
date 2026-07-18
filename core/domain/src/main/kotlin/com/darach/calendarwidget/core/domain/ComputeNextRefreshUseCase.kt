package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * The next moment the rendered agenda would visibly change: the earliest of
 * the next local midnight and every visible event's future start/end.
 * Alarms scheduled from this are deliberately inexact (no exact-alarm permission).
 */
class ComputeNextRefreshUseCase
    @Inject
    constructor() {
        operator fun invoke(
            events: List<CalendarEvent>,
            now: Instant,
            zone: ZoneId,
        ): Instant {
            val nextMidnight =
                now
                    .atZone(zone)
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(zone)
                    .toInstant()
            val nextBoundary =
                events
                    .asSequence()
                    .flatMap { sequenceOf(it.startsAt, it.endsAt) }
                    .filter { it > now }
                    .minOrNull()
            return if (nextBoundary != null && nextBoundary < nextMidnight) nextBoundary else nextMidnight
        }
    }
