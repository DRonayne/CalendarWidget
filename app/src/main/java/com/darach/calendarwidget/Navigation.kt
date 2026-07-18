package com.darach.calendarwidget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.darach.calendarwidget.feature.calendars.CalendarsScreen
import com.darach.calendarwidget.feature.settings.ConfigureScreen

/**
 * Nav3 graph. ViewModels emit navigation effects; Screens surface them as
 * callbacks; this layer is the only place that mutates the back stack.
 */
@Composable
fun MainNavigation(
    startRoute: NavKey,
    onFinishConfigure: (Int) -> Unit,
    onCloseApp: () -> Unit,
) {
    val backStack = rememberNavBackStack(startRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<SettingsRoute> {
                    val entryViewModel = hiltViewModel<SettingsEntryViewModel>()
                    val targetAppWidgetId by entryViewModel.targetAppWidgetId.collectAsStateWithLifecycle()
                    val resolvedId = targetAppWidgetId
                    if (resolvedId == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ConfigureScreen(
                            appWidgetId = resolvedId,
                            onOpenCalendars = { backStack.add(CalendarsRoute(it)) },
                            onFinishWithResult = {},
                            onCloseApp = onCloseApp,
                        )
                    }
                }
                entry<ConfigureRoute> { route ->
                    ConfigureScreen(
                        appWidgetId = route.appWidgetId,
                        onOpenCalendars = { backStack.add(CalendarsRoute(it)) },
                        onFinishWithResult = onFinishConfigure,
                        onCloseApp = onCloseApp,
                    )
                }
                entry<CalendarsRoute> { route ->
                    CalendarsScreen(appWidgetId = route.appWidgetId)
                }
            },
    )
}
