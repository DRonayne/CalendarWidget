package com.darach.calendarwidget.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.calendarwidget.core.designsystem.components.PreferenceChoiceRow
import com.darach.calendarwidget.core.designsystem.components.PreferenceNavRow
import com.darach.calendarwidget.core.designsystem.components.PreferenceSliderRow
import com.darach.calendarwidget.core.designsystem.components.PreferenceSwitchRow
import com.darach.calendarwidget.core.designsystem.components.SectionHeader
import com.darach.calendarwidget.core.designsystem.theme.CalendarWidgetTheme
import com.darach.calendarwidget.core.model.EmptyDayBehavior
import com.darach.calendarwidget.core.model.TextScale
import com.darach.calendarwidget.core.model.WidgetConfig
import kotlinx.collections.immutable.persistentListOf
import java.time.DayOfWeek
import kotlin.math.roundToInt

/** Stateful screen: owns the ViewModel and translates effects to navigation. */
@Composable
fun ConfigureScreen(
    appWidgetId: Int,
    onOpenCalendars: (Int) -> Unit,
    onOpenAbout: () -> Unit,
    onFinishWithResult: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        hiltViewModel<ConfigureViewModel, ConfigureViewModel.Factory>(
            key = "configure-$appWidgetId",
        ) { factory -> factory.create(appWidgetId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ConfigureEffect.OpenCalendars -> onOpenCalendars(effect.appWidgetId)
                ConfigureEffect.OpenAbout -> onOpenAbout()
                is ConfigureEffect.FinishWithResult -> onFinishWithResult(effect.appWidgetId)
                ConfigureEffect.Saved -> snackbarHostState.showSnackbar("Saved")
            }
        }
    }

    ConfigureContent(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/** Stateless content. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureContent(
    state: ConfigureUiState,
    onEvent: (ConfigureEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.isInstance) "Widget settings" else "Default widget settings") },
                actions = {
                    if (!state.isInstance) {
                        IconButton(onClick = { onEvent(ConfigureEvent.AboutClicked) }) {
                            Text("i", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
            )
        },
        bottomBar = {
            SaveBar(state = state, onEvent = onEvent)
        },
    ) { padding ->
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
        } else {
            ConfigForm(
                config = state.config,
                onConfigChanged = { onEvent(ConfigureEvent.ConfigChanged(it)) },
                onCalendarsClicked = { onEvent(ConfigureEvent.CalendarsClicked) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun SaveBar(
    state: ConfigureUiState,
    onEvent: (ConfigureEvent) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { onEvent(ConfigureEvent.SaveClicked) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isInstance) "Save widget" else "Save defaults")
        }
        if (state.isInstance) {
            TextButton(
                onClick = { onEvent(ConfigureEvent.SaveAsDefaultClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save and make default")
            }
        }
    }
}

@Composable
private fun ConfigForm(
    config: WidgetConfig,
    onConfigChanged: (WidgetConfig) -> Unit,
    onCalendarsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Time window")
        PreferenceSliderRow(
            title = "Days ahead",
            value = config.daysAhead.toFloat(),
            valueLabel = "${config.daysAhead}",
            valueRange = WidgetConfig.MIN_DAYS_AHEAD.toFloat()..WidgetConfig.MAX_DAYS_AHEAD.toFloat(),
            steps = WidgetConfig.MAX_DAYS_AHEAD - WidgetConfig.MIN_DAYS_AHEAD - 1,
            onValueChange = { onConfigChanged(config.copy(daysAhead = it.roundToInt())) },
        )
        PreferenceSwitchRow(
            title = "Include yesterday",
            checked = config.includeYesterday,
            onCheckedChange = { onConfigChanged(config.copy(includeYesterday = it)) },
        )

        SectionHeader("Appearance")
        PreferenceSliderRow(
            title = "Background opacity",
            value = config.backgroundOpacity,
            valueLabel = "${(config.backgroundOpacity * 100).roundToInt()}%",
            valueRange = 0.2f..1f,
            steps = 0,
            onValueChange = { onConfigChanged(config.copy(backgroundOpacity = it)) },
        )
        PreferenceChoiceRow(
            title = "Text size",
            options = persistentListOf("Small", "Default", "Large"),
            selectedIndex = config.textScale.ordinal,
            onSelect = { onConfigChanged(config.copy(textScale = TextScale.entries[it])) },
        )
        PreferenceSwitchRow(
            title = "Show empty days",
            subtitle = "Render a placeholder for days without events",
            checked = config.emptyDayBehavior == EmptyDayBehavior.PLACEHOLDER,
            onCheckedChange = {
                val behavior = if (it) EmptyDayBehavior.PLACEHOLDER else EmptyDayBehavior.SKIP
                onConfigChanged(config.copy(emptyDayBehavior = behavior))
            },
        )

        SectionHeader("Events")
        PreferenceSwitchRow(
            title = "Hide declined events",
            checked = config.hideDeclined,
            onCheckedChange = { onConfigChanged(config.copy(hideDeclined = it)) },
        )
        PreferenceNavRow(
            title = "Calendars",
            subtitle =
                if (config.hiddenCalendarIds.isEmpty()) {
                    "All calendars shown"
                } else {
                    "${config.hiddenCalendarIds.size} hidden"
                },
            onClick = onCalendarsClicked,
        )

        SectionHeader("Format")
        PreferenceChoiceRow(
            title = "Time format",
            options = persistentListOf("System", "12h", "24h"),
            selectedIndex =
                when (config.use24HourOverride) {
                    null -> 0
                    false -> 1
                    true -> 2
                },
            onSelect = {
                val value =
                    when (it) {
                        1 -> false
                        2 -> true
                        else -> null
                    }
                onConfigChanged(config.copy(use24HourOverride = value))
            },
        )
        PreferenceChoiceRow(
            title = "Week starts on",
            options = persistentListOf("System", "Mon", "Sun", "Sat"),
            selectedIndex =
                when (config.weekStartOverride) {
                    null -> 0
                    DayOfWeek.MONDAY -> 1
                    DayOfWeek.SUNDAY -> 2
                    else -> 3
                },
            onSelect = {
                val value =
                    when (it) {
                        1 -> DayOfWeek.MONDAY
                        2 -> DayOfWeek.SUNDAY
                        3 -> DayOfWeek.SATURDAY
                        else -> null
                    }
                onConfigChanged(config.copy(weekStartOverride = value))
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigureContentPreview() {
    CalendarWidgetTheme {
        ConfigureContent(
            state = ConfigureUiState(loading = false, isInstance = true),
            onEvent = {},
        )
    }
}
