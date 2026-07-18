package com.darach.calendarwidget.widget.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.darach.calendarwidget.core.data.refresh.PlacedWidgets
import com.darach.calendarwidget.widget.AgendaGlanceWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacedWidgetsImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : PlacedWidgets {
        override suspend fun ids(): List<Int> {
            val manager = GlanceAppWidgetManager(context)
            return manager.getGlanceIds(AgendaGlanceWidget::class.java).map(manager::getAppWidgetId)
        }
    }
