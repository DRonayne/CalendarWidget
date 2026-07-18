package com.darach.calendarwidget.core.domain

import com.darach.calendarwidget.core.model.WidgetConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AgendaWindowTest {
    private val today = LocalDate.of(2026, 7, 20)

    @Test
    fun `default window starts today and spans daysAhead`() {
        val window = AgendaWindow.from(WidgetConfig(daysAhead = 7), today)
        assertEquals(today, window.start)
        assertEquals(7, window.dayCount)
        assertEquals(today.plusDays(7), window.endExclusive)
    }

    @Test
    fun `includeYesterday extends the window backwards without shrinking it forwards`() {
        val window = AgendaWindow.from(WidgetConfig(daysAhead = 7, includeYesterday = true), today)
        assertEquals(today.minusDays(1), window.start)
        assertEquals(8, window.dayCount)
        assertEquals(today.plusDays(7), window.endExclusive)
    }

    @Test
    fun `days lists every date in order`() {
        val window = AgendaWindow(today, 3)
        assertEquals(listOf(today, today.plusDays(1), today.plusDays(2)), window.days())
    }
}
