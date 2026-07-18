package com.darach.calendarwidget.core.data.repository

import com.darach.calendarwidget.core.domain.AgendaWindow
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.CalendarInfo
import java.time.ZoneId

/**
 * Read access to the OS calendar provider. Main-safe by contract;
 * failures surface as [com.darach.calendarwidget.core.model.DomainException] inside Result.
 */
interface CalendarRepository {
    /**
     * Expanded event instances overlapping [window] (queried with a one-day
     * margin each side so all-day UTC events are never clipped).
     */
    suspend fun events(
        window: AgendaWindow,
        zone: ZoneId,
        hiddenCalendarIds: Set<Long>,
        hideDeclined: Boolean,
    ): Result<List<CalendarEvent>>

    /** All calendars on the device, for the selection screen. */
    suspend fun calendars(): Result<List<CalendarInfo>>
}
