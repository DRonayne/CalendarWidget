package com.darach.calendarwidget.widget.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import com.darach.calendarwidget.core.model.WidgetConfig
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AgendaWidgetContentTest {
    private val zone: ZoneId = ZoneId.of("Europe/London")
    private val today = LocalDate.of(2026, 7, 20)

    private fun event(
        title: String,
        location: String? = null,
        hour: Int = 9,
    ) = CalendarEvent(
        eventId = title.hashCode().toLong(),
        title = title,
        location = location,
        startsAt = LocalDateTime.of(today, LocalTime.of(hour, 0)).atZone(zone).toInstant(),
        endsAt = LocalDateTime.of(today, LocalTime.of(hour + 1, 0)).atZone(zone).toInstant(),
        isAllDay = false,
        color = 0xFF3366,
        calendarId = 1L,
        selfAttendeeStatus = AttendeeStatus.ACCEPTED,
    )

    private fun state(
        days: List<AgendaDay>,
        config: WidgetConfig = WidgetConfig(),
        hasPermission: Boolean = true,
    ) = WidgetRenderState(
        days = days,
        config = config,
        today = today,
        zone = zone,
        use24Hour = true,
        packageName = "com.darach.calendarwidget",
        hasCalendarPermission = hasPermission,
    )

    @Test
    fun `renders event rows with title and secondary line`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event("Standup", location = "Room 4"))))))
            }
            onNode(hasText("Standup")).assertExists()
            onNode(hasText("09:00 – 10:00 · Room 4")).assertExists()
        }

    @Test
    fun `renders relative day headers`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        listOf(
                            AgendaDay(today, listOf(event("A"))),
                            AgendaDay(today.plusDays(1), listOf(event("B"))),
                        ),
                    ),
                )
            }
            onNode(hasText("Today · 20 Jul")).assertExists()
            onNode(hasText("Tomorrow · 21 Jul")).assertExists()
        }

    @Test
    fun `empty agenda shows empty state`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(emptyList()))
            }
            onNode(hasText("No upcoming events")).assertExists()
        }

    @Test
    fun `placeholder day renders nothing scheduled row`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        days = listOf(AgendaDay(today, emptyList()), AgendaDay(today.plusDays(1), listOf(event("A")))),
                        config = WidgetConfig(emptyDayBehavior = EmptyDayBehavior.PLACEHOLDER),
                    ),
                )
            }
            onNode(hasText("Nothing scheduled")).assertExists()
        }

    @Test
    fun `missing permission renders grant state`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(emptyList(), hasPermission = false))
            }
            onNode(hasText("Calendar access needed")).assertExists()
            onNode(hasText("Open app")).assertExists()
        }

    @Test
    fun `untitled events render placeholder title`() =
        runGlanceAppWidgetUnitTest {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event(""))))))
            }
            onNode(hasText("(No title)")).assertExists()
        }
}
