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
    fun `day headers use relative names`() {
        assertEquals("Today · 20 Jul", AgendaFormatters.dayHeader(day, day, Locale.UK))
        assertEquals("Tomorrow · 21 Jul", AgendaFormatters.dayHeader(day.plusDays(1), day, Locale.UK))
        assertEquals("Yesterday · 19 Jul", AgendaFormatters.dayHeader(day.minusDays(1), day, Locale.UK))
        assertEquals("Wednesday · 22 Jul", AgendaFormatters.dayHeader(day.plusDays(2), day, Locale.UK))
    }

    @Test
    fun `same-day event formats a 24h range`() {
        val label = AgendaFormatters.timeLabel(event(at(day, 9), at(day, 10, 15)), day, london, use24Hour = true)
        assertEquals("09:00 – 10:15", label)
    }

    @Test
    fun `same-day event formats a 12h range`() {
        val label = AgendaFormatters.timeLabel(event(at(day, 9), at(day, 10, 15)), day, london, use24Hour = false)
        val normalized = label.lowercase().replace(' ', ' ').replace(' ', ' ')
        assertEquals("9:00 am – 10:15 am", normalized)
    }

    @Test
    fun `all-day event says all day`() {
        val label =
            AgendaFormatters.timeLabel(
                event(at(day, 0), at(day.plusDays(1), 0), allDay = true),
                day,
                london,
                true,
            )
        assertEquals("All day", label)
    }

    @Test
    fun `multi-day event shows continuation arrows per day`() {
        val e = event(at(day, 22), at(day.plusDays(2), 2))
        assertEquals("22:00 →", AgendaFormatters.timeLabel(e, day, london, true))
        assertEquals("All day", AgendaFormatters.timeLabel(e, day.plusDays(1), london, true))
        assertEquals("→ 02:00", AgendaFormatters.timeLabel(e, day.plusDays(2), london, true))
    }

    @Test
    fun `event ending at midnight counts as ending on that day`() {
        val e = event(at(day, 23), at(day.plusDays(1), 0))
        assertEquals("23:00 – 00:00", AgendaFormatters.timeLabel(e, day, london, true))
    }
}
