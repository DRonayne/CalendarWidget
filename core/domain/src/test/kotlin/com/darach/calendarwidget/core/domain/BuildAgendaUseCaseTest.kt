package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class BuildAgendaUseCaseTest {
    private val useCase = BuildAgendaUseCase()
    private val london: ZoneId = ZoneId.of("Europe/London")

    private fun event(
        start: Instant,
        end: Instant,
        allDay: Boolean = false,
        title: String = "event",
        id: Long = 1L,
    ) = CalendarEvent(
        eventId = id,
        title = title,
        location = null,
        startsAt = start,
        endsAt = end,
        isAllDay = allDay,
        color = 0,
        calendarId = 1L,
        selfAttendeeStatus = AttendeeStatus.ACCEPTED,
    )

    private fun at(
        date: LocalDate,
        hour: Int,
        minute: Int = 0,
        zone: ZoneId = london,
    ): Instant = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute)).atZone(zone).toInstant()

    @Test
    fun `timed event lands on its local day`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 7)
        val agenda =
            useCase(
                listOf(event(at(day, 9), at(day, 10))),
                window,
                london,
                EmptyDayBehavior.SKIP,
            )

        assertEquals(listOf(day), agenda.map { it.date })
        assertEquals(1, agenda.single().events.size)
    }

    @Test
    fun `multi-day timed event appears on every overlapped day`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 7)
        val agenda =
            useCase(
                listOf(event(at(day, 22), at(day.plusDays(2), 2))),
                window,
                london,
                EmptyDayBehavior.SKIP,
            )

        assertEquals(listOf(day, day.plusDays(1), day.plusDays(2)), agenda.map { it.date })
    }

    @Test
    fun `event ending exactly at midnight does not bleed into the next day`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 7)
        val agenda =
            useCase(
                listOf(event(at(day, 23), at(day.plusDays(1), 0))),
                window,
                london,
                EmptyDayBehavior.SKIP,
            )

        assertEquals(listOf(day), agenda.map { it.date })
    }

    @Test
    fun `all-day event is bucketed in UTC regardless of local zone`() {
        // All-day event stored as UTC midnights per CalendarContract.
        val day = LocalDate.of(2026, 7, 20)
        val start = day.atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val window = AgendaWindow(day, 7)

        for (zone in listOf("Pacific/Auckland", "America/Los_Angeles", "Europe/London")) {
            val agenda =
                useCase(
                    listOf(event(start, end, allDay = true)),
                    window,
                    ZoneId.of(zone),
                    EmptyDayBehavior.SKIP,
                )
            assertEquals(listOf(day), agenda.map { it.date }, "zone $zone")
        }
    }

    @Test
    fun `events outside the window are dropped`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 3)
        val agenda =
            useCase(
                listOf(
                    event(at(day.minusDays(1), 9), at(day.minusDays(1), 10)),
                    event(at(day.plusDays(5), 9), at(day.plusDays(5), 10)),
                ),
                window,
                london,
                EmptyDayBehavior.SKIP,
            )

        assertTrue(agenda.isEmpty())
    }

    @Test
    fun `within a day all-day events sort first then by start end title`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 1)
        val allDay =
            event(
                day.atStartOfDay(ZoneOffset.UTC).toInstant(),
                day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                allDay = true,
                title = "z-all-day",
            )
        val nine = event(at(day, 9), at(day, 10), title = "b")
        val nineLonger = event(at(day, 9), at(day, 11), title = "a")
        val eight = event(at(day, 8), at(day, 9), title = "c")

        val agenda = useCase(listOf(nine, allDay, nineLonger, eight), window, london, EmptyDayBehavior.SKIP)

        assertEquals(
            listOf("z-all-day", "c", "b", "a"),
            agenda.single().events.map { it.title },
        )
    }

    @Test
    fun `placeholder mode keeps empty days, skip mode drops them`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 3)
        val events = listOf(event(at(day.plusDays(1), 9), at(day.plusDays(1), 10)))

        val skipped = useCase(events, window, london, EmptyDayBehavior.SKIP)
        assertEquals(listOf(day.plusDays(1)), skipped.map { it.date })

        val placeholders = useCase(events, window, london, EmptyDayBehavior.PLACEHOLDER)
        assertEquals(listOf(day, day.plusDays(1), day.plusDays(2)), placeholders.map { it.date })
        assertTrue(placeholders[0].events.isEmpty())
        assertTrue(placeholders[2].events.isEmpty())
    }

    @Test
    fun `spring-forward DST day buckets events correctly`() {
        // Europe/London springs forward 2026-03-29 01:00 GMT -> 02:00 BST.
        val dstDay = LocalDate.of(2026, 3, 29)
        val window = AgendaWindow(dstDay, 1)
        val morning = event(at(dstDay, 3), at(dstDay, 4))

        val agenda = useCase(listOf(morning), window, london, EmptyDayBehavior.SKIP)

        assertEquals(listOf(dstDay), agenda.map { it.date })
    }

    @Test
    fun `fall-back DST day buckets late events on the same day`() {
        // Europe/London falls back 2026-10-25 02:00 BST -> 01:00 GMT.
        val dstDay = LocalDate.of(2026, 10, 25)
        val window = AgendaWindow(dstDay, 1)
        val evening = event(at(dstDay, 23), at(dstDay, 23, 30))

        val agenda = useCase(listOf(evening), window, london, EmptyDayBehavior.SKIP)

        assertEquals(listOf(dstDay), agenda.map { it.date })
        assertEquals(1, agenda.single().events.size)
    }

    @Test
    fun `event spanning far beyond the window is clipped to the window days`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 3)
        // A malformed sync entry with an effectively unbounded span must not
        // enumerate every day between its start and end.
        val everlasting = event(Instant.ofEpochMilli(0), Instant.ofEpochMilli(Long.MAX_VALUE))

        val agenda = useCase(listOf(everlasting), window, london, EmptyDayBehavior.SKIP)

        assertEquals(listOf(day, day.plusDays(1), day.plusDays(2)), agenda.map { it.date })
    }

    @Test
    fun `zero-duration event occupies exactly its start day`() {
        val day = LocalDate.of(2026, 7, 20)
        val window = AgendaWindow(day, 2)
        val instant = at(day, 12)

        val agenda = useCase(listOf(event(instant, instant)), window, london, EmptyDayBehavior.SKIP)

        assertEquals(listOf(day), agenda.map { it.date })
    }
}
