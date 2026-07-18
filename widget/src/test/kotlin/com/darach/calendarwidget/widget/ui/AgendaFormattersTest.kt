package com.darach.calendarwidget.widget.ui

import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class AgendaFormattersTest {
    private val london: ZoneId = ZoneId.of("Europe/London")
    private val day = LocalDate.of(2026, 7, 20)

    private fun event(
        start: Instant,
        end: Instant,
        allDay: Boolean = false,
    ) = CalendarEvent(
        eventId = 1L,
        title = "t",
        location = null,
        startsAt = start,
        endsAt = end,
        isAllDay = allDay,
        color = 0,
        calendarId = 1L,
        selfAttendeeStatus = AttendeeStatus.ACCEPTED,
    )

    private fun at(
        d: LocalDate,
        h: Int,
        m: Int = 0,
    ): Instant = LocalDateTime.of(d, LocalTime.of(h, m)).atZone(london).toInstant()

    @Test
    fun `today header is TODAY, other days use full uppercase names`() {
        assertEquals("TODAY", AgendaFormatters.dayHeader(day, day, Locale.UK))
        assertEquals("TUESDAY 21 JULY", AgendaFormatters.dayHeader(day.plusDays(1), day, Locale.UK))
        assertEquals("SUNDAY 19 JULY", AgendaFormatters.dayHeader(day.minusDays(1), day, Locale.UK))
        assertEquals("WEDNESDAY 22 JULY", AgendaFormatters.dayHeader(day.plusDays(2), day, Locale.UK))
    }

    @Test
    fun `same-day event shows start time only in 24h`() {
        val label = AgendaFormatters.startTimeLabel(event(at(day, 9), at(day, 10, 15)), day, london, use24Hour = true)
        assertEquals("09:00", label)
    }

    @Test
    fun `same-day event shows start time only in 12h`() {
        val label = AgendaFormatters.startTimeLabel(event(at(day, 9), at(day, 10, 15)), day, london, use24Hour = false)
        val normalized = label.lowercase().replace(' ', ' ').replace(' ', ' ')
        assertEquals("9:00 am", normalized)
    }

    @Test
    fun `all-day event says all day`() {
        val label =
            AgendaFormatters.startTimeLabel(
                event(at(day, 0), at(day.plusDays(1), 0), allDay = true),
                day,
                london,
                true,
            )
        assertEquals("All day", label)
    }

    @Test
    fun `multi-day event shows start on first day and ongoing after`() {
        val e = event(at(day, 22), at(day.plusDays(2), 2))
        assertEquals("22:00", AgendaFormatters.startTimeLabel(e, day, london, true))
        assertEquals("Ongoing", AgendaFormatters.startTimeLabel(e, day.plusDays(1), london, true))
        assertEquals("Ongoing", AgendaFormatters.startTimeLabel(e, day.plusDays(2), london, true))
    }
}
