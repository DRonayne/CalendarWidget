package com.darach.calendarwidget.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.text.format.DateFormat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepository
import com.darach.calendarwidget.core.model.SnapshotStore
import com.darach.calendarwidget.core.model.WidgetConfigStore
import com.darach.calendarwidget.widget.ui.AgendaWidget
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

            val state =
                WidgetRenderState(
                    days = snapshot?.days.orEmpty(),
                    config = config,
                    today = LocalDate.now(entry.clock()),
                    zone = zone,
                    use24Hour = config.use24HourOverride ?: DateFormat.is24HourFormat(context),
                    packageName = context.packageName,
                    hasCalendarPermission = hasPermission,
                )

            GlanceTheme {
                AgendaWidget(state)
            }
        }
    }

    private companion object {
        val COMPACT = DpSize(180.dp, 110.dp)
        val WIDE = DpSize(270.dp, 110.dp)
        val TALL = DpSize(270.dp, 280.dp)
    }
}
