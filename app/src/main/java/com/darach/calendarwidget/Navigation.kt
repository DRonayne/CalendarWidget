package com.darach.calendarwidget

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.darach.calendarwidget.feature.about.AboutScreen
import com.darach.calendarwidget.feature.calendars.CalendarsScreen
import com.darach.calendarwidget.feature.settings.ConfigureScreen

/**
 * Nav3 graph. ViewModels emit navigation effects; Screens surface them as
 * callbacks; this layer is the only place that mutates the back stack.
 */
@Composable
fun MainNavigation(
    startRoute: NavKey,
    versionName: String,
    onFinishConfigure: (Int) -> Unit,
) {
    val backStack = rememberNavBackStack(startRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<SettingsRoute> {
                    ConfigureScreen(
                        appWidgetId = 0,
                        onOpenCalendars = { backStack.add(CalendarsRoute(it)) },
                        onOpenAbout = { backStack.add(AboutRoute) },
                        onFinishWithResult = {},
                    )
                }
                entry<ConfigureRoute> { route ->
                    ConfigureScreen(
                        appWidgetId = route.appWidgetId,
                        onOpenCalendars = { backStack.add(CalendarsRoute(it)) },
                        onOpenAbout = { backStack.add(AboutRoute) },
                        onFinishWithResult = onFinishConfigure,
                    )
                }
                entry<CalendarsRoute> { route ->
                    CalendarsScreen(appWidgetId = route.appWidgetId)
                }
                entry<AboutRoute> {
                    AboutScreen(versionName = versionName)
                }
            },
    )
}
