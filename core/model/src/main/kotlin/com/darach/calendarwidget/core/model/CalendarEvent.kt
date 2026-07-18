@file:UseSerializers(InstantSerializer::class)

package com.darach.calendarwidget.core.model

import com.darach.calendarwidget.core.model.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/**
 * One expanded event instance from the calendar provider (recurring events
 * arrive pre-expanded, so two occurrences are two [CalendarEvent]s).
 */
@Serializable
data class CalendarEvent(
    val eventId: Long,
    val title: String,
    val location: String?,
    val startsAt: Instant,
    val endsAt: Instant,
    val isAllDay: Boolean,
    val color: Int,
    val calendarId: Long,
    val selfAttendeeStatus: AttendeeStatus,
)

@Serializable
enum class AttendeeStatus {
    NONE,
    ACCEPTED,
    DECLINED,
    INVITED,
    TENTATIVE,
}
