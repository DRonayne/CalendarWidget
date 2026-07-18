package com.darach.calendarwidget.core.model

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek

class WidgetConfigStoreTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `configFor falls back to the global template`() {
        val store =
            WidgetConfigStore(
                global = WidgetConfig(daysAhead = 14),
                byWidgetId = mapOf(7 to WidgetConfig(daysAhead = 3)),
            )
        assertEquals(3, store.configFor(7).daysAhead)
        assertEquals(14, store.configFor(99).daysAhead)
    }

    @Test
    fun `config round-trips through JSON including java-time fields`() {
        val config =
            WidgetConfig(
                daysAhead = 10,
                includeYesterday = true,
                backgroundOpacity = 0.42f,
                textScale = TextScale.LARGE,
                emptyDayBehavior = EmptyDayBehavior.PLACEHOLDER,
                weekStartOverride = DayOfWeek.MONDAY,
                use24HourOverride = false,
                hideDeclined = false,
                hiddenCalendarIds = setOf(3L, 5L),
                showAttendeePhotos = false,
            )
        val store = WidgetConfigStore(global = config, byWidgetId = mapOf(1 to config))

        val decoded =
            json.decodeFromString<WidgetConfigStore>(
                json.encodeToString(WidgetConfigStore.serializer(), store),
            )

        assertEquals(store, decoded)
    }

    @Test
    fun `unknown fields in stored JSON are tolerated - forward compatibility`() {
        val stored = """{"global":{"daysAhead":5,"someFutureField":true},"byWidgetId":{}}"""
        val decoded = json.decodeFromString<WidgetConfigStore>(stored)
        assertEquals(5, decoded.global.daysAhead)
    }

    @Test
    fun `missing fields decode to defaults - backward compatibility`() {
        val stored = """{"global":{},"byWidgetId":{}}"""
        val decoded = json.decodeFromString<WidgetConfigStore>(stored)
        assertEquals(WidgetConfig(), decoded.global)
    }
}
