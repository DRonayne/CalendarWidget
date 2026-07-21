package com.darach.calendarwidget.widget.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import com.darach.calendarwidget.core.model.WidgetConfig
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.seconds

class AgendaWidgetContentTest {
    private val zone: ZoneId = ZoneId.of("Europe/London")
    private val today = LocalDate.of(2026, 7, 20)
    private val morning: Instant = LocalDateTime.of(today, LocalTime.of(8, 0)).atZone(zone).toInstant()

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
        now: Instant = morning,
    ) = WidgetRenderState(
        days = days,
        config = config,
        today = today,
        now = now,
        zone = zone,
        use24Hour = true,
        packageName = "com.darach.calendarwidget",
        hasCalendarPermission = hasPermission,
    )

    @Test
    fun `renders event rows with bold title and start time line`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event("Standup", location = "Room 4"))))))
            }
            onNode(hasText("Standup")).assertExists()
            onNode(hasText("09:00 · Room 4")).assertExists()
        }

    @Test
    fun `renders day headers in timeline style`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
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
            onNode(hasText("TODAY")).assertExists()
            onNode(hasText("TUESDAY 21 JULY")).assertExists()
        }

    @Test
    fun `empty agenda shows empty state`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(emptyList()))
            }
            onNode(hasText("No upcoming events")).assertExists()
        }

    @Test
    fun `query failed with no snapshot shows friendly message`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(emptyList()).copy(lastError = DomainError.QueryFailed("cursor timeout")))
            }
            onNode(hasText("Couldn't load your events right now")).assertExists()
            onNode(hasText("No upcoming events")).assertDoesNotExist()
        }

    @Test
    fun `query failed shows technical detail only in debug builds`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(emptyList()).copy(
                        lastError = DomainError.QueryFailed("cursor timeout"),
                        isDebugBuild = true,
                    ),
                )
            }
            onNode(hasText("DEBUG: cursor timeout")).assertExists()
        }

    @Test
    fun `query failed hides technical detail outside debug builds`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(emptyList()).copy(
                        lastError = DomainError.QueryFailed("cursor timeout"),
                        isDebugBuild = false,
                    ),
                )
            }
            onNode(hasText("DEBUG: cursor timeout")).assertDoesNotExist()
        }

    @Test
    fun `provider unavailable with stale events shows failed-refresh banner`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(listOf(AgendaDay(today, listOf(event("Standup")))))
                        .copy(lastError = DomainError.ProviderUnavailable),
                )
            }
            onNode(hasText("Standup")).assertExists()
            onNode(hasText("Showing saved events — couldn't reach your calendar app")).assertExists()
        }

    @Test
    fun `placeholder day renders nothing scheduled row`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
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
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(emptyList(), hasPermission = false))
            }
            onNode(hasText("Calendar access needed")).assertExists()
            onNode(hasText("Open app")).assertExists()
        }

    @Test
    fun `untitled events render placeholder title`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event(""))))))
            }
            onNode(hasText("(No title)")).assertExists()
        }

    @Test
    fun `header shows todays date and hides add button by default`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event("A"))))))
            }
            onNode(hasText("Mon 20 July")).assertExists()
            onNode(hasText("＋")).assertDoesNotExist()
        }

    @Test
    fun `add button renders when enabled in config`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        listOf(AgendaDay(today, listOf(event("A")))),
                        config = WidgetConfig(showAddButton = true),
                    ),
                )
            }
            onNode(hasText("＋")).assertExists()
        }

    @Test
    fun `events whose end has passed are hidden`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        days = listOf(AgendaDay(today, listOf(event("Ended", hour = 9), event("Later", hour = 15)))),
                        now = LocalDateTime.of(today, LocalTime.of(11, 0)).atZone(zone).toInstant(),
                    ),
                )
            }
            onNode(hasText("Ended")).assertDoesNotExist()
            onNode(hasText("Later")).assertExists()
        }

    @Test
    fun `end time is hidden by default and shown when enabled`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        listOf(AgendaDay(today, listOf(event("Standup", location = "Room 4")))),
                        config = WidgetConfig(showEndTime = true),
                    ),
                )
            }
            onNode(hasText("09:00 - 10:00 · Room 4")).assertExists()
        }

    @Test
    fun `duration chip is hidden by default and shown when enabled`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        listOf(AgendaDay(today, listOf(event("Standup")))),
                        config = WidgetConfig(showDurationChip = true),
                    ),
                )
            }
            onNode(hasText("1hr")).assertExists()
        }

    @Test
    fun `duration chip does not render when disabled`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(state(listOf(AgendaDay(today, listOf(event("Standup"))))))
            }
            onNode(hasText("1hr")).assertDoesNotExist()
        }

    @Test
    fun `day drops out entirely once all its events have passed`() =
        runGlanceAppWidgetUnitTest(timeout = 60.seconds) {
            setAppWidgetSize(DpSize(270.dp, 280.dp))
            provideComposable {
                AgendaWidget(
                    state(
                        days = listOf(AgendaDay(today, listOf(event("Ended", hour = 9)))),
                        now = LocalDateTime.of(today, LocalTime.of(23, 0)).atZone(zone).toInstant(),
                    ),
                )
            }
            onNode(hasText("Ended")).assertDoesNotExist()
            onNode(hasText("No upcoming events")).assertExists()
        }
}
