package com.darach.calendarwidget.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.text.format.DateFormat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.darach.calendarwidget.core.common.flags.FeatureFlags
import com.darach.calendarwidget.core.common.flags.Flag
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepository
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.AttendeeStatus
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.SnapshotStore
import com.darach.calendarwidget.core.model.WidgetConfig
import com.darach.calendarwidget.core.model.WidgetConfigStore
import com.darach.calendarwidget.widget.ui.AgendaWidget
import com.darach.calendarwidget.widget.ui.AvatarLoader
import com.darach.calendarwidget.widget.ui.WidgetRenderState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDate

class AgendaGlanceWidget : GlanceAppWidget() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun configRepository(): WidgetConfigRepository

        fun snapshotRepository(): SnapshotRepository

        fun clock(): Clock

        fun avatarLoader(): AvatarLoader

        fun featureFlags(): FeatureFlags
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(COMPACT, WIDE, TALL))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val entry = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val zone = entry.clock().zone

        provideContent {
            val configStore by entry.configRepository().store.collectAsState(initial = WidgetConfigStore())
            val snapshots by entry.snapshotRepository().store.collectAsState(initial = SnapshotStore())
            val config = configStore.configFor(appWidgetId)
            val snapshot = snapshots.byWidgetId[appWidgetId]
            val hasPermission =
                context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
                    PackageManager.PERMISSION_GRANTED
            val avatarUris =
                snapshot
                    ?.days
                    .orEmpty()
                    .flatMap(AgendaDay::events)
                    .flatMap(CalendarEvent::attendeePhotoUris)
            val avatars by produceState(emptyMap(), avatarUris) {
                value = if (config.showAttendeePhotos) entry.avatarLoader().load(avatarUris) else emptyMap()
            }

            val state =
                WidgetRenderState(
                    days = snapshot?.days.orEmpty(),
                    config = config,
                    today = LocalDate.now(entry.clock()),
                    zone = zone,
                    use24Hour = config.use24HourOverride ?: DateFormat.is24HourFormat(context),
                    packageName = context.packageName,
                    hasCalendarPermission = hasPermission,
                    avatars = avatars,
                    snapScrollEnabled = entry.featureFlags().isEnabled(Flag.SNAP_SCROLL),
                )

            GlanceTheme {
                AgendaWidget(state)
            }
        }
    }

    /** Widget-picker generated preview (sample data, no providers touched). */
    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        provideContent {
            GlanceTheme {
                AgendaWidget(previewState())
            }
        }
    }

    private fun previewState(): WidgetRenderState {
        val zone = java.time.ZoneId.systemDefault()
        val today = LocalDate.now()

        fun event(
            title: String,
            hour: Int,
            location: String?,
        ) = CalendarEvent(
            eventId = hour.toLong(),
            title = title,
            location = location,
            startsAt = today.atTime(hour, 0).atZone(zone).toInstant(),
            endsAt = today.atTime(hour + 1, 0).atZone(zone).toInstant(),
            isAllDay = false,
            color = 0xFF2E9E8F.toInt(),
            calendarId = 1,
            selfAttendeeStatus = AttendeeStatus.ACCEPTED,
        )
        return WidgetRenderState(
            days =
                listOf(
                    AgendaDay(today, listOf(event("Morning standup", 9, null), event("Lunch", 13, "Café"))),
                    AgendaDay(today.plusDays(1), listOf(event("Gym", 18, null))),
                ),
            config = WidgetConfig(),
            today = today,
            zone = zone,
            use24Hour = true,
            packageName = "",
            hasCalendarPermission = true,
        )
    }

    private companion object {
        val COMPACT = DpSize(180.dp, 110.dp)
        val WIDE = DpSize(270.dp, 110.dp)
        val TALL = DpSize(270.dp, 280.dp)
    }
}
