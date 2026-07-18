package com.darach.calendarwidget

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Typed routes — the deep-link surface of the app. */
@Serializable
data object SettingsRoute : NavKey

@Serializable
data class ConfigureRoute(
    val appWidgetId: Int,
) : NavKey

@Serializable
data class CalendarsRoute(
    val appWidgetId: Int,
) : NavKey

@Serializable
data object AboutRoute : NavKey
