package com.darach.calendarwidget.core.data.repository

import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.Instant

/**
 * A raw Instances-table row as primitives, so the Cursor-to-domain mapping
 * stays a pure, unit-testable function.
 */
data class InstanceRow(
    val eventId: Long,
    val title: String?,
    val location: String?,
    val beginMillis: Long,
    val endMillis: Long,
    val allDay: Int,
    val displayColor: Int,
    val calendarId: Long,
    val selfAttendeeStatus: Int,
) {
    fun toCalendarEvent(): CalendarEvent =
        CalendarEvent(
            eventId = eventId,
            title = title?.takeIf { it.isNotBlank() } ?: "",
            location = location?.takeIf { it.isNotBlank() },
            startsAt = Instant.ofEpochMilli(beginMillis),
            endsAt = Instant.ofEpochMilli(endMillis),
            isAllDay = allDay == 1,
            color = displayColor,
            calendarId = calendarId,
            selfAttendeeStatus = selfAttendeeStatus.toAttendeeStatus(),
        )
}

/** CalendarContract.Attendees.ATTENDEE_STATUS_* constants. */
private fun Int.toAttendeeStatus(): AttendeeStatus =
    when (this) {
        1 -> AttendeeStatus.ACCEPTED
        2 -> AttendeeStatus.DECLINED
        3 -> AttendeeStatus.INVITED
        4 -> AttendeeStatus.TENTATIVE
        else -> AttendeeStatus.NONE
    }
