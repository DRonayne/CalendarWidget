package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ComputeNextRefreshUseCaseTest {
    private val useCase = ComputeNextRefreshUseCase()
    private val london: ZoneId = ZoneId.of("Europe/London")

    private fun event(
        start: Instant,
        end: Instant,
    ) = CalendarEvent(
        eventId = 1L,
        title = "event",
        location = null,
        startsAt = start,
        endsAt = end,
        isAllDay = false,
        color = 0,
        calendarId = 1L,
        selfAttendeeStatus = AttendeeStatus.ACCEPTED,
    )

    private fun instant(
        date: LocalDate,
        time: LocalTime,
        zone: ZoneId = london,
    ): Instant = LocalDateTime.of(date, time).atZone(zone).toInstant()

    private val day = LocalDate.of(2026, 7, 20)
    private val noon = instant(day, LocalTime.NOON)
    private val nextMidnight = instant(day.plusDays(1), LocalTime.MIDNIGHT)

    @Test
    fun `no events - next local midnight`() {
        assertEquals(nextMidnight, useCase(emptyList(), noon, london))
    }

    @Test
    fun `upcoming event start before midnight wins`() {
        val e = event(instant(day, LocalTime.of(15, 0)), instant(day, LocalTime.of(16, 0)))
        assertEquals(e.startsAt, useCase(listOf(e), noon, london))
    }

    @Test
    fun `ongoing event - its end wins when before midnight`() {
        val e = event(instant(day, LocalTime.of(11, 0)), instant(day, LocalTime.of(13, 0)))
        assertEquals(e.endsAt, useCase(listOf(e), noon, london))
    }

    @Test
    fun `boundaries in the past are ignored`() {
        val e = event(instant(day, LocalTime.of(8, 0)), instant(day, LocalTime.of(9, 0)))
        assertEquals(nextMidnight, useCase(listOf(e), noon, london))
    }

    @Test
    fun `boundary exactly at now is not a future boundary`() {
        val e = event(instant(day, LocalTime.of(11, 0)), noon)
        assertEquals(nextMidnight, useCase(listOf(e), noon, london))
    }

    @Test
    fun `boundary after midnight loses to midnight`() {
        val e = event(instant(day.plusDays(1), LocalTime.of(9, 0)), instant(day.plusDays(1), LocalTime.of(10, 0)))
        assertEquals(nextMidnight, useCase(listOf(e), noon, london))
    }

    @Test
    fun `earliest of several future boundaries wins`() {
        val early = event(instant(day, LocalTime.of(14, 0)), instant(day, LocalTime.of(15, 0)))
        val late = event(instant(day, LocalTime.of(18, 0)), instant(day, LocalTime.of(19, 0)))
        assertEquals(early.startsAt, useCase(listOf(late, early), noon, london))
    }

    @Test
    fun `midnight respects DST - fall-back day is 25 hours long`() {
        // 2026-10-25: clocks fall back, so 00:00 -> 00:00 spans 25 real hours.
        val dstDay = LocalDate.of(2026, 10, 25)
        val start = instant(dstDay, LocalTime.MIDNIGHT)
        val end = instant(dstDay.plusDays(1), LocalTime.MIDNIGHT)
        assertEquals(25L * 3600, end.epochSecond - start.epochSecond)
        assertEquals(end, useCase(emptyList(), start.plusSeconds(60), london))
    }
}
