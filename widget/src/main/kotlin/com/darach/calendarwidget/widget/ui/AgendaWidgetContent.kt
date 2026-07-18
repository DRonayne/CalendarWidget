package com.darach.calendarwidget.widget.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.VerticalScrollMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import com.darach.calendarwidget.widget.R
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val OPAQUE_THRESHOLD = 0.99f
private const val SNAP_SCROLL_MIN_SDK = 37
private const val HORIZONTAL_PADDING = 10
private const val RAIL_WIDTH = 30
private const val ICON_BADGE_SIZE = 22
private const val ICON_GLYPH_SIZE = 12

@Composable
fun AgendaWidget(state: WidgetRenderState) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(widgetBackground(state.config.backgroundOpacity))
                .cornerRadius(16.dp),
    ) {
        when {
            !state.hasCalendarPermission -> PermissionMissing(state.packageName)
            else -> Agenda(state)
        }
    }
}

@Composable
private fun widgetBackground(alpha: Float): ColorProvider {
    val themed = GlanceTheme.colors.widgetBackground
    if (alpha >= OPAQUE_THRESHOLD) return themed
    val resolved = themed.getColor(LocalContext.current)
    return ColorProvider(resolved.copy(alpha = alpha.coerceIn(0f, 1f)))
}

@Composable
private fun PermissionMissing(packageName: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Calendar access needed",
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp),
        )
        Spacer(GlanceModifier.height(8.dp))
        androidx.glance.Button(text = "Open app", onClick = WidgetActions.openApp(packageName))
    }
}

@Composable
private fun Agenda(state: WidgetRenderState) {
    val scale = state.config.textScale.factor
    val days = visibleDays(state)
    Column(modifier = GlanceModifier.fillMaxSize()) {
        if (days.isEmpty()) {
            HeaderBar(state, scale)
            EmptyAgenda(scale)
        } else {
            AgendaList(days, state, scale)
        }
    }
}

@Composable
private fun HeaderBar(
    state: WidgetRenderState,
    scale: Float,
) {
    val headline = state.today.format(DateTimeFormatter.ofPattern("EEE d MMMM", Locale.getDefault()))
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(start = (HORIZONTAL_PADDING + 4).dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = headline,
            style =
                TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Bold,
                ),
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .clickable(
                        WidgetActions.openDay(
                            state.today
                                .atStartOfDay(state.zone)
                                .toInstant()
                                .toEpochMilli(),
                        ),
                    ),
        )
        if (state.config.showAddButton) {
            Text(
                text = "＋",
                style = TextStyle(color = GlanceTheme.colors.primary, fontSize = (18 * scale).sp),
                modifier = GlanceModifier.padding(horizontal = 10.dp).clickable(WidgetActions.newEvent()),
            )
        }
    }
}

/** Drops events whose end has already passed, then empty days per config. */
private fun visibleDays(state: WidgetRenderState): List<AgendaDay> =
    state.days.mapNotNull { day ->
        val remaining =
            day.events.filter { event ->
                if (event.isAllDay) !day.date.isBefore(state.today) else event.endsAt > state.now
            }
        when {
            remaining.isNotEmpty() -> {
                day.copy(events = remaining)
            }

            day.events.isEmpty() || state.config.emptyDayBehavior == EmptyDayBehavior.PLACEHOLDER -> {
                day.copy(events = emptyList())
            }

            else -> {
                null
            }
        }
    }

@Composable
private fun EmptyAgenda(scale: Float) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No upcoming events",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = (13 * scale).sp),
        )
    }
}

@Composable
private fun AgendaList(
    days: List<AgendaDay>,
    state: WidgetRenderState,
    scale: Float,
) {
    val snapScroll = state.snapScrollEnabled && Build.VERSION.SDK_INT >= SNAP_SCROLL_MIN_SDK
    val scrollMode =
        if (snapScroll) {
            VerticalScrollMode.SnapScrollMatchHeight(LocalSize.current.height)
        } else {
            VerticalScrollMode.Normal
        }
    LazyColumn(modifier = GlanceModifier.fillMaxSize(), verticalScrollMode = scrollMode) {
        item { HeaderBar(state, scale) }
        days.forEach { day ->
            item { DayHeader(day, state, scale) }
            if (day.events.isEmpty()) {
                item { NothingScheduled(scale) }
            } else {
                day.events.forEach { event ->
                    item { EventRow(event, day, state, scale) }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: AgendaDay,
    state: WidgetRenderState,
    scale: Float,
) {
    Text(
        text = AgendaFormatters.dayHeader(day.date, state.today, Locale.getDefault()),
        style =
            TextStyle(
                color = GlanceTheme.colors.secondary,
                fontSize = (12 * scale).sp,
                fontWeight = FontWeight.Medium,
            ),
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(start = HORIZONTAL_PADDING.dp, end = HORIZONTAL_PADDING.dp, top = 8.dp, bottom = 2.dp)
                .clickable(
                    WidgetActions.openDay(
                        day.date
                            .atStartOfDay(state.zone)
                            .toInstant()
                            .toEpochMilli(),
                    ),
                ),
    )
}

@Composable
private fun NothingScheduled(scale: Float) {
    Text(
        text = "Nothing scheduled",
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = (12 * scale).sp),
        modifier = GlanceModifier.padding(start = (HORIZONTAL_PADDING + 12).dp, top = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    day: AgendaDay,
    state: WidgetRenderState,
    scale: Float,
) {
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = HORIZONTAL_PADDING.dp)
                .clickable(WidgetActions.openEvent(event)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimelineIcon(event, scale)
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight().padding(vertical = 5.dp)) {
            Text(
                text = event.title.ifEmpty { "(No title)" },
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = (13 * scale).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
            SecondaryLine(event, day, state, scale)
        }
        AvatarStack(event, state, scale)
    }
}

/** The timeline rail: a vertical line with the event's icon badge sitting on it. */
@Composable
private fun TimelineIcon(
    event: CalendarEvent,
    scale: Float,
) {
    Box(
        modifier = GlanceModifier.width((RAIL_WIDTH * scale).dp).fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(GlanceTheme.colors.outline),
        ) {}
        Box(
            modifier =
                GlanceModifier
                    .size((ICON_BADGE_SIZE * scale).dp)
                    .background(ColorProvider(Color(event.color).copy(alpha = 1f)))
                    .cornerRadius((ICON_BADGE_SIZE * scale / 2).dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_event),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                modifier = GlanceModifier.size((ICON_GLYPH_SIZE * scale).dp),
            )
        }
    }
}

@Composable
private fun AvatarStack(
    event: CalendarEvent,
    state: WidgetRenderState,
    scale: Float,
) {
    if (!state.config.showAttendeePhotos) return
    val bitmaps = event.attendeePhotoUris.mapNotNull(state.avatars::get)
    if (bitmaps.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        bitmaps.forEach { bitmap ->
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier.size((18 * scale).dp).padding(start = 2.dp),
            )
        }
    }
}

@Composable
private fun SecondaryLine(
    event: CalendarEvent,
    day: AgendaDay,
    state: WidgetRenderState,
    scale: Float,
) {
    val time = AgendaFormatters.startTimeLabel(event, day.date, state.zone, state.use24Hour)
    val text = if (event.location != null) "$time · ${event.location}" else time
    Text(
        text = text,
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = (11 * scale).sp),
        maxLines = 1,
    )
}
