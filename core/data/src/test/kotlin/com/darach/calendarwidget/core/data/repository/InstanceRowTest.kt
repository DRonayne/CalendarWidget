package com.darach.calendarwidget.core.data.repository

import com.darach.calendarwidget.core.model.AttendeeStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class InstanceRowTest {
    private fun row(
        title: String? = "Standup",
        location: String? = "Room 1",
        allDay: Int = 0,
        status: Int = 1,
    ) = InstanceRow(
        eventId = 7L,
        title = title,
        location = location,
        beginMillis = 1_753_000_000_000,
        endMillis = 1_753_003_600_000,
        allDay = allDay,
        displayColor = 0xFF00AA,
        calendarId = 3L,
        selfAttendeeStatus = status,
    )

    @Test
    fun `maps primitives to domain event`() {
        val event = row().toCalendarEvent()
        assertEquals(7L, event.eventId)
        assertEquals("Standup", event.title)
        assertEquals("Room 1", event.location)
        assertEquals(Instant.ofEpochMilli(1_753_000_000_000), event.startsAt)
        assertEquals(Instant.ofEpochMilli(1_753_003_600_000), event.endsAt)
        assertEquals(0xFF00AA, event.color)
        assertEquals(3L, event.calendarId)
        assertEquals(AttendeeStatus.ACCEPTED, event.selfAttendeeStatus)
    }

    @Test
    fun `blank title becomes empty and blank location becomes null`() {
        val event = row(title = "  ", location = " ").toCalendarEvent()
        assertEquals("", event.title)
        assertNull(event.location)
    }

    @Test
    fun `all-day flag maps from provider int`() {
        assertTrue(row(allDay = 1).toCalendarEvent().isAllDay)
        assertTrue(!row(allDay = 0).toCalendarEvent().isAllDay)
    }

    @Test
    fun `attendee status ints map per CalendarContract`() {
        assertEquals(AttendeeStatus.NONE, row(status = 0).toCalendarEvent().selfAttendeeStatus)
        assertEquals(AttendeeStatus.ACCEPTED, row(status = 1).toCalendarEvent().selfAttendeeStatus)
        assertEquals(AttendeeStatus.DECLINED, row(status = 2).toCalendarEvent().selfAttendeeStatus)
        assertEquals(AttendeeStatus.INVITED, row(status = 3).toCalendarEvent().selfAttendeeStatus)
        assertEquals(AttendeeStatus.TENTATIVE, row(status = 4).toCalendarEvent().selfAttendeeStatus)
        assertEquals(AttendeeStatus.NONE, row(status = 99).toCalendarEvent().selfAttendeeStatus)
    }
}
