package com.darach.calendarwidget.core.model

/** A device calendar as listed by the provider (for the selection screen). */
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val color: Int,
)
