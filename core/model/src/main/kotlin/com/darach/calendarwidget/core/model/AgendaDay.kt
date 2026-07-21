@file:UseSerializers(LocalDateSerializer::class, InstantSerializer::class)

package com.darach.calendarwidget.core.model

import com.darach.calendarwidget.core.model.serialization.InstantSerializer
import com.darach.calendarwidget.core.model.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant
import java.time.LocalDate

/** One day bucket of the agenda. [events] may be empty when the config asks for placeholder days. */
@Serializable
data class AgendaDay(
    val date: LocalDate,
    val events: List<CalendarEvent>,
)

/**
 * The rendered agenda for one widget instance, persisted for stale-then-fresh
 * rendering. [lastError] carries the reason the most recent refresh failed,
 * if any; it is cleared as soon as a refresh succeeds.
 */
@Serializable
data class AgendaSnapshot(
    val generatedAt: Instant,
    val days: List<AgendaDay>,
    val lastError: DomainError? = null,
)

/** All snapshots, keyed by appWidgetId. */
@Serializable
data class SnapshotStore(
    val byWidgetId: Map<Int, AgendaSnapshot> = emptyMap(),
)
