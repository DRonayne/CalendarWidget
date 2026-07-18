package com.darach.calendarwidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.widget.refresh.RefreshScheduler
import com.darach.calendarwidget.widget.refresh.WidgetCleanupWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AgendaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgendaGlanceWidget()

    @Inject
    lateinit var refresher: WidgetRefresher

    @Inject
    lateinit var scheduler: RefreshScheduler

    @Inject
    lateinit var analytics: Analytics

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        refresher.requestRefresh(RefreshReason.WIDGET_LIFECYCLE)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        analytics.track(AnalyticsEvent.WidgetPlaced)
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        super.onDeleted(context, appWidgetIds)
        WidgetCleanupWorker.enqueue(context, appWidgetIds)
        analytics.track(AnalyticsEvent.WidgetRemoved)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scheduler.cancel()
    }
}
