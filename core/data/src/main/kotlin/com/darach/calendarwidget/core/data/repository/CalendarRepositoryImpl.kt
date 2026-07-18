package com.darach.calendarwidget.core.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.CalendarContract
import com.darach.calendarwidget.core.common.coroutines.CwDispatchers
import com.darach.calendarwidget.core.common.coroutines.Dispatcher
import com.darach.calendarwidget.core.domain.AgendaWindow
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.CalendarInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl
    @Inject
    constructor(
        private val contentResolver: ContentResolver,
        @param:Dispatcher(CwDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : CalendarRepository {
        override suspend fun events(
            window: AgendaWindow,
            zone: ZoneId,
            hiddenCalendarIds: Set<Long>,
            hideDeclined: Boolean,
        ): Result<List<CalendarEvent>> =
            withContext(ioDispatcher) {
                safeProviderCall {
                    val selection = buildSelection(hiddenCalendarIds, hideDeclined)
                    readInstances(instancesUri(window, zone), selection).map(InstanceRow::toCalendarEvent)
                }
            }

        /**
         * One-day margin each side: all-day events are UTC-based and would
         * otherwise clip at zone offsets beyond the local window edges.
         */
        private fun instancesUri(
            window: AgendaWindow,
            zone: ZoneId,
        ): android.net.Uri {
            val beginMillis =
                window.start
                    .minusDays(1)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli()
            val endMillis =
                window.endExclusive
                    .plusDays(1)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli()
            return CalendarContract.Instances.CONTENT_URI
                .buildUpon()
                .let { ContentUris.appendId(it, beginMillis) }
                .let { ContentUris.appendId(it, endMillis) }
                .build()
        }

        private fun buildSelection(
            hiddenCalendarIds: Set<Long>,
            hideDeclined: Boolean,
        ): String? =
            buildList {
                if (hideDeclined) {
                    add(
                        "${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != " +
                            CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED,
                    )
                }
                if (hiddenCalendarIds.isNotEmpty()) {
                    val ids = hiddenCalendarIds.joinToString(",")
                    add("${CalendarContract.Instances.CALENDAR_ID} NOT IN ($ids)")
                }
            }.takeIf { it.isNotEmpty() }?.joinToString(" AND ")

        private fun readInstances(
            uri: android.net.Uri,
            selection: String?,
        ): List<InstanceRow> {
            val rows = mutableListOf<InstanceRow>()
            contentResolver
                .query(uri, INSTANCE_PROJECTION, selection, null, "${CalendarContract.Instances.BEGIN} ASC")
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        rows +=
                            InstanceRow(
                                eventId = cursor.getLong(0),
                                title = cursor.getString(1),
                                location = cursor.getString(2),
                                beginMillis = cursor.getLong(3),
                                endMillis = cursor.getLong(4),
                                allDay = cursor.getInt(5),
                                displayColor = cursor.getInt(6),
                                calendarId = cursor.getLong(7),
                                selfAttendeeStatus = cursor.getInt(8),
                            )
                    }
                } ?: providerUnavailable()
            return rows
        }

        override suspend fun calendars(): Result<List<CalendarInfo>> =
            withContext(ioDispatcher) {
                safeProviderCall {
                    val calendars = mutableListOf<CalendarInfo>()
                    contentResolver
                        .query(
                            CalendarContract.Calendars.CONTENT_URI,
                            CALENDAR_PROJECTION,
                            null,
                            null,
                            "${CalendarContract.Calendars.ACCOUNT_NAME} ASC, " +
                                "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC",
                        )?.use { cursor ->
                            while (cursor.moveToNext()) {
                                calendars +=
                                    CalendarInfo(
                                        id = cursor.getLong(0),
                                        displayName = cursor.getString(1).orEmpty(),
                                        accountName = cursor.getString(2).orEmpty(),
                                        color = cursor.getInt(3),
                                    )
                            }
                        } ?: providerUnavailable()
                    calendars
                }
            }

        private companion object {
            val INSTANCE_PROJECTION =
                arrayOf(
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.EVENT_LOCATION,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                    CalendarContract.Instances.DISPLAY_COLOR,
                    CalendarContract.Instances.CALENDAR_ID,
                    CalendarContract.Instances.SELF_ATTENDEE_STATUS,
                )

            val CALENDAR_PROJECTION =
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.CALENDAR_COLOR,
                )
        }
    }
