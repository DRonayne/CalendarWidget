package com.darach.calendarwidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.CalendarEvent
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val OPAQUE_THRESHOLD = 0.99f

@Composable
fun AgendaWidget(state: WidgetRenderState) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(widgetBackground(state.config.backgroundOpacity))
                .cornerRadius(16.dp)
                .padding(12.dp),
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
    Column(modifier = GlanceModifier.fillMaxSize()) {
        HeaderBar(state, scale)
        Spacer(GlanceModifier.height(8.dp))
        if (state.days.all { it.events.isEmpty() } && state.days.isEmpty()) {
            EmptyAgenda(scale)
        } else {
            AgendaList(state, scale)
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
        modifier = GlanceModifier.fillMaxWidth(),
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
        Text(
            text = "＋",
            style = TextStyle(color = GlanceTheme.colors.primary, fontSize = (18 * scale).sp),
            modifier = GlanceModifier.padding(horizontal = 8.dp).clickable(WidgetActions.newEvent()),
        )
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
    state: WidgetRenderState,
    scale: Float,
) {
    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
        state.days.forEach { day ->
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
                .padding(top = 8.dp, bottom = 2.dp)
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
        modifier = GlanceModifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
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
                .padding(vertical = 3.dp)
                .clickable(WidgetActions.openEvent(event)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .width(4.dp)
                    .height((30 * scale).dp)
                    .background(ColorProvider(Color(event.color).copy(alpha = 1f)))
                    .cornerRadius(2.dp),
        ) {}
        Spacer(GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = event.title.ifEmpty { "(No title)" },
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = (13 * scale).sp),
                maxLines = 1,
            )
            SecondaryLine(event, day, state, scale)
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
    val time = AgendaFormatters.timeLabel(event, day.date, state.zone, state.use24Hour)
    val text = if (event.location != null) "$time · ${event.location}" else time
    Text(
        text = text,
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = (11 * scale).sp),
        maxLines = 1,
    )
}
