package com.darach.calendarwidget.core.model

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class AgendaSnapshotSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `snapshot store round-trips`() {
        val event =
            CalendarEvent(
                eventId = 42L,
                title = "Standup",
                location = "Meet",
                startsAt = Instant.parse("2026-07-20T09:00:00Z"),
                endsAt = Instant.parse("2026-07-20T09:15:00Z"),
                isAllDay = false,
                color = 0xFF00FF,
                calendarId = 2L,
                selfAttendeeStatus = AttendeeStatus.TENTATIVE,
            )
        val store =
            SnapshotStore(
                byWidgetId =
                    mapOf(
                        12 to
                            AgendaSnapshot(
                                generatedAt = Instant.parse("2026-07-20T08:00:00Z"),
                                days = listOf(AgendaDay(LocalDate.of(2026, 7, 20), listOf(event))),
                            ),
                    ),
            )

        val decoded = json.decodeFromString<SnapshotStore>(json.encodeToString(SnapshotStore.serializer(), store))

        assertEquals(store, decoded)
    }

    @Test
    fun `snapshot with each lastError variant round-trips`() {
        val errors =
            listOf(
                DomainError.PermissionMissing,
                DomainError.ProviderUnavailable,
                DomainError.QueryFailed("boom"),
                DomainError.QueryFailed(null),
            )
        for (error in errors) {
            val store =
                SnapshotStore(
                    byWidgetId =
                        mapOf(
                            12 to
                                AgendaSnapshot(
                                    generatedAt = Instant.parse("2026-07-20T08:00:00Z"),
                                    days = emptyList(),
                                    lastError = error,
                                ),
                        ),
                )

            val decoded = json.decodeFromString<SnapshotStore>(json.encodeToString(SnapshotStore.serializer(), store))

            assertEquals(store, decoded, "round-trip failed for $error")
        }
    }
}
