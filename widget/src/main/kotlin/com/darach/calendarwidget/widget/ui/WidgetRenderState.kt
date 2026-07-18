package com.darach.calendarwidget.widget.ui

import android.graphics.Bitmap
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.WidgetConfig
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Everything the Glance composables need, fully resolved — no Context reads
 * inside the composition, so the Glance unit-test API can drive it directly.
 */
data class WidgetRenderState(
    val days: List<AgendaDay>,
    val config: WidgetConfig,
    val today: LocalDate,
    val now: Instant,
    val zone: ZoneId,
    val use24Hour: Boolean,
    val packageName: String,
    val hasCalendarPermission: Boolean,
    val avatars: Map<String, Bitmap> = emptyMap(),
    val snapScrollEnabled: Boolean = false,
)
